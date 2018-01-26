//发布job初始化
def folder = 'Prod-Public'

def pipelineJobName = folder+'/'+'Prod-Public-Email-Job'
def emailNotifyJob = folder+'/'+'Prod-Public-Email-Notify'
def gitAuth = 'dingqishi'
def message = '时间:$TIME\n发布任务:$JiraID\n发布项目:$APP_NAME\n发布版本:$TAG\n发布内容:\n$CONTENT'

freeStyleJob("$emailNotifyJob") {
    logRotator(30, 100)
    parameters {
        stringParam ('APP_NAME',"")
        textParam ('CONTENT',"")
        stringParam ('TAG',"")
        stringParam ('JiraID',"")
        stringParam ('RECIPIENT',"")
        stringParam ('TIME',"")

    }
    publishers {
        extendedEmail {
            recipientList('$RECIPIENT')
            defaultSubject('发布通告：$APP_NAME')
            defaultContent("$message")
            contentType('text/plain')
            triggers {
                always {
                        sendTo {
                        recipientList()

                    }
                }
            }
        }
    }
}

pipelineJob("$pipelineJobName") {
    logRotator(30, 100)
    parameters {
        stringParam ('VERSION',"")
        stringParam ('JIRA_ID',"")
        stringParam ('GROUP', "")
        stringParam ('APPLICATION_NAME',"")
        textParam ('DESCRIPTION',"")

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
      scriptPath("jenkins-pipelines/prodd/public/PublicEmailJobPipeline.groovy")
    }
  }
}