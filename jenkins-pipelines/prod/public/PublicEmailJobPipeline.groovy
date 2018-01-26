deploy_email_job = 'Prod-Public-Email-Notify'
def tag = params.VERSION
def jiraId = params.JIRA_ID
def group = params.GROUP
def applicationName = params.APPLICATION_NAME
def description = params.DESCRIPTION
def url = 'http://h.quyiyuan.com/scm/devops/devops-shell.git'

node {
    stage('email-notification') {
        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'dingqishi', url: url]]])
        RECIPIENT = sh returnStdout: true, script: "python deploy_email_notify.py $group"
        print RECIPIENT
        T = sh returnStdout: true, script: 'date +"%Y-%m-%d %H:%M"'
        TIME = T.replace('\n','')
        build job: deploy_email_job, parameters: [string(name: 'TAG', value: tag), string(name: 'TIME', value: TIME), string(name: 'JiraID', value: jiraId), text(name: 'CONTENT', value: description), string(name: 'APP_NAME', value: applicationName), string(name: 'RECIPIENT', value: RECIPIENT)]
    }
}