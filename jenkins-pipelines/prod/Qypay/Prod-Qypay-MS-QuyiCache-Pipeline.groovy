package jenkins.deploy

/**
 * Created by Andy on 2017/06/19.
 */

def COMPILE_JOB_NAME = 'Prod-Qypay-MS-QuyiCache-Compile'
def DEPLOY_JOB_NAME1 = 'Prod-Qypay-MS-QuyiCache-Deploy1'
def DEPLOY_JOB_NAME2 = 'Prod-Qypay-MS-QuyiCache-Deploy2'
def BRANCH = '*/release'
def WAR_NAME = 'ms-quyicache.war'
def SHELL_NAME = 'Prod-MicroService'



properties ([
        parameters ([
                string (
                        defaultValue: '',
                        description: '',
                        name: 'TAG'),
                string (
                        defaultValue: '',
                        description: '',
                        name: 'OLD_VERSION'),
                password  (
                        defaultValue: '',
                        description: 'JPS_PWD',
                        name: 'JPS_PWD'),
                string (
                        name:'JiraID',
                        defaultValue: ""
                ),
                string (
                        name:'APP_NAME',
                        defaultValue: ""
                ),
                text (
                        defaultValue: '',
                        description: '',
                        name: 'Description')
        ])
])

node('base'){
    stage('deploy notification'){
        def userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip = 'skip' + userInput
        if (skip == 'skipfalse') {
            description = Description.replace("\r","#").replace("\n","#")
            build job: deploy_notification_job, parameters: [string(name: 'TAG', value: TAG), string(name: 'JiraID', value: JiraID), text(name: 'Description', value: description), string(name: 'group', value: GROUP), string(name: 'app_name', value: APP_NAME), string(name: 'stage', value: 'start')]

        }
    }
}

node {
    stage('compile') {
        build job: "$COMPILE_JOB_NAME", parameters: [string(name: 'TAG', value:env.TAG), string(name: 'BRANCH', value: BRANCH), string(name: 'WAR_NAME', value: WAR_NAME),text(name: 'Description', value: env.Description)]
    }
    stage('deploy1'){
        build job: "$DEPLOY_JOB_NAME1",parameters :[string(name: 'TAG', value:env.TAG), string(name: 'WAR_NAME', value: WAR_NAME), password(name: 'JPS_PWD', value:env.JPS_PWD)]
    }
    stage('deploy2'){
        input 'Whether to release Prod-Qypay-MS-QuyiCache-Deploy2?'
        build job: "$DEPLOY_JOB_NAME2",parameters :[string(name: 'TAG', value:env.TAG),string(name: 'WAR_NAME', value: WAR_NAME), password(name: 'JPS_PWD', value:env.JPS_PWD)]
    }
}

node('base'){
    stage('complete notification') {
        def userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip = 'skip' + userInput
        if (skip == 'skipfalse') {
            description = Description.replace("\r","#").replace("\n","#")
            build job: deploy_notification_job, parameters: [string(name: 'TAG', value: TAG), string(name: 'JiraID', value: JiraID), text(name: 'Description', value: description), string(name: 'group', value: GROUP), string(name: 'app_name', value: APP_NAME), string(name: 'stage', value: 'end')]
        }
    }
    stage('email notification') {
        def userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip = 'skip' + userInput
        if (skip == 'skipfalse') {
            checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'dingqishi', url: 'http:// <git>/scm/devops/devops-shell.git']]])
            RECIPIENT = sh returnStdout: true, script: "python deploy_email_notify.py $GROUP"
            print RECIPIENT
            T = sh returnStdout: true, script: 'date +"%Y-%m-%d %H:%M"'
            TIME = T.replace('\n','')
            build job: deploy_email_job, parameters: [string(name: 'TAG', value: TAG), string(name: 'JiraID', value: JiraID), text(name: 'CONTENT', value: Description), string(name: 'APP_NAME', value: APP_NAME), string(name: 'TIME', value: TIME), string(name: 'RECIPIENT', value: RECIPIENT)]
        }
    }
    stage('jira-confirm'){
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
            releaser = env.BUILD_USER

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

        deploy_record = '{"applicationName": "' + APP_NAME + '", "rollback": "' + rollback + '", "rollbackReason": "' + rollbackReason + '", "succeed": "'+ succeed + '", "JIRARefused": "' + JIRARefused + '", "releaser": "' + releaser + '", "JIRAId": "' + JiraID + '", "failReason": "' + failReason + '", "version": "' + TAG + '", "rollbackVersion": "' + OLD_VERSION + '", "JIRARefusedReason": "' + JIRARefusedReason + '"}'
//        result = httpRequest consoleLogResponseBody: true, acceptType: 'APPLICATION_JSON_UTF8', contentType: 'APPLICATION_JSON_UTF8', httpMode: 'POST', ignoreSslErrors: true, requestBody: request, responseHandle: 'NONE', url: JPS_API + '/api/add_release_record/'
        build job: deploy_record_job, parameters: [text(name: 'deploy_record', value: deploy_record)]
    }
}