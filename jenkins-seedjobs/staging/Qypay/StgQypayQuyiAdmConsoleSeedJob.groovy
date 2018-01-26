#!/usr/bin/env groovy
//发布job初始化
def Folder = 'Stg-QyPay'
def COMPILE_JOB_NAME = Folder+'/'+'Stg-Qypay-QuyiAdmConsole-Compile' //编译job名称，不可以使用中文
def DEPLOY_JOB_NAME = Folder+'/'+'Stg-Qypay-QuyiAdmConsole-Deploy' //发布job名称，不可以使用中文
def PIPELINE_JOB_NAME1 = Folder+'/'+'Stg-Qypay-QuyiAdmConsole-Pipeline'

def GITURL = 'http://h.quyiyuan.com/scm/qypay/quyiadmconsole.git' //git仓库地址
def Git_AUTH = 'dingqishi' //git仓库认证账号
def BRANCH = '*/staging'  //发布分支

def OSS_BUCKET = 'deployment-package'  //oss存储bucket
def ENV = 'staging'  //配置文件环境
def GROUP = 'qypay'  //所属组
def PRODUCTION = 'QuyiAdmConsole'   //产品名称，该参数表示oss存储路径和部署服务器路径中微服务名
def WAR_NAME = 'QuyiAdmConsole.war'  //WAR包名称

def APP_NAME = ''  //微服务名称,一般固定在代码里，若不修改则为空
def APP_OPTS = ""   //此参数用于强制注入微服务名称，一般会固定在代码中，若不修改该参数为空
def SERVER_PORT ="7000"
def TOMCAT_PORT="7005"
def SSL_PORT="8443"
def AJP_PORT="7009"
def SPRING_CONFIG_PROFILE=""

def ECS_IP= ' '
def EXPORT =''
//def Xms = '128M'
//def Xmx = '256M'
//def JAVA_OPTS = "-Xms${Xms} -Xmx${Xmx} --spring.cloud.config.uri=http:// :7777 "//java启动参数，若有需要请自行添加
def JAVA_OPTS = ""
def SHELL_NAME = 'Prod-Configisis'    //选择jumpserver发布脚本，按环境选择

//以下参数无特殊情况请勿修改
def DATE = '`date "+%Y-%m-%d"`'    //该参数为固定参数
def TIME = '`date "+%H:%M:%S "`'   //该参数为固定参数
//def EUREKA_OPTS = '--eureka.instance.metadataMap.OldVersion=$OLD_VERSION --eureka.instance.metadataMap.LastVersion=$TAG --eureka.instance.metadataMap.WarName=$WAR_NAME' + " --eureka.instance.metadataMap.Date=$DATE --eureka.instance.metadataMap.Time=$TIME --eureka.instance.metadataMap.JavaOptXms=$Xms --eureka.instance.metadataMap.JavaOptXmx=$Xmx"
def EUREKA_OPTS = ''

freeStyleJob("${COMPILE_JOB_NAME}") {
    logRotator(3, 3)       //参数为job保留时间，队列最大job构建数
    parameters {
        stringParam ('TAG',"")
        stringParam ('BRANCH',"$BRANCH")
        stringParam ('WAR_NAME',"$WAR_NAME")
    }
    label('compile')   //构建job节点选择
    /*preBuildSteps {
        shell(
                "yum install ant -y"
        )
    }*/
    scm {
        git {
            remote {
                url("$GITURL")
                branch('$BRANCH')
                credentials("$Git_AUTH")
            }
        }
    }
    steps {
        ant {
            //target('test')
            //targets(['publish', 'deploy'])
            //prop('clean install -qy.project.version', '$TAG')
            props('clean install -qy.project.version':'$TAG' ,'-Dmaven.test.skip':true)
            buildFile('build.xml')
            //javaOpt('-Xmx1g')
            //javaOpts(['-Dprop2=value2', '-Dprop3=value3'])
            antInstallation('Ant 1.8')
        }
    }

    publishers {
        aliyunOSSPublisher {
            bucketName("$OSS_BUCKET")
            filesPath('$WAR_NAME')
            objectPrefix("$ENV/$GROUP/$PRODUCTION/"+'$TAG')
        }
    }
}

freeStyleJob("$DEPLOY_JOB_NAME") {
    logRotator(3, 3)
    parameters {
        password {
            name('JPS_PWD')
            defaultValue('')
            description('')
        }
        stringParam ('TAG',"")
        //stringParam ('OLD_VERSION',"")
        stringParam ('WAR_NAME',"$WAR_NAME")
        textParam ('JPS_PARAMETER',"{'PRODUCTION':'$PRODUCTION','GROUP':'$GROUP','APP_OPTS':'$APP_OPTS','ENV':'$ENV','JAVA_OPTS':'$JAVA_OPTS','EUREKA_OPTS':'$EUREKA_OPTS',"+'"TAG":"$TAG","WAR_NAME":"$WAR_NAME","EXPORT":""}')
        stringParam ('SHELL_NAME',"$SHELL_NAME")

    }
    steps {
        jumpserverHttpsBuilder {
            //调用jumpserver插件发布
            jpsURL('生产环境jps')
            appName("$PRODUCTION")   //记录jumpserver上发布的微服务名称
            ecsSearchOption("$ECS_IP")

        }
    }
}

pipelineJob("$PIPELINE_JOB_NAME1") {
    logRotator(3, 3)
    parameters {
        password {
            name('JPS_PWD')
            defaultValue('')
            description('')
        }
        stringParam ('TAG',"")
        //stringParam ('OLD_VERSION',"")
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
            scriptPath("jenkins-pipelines/staging/Qypay/Stg-Qypay-QuyiAdmConsole-Pipeline.groovy")
        }
    }
}