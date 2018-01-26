//发布job初始化
def folder = 'Prod-Public'

def pipelineJobName = folder+'/'+'Prod-Public-Jira-Confirm'
def Git_AUTH = 'dingqishi'

pipelineJob("$pipelineJobName") {
    logRotator(30, 300)
    parameters {
        stringParam ('VERSION',"")
        stringParam ('OLD_VERSION',"")
        stringParam ('APP_NAME', "")
        stringParam ('JIRA_ID',"")
        stringParam ('JIRA_REFUSED',"否")
        textParam ('REFUSED_REASON', "/")
        stringParam ('SUCCEED', "是")
        textParam ('FAIL_REASON', "/")
        stringParam ('ROLLBACK', "否")
        textParam ('ROLLBACK_REASON', "/")
    }

  definition {
    cpsScm {
      scm {
        git {
          remote {
            url("https://github.com/Aaron1989/jenkins-groovy.git")
            branch("*/master")
            credentials("$Git_AUTH")
          }
        }
      }
      scriptPath("jenkins-pipelines/prodd/public/ProdJiraConfirmPipeline.groovy")
    }
  }
}