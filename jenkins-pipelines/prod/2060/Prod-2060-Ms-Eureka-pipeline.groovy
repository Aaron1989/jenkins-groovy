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

def jpsApi = 'http:// :8002'
def apolloUrl = 'http:// :8070/configs/'
def env = 'dev'
def nameSpaces = ['prod', 'prod_cloud', 'gray_cloud','gray']
def applicationName = '2060-eureka'
def compileJobName = '2060-Eureka-Server-Compile'
//def compileJobName = applicationName + '-compile'

def jiraId = params.JIRA_ID
def description = params.DESCRIPTION
def jpsPassword = params.PASSWORD
def VERSION = params.VERSION
def OLD_VERSION = params.OLD_VERSION

node () {
    stage('Get Parameters') {
        result = httpRequest consoleLogResponseBody: true, responseHandle: 'NONE', url: apolloUrl + '/' +
                applicationName + '/' + env + '/' + nameSpaces[0]
        prodInfo = jsonParse(result.getContent())['configurations']

        result = httpRequest consoleLogResponseBody: true, responseHandle: 'NONE', url: apolloUrl + '/' +
                applicationName + '/' + env + '/' + nameSpaces[3]
        grayInfo = jsonParse(result.getContent())['configurations']

        result = httpRequest consoleLogResponseBody: true, responseHandle: 'NONE', url: apolloUrl + '/' +
                applicationName + '/' + env + '/' + nameSpaces[1]
        prodCloudInfo = jsonParse(result.getContent())['configurations']

        result = httpRequest consoleLogResponseBody: true, responseHandle: 'NONE', url: apolloUrl +
                'deploy-public/dev/DevOps.deploy-public'
        publicCloudInfo = jsonParse(result.getContent())['configurations']
        applicationName1 = prodInfo['service']
        applicationName2 = grayInfo['service']
        prefix1 = 'PROD/' + prodInfo['group'] + '/' + applicationName1
        prefix2 = 'PROD/' + grayInfo['group'] + '/' + applicationName2


        prodServer = prodInfo['service']
        grayServer = grayInfo['service']

        result1 = httpRequest consoleLogResponseBody: true, responseHandle: 'NONE', url: jpsApi +
                '/api/jplay/?app=' + prodServer
        result2 = httpRequest consoleLogResponseBody: true, responseHandle: 'NONE', url: jpsApi +
                '/api/jplay/?app=' + grayServer
        prodJpsInfo = jsonParse(result1.getContent())
        grayJpsInfo = jsonParse(result2.getContent())
        print grayJpsInfo

        grayIps = []
        for (def item: grayJpsInfo) {
            grayIps.add(item['ip'])

        }
        print grayIps

        prodIps = []
        for (def item: prodJpsInfo) {
            prodIps.add(item['ip'])

        }

//        prodInstances = []
//        for (def item: prodJpsInfo) {
//            prodIps.add(item['instance_id'])
//        }

    }

    stage('Deploy Notification') {
        build job: 'Prod-Public/Prod-Public-Wechat-Job', parameters: [string(name: 'VERSION', value: VERSION),
               string(name: 'JIRA_ID', value: jiraId), string(name: 'GROUP', value: prodInfo['group']),
               string(name: 'APPLICATION_NAME', value: prodInfo['service']), string(name: 'STAGE', value: 'start'),
               string(name: 'DESCRIPTION', value: description)]
    }

//     stage('Cloud Check') {
//         print prodCloudInfo['balancer_id']
//         print listToOutput(prodIps)
//         print listToOutput(prodInstances)
//         print prodCloudInfo['balancer_id'].getClass()
//         build job: 'Prod-Public/Prod-Public-Cloud-Check', parameters: [string(name: 'SLBS', value: prodCloudInfo['balancer_id']),
//               string(name: 'VGROUPS', value: prodCloudInfo['virtual_group']), string(name: 'SGROUPS', value: prodCloudInfo['security_group_id']),
//               string(name: 'INSTANCES', value: listToOutput(prodInstances)), string(name: 'RDSS', value: prodCloudInfo['rds_id']),
//               string(name: 'LGROUP', value: prodCloudInfo['log_group']), string(name: 'LPROJECT', value: prodCloudInfo['log_project']),
//               string(name: 'IPS', value: listToOutput(prodIps))]

//         print OLD_VERSION
//         build job: 'Prod-Public/Prod-Public-Package-Check', parameters: [string(name: 'VERSION', value: ''), string(name: 'OLD_VERSION', value: ''), string(name: 'OSS_BUCKET', value: ''), string(name: 'WAR_NAME', value: ''), string(name: 'PREFIX', value: '')]
//         build job: 'Prod-Public/Prod-Public-Package-Check', parameters: [string(name: 'VERSION', value: VERSION),
//                 string(name: 'OLD_VERSION', value: OLD_VERSION), string(name: 'OSS_BUCKET', value: publicCloudInfo['bucket_name']),
//                 string(name: 'WAR_NAME', value: prodInfo['war_name']), string(name: 'PREFIX', value: prefix)]
//     }

    stage('Prod-Compile') {
        print 'compile'
        build job: compileJobName, parameters: [string(name: 'PREFIX', value: prefix1), string(name: 'GIT_URL',
                value: prodInfo['git_url']), string(name: 'VERSION', value: VERSION), string(name: 'BRANCH', value: prodInfo['branch']),
                   string(name: 'WAR_NAME', value: prodInfo['war_name'])]

    }

    stage('Gray-Compile') {
        print 'compile'
        build job: compileJobName, parameters: [string(name: 'PREFIX', value: prefix2), string(name: 'GIT_URL',
                value: grayInfo['git_url']), string(name: 'VERSION', value: VERSION), string(name: 'BRANCH', value: grayInfo['branch']),
                   string(name: 'WAR_NAME', value: grayInfo['war_name'])]

    }

    stage('Prod Deploy'){
        for (def prodIp: prodIps) {
            print prodIp
            build job: 'Prod-Public/Prod-Public-Deploy', parameters: [string(name: 'APP_NAME', value: prodInfo['service']),
                  string(name: 'IP', value: prodIp), password(description: '', name: 'JPS_PWD',
                    value: jpsPassword.toString())]

        }
    }
    stage('Gray Deploy'){
        for (def grayIp: grayIps) {
            print grayIp
            build job: 'Prod-Public/Prod-Public-Deploy', parameters: [string(name: 'APP_NAME', value: grayInfo['service']),
                  string(name: 'IP', value: grayIp), password(description: '', name: 'JPS_PWD',
                    value: jpsPassword.toString())]

        }
    }

    stage('Complete Notification') {
        build job: 'Prod-Public/Prod-Public-Wechat-Job', parameters: [string(name: 'VERSION', value: VERSION),
              string(name: 'JIRA_ID', value: jiraId), string(name: 'GROUP', value: prodInfo['group']),
              string(name: 'APPLICATION_NAME', value: prodInfo['service']), string(name: 'STAGE', value: 'end'),
              string(name: 'DESCRIPTION', value: description)]
    }

    stage('Jira Confirm') {
        def userInput = input(
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

        JIRARefused = userInput['jira是否打回'].toString().replace('false','否').replace('true','是')
        JIRARefusedReason = userInput['jira_reason']
        if (JIRARefusedReason == '') {
            JIRARefusedReason = '/'
        }
        succeed = userInput['发布是否成功'].toString().replace('false','否').replace('true','是')
        failReason = userInput['deploy_reason']
        if (failReason == '') {
            failReason = '/'
        }
        rollback = userInput['是否回滚'].toString().replace('false','否').replace('true','是')
        rollbackReason = userInput['rollback_reason']
        if (rollbackReason == '') {
            rollbackReason = '/'
        }


        build job: 'Prod-Public/Prod-Public-Jira-Confirm', parameters: [string(name: 'VERSION', value: VERSION),
                string(name: 'OLD_VERSION', value: OLD_VERSION), string(name: 'APP_NAME', value: applicationName),
                string(name: 'JIRA_ID', value: jiraId), string(name: 'JIRA_REFUSED', value: JIRARefused),
                text(name: 'REFUSED_REASON', value: JIRARefusedReason), string(name: 'SUCCEED', value: succeed),
                text(name: 'FAIL_REASON', value: failReason), string(name: 'ROLLBACK', value: rollback),
                text(name: 'ROLLBACK_REASON', value: rollbackReason)]
    }

    stage('Merge') {
//        build job: 'Prod-Public/Prod-Public-Merge-Job', parameters: [string(name: 'VERSION', value: VERSION),
//                 string(name: 'REPOSITORY_URL', value: VERSION), string(name: 'BRANCH', value: prodInfo['branch']),
//                 text(name: 'DESCRIPTION', value: description)]
    }
}