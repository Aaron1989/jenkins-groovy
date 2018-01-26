//发布job初始化
def folder = 'Prod-Public'

def pipelineJobName = folder+'/'+'Prod-Public-Wechat-Job'
def gitAuth = 'dingqishi'

pipelineJob("$pipelineJobName") {
    logRotator(30, 300)
    label('docker-python')
    parameters {
        stringParam ('VERSION',"")
        stringParam ('JIRA_ID',"")
        stringParam ('GROUP', "")
        stringParam ('APPLICATION_NAME',"")
        stringParam ('STAGE',"")
        textParam ('DESCRIPTION', "")
    }

  definition {
    cpsScm {
      scm {
        git {
          remote {
            url("https://github.com/Aaron1989/jenkins-groovy.git")
            branch("*/master")
            credentials("$gitAuth")
          }
        }
      }
      scriptPath("jenkins-pipelines/prodd/public/ProdWechatPipeline.groovy")
    }
  }
}