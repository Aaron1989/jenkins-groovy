
/**
 * Created by ronin on 2017/6/23.
 */
//发布job初始化

def Folder = 'Prod-MiddleLayer'

def COMPILE_JOB_NAME = Folder+'/'+'Prod-MiddleLayer-MsConfigIsis-Compile' //编译job名称，不可以使用中文
def DEPLOY_JOB_NAME1 = Folder+'/'+'Prod-MiddleLayer-MsConfigIsis-Deploy' //发布job名称，不可以使用中文
def PIPELINE_JOB_NAME1 = Folder+'/'+'Prod-MiddleLayer-MsConfigIsis-Pipeline'
def ECS_IP = [' ']

def GITURL = 'http:// <git>/scm/ms/isis-config.git' //git仓库地址

def Git_AUTH = 'dingqishi' //git仓库认证账号
def BRANCH = '*/release'  //发布分支
def pipelineGitUrl = 'https://github.com/Aaron1989/jenkins-groovy.git'



def OSS_BUCKET = 'deployment-package'  //oss存储bucket
def ENV = 'release'  //配置文件环境
def GROUP = 'middlelayer'  //所属组
def PRODUCTION = 'ms-config-isis'   //产品名称，该参数表示oss存储路径和部署服务器路径中微服务名
def WAR_NAME = 'simpleapp.war'  //jar包名称
def WAR_PATH = 'myapp/webapp/target'



def SHELL_NAME = 'Prod-Configisis'    //选择jumpserver发布脚本，按环境选择

//以下参数无特殊情况请勿修改
def DATE = '`date "+%Y-%m-%d"`'    //该参数为固定参数
def TIME = '`date "+%H:%M:%S "`'   //该参数为固定参数

def ScriptPath = 'prod/'+GROUP+'/prod-MiddleLayer-MsConfigIsis-Pipeline.groovy'

mavenJob("${COMPILE_JOB_NAME}") {
    logRotator(3, 2)       //参数为job保留时间，队列最大job构建数
    parameters {
        stringParam ('TAG',"")
        stringParam ('BRANCH',"$BRANCH")
        stringParam ('WAR_NAME',"$WAR_NAME")
    }

    label('node3')   //构建job节点选择
    goals('clean install  -Dmaven.test.skip=true')
    rootPOM('myapp/pom.xml')
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
            filesPath("$WAR_PATH"+"/"+"$WAR_NAME")
            objectPrefix("$ENV/$GROUP/$PRODUCTION/"+'$TAG')
        }
    }
}

freeStyleJob("$DEPLOY_JOB_NAME1") {
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
        stringParam ('SERVER_PORT','8080')  //指定一个端口，这个端口负责监听关闭tomcat 的请求
        stringParam ('TOMCAT_PORT','8005')  //指定服务器端要创建的端口号，并在这个断口监听来自客户端的请求
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
            ecsSearchOption(ECS_IP[0])
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
        stringParam ('OLD_VERSION',"")
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
            scriptPath(ScriptPath)
        }
    }
}