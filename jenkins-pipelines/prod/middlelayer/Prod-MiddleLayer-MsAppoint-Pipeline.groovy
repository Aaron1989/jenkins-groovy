package jenkins.deploy

def GROUP = 'middle'
def deploy_notification_job = 'Prod-Public/Prod-Public-Wechat-Job'
def deploy_record_job = 'Prod-Public/Prod-Public-Jira-Confirm'
def deploy_email_job = 'Prod-Public/Prod-Public-Email-Job'

def COMPILE_JOB_NAME = 'Prod-MiddleLayer-MsAppoint-Compile'
def DEPLOY_JOB_NAMES = ['Gray-MiddleLayer-MsAppoint-Deploy1','Prod-MiddleLayer-MsAppoint-Deploy1','Prod-MiddleLayer-MsAppoint-Deploy2','Prod-MiddleLayer-MsAppoint-Deploy3']
def BRANCH = '*/release'
def WAR_NAME = 'ms-appoint-1.0-SNAPSHOT.jar'
//def SHELL_NAME = 'Prod-MicroService'

node{
        stage('deploy notification'){
            def userInput = input(
                    id: 'userInput', message: 'go on?', parameters: [
                    [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
            ])
            skip = 'skip' + userInput
            if (skip == 'skipfalse') {
            description = Description.replace("\r","#").replace("\n","#").replace(" ","")
            build job: deploy_notification_job, parameters: [string(name: 'VERSION', value: TAG),
                string(name: 'JIRA_ID', value: JiraID),
                text(name: 'DESCRIPTION', value: description),
                string(name: 'GROUP', value: GROUP),
                string(name: 'APPLICATION_NAME', value: APP_NAME),
                string(name: 'STAGE', value: 'start')]
            }
        }

    stage('compile') {
        build job: "$COMPILE_JOB_NAME", parameters: [string(name: 'TAG', value:env.TAG), string(name: 'BRANCH', value: BRANCH), string(name: 'WAR_NAME', value: WAR_NAME)]
    }
    for (def item : DEPLOY_JOB_NAMES){
        stage('Prod-MiddleLayer/'+item){
            input 'chekout and continue'
            build job: item,parameters :[string(name: 'TAG', value:env.TAG), string(name: 'OLD_VERSION', value:env.OLD_VERSION), password(name: 'JPS_PWD', value:env.JPS_PWD)]
        }
    }
    stage('complete notification') {
            def userInput = input(
                    id: 'userInput', message: 'go on?', parameters: [
                    [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
            ])
            skip = 'skip' + userInput
            if (skip == 'skipfalse') {
            description = Description.replace("\r","#").replace("\n","#").replace(" ","")
            build job: deploy_notification_job, parameters: [string(name: 'VERSION', value: TAG),
                string(name: 'JIRA_ID', value: JiraID),
                text(name: 'DESCRIPTION', value: description),
                string(name: 'GROUP', value: GROUP),
                string(name: 'APPLICATION_NAME', value: APP_NAME),
                string(name: 'STAGE', value: 'end')]
        }
    }
    stage('email notification') {
        def userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip = 'skip' + userInput
        if (skip == 'skipfalse') {

          build job: deploy_email_job, parameters: [string(name: 'VERSION', value: TAG),
            string(name: 'GROUP', value: GROUP),
            string(name: 'JIRA_ID', value: JiraID),
            text(name: 'DESCRIPTION', value: Description),
            string(name: 'APPLICATION_NAME', value: APP_NAME)]
        }
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
        build job: deploy_record_job, parameters: [string(name: 'VERSION', value: TAG),
                string(name: 'OLD_VERSION', value: OLD_VERSION ), string(name: 'APP_NAME', value: APP_NAME),
                string(name: 'JIRA_ID', value: JiraID), string(name: 'JIRA_REFUSED', value: JIRARefused),
                text(name: 'REFUSED_REASON', value: JIRARefusedReason), string(name: 'SUCCEED', value: succeed),
                text(name: 'FAIL_REASON', value: failReason), string(name: 'ROLLBACK', value: rollback),
                text(name: 'ROLLBACK_REASON', value: rollbackReason)]
    }
}



