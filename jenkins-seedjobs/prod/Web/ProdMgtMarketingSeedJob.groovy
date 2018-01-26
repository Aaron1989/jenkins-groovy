#!/usr/bin/env groovy
//发布job初始化

def env = 'Prod'
def group = 'Web'
def productName = 'Website'
def deployEcsIPs = [' ',' ',' ',' ',' ','  ']
def DEPLOY_JOB_NAME =['Prod-Web-Mgt-Marketing-Build', 'Prod-Web-Mgt-Marketing-Deploy']
def Pipeline_Job_name = 'Prod-Mgt-Marketing-Pipeline'
def gitUrl = "http:// <git>/scm/bigdata/mgt-marketing.git"
def dockerGitUrl = "http:// <git>/scm/dcd/dockercd-website.git"
def gitBranch = "*/release"
def gitAuth = "dingqishi"
def BRANCH_DOCKER = ""

def OSSBucket = "deployment-package"
def OSSObject = "prod/marketing/mgt-marketing"
def artifacts = "target/*.jar"
def objectPre = "prod/marketing/mgt-marketing/\$TAG"
def warName = "gateway-0.0.1-SNAPSHOT.jar"
def shellName = "Prod-MicroService"
def jpsParameter = '''{'PRODUCTION':'web','GROUP':'website','APP_OPTS':' ','JAVA_OPTS':'-Xms2048M -Xmx2048M','EUREKA_OPTS':'--eureka.instance.metadataMap.OldVersion=$OLD_VERSION --eureka.instance.metadataMap.LastVersion=$TAG --eureka.instance.metadataMap.WarName=$WAR_NAME --eureka.instance.metadataMap.Date=`date "+%Y-%m-%d"` --eureka.instance.metadataMap.Time=`date "+%H:%M:%S "` --eureka.instance.metadataMap.JavaOptXms=2048M --eureka.instance.metadataMap.JavaOptXmx=2048M','EXPORT':'export eurekaserverlistzone=http:// :8762/eureka/,http:// :8762/eureka/ export platform=desktop','ENV':'distrelease',"TAG":"$TAG","WAR_NAME":"$WAR_NAME"}'''
def applicationName = env+"-Website-gateway"

def Folder = "Prod-Web"
def compileJobName = Folder+ '/' +'Prod-Web-Mgt-Marketing-Compile'
def deployJobNameTemple = env+'-'+group+'-'+productName+'-Deploy'

def deployJobInfo = [:]
def deployJobNames =[]
int i = 0
for (item in deployEcsIPs) {
    def deployJobName = Folder + '/' + deployJobNameTemple + '-' + i
    deployJobInfo[deployJobName] = deployEcsIPs[i]
    deployJobNames.add(deployJobName)
    i++
}

def DockerMasterUrl = "https://master2g3.cs-cn-qingdao.aliyun.com:20111"
def DockerAuth = "website-prod-auth"
def DockerAppName = "mgt-marketing"
def DockerComposeTemplate = "marketing/mgt-marketing/docker-compose-deploy.yml"


def EXPORT = 'export eurekaserverlistzone=http:// :8762/eureka/,http:// :8762/eureka/ export platform=desktop'
def Xms = '2048M'
def Xmx = '2048M'
def JAVA_OPTS = "-Xms${Xms} -Xmx${Xmx}"//java启动参数，若有需要请自行添加
def SHELL_NAME = 'Prod-MicroService'    //选择jumpserver发布脚本，按环境选择

//以下参数无特殊情况请勿修改
def DATE = '`date "+%Y-%m-%d"`'    //该参数为固定参数
def TIME = '`date "+%H:%M:%S "`'   //该参数为固定参数
def EUREKA_OPTS = '--eureka.instance.metadataMap.OldVersion=$OLD_VERSION --eureka.instance.metadataMap.LastVersion=$TAG --eureka.instance.metadataMap.WarName=$WAR_NAME' + " --eureka.instance.metadataMap.Date=$DATE --eureka.instance.metadataMap.Time=$TIME --eureka.instance.metadataMap.JavaOptXms=$Xms --eureka.instance.metadataMap.JavaOptXmx=$Xmx"

def cacheShell = 'sleep 2m'+'\n'
for (item in deployEcsIPs) {
    cacheShell = cacheShell + "curl -X POST -H \"env_host:https://www.quyiyuan.com\" \""+item+":8080/caches/HomeCache/update/NzVlMzdhMjhiOGUxMWU5OTVmOGQwMjcyZGE1NzllYTQ4ODNlNDRkOA\" &"+'\n'
    cacheShell = cacheShell + "curl -X POST -H \"env_host:https://www.7yiyuan.com\" \""+item+":8080/caches/HomeCache/update/NzVlMzdhMjhiOGUxMWU5OTVmOGQwMjcyZGE1NzllYTQ4ODNlNDRkOA\" &"+'\n'
}

mavenJob(compileJobName) {
    logRotator(3, 2)       //参数为job保留时间，队列最大job构建数
//    folder('官网生产发布环境') {
//        primaryView('官网生产发布环境')
//    }
//    label('compile')
    parameters {
        stringParam ('TAG',"")
        textParam ('Description',"")
    }

    label('compile')   //构建job节点选择

//    preBuildSteps {
//        shell(
//                "#yum install npm -y\n" +
//                        "#npm install gulp -g\n" +
//                        "#npm isntall gulp-cli\n" +
//                        "#npm install gulp-autoprefixer\n" +
//                        "#npm install\n" +
//                        "#yum install gem -y\n" +
//                        "#gem install sass\n" +
//                        "#npm install -g n\n" +
//                        "#n stable\n" +
//                        "gulp prod\n" +
//                        "cd /data\n" +
//                        "python osscmd uploadfromdir /opt/jenkins/workspace/Gray-Website-Compile/static/ oss://qy-staticresources/website/ --check_md5=true --check_point=/opt/jenkins/workspace/Gray-Website-Compile/cp.txt"
//
//        )
//    }
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

                '''DOCKER_HOST=''\ndocker-compose -f marketing/mgt-marketing/docker-compose-build.yml build --no-cache\ndocker push registry-internal.cn-qingdao.aliyuncs.com/quyiyuan/mgt-marketing:$DOCKER_TAG_VERSION'''

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
            scriptPath("jenkins-pipelines/prodd/Web/Prod-Mgt-Marketing-Pipeline")
        }
    }
}

//for (def item : deployJobNames){
//    freeStyleJob(item) {
//        logRotator(3, 3)
//        parameters {
//            password {
//                name('JPS_PWD')
//                defaultValue('')
//                description('')
//            }
//            stringParam ('TAG',"")
//            stringParam ('OLD_VERSION',"")
//            stringParam ('WAR_NAME', warName)
////            textParam ('JPS_PARAMETER',"{'PRODUCTION':'$PRODUCTION','GROUP':'$GROUP','APP_OPTS':'$APP_OPTS','JAVA_OPTS':'$JAVA_OPTS','EUREKA_OPTS':'$EUREKA_OPTS','EXPORT':'$EXPORT','ENV':'$ENV',"+'"TAG":"$TAG","WAR_NAME":"$WAR_NAME"}')
//            textParam ('JPS_PARAMETER', jpsParameter)
//            stringParam ('SHELL_NAME', shellName)
//
//        }
//        steps {
//            jumpserverHttpsBuilder {
//                //调用jumpserver插件发布
//                jpsURL('生产环境jps')
//                appName(applicationName)   //记录jumpserver上发布的微服务名称
//                ecsSearchOption(deployJobInfo[item])
//
//            }
//        }
//    }
//}
//
//freeStyleJob(Folder+'/'+env+'-'+productName+"-Cache") {
//    logRotator(3, 3)
//    steps {
//        shell(
//                cacheShell
//        )
//    }
//}

