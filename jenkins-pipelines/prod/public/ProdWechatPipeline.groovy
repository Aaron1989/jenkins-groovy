def version = params.VERSION
def jiraId = params.JIRA_ID
def group = params.GROUP
def applicationName = params.APPLICATION_NAME
def stageStatus = params.STAGE
def description = params.DESCRIPTION
def gitUrl = 'http://h.quyiyuan.com/scm/devops/devops-shell.git'

node('docker-python') {
    stage ('Send wechat message') {
        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false,
                  extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'dingqishi', url:
                gitUrl]]])
        des = description.replace("\r","#").replace("\n","#").replace(" ","")
        sh "python deploy_notify.py devops $des $jiraId $applicationName $version $stageStatus"
        sh "python deploy_notify.py $group $des $jiraId $applicationName $version $stageStatus"
    }
}