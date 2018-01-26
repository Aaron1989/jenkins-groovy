def ip = params.IP
def port = params.PORT
def gitUrl = 'http:// <git>/scm/devops/devops-shell.git'

node('docker-python') {
    stage ('Send wechat message') {
        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false,
                  extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'dingqishi', url:
                gitUrl]]])
        sh "python port-check.py $ip $port"
    }
}