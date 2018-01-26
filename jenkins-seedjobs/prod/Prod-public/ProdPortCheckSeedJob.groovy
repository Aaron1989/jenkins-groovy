//发布job初始化
def folder = 'Prod-Public'

def pipelineJobName = folder+'/'+'Prod-Public-Check-Port-Job'
def gitAuth = 'dingqishi'

pipelineJob("$pipelineJobName") {
    logRotator(30, 300)
    label('docker-python')
    parameters {
        stringParam ('IP',"")
        stringParam ('PORT',"")
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
            scriptPath("jenkins-pipelines/prodd/public/ProdPortCheckPipeline.groovy")
        }
    }
}