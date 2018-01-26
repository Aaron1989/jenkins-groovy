//发布job初始化
def folder = 'Prod-Public'

def pipelineJobName = folder+'/'+'Prod-Public-Cloud-Check'
def gitAuth = 'dingqishi'

pipelineJob("$pipelineJobName") {
    logRotator(30, 100)
    parameters {
        stringParam ('SLBS',"")
        stringParam ('VGROUPS',"")
        stringParam ('SGROUPS', "")
        stringParam ('INSTANCES',"")
        stringParam ('RDSS',"")
        stringParam ('LGROUP', "")
        stringParam ('LPROJECT', "")
        stringParam ('IPS', "")
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
      scriptPath("jenkins-pipelines/prodd/public/ProdCloudCheckPipeline.groovy")
    }
  }
}