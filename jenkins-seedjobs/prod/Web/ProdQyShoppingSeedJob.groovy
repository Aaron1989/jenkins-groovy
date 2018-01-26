#!/usr/bin/env groovy
//发布job初始化


def DEPLOY_JOB_NAME =['Prod-Web-Qy-Shopping-Build', 'Prod-Web-Qy-Shopping-Deploy']
def Pipeline_Job_name = 'Prod-Qy-Shopping-Pipeline'
def gitUrl = "http:// <git>/scm/bigdata/qy-shopping.git"
def dockerGitUrl = "http:// <git>/scm/dcd/dockercd-website.git"
def gitBranch = "*/release"
def gitAuth = "dingqishi"

def OSSBucket = "deployment-package"
def OSSObject = "prod/marketing/qy-shopping"
def artifacts = "site/target/*.jar"
def objectPre = "prod/marketing/qy-shopping/\$TAG"

def Folder = "Prod-Web"
def compileJobName = Folder+ '/' +'Prod-Web-Qy-Shopping-Compile'

def DockerMasterUrl = "https://master2g3.cs-cn-qingdao.aliyun.com:20111"
def DockerAuth = "website-prod-auth"
def DockerAppName = "qy-shopping"
def DockerComposeTemplate = "marketing/qy-shopping/docker-compose-deploy.yml"


mavenJob(compileJobName) {
    logRotator(3, 2)       //参数为job保留时间，队列最大job构建数
    parameters {
        stringParam ('TAG',"")
        textParam ('Description',"")
    }

    label('compile')   //构建job节点选择
    goals('clean install -Pmono-mode -Dmaven.test.skip=true')
    rootPOM('pom.xml')
    mavenInstallation('maven-3.2.5')
    scm {
        git {
            remote {
                url(gitUrl)
                branch(gitBranch)
                credentials(gitAuth)
            }
            extensions{
                preBuildMerge {
                    options{
                        mergeRemote('origin')
                        mergeTarget('master')
                        mergeStrategy('default')
                        fastForwardMode('FF')
                    }
                }
            }
        }
    }

    publishers {
        gitPublisher{
            tagsToPush {
                tagToPush {
                    targetRepoName('origin')
                    tagName('$TAG')
                    tagMessage('$Description')
                    forcePush(false)
                    createTag(true)
                    updateTag(true)
                }
            }
            pushOnlyIfSuccess(true)
            pushMerge(true)
        }
        aliyunOSSPublisher {
            bucketName(OSSBucket)
            filesPath(artifacts)
            objectPrefix(objectPre)
        }
    }
}

freeStyleJob(Folder+ '/' +DEPLOY_JOB_NAME[0]) {
    logRotator(2, 2)
    parameters {
        stringParam {
            name('DOCKER_TAG_VERSION')
            defaultValue('')
            description('')
        }
        stringParam {
            name('OSS_OBJECT')
            defaultValue(OSSObject)
            description('')
        }
        stringParam {
            name('OSS_BUCKET')
            defaultValue(OSSBucket)
            description('')
        }
        stringParam {
            name('BRANCH_DOCKER')
            defaultValue('feature/marketing')
            description('')
        }
    }
    label('docker-python')
    steps{
        shell(
                '''DOCKER_HOST=''\ndocker-compose -f marketing/qy-shopping/site/docker-compose-build.yml build --no-cache\ndocker push registry-internal.cn-qingdao.aliyuncs.com/quyiyuan/qy-shopping-site:$DOCKER_TAG_VERSION'''
        )
    }

    scm {
        git {
            remote {
                url(dockerGitUrl)
                branch('*/$BRANCH_DOCKER')
                credentials(gitAuth)
            }
        }
    }
}

freeStyleJob(Folder+ '/' +DEPLOY_JOB_NAME[1]) {
    logRotator(2, 2)
    parameters {
        stringParam {
            name('DOCKER_TAG_VERSION')
            defaultValue('')
            description('')
        }
        stringParam {
            name('BRANCH_DOCKER')
            defaultValue('feature/marketing')
            description('')
        }
        stringParam {
            name('PROFILE_ACTIVE')
            defaultValue('')
            description('')
        }

    }
    label('docker-python')
    scm {
        git {
            remote {
                url(dockerGitUrl)
                branch('*/$BRANCH_DOCKER')
                credentials(gitAuth)
            }
        }
    }

    steps{
        deployBuilder {
            masterurl(DockerMasterUrl)
            appName(DockerAppName)
            composeTemplate(DockerComposeTemplate)
            credentialsId(DockerAuth)
            publishStrategy("rolling")
        }
    }
}

pipelineJob(Folder+ '/'+Pipeline_Job_name) {
    logRotator(3, 3)
    parameters {
        password {
            name('JPS_PWD')
            defaultValue('')
            description('')
        }
        stringParam ('TAG',"")
//        stringParam ('OLD_VERSION',"")
        textParam ('Description',"")
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url("https://github.com/Aaron1989/jenkins-groovy.git")
                        branch("*/master")
                        credentials(gitAuth)
                    }
                }
            }
            scriptPath("jenkins-pipelines/prodd/Web/Prod-Qy-Shopping-Pipeline")
        }
    }
}



