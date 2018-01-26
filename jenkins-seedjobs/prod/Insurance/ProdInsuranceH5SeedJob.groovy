//发布job初始化
def Folder = 'Prod-Insurance'

def COMPILE_JOB_NAME = Folder+'/'+'Prod-Insurance-H5-Compile' //编译job名称，不可以使用中文
def DEPLOY_JOB_NAME = Folder+'/'+'Prod-Insurance-H5-Deploy' //发布job名称，不可以使用中文
def PIPELINE_JOB_NAME = Folder+'/'+'Prod-Insurance-H5-Pipeline'

def GITURL = 'http:// <git>/scm/in/insurance-h5.git' //git仓库地址
def Git_AUTH = '6b5de867-3f96-4162-9384-4c81e0e7063c' //git仓库认证账号
def BRANCH = ''  //发布分支

def ENV = 'release'  //配置文件环境
def GROUP = 'qybx'  //所属组
def PRODUCTION = 'insuranceH5'   //产品名称，该参数表示oss存储路径和部署服务器路径中微服务名
def WAR_NAME = 'www.tar.gz'  //WAR包名称
def OSS_BUCKET = 'deployment-package'  //oss存储bucket

def ECS_IP = ' '

def SHELL_NAME = 'Prod-InsuranceH5'    //选择jumpserver发布脚本，按环境选择

freeStyleJob("${COMPILE_JOB_NAME}") {
    logRotator(3, 3)       //参数为job保留时间，队列最大job构建数
    parameters {
      stringParam ('TAG',"")
      stringParam ('BRANCH',"")
      stringParam ('WAR_NAME',"$WAR_NAME")
      textParam ('Description',"")

    }
    label('insurance')   //构建job节点选择
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
        shell("cd ./insuranceH5; tar -cvzf www.tar.gz www")
    }

    publishers {
        aliyunOSSPublisher {
          bucketName("$OSS_BUCKET")
          filesPath("$PRODUCTION/www.tar.gz")
          objectPrefix("release/$GROUP/$PRODUCTION/"+'$TAG')
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
        textParam ('JPS_PARAMETER',"{'PRODUCTION':'$PRODUCTION','GROUP':'$GROUP','ENV':'$ENV',"+'"TAG":"$TAG","WAR_NAME":"$WAR_NAME"}')
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
        stringParam ('JiraID', "")
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
      scriptPath("jenkins-pipelines/prodd/Insurance/Prod-insurance-H5-Pipeline.groovy")
    }
  }
}