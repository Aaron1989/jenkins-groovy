//发布job初始化
def Folder = 'Prod-Insurance'

def COMPILE_JOB_NAME = Folder+'/'+'Prod-Insurance-Lp-Manage-Compile' //编译job名称，不可以使用中文
def DEPLOY_JOB_NAME1 = Folder+'/'+'Prod-Insurance-Lp-Manage-Deploy1' //发布job名称，不可以使用中文
def DEPLOY_JOB_NAME2 = Folder+'/'+'Prod-Insurance-Lp-Manage-Deploy2' //发布job名称，不可以使用中文
def PIPELINE_JOB_NAME = Folder+'/'+'Prod-Insurance-Lp-Manage-Pipeline'

def GITURL = 'http://h.quyiyuan.com/scm/pt/insurance_lp_server.git' //git仓库地址
def Git_AUTH = 'GIT_2060' //git仓库认证账号
def BRANCH = ''  //发布分支

def ENV = 'prod'  //配置文件环境
def GROUP = 'qybx'  //所属组
def PRODUCTION = 'insurance-lp-manage'   //产品名称，该参数表示oss存储路径和部署服务器路径中微服务名
def WAR_NAME = 'insurance_lp_manage-0.0.1.jar'  //WAR包名称
def OSS_BUCKET = 'deployment-package'  //oss存储bucket

def APP_OPTS = "--eureka.backup.zone= "   //此参数用于强制注入微服务名称，一般会固定在代码中，若不修改该参数为空
def EXPORT = "export redis_pass=dasDHkkJGU12U08R export dbusername=lp_prod export dbpassword=D6X9NncCcEpDieTy"

def ECS_IP1 = ' '
def ECS_IP2 = ' '

def Xms = '256M'
def Xmx = '512M'
def Threads = '2'
def JAVA_OPTS = "-Xms${Xms} -Xmx${Xmx} -XX:PermSize=${Xms} -XX:MaxNewSize=${Xms} -XX:MaxPermSize=${Xmx} -XX:ParallelGCThreads=${Threads}"//java启动参数，若有需要请自行添加
def SHELL_NAME = 'Prod-Insurance-Service'    //选择jumpserver发布脚本，按环境选择

//以下参数无特殊情况请勿修改
def DATE = '`date "+%Y-%m-%d"`'    //该参数为固定参数
def TIME = '`date "+%H:%M:%S "`'   //该参数为固定参数
def EUREKA_OPTS = '--eureka.instance.metadataMap.OldVersion=$OLD_VERSION --eureka.instance.metadataMap.LastVersion=$TAG --eureka.instance.metadataMap.WarName=$WAR_NAME' + " --eureka.instance.metadataMap.Date=$DATE --eureka.instance.metadataMap.Time=$TIME --eureka.instance.metadataMap.JavaOptXms=$Xms --eureka.instance.metadataMap.JavaOptXmx=$Xmx"

mavenJob("${COMPILE_JOB_NAME}") {
    logRotator(3, 3)       //参数为job保留时间，队列最大job构建数
    parameters {
      stringParam ('TAG',"")
      stringParam ('BRANCH',"")
      stringParam ('WAR_NAME',"$WAR_NAME")
      textParam ('Description',"")

    }
    preBuildSteps {
        shell("export JAVA_HOME=/usr/java/jdk1.8.0_45/jre; cd insurance_lp_common/; mvn clean install -Dmaven.test.skip=true")
    }

    label('insurance')   //构建job节点选择
    goals('clean install -Dmaven.test.skip=true')
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
          filesPath('insurance_lp_manage/target/*.jar')
          objectPrefix("release/$GROUP/$PRODUCTION/"+'$TAG')
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
        textParam ('JPS_PARAMETER',"{'PRODUCTION':'$PRODUCTION','GROUP':'$GROUP','APP_OPTS':'$APP_OPTS','ENV':'$ENV','JAVA_OPTS':'$JAVA_OPTS','EUREKA_OPTS':'$EUREKA_OPTS','EXPORT':'$EXPORT',"+'"TAG":"$TAG","WAR_NAME":"$WAR_NAME"}')
        stringParam ('SHELL_NAME',"$SHELL_NAME")

    }
    steps {
      jumpserverHttpsBuilder {
      //调用jumpserver插件发布
            jpsURL('生产环境jps')
            appName("$PRODUCTION")   //记录jumpserver上发布的微服务名称
        	ecsSearchOption("$ECS_IP1")


        }
    }
}
freeStyleJob("$DEPLOY_JOB_NAME2") {
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
        textParam ('JPS_PARAMETER',"{'PRODUCTION':'$PRODUCTION','GROUP':'$GROUP','APP_OPTS':'$APP_OPTS','ENV':'$ENV','JAVA_OPTS':'$JAVA_OPTS','EUREKA_OPTS':'$EUREKA_OPTS','EXPORT':'$EXPORT',"+'"TAG":"$TAG","WAR_NAME":"$WAR_NAME"}')
        stringParam ('SHELL_NAME',"$SHELL_NAME")

    }
    steps {
      jumpserverHttpsBuilder {
      //调用jumpserver插件发布
            jpsURL('生产环境jps')
            appName("$PRODUCTION")   //记录jumpserver上发布的微服务名称
        	ecsSearchOption("$ECS_IP2")


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
            url("https://github.com/Aaron1989/jenkins-groovy.git")
            branch("*/master")
            credentials("$Git_AUTH")
          }
        }
      }
      scriptPath("jenkins-pipelines/prodd/Insurance/Prod-insurance-lp-manage-Pipeline.groovy")
    }
  }
}