def COMPILE_JOB_NAME = 'Prod-Bozhou-bzxnh-payment-Compile' //编译job名称，不可以使用中文

def GROUP = 'web'
def deploy_notification_job = 'Prod-Deploy-Notification'
def deploy_record_job = 'Prod-Deploy-Record'
def deploy_email_job = 'Prod-Email-Notification'

def deployJobName = ['app2-8500','app2-8888','app3-9500','app3-9999','app4-8500','app4-8888','app5-9500','app5-9999']
def deployJobTitle = 'Prod-bzxnh-payment-deploy-'

def WAR_NAME = 'bzxnh-payment-1.0-SNAPSHOT.jar'

node {
    def JobResult
    def pipelineStatus
    stage('Compile') {
        build job: "$COMPILE_JOB_NAME", parameters: [string(name: 'TAG', value: env.TAG), text(name: 'Description', value: env.Description)]
    }
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

    def testApp = 'app2-8500'
//    stage('Deploy-' + testApp) {
//        input 'ok?'
//        JobResult = build job : deployJobTitle + testApp,parameters :[string(name: 'TAG', value:env.TAG), string(name: 'WAR_NAME', value:env.WAR_NAME)]
//    }

    for (def item : deployJobName){
        if (item == testApp) {
            input 'ok?'
            print deployJobTitle + item
            JobResult = build job : deployJobTitle + item,parameters :[string(name: 'TAG', value:env.TAG), string(name: 'WAR_NAME', value:env.WAR_NAME)]
        } else {
            stage('Deploy-' + item) {
                input 'ok?'
                print deployJobTitle + item
                print 'there should not be a input'
                JobResult = build job : deployJobTitle + item,parameters :[string(name: 'TAG', value:env.TAG), string(name: 'WAR_NAME', value:env.WAR_NAME)]
            }
        }
//
//        stage('Deploy-' + item) {
//            input 'ok?'
//            print deployJobTitle + item
//           JobResult = build job : deployJobTitle + item,parameters :[string(name: 'TAG', value:env.TAG), string(name: 'WAR_NAME', value:env.WAR_NAME)]
//        }
    }
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
        build job: deploy_email_job, parameters: [string(name: 'TAG', value: TAG), string(name: 'TIME', value: TIME), string(name: 'JiraID', value: JiraID), text(name: 'CONTENT', value: Description), string(name: 'APP_NAME', value: APP_NAME), string(name: 'RECIPIENT', value: RECIPIENT)]
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

