//发布job初始化
def folder = 'Prod-Public'

def pipelineJobName = folder+'/'+'Prod-Public-Deploy'
def gitAuth = 'dingqishi'

pipelineJob("$pipelineJobName") {
    logRotator(30, 500)
    parameters {
        password {
            name('JPS_PWD')
            defaultValue('')
            description('')
        }
        stringParam ('APP_NAME',"")
        stringParam ('IP', "")
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
      scriptPath("jenkins-pipelines/prodd/public/ProdDeployPipeline.groovy")
    }
  }
}