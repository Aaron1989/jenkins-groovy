def jsonParse(def json) {
    new groovy.json.JsonSlurperClassic().parseText(json)
}
import groovy.json.JsonOutput

def listToOutput(def input) {
    print input
    print input.remove(null)
    def output = input.toString().replace('[','').replace(']','')
    print output
    return output
}

def skipStage(def userInput) {
        userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip = 'skip' + userInput
        return skip
}

def jpsApi = 'http:// :8002'
def apolloUrl = 'http:// :8070/configs/'
def env = 'dev'
def nameSpaces = ['prod', 'prod_cloud', 'DevOps.deploy-public']
def applicationName = 'insurance-link-customer'
def compileJobName = 'Prod-Insurance-Link-Customer-Compile'

def jiraId = params.JIRA_ID
def description = params.DESCRIPTION
def jpsPassword = params.PASSWORD

node () {
    stage('Get Parameters') {
        result = httpRequest consoleLogResponseBody: true, responseHandle: 'NONE', url: apolloUrl + '/' +
                applicationName + '/' + env + '/' + nameSpaces[0]
        prodInfo = jsonParse(result.getContent())['configurations']

        result = httpRequest consoleLogResponseBody: true, responseHandle: 'NONE', url: apolloUrl + '/' +
                applicationName + '/' + env + '/' + nameSpaces[1]
        prodCloudInfo = jsonParse(result.getContent())['configurations']

        result = httpRequest consoleLogResponseBody: true, responseHandle: 'NONE', url: apolloUrl +
                'deploy-public/dev/DevOps.deploy-public'
        publicCloudInfo = jsonParse(result.getContent())['configurations']
        prefix = 'PROD/' + prodInfo['group'] + '/' + applicationName

        prodServer = prodInfo['service']

        result = httpRequest consoleLogResponseBody: true, responseHandle: 'NONE', url: jpsApi +
                '/api/jplay/?app=' + prodServer
        prodJpsInfo = jsonParse(result.getContent())

        prodIps = []
        for (def item: prodJpsInfo) {
            prodIps.add(item['ip'])

        }

        prodInstances = []
        for (def item: prodJpsInfo) {
            prodInstances.add(item['instance_id'])
        }
    }

    stage('Deploy Notification') {
        def userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip = 'skip' + userInput
        if (skip == 'skipfalse') {
            build job: 'Prod-Public/Prod-Public-Wechat-Job', parameters: [string(name: 'VERSION', value: prodInfo['tag']),
                   string(name: 'JIRA_ID', value: jiraId), string(name: 'GROUP', value: prodInfo['group']),
                   string(name: 'APPLICATION_NAME', value: prodInfo['service']), string(name: 'STAGE', value: 'start'),
                   string(name: 'DESCRIPTION', value: description)]
        }
    }

    stage('Cloud Check') {
        skipStage(def userInput)
        if (skip == 'skipfalse') {
            print prodCloudInfo['balancer_id']
            print listToOutput(prodIps)
            print listToOutput(prodInstances)
            print prodCloudInfo['balancer_id'].getClass()
            build job: 'Prod-Public/Prod-Public-Cloud-Check', parameters: [string(name: 'SLBS', value: prodCloudInfo['balancer_id']),
                   string(name: 'VGROUPS', value: prodCloudInfo['virtual_group']), string(name: 'SGROUPS', value: prodCloudInfo['security_group_id']),
                   string(name: 'INSTANCES', value: listToOutput(prodInstances)), string(name: 'RDSS', value: prodCloudInfo['rds_id']),
                   string(name: 'LGROUP', value: prodCloudInfo['log_group']), string(name: 'LPROJECT', value: prodCloudInfo['log_project']),
                   string(name: 'IPS', value: listToOutput(prodIps))]

            build job: 'Prod-Public/Prod-Public-Package-Check', parameters: [string(name: 'VERSION', value: prodInfo['tag']),
                    string(name: 'OLD_VERSION', value: prodInfo['old_version']), string(name: 'OSS_BUCKET', value: publicCloudInfo['bucket_name']),
                    string(name: 'WAR_NAME', value: prodInfo['war_name']), string(name: 'PREFIX', value: prefix)]
        }
    }
    stage('Compile') {
        skipStage(def userInput)
        if (skip == 'skipfalse') {
            print 'compile'
            build job: compileJobName, parameters: [string(name: 'PREFIX', value: prefix), string(name: 'GIT_URL',
                    value: prodInfo['git_url']), string(name: 'VERSION', value: prodInfo['tag']), string(name: 'BRANCH', value: prodInfo['branch']),
                       string(name: 'WAR_NAME', value: prodInfo['war_name'])]

        }
    }

    stage('Prod Deploy'){
        skipStage(def userInput)
        if (skip == 'skipfalse') {
            for (def ip: prodIps) {
                print ip
                build job: 'Prod-Public/Prod-Public-Deploy', parameters: [string(name: 'APP_NAME', value: prodInfo['service']),
                      string(name: 'IP', value: ip), password(description: '', name: 'JPS_PWD',
                        value: jpsPassword.toString())]

            }
        }
    }

    stage('Complete Notification') {
        skipStage(def userInput)
        if (skip == 'skipfalse') {
        build job: 'Prod-Public/Prod-Public-Wechat-Job', parameters: [string(name: 'VERSION', value: prodInfo['tag']),
              string(name: 'JIRA_ID', value: jiraId), string(name: 'GROUP', value: prodInfo['group']),
              string(name: 'APPLICATION_NAME', value: prodInfo['service']), string(name: 'STAGE', value: 'end'),
              string(name: 'DESCRIPTION', value: description)]
        }
    }
    stage('email notification') {
        skipStage(def userInput)
        if (skip == 'skipfalse') {
              build job: 'Prod-Public/Prod-Public-Email-Job', parameters: [string(name: 'VERSION', value: prodInfo['tag']),
                string(name: 'GROUP', value: prodInfo['group']),
                string(name: 'JIRA_ID', value: jiraId),
                text(name: 'DESCRIPTION', value: description),
                string(name: 'APPLICATION_NAME', value: prodInfo['service'])]
        }
    }
    stage('Jira Confirm') {
        skipStage(def userInput)
        if (skip == 'skipfalse') {
            def jiraRecord = input(
                    id: 'userInput', message: '发布记录', parameters: [
                    [$class: 'BooleanParameterDefinition', defaultValue: false, description: '默认为否', name: 'jira是否打回'],
                    text(defaultValue: '', description: 'JIRA打回原因', name: 'jira_reason'),
                    [$class: 'BooleanParameterDefinition', defaultValue: true, description: '默认为是', name: '发布是否成功'],
                    text(defaultValue: '', description: '发布失败原因', name: 'deploy_reason'),
                    [$class: 'BooleanParameterDefinition', defaultValue: false, description: '默认为否', name: '是否回滚'],
                    text(defaultValue: '', description: '回滚原因', name: 'rollback_reason'),
            ])
            wrap([$class: 'BuildUser']) {
                releaser = BUILD_USER

            }

            JIRARefused = jiraRecord['jira是否打回'].toString().replace('false','否').replace('true','是')
            JIRARefusedReason = jiraRecord['jira_reason']
            if (JIRARefusedReason == '') {
                JIRARefusedReason = '/'
            }
            succeed = jiraRecord['发布是否成功'].toString().replace('false','否').replace('true','是')
            failReason = jiraRecord['deploy_reason']
            if (failReason == '') {
                failReason = '/'
            }
            rollback = jiraRecord['是否回滚'].toString().replace('false','否').replace('true','是')
            rollbackReason = jiraRecord['rollback_reason']
            if (rollbackReason == '') {
                rollbackReason = '/'
            }

            build job: 'Prod-Public/Prod-Public-Jira-Confirm', parameters: [string(name: 'VERSION', value: prodInfo['tag']),
                    string(name: 'OLD_VERSION', value: prodInfo['old_version']), string(name: 'APP_NAME', value: applicationName),
                    string(name: 'JIRA_ID', value: jiraId), string(name: 'JIRA_REFUSED', value: JIRARefused),
                    text(name: 'REFUSED_REASON', value: JIRARefusedReason), string(name: 'SUCCEED', value: succeed),
                    text(name: 'FAIL_REASON', value: failReason), string(name: 'ROLLBACK', value: rollback),
                    text(name: 'ROLLBACK_REASON', value: rollbackReason)]
        }
    }

}