def version = params.VERSION
def jiraId = params.JIRA_ID
def group = params.GROUP
def applicationName = params.APPLICATION_NAME
def stageStatus = params.STAGE
def description = params.DESCRIPTION
def gitUrl = 'http:// <git>/scm/devops/devops-shell.git'

node() {
    stage ('Send wechat message') {
        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false,
                  extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'dingqishi', url:
                gitUrl]]])
        sh "python deploy_notify.py $group $description $jiraId $applicationName $version $stageStatus"
    }
}