def Folder = 'Stg-Website'

def COMPILE_JOB_NAME = Folder+'/'+'Stg-Website-Compile-3'
def COMPILE_JOB_NAME2 = Folder+'/'+'Stg-Website-Import-Compile-3'
def DEPLOY_JOB_NAME1 = Folder+'/'+'Stg-Website-Compose-Build-3'
def DEPLOY_JOB_NAME2 = Folder+'/'+'Stg-Website-Image-Push-3'
def DEPLOY_JOB_NAME3 = Folder+'/'+'Stg-Website-Compose-Up-3'
def PIPELINE_JOB_NAME1 = Folder+'/'+'Stg-Website-Pipeline-3'
def OSS_BUCKET = 'deployment-package'
def OSS_OBJECT = 'dev/website/web'
def ARTIFACTS = 'dists/dist1/target/*.jar,dists/dist2/target/*.jar,gateway/target/*.jar,eureka/target/*.jar,ui-job/target/*.jar'
def WAR_NAME = 'ms-import-1.0-SNAPSHOT.jar'
def GITURL1 = 'http:// <git>/scm/qyw/qy-web-ant.git'
def GITURL2 = 'http:// <git>/scm/dcd/dockercd-website'
def GITURL3 = 'http:// <git>/scm/qyw/qy-web-import.git'
def PROFILE_ACTIVE = 'diststaging'
def GULP_ENV = 'stg'

def DockerMasterUrl ='https://master3g3.cs-cn-qingdao.aliyun.com:20063'
def DockerAppName = 'stg-3'
def DockerComposeTemplate ='docker-compose-test1-deploy.yml'
def DockerCredentialsId = 'website-docker-auth'


mavenJob("$COMPILE_JOB_NAME") {
    logRotator(2, 2)       //参数为job保留时间，队列最大job构建数

    parameters {
        stringParam {
            name('branch')
            defaultValue('')
            description('')
        }
        stringParam {
            name('DOCKER_TAG_VERSION')
            defaultValue('')
            description('')
        }
    }

    label('compile')   //构建job节点选择

    preBuildSteps {
        shell(
                "export LANG=en_US.UTF-8\n" +
                        "npm install\n" +
                        "gulp $GULP_ENV\n" +
                        //"cd /opt/OSS_Python_API\n" +
                        'python /opt/OSS_Python_API/osscmd uploadfromdir ./static/ oss://qy-staticresources-test/website/'
        )
    }

    goals('clean install -Pmono-mode -Dmaven.test.skip=true')
    rootPOM('pom.xml')
    mavenInstallation('maven-3.2.5')

    scm {
        git {
            remote {
                url("$GITURL1")
                branch('*/$branch')
                credentials("dingqishi")
            }
        }
    }


    publishers {
        aliyunOSSPublisher {
            bucketName("$OSS_BUCKET")
            filesPath("$ARTIFACTS")
            objectPrefix("$OSS_OBJECT/"+'$DOCKER_TAG_VERSION')
        }
    }
}

mavenJob("$COMPILE_JOB_NAME2") {
    logRotator(2, 2)       //参数为job保留时间，队列最大job构建数

    parameters {
        stringParam {
            name('IMPORT_BRANCH')
            defaultValue('')
            description('')
        }
        stringParam {
            name('DOCKER_TAG_VERSION')
            defaultValue('')
            description('')
        }
    }

    label('compile')   //构建job节点选择
    goals('clean install -U -DskipTests=true')
    rootPOM('pom.xml')
    mavenInstallation('maven-3.2.5')

    scm {
        git {
            remote {
                url("$GITURL3")
                branch('*/$IMPORT_BRANCH')
                credentials("dingqishi")
            }
        }
    }


    publishers {
        aliyunOSSPublisher {
            bucketName("$OSS_BUCKET")
            filesPath("target/$WAR_NAME")
            objectPrefix("$OSS_OBJECT/"+'$DOCKER_TAG_VERSION')
        }
    }
}


freeStyleJob("$DEPLOY_JOB_NAME1") {
    logRotator(2, 2)

    parameters {
        stringParam {
            name('DOCKER_TAG_VERSION')
            defaultValue('')
            description('')
        }
        stringParam {
            name('OSS_OBJECT')
            defaultValue('')
            description('')
        }
        stringParam {
            name('OSS_BUCKET')
            defaultValue('')
            description('')
        }
        stringParam {
            name('PROFILE_ACTIVE')
            defaultValue('')
            description('')
        }
        stringParam {
            name('platform')
            defaultValue('')
            description('')
        }
        stringParam {
            name('BRANCH_DOCKER')
            defaultValue('')
            description('')
        }
    }

    label('docker-python')

    steps{
        shell(
                "DOCKER_HOST=''\n" +
                        "docker-compose -f docker-compose-build.yml build --no-cache"
        )
    }

    scm {
        git {
            remote {
                url("$GITURL2")
                branch('*/$BRANCH_DOCKER')
                credentials("dingqishi")
            }
        }
    }
}



freeStyleJob("$DEPLOY_JOB_NAME2") {
    logRotator(2, 2)
    parameters {
        stringParam {
            name('DOCKER_TAG_VERSION')
            defaultValue('')
            description('')
        }
        stringParam {
            name('HAS_IMPORT')
            defaultValue('')
            description('')
        }
    }
    label('docker-python')

    steps {
        shell(
                "DOCKER_HOST=''\n" +
                'docker push registry-internal.cn-qingdao.aliyuncs.com/quyiyuan/gateway:$DOCKER_TAG_VERSION\n' +
//                'docker push registry-internal.cn-qingdao.aliyuncs.com/quyiyuan/eureka:$DOCKER_TAG_VERSION\n' +
                'docker push registry-internal.cn-qingdao.aliyuncs.com/quyiyuan/jobs:$DOCKER_TAG_VERSION\n' +
                'if [ "$HAS_IMPORT" = "true" ]\n' +
                'then\n' +
                'docker push registry-internal.cn-qingdao.aliyuncs.com/quyiyuan/ms-import:$DOCKER_TAG_VERSION\n' +
                'fi'

        )
    }
}



freeStyleJob("$DEPLOY_JOB_NAME3") {
    logRotator(2, 2)
    parameters {
        stringParam {
            name('DOCKER_TAG_VERSION')
            defaultValue('')
            description('')
        }
        stringParam {
            name('OSS_OBJECT')
            defaultValue('')
            description('')
        }
        stringParam {
            name('OSS_BUCKET')
            defaultValue('')
            description('')
        }
        stringParam {
            name('PROFILE_ACTIVE')
            defaultValue('')
            description('')
        }
        stringParam {
            name('platform')
            defaultValue('')
            description('')
        }
        stringParam {
            name('APPLICATION_NAME')
            defaultValue('')
            description('')
        }
        stringParam {
            name('domainName')
            defaultValue('')
            description('')
        }
        stringParam {
            name('subName')
            defaultValue('')
            description('')
        }
        stringParam {
            name('PROFILE_ACTIVE_IMPORT')
            defaultValue('staging')
            description('')
        }
        stringParam {
            name('BRANCH_DOCKER')
            defaultValue('')
            description('')
        }
    }

    label('docker-python')

    scm {
        git {
            remote {
                url("$GITURL2")
                branch('*/$BRANCH_DOCKER')
                credentials("dingqishi")
            }
        }
    }
    steps{
        deployBuilder {
            masterurl(DockerMasterUrl)
            appName(DockerAppName)
            composeTemplate(DockerComposeTemplate)
            credentialsId(DockerCredentialsId)
            publishStrategy('rolling')
        }
    }

}

pipelineJob("$PIPELINE_JOB_NAME1") {
    logRotator(3, 3)
    parameters {
            string {
                defaultValue('')
                description('官网编译git分支')
                name('branch')
            }
            string {
                defaultValue('')
                description( 'docker的版本号，也是包上传的地址，每次需要新的未曾使用的版本号，请严格按照月日时分格式，如05201709')
                name('DOCKER_TAG_VERSION')
            }
            string {
                    defaultValue('')
                    description('测试地址为www.quyiyuan.net')
                    name('platform')
            }
            string {
                defaultValue('false')
                description('发布时有import填true,没有import填false')
                name('HAS_IMPORT')
            }
            string {
                defaultValue('')
                description('import的git分支,不发import为空')
                name('IMPORT_BRANCH')
            }
            string {
                defaultValue('feature/new-stg')
                description('发布时有import在默认值之后加上-import')
                name('BRANCH_DOCKER')
            }
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url("https://github.com/Aaron1989/jenkins-groovy.git")
                        branch("*/master")
                        credentials("dingqishi")
                    }
                }
            }
            scriptPath("jenkins-pipelines/staging/Website/Stg-Website-Pipeline-3.groovy")
        }
    }
}

