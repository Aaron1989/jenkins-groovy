//发布job初始化
def folder = 'Prod-Public'

def pipelineJobName = folder+'/'+'Prod-Public-Package-Check'
def gitAuth = 'dingqishi'

pipelineJob("$pipelineJobName") {
    logRotator(30, 100)
    parameters {
        stringParam ('VERSION',"")
        stringParam ('OLD_VERSION',"")
        stringParam ('OSS_BUCKET', "")
        stringParam ('WAR_NAME',"")
        stringParam ('PREFIX',"")

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
      scriptPath("jenkins-pipelines/prodd/public/ProdPackageCheckPipeline.groovy")
    }
  }
}