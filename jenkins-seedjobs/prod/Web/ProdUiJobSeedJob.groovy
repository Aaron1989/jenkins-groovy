#!/usr/bin/env groovy
//发布job初始化

def env = 'Prod'
def group = 'Web'
def productName = 'Website'
def deployEcsIPs = [' ',' ']
def gitUrl = "http:// <git>/scm/qyw/qy-web-ant.git"
def gitBranch = "*/release"
def gitAuth = "dingqishi"

def OSSBucket = ""
def artifacts = ""
def objectPre = "distrelease/website/web/\$TAG"
def warName = "ui-job-0.0.1-SNAPSHOT.jar"
def shellName = "Prod-MicroService"
def GrayJpsParameter = '''{'PRODUCTION':'web','GROUP':'website','APP_OPTS':' ','JAVA_OPTS':'-Xms2048M -Xmx2048M','EUREKA_OPTS':'--eureka.instance.metadataMap.OldVersion=$OLD_VERSION --eureka.instance.metadataMap.LastVersion=$TAG --eureka.instance.metadataMap.WarName=$WAR_NAME --eureka.instance.metadataMap.Date=`date "+%Y-%m-%d"` --eureka.instance.metadataMap.Time=`date "+%H:%M:%S "` --eureka.instance.metadataMap.JavaOptXms=2048M --eureka.instance.metadataMap.JavaOptXmx=2048M','EXPORT':'export eurekaserverlistzone=http:// :8761/eureka/,http:// :8761/eureka/ export platform=desktop','ENV':'distrelease',"TAG":"$TAG","WAR_NAME":"$WAR_NAME"}'''
def ProdJpsParameter = '''{'PRODUCTION':'web','GROUP':'website','APP_OPTS':' ','JAVA_OPTS':'-Xms2048M -Xmx2048M','EUREKA_OPTS':'--eureka.instance.metadataMap.OldVersion=$OLD_VERSION --eureka.instance.metadataMap.LastVersion=$TAG --eureka.instance.metadataMap.WarName=$WAR_NAME --eureka.instance.metadataMap.Date=`date "+%Y-%m-%d"` --eureka.instance.metadataMap.Time=`date "+%H:%M:%S "` --eureka.instance.metadataMap.JavaOptXms=2048M --eureka.instance.metadataMap.JavaOptXmx=2048M','EXPORT':'export eurekaserverlistzone=http:// :8762/eureka/,http:// :8762/eureka/ export platform=desktop','ENV':'distrelease',"TAG":"$TAG","WAR_NAME":"$WAR_NAME"}'''
def applicationName = env+"-Website-ui-job"

def Folder = "Prod-Web"
def compileJobName = Folder+ '/' +' Prod-Web-Website-Compile'
def deployJobNameTemple = env+'-'+group+'-'+productName+'Deploy'

def deployJobInfo = [:]
def deployJobNames =[]
int i = 0
for (item in deployEcsIPs) {
    def deployJobName = Folder + '/' + deployJobNameTemple + '-' + i
    deployJobInfo[deployJobName] = deployEcsIPs[i]
    deployJobNames.add(deployJobName)
    i++
}

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



freeStyleJob(Folder+ '/' +'Gray-Ui-Job-Deploy') {
    logRotator(3, 3)
    label('deploy')
    parameters {
        password {
            name('JPS_PWD')
            defaultValue('')
            description('')
        }
        stringParam ('TAG',"")
        stringParam ('OLD_VERSION',"")
        stringParam ('WAR_NAME', warName)
//            textParam ('JPS_PARAMETER',"{'PRODUCTION':'$PRODUCTION','GROUP':'$GROUP','APP_OPTS':'$APP_OPTS','JAVA_OPTS':'$JAVA_OPTS','EUREKA_OPTS':'$EUREKA_OPTS','EXPORT':'$EXPORT','ENV':'$ENV',"+'"TAG":"$TAG","WAR_NAME":"$WAR_NAME"}')
        textParam ('JPS_PARAMETER', GrayJpsParameter)
        stringParam ('SHELL_NAME', shellName)

    }
    steps {
        jumpserverHttpsBuilder {
            //调用jumpserver插件发布
            jpsURL('生产环境jps')
            appName("Gray"+"-Website-ui-job")   //记录jumpserver上发布的微服务名称
            ecsSearchOption(deployEcsIPs[0])

        }
    }
}

freeStyleJob(Folder+ '/' +'Prod-Ui-Job-Deploy') {
    logRotator(3, 3)
    label('deploy')
    parameters {
        password {
            name('JPS_PWD')
            defaultValue('')
            description('')
        }
        stringParam ('TAG',"")
        stringParam ('OLD_VERSION',"")
        stringParam ('WAR_NAME', warName)
//            textParam ('JPS_PARAMETER',"{'PRODUCTION':'$PRODUCTION','GROUP':'$GROUP','APP_OPTS':'$APP_OPTS','JAVA_OPTS':'$JAVA_OPTS','EUREKA_OPTS':'$EUREKA_OPTS','EXPORT':'$EXPORT','ENV':'$ENV',"+'"TAG":"$TAG","WAR_NAME":"$WAR_NAME"}')
        textParam ('JPS_PARAMETER', ProdJpsParameter)
        stringParam ('SHELL_NAME', shellName)

    }
    steps {
        jumpserverHttpsBuilder {
            //调用jumpserver插件发布
            jpsURL('生产环境jps')
            appName(applicationName)   //记录jumpserver上发布的微服务名称
            ecsSearchOption(deployEcsIPs[1])

        }
    }
}


