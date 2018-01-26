def COMPILE_JOB_NAME1 = 'Prod-Insurance-Link-CustomerCommon-Compile' //编译job名称，不可以使用中文
def COMPILE_JOB_NAME2 = 'Prod-Insurance-Link-Customer-Compile' //编译job名称，不可以使用中文
def COMPILE_JOB_NAME3 = 'Prod-Insurance-Link-Hospital-Compile' //编译job名称，不可以使用中文
def DEPLOY_JOB_NAME1 = 'Prod-Insurance-Link-CustomerCommon-Deploy1' //发布job名称，不可以使用中文
def DEPLOY_JOB_NAME2 = 'Prod-Insurance-Link-CustomerCommon-Deploy2' //发布job名称，不可以使用中文
def DEPLOY_JOB_NAME3 = 'Prod-Insurance-Link-Customer-Deploy1' //发布job名称，不可以使用中文
def DEPLOY_JOB_NAME4 = 'Prod-Insurance-Link-Customer-Deploy2' //发布job名称，不可以使用中文
def DEPLOY_JOB_NAME5 = 'Prod-Insurance-Link-Hospital-Deploy1' //发布job名称，不可以使用中文
def DEPLOY_JOB_NAME6 = 'Prod-Insurance-Link-Hospital-Deploy2' //发布job名称，不可以使用中文
def skip1
def skip2
def skip3

def GROUP = 'qybx'
def deploy_notification_job = 'Prod-Deploy-Notification'
def deploy_record_job = 'Prod-Deploy-Record'
def deploy_email_job = 'Prod-Email-Notification'
def REPOSITORY_URL = 'http://h.quyiyuan.com/scm/in/insurance_link.git'

//def WAR_NAME1 = 'customer-common-1.0.0.jar'
//def WAR_NAME2 = 'customer_link-0.0.1-SNAPSHOT.jar'
//def WAR_NAME3 = 'hospital_link-0.0.1-SNAPSHOT.jar'

node('base'){
    stage('deploy notification'){
        def userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip = 'skip' + userInput
        if (skip == 'skipfalse') {
        description = Description.replace("\r","#").replace("\n","#").replace(" ","")
        build job: deploy_notification_job, parameters: [string(name: 'TAG', value: TAG), string(name: 'JiraID', value: JiraID), text(name: 'Description', value: description), string(name: 'group', value: GROUP), string(name: 'app_name', value: APP_NAME), string(name: 'stage', value: 'start')]

        }
    }
}

node {
    def JobResult
    def pipelineStatus
    stage('CustomerCommon-Compile') {
        def userInput1 = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip1 = 'skip' + userInput1
        if (skip1 == 'skipfalse') {
            build job: "$COMPILE_JOB_NAME1", parameters: [string(name: 'TAG', value: env.TAG), string(name: 'BRANCH', value: env.BRANCH), text(name: 'Description', value: env.Description)]
        }
    }
}
node('deploy') {
    stage('CustomerCommon-Deploy1') {
        if (skip1 == 'skipfalse') {
            input 'ok?'
            JobResult = build job: "$DEPLOY_JOB_NAME1", parameters: [string(name: 'TAG', value: env.TAG), string(name: "OLD_VERSION", value: env.OLD_VERSION), password(name: 'JPS_PWD', value: env.JPS_PWD)]
            pipelineStatus = JobResult.result
            echo 'deployment result is: + pipelineStatus'
        }
    }
    stage('CustomerCommon-Deploy2') {
        if (skip1 == 'skipfalse') {
            input 'ok?'
            JobResult = build job: "$DEPLOY_JOB_NAME2", parameters: [string(name: 'TAG', value: env.TAG), string(name: "OLD_VERSION", value: env.OLD_VERSION), password(name: 'JPS_PWD', value: env.JPS_PWD)]
            pipelineStatus = JobResult.result
            echo 'deployment result is: + pipelineStatus'
        }
    }
}
node {
    stage('CustomerLink-Compile') {
        def userInput2 = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip2 = 'skip' + userInput2
        if (skip2 == 'skipfalse') {
            build job: "$COMPILE_JOB_NAME2", parameters: [string(name: 'TAG', value: env.TAG), string(name: 'BRANCH', value: env.BRANCH), text(name: 'Description', value: env.Description)]
        }
    }
}
node('deploy') {
    stage('CustomerLink-Deploy1') {
        if (skip2 == 'skipfalse') {
            input 'ok?'
            JobResult = build job: "$DEPLOY_JOB_NAME3", parameters: [string(name: 'TAG', value: env.TAG), string(name: "OLD_VERSION", value: env.OLD_VERSION), password(name: 'JPS_PWD', value: env.JPS_PWD)]
            pipelineStatus = JobResult.result
            echo 'deployment result is: + pipelineStatus'
        }
    }
    stage('CustomerLink-Deploy2') {
        if (skip2 == 'skipfalse') {
            input 'ok?'
            JobResult = build job: "$DEPLOY_JOB_NAME4", parameters: [string(name: 'TAG', value: env.TAG), string(name: "OLD_VERSION", value: env.OLD_VERSION), password(name: 'JPS_PWD', value: env.JPS_PWD)]
            pipelineStatus = JobResult.result
            echo 'deployment result is: + pipelineStatus'
        }
    }
}
node {
    stage('HospitalLink-Compile') {
        def userInput3 = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip3 = 'skip' + userInput3
        if (skip3 == 'skipfalse') {
            build job: "$COMPILE_JOB_NAME3", parameters: [string(name: 'TAG', value: env.TAG), string(name: 'BRANCH', value: env.BRANCH), text(name: 'Description', value: env.Description)]
        }
    }
}
node('deploy'){
    stage('HospitalLink-Deploy1') {
        if (skip3 == 'skipfalse') {
            input 'ok?'
            JobResult = build job: "$DEPLOY_JOB_NAME5", parameters: [string(name: 'TAG', value: env.TAG), string(name: "OLD_VERSION", value: env.OLD_VERSION), password(name: 'JPS_PWD', value: env.JPS_PWD)]
            pipelineStatus = JobResult.result
            echo 'deployment result is: + pipelineStatus'
        }
    }
    stage('HospitalLink-Deploy2') {
        if (skip3 == 'skipfalse') {
            input 'ok?'
            JobResult = build job: "$DEPLOY_JOB_NAME6", parameters: [string(name: 'TAG', value: env.TAG), string(name: "OLD_VERSION", value: env.OLD_VERSION), password(name: 'JPS_PWD', value: env.JPS_PWD)]
            pipelineStatus = JobResult.result
            echo 'deployment result is: + pipelineStatus'
        }
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
        description = Description.replace("\r","#").replace("\n","#").replace(" ","")
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
        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'dingqishi', url: 'http://h.quyiyuan.com/scm/devops/devops-shell.git']]])
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
        build job: deploy_record_job, parameters: [text(name: 'deploy_record', value: deploy_record)]
   }
}