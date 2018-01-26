//发布job初始化

def Folder = 'Prod-Insurance'

def COMPILE_JOB_NAME = Folder+'/'+'Prod-Insurance-Manager-Schedule-Compile' //编译job名称，不可以使用中文
def DEPLOY_JOB_NAME = Folder+'/'+'Prod-Insurance-Manager-Schedule-Deploy' //发布job名称，不可以使用中文
def PIPELINE_JOB_NAME = Folder+'/'+'Prod-Insurance-Manager-Schedule-Pipeline'
def ECS_IP = ' '

def GITURL = 'http://h.quyiyuan.com/scm/in/insurance-schedule.git' //git仓库地址

def Git_AUTH = '6b5de867-3f96-4162-9384-4c81e0e7063c' //git仓库认证账号
def BRANCH = ''  //发布分支
def pipelineGitUrl = 'https://github.com/Aaron1989/jenkins-groovy.git'

def OSS_BUCKET = 'deployment-package'  //oss存储bucket
def ENV = 'release'  //配置文件环境
def GROUP = 'qybx'  //所属组
def PRODUCTION = 'ManagerSchedule'   //产品名称，该参数表示oss存储路径和部署服务器路径中微服务名
def WAR_NAME = 'insurance-schedule.war'  //jar包名称

def SHELL_NAME = 'Prod-Configisis'    //选择jumpserver发布脚本，按环境选择


mavenJob("${COMPILE_JOB_NAME}") {
    logRotator(3, 3)       //参数为job保留时间，队列最大job构建数
    parameters {
        stringParam ('TAG',"")
        stringParam ('BRANCH',"$BRANCH")
        stringParam ('WAR_NAME',"$WAR_NAME")
        textParam ('Description',"")
    }

    label('insurance')   //构建job节点选择
    goals('clean package -Pprod')
    rootPOM('pom.xml')
    mavenInstallation('maven-3.2.5')

    scm {
        git {
            remote {
                url("$GITURL")
                branch('$BRANCH')
                credentials("$Git_AUTH")
            }
        }
    }
    publishers {
        aliyunOSSPublisher {
            bucketName("$OSS_BUCKET")
            filesPath("target/*.war")
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
        stringParam ('OLD_VERSION',"")
        stringParam ('WAR_NAME',"$WAR_NAME")
        stringParam ('SERVER_PORT','8080')  //指定服务器端要创建的端口号，并在这个断口监听来自客户端的请求
        stringParam ('TOMCAT_PORT','8005')  //指定一个端口，这个端口负责监听关闭tomcat 的请求
        stringParam ('SSL_PORT','8443')      //redirectPort指定服务器正在处理http请求时收到一个SSL传输请求后重定向的端口号
        stringParam ('AJP_PORT','8009')   //第二个 指定服务器端要创建的端口号，并在这个断口监听来自客户端的请求
        textParam ('JPS_PARAMETER',"{'PRODUCTION':'$PRODUCTION','GROUP':'$GROUP','ENV':'release',"+'"TAG":"$TAG","WAR_NAME":"$WAR_NAME","EXPORT":"","TOMCAT_PORT":"$TOMCAT_PORT","SERVER_PORT":"$SERVER_PORT","SSL_PORT":"$SSL_PORT","AJP_PORT":"$AJP_PORT","SPRING_CONFIG_PROFILE":"release"}')
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


pipelineJob("$PIPELINE_JOB_NAME") {
    logRotator(3, 3)
    parameters {
        password {
            name('JPS_PWD')
            defaultValue('')
            description('')
        }
        stringParam ('TAG',"")
        stringParam ('OLD_VERSION',"")
        stringParam ('BRANCH', "")
        stringParam ('WAR_NAME',"$WAR_NAME")
        textParam ('Description',"")
        stringParam ('JiraID',"")
        stringParam ('APP_NAME', "")
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url(pipelineGitUrl)
                        branch("*/master")
                        credentials("$Git_AUTH")
                    }
                }
            }
            scriptPath("jenkins-pipelines/prodd/Insurance/Prod-insurance-manager-schedule-Pipeline.groovy")
        }
    }
}