#!/usr/bin/env groovy
//发布job初始化

def env = 'Prod'
def group = 'Web'
def productName = 'Website'
def deployEcsIPs = [' ',' ',' ',' ',' ','  ']
def gitUrl = "http:// <git>/scm/qyw/qy-web-ant.git"
def gitBranch = "*/release"
def gitAuth = "dingqishi"

def OSSBucket = ""
def artifacts = ""
def objectPre = "distrelease/website/web/\$TAG"
def warName = "gateway-0.0.1-SNAPSHOT.jar"
def shellName = "Prod-MicroService"
def jpsParameter = "{'PRODUCTION':'web','GROUP':'website','APP_OPTS':' ','JAVA_OPTS':'-Xms2048M -Xmx2048M','EUREKA_OPTS':'--eureka.instance.metadataMap.OldVersion=\$OLD_VERSION --eureka.instance.metadataMap.LastVersion=\$TAG --eureka.instance.metadataMap.WarName=\$WAR_NAME --eureka.instance.metadataMap.Date=`date \"+%Y-%m-%d\"` --eureka.instance.metadataMap.Time=`date \"+%H:%M:%S \"` --eureka.instance.metadataMap.JavaOptXms=2048M --eureka.instance.metadataMap.JavaOptXmx=2048M','EXPORT':'export eurekaserverlistzone=http:// :8761/eureka/,http:// :8761/eureka/ export platform=desktop','ENV':'distrelease',\"TAG\":\"\$TAG\",\"WAR_NAME\":\"\$WAR_NAME\"}"
def applicationName = env+"-Website-gateway"

def slbNames = ['all7yiyuancom', 'allm7yiyuancom', 'allmquyiyuancom', 'allquyiyuancom']
//def slbJobNames = ['Prod-Web-Website-SLB-1', 'Prod-Web-Website-SLB-2', 'Prod-Web-Website-SLB-3', 'Prod-Web-Website-SLB-4','Prod-Web-Msite-SLB-1','Prod-Web-Msite-SLB-2', 'Prod-Web-Msite-SLB-3', 'Prod-Web-Msite-SLB-4']
//def slbJobNames = []
//for (item in slbNames){
//    def slbJobName = Folder + '/Prod-' + item + '-SLB'
//    slbJobNames.add(slbJobName)
//}

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

for (item in slbNames){
    def slbJobName = Folder + '/Prod-' + item + '-SLB'
    freeStyleJob(slbJobName) {
        logRotator(3, 3)
        parameters {
            stringParam ('gray_Weight',"")
            stringParam ('docker_Weight',"")
            stringParam ('prod_Weight',"")
        }
        label('docker-python')
        scm {
            git {
                remote {
                    url("https://github.com/Aaron1989/jenkins-groovy.git")
                    branch("*/master")
                    credentials(gitAuth)
                }
            }
        }

        steps {
            shell(
                "export PYTHONIOENCODING=utf-8\npython prod/Web/slb-scripts/${item}.py \${gray_Weight} \${docker_Weight} \${prod_Weight}"
            )
        }
    }
}


//freeStyleJob(Folder+'/'+slbJobNames[0]) {
//    logRotator(3, 3)
//    parameters {
//        stringParam ('Weight',"")
//        textParam ('Description',"")
//    }
//    scm {
//        git {
//            remote {
//                url("https://github.com/Aaron1989/jenkins-groovy.git")
//                branch("*/master")
//                credentials(gitAuth)
//            }
//        }
//    }
//    steps {
//        shell(
//                '''
//                export PYTHONIOENCODING=utf-8
//                sed -i "115,116s/100/$Weight/g" prod/Web/slb-scripts/Website_Gray_quyiyuan_new.py
//                sed -i "117,125s/100/$Docker_Weight/g" prod/Web/slb-scripts/Website_Gray_quyiyuan_new.py
//                cat prod/Web/slb-scripts/Website_Gray_quyiyuan_new.py
//                python prod/Web/slb-scripts/Website_Gray_quyiyuan_new.py
//                '''
//        )
//    }
//}
//
//freeStyleJob(Folder+'/'+slbJobNames[1]) {
//    logRotator(3, 3)
//    parameters {
//        stringParam ('Weight',"")
//        textParam ('Description',"")
//    }
//    scm {
//        git {
//            remote {
//                url("https://github.com/Aaron1989/jenkins-groovy.git")
//                branch("*/master")
//                credentials(gitAuth)
//            }
//        }
//    }
//    steps {
//        shell(
//                cacheShell
//        )
//    }
//}
//
//freeStyleJob(Folder+'/'+slbJobNames[2]) {
//    logRotator(3, 3)
//    parameters {
//        stringParam ('Weight',"")
//        textParam ('Description',"")
//    }
//    scm {
//        git {
//            remote {
//                url("https://github.com/Aaron1989/jenkins-groovy.git")
//                branch("*/master")
//                credentials(gitAuth)
//            }
//        }
//    }
//    steps {
//        shell(
//                cacheShell
//        )
//    }
//}
//
//freeStyleJob(Folder+'/'+slbJobNames[3]) {
//    logRotator(3, 3)
//    parameters {
//        stringParam ('Weight',"")
//        textParam ('Description',"")
//    }
//    scm {
//        git {
//            remote {
//                url("https://github.com/Aaron1989/jenkins-groovy.git")
//                branch("*/master")
//                credentials(gitAuth)
//            }
//        }
//    }
//    steps {
//        shell(
//                cacheShell
//        )
//    }
//}
//
//freeStyleJob(Folder+'/'+slbJobNames[4]) {
//    logRotator(3, 3)
//    parameters {
//        stringParam ('Weight',"")
//        textParam ('Description',"")
//    }
//    scm {
//        git {
//            remote {
//                url("https://github.com/Aaron1989/jenkins-groovy.git")
//                branch("*/master")
//                credentials(gitAuth)
//            }
//        }
//    }
//    steps {
//        shell(
//                cacheShell
//        )
//    }
//}
//
//freeStyleJob(Folder+'/'+slbJobNames[5]) {
//    logRotator(3, 3)
//    parameters {
//        stringParam ('Weight',"")
//        textParam ('Description',"")
//    }
//    scm {
//        git {
//            remote {
//                url("https://github.com/Aaron1989/jenkins-groovy.git")
//                branch("*/master")
//                credentials(gitAuth)
//            }
//        }
//    }
//    steps {
//        shell(
//                cacheShell
//        )
//    }
//}
//
//freeStyleJob(Folder+'/'+slbJobNames[6]) {
//    logRotator(3, 3)
//    parameters {
//        stringParam ('Weight',"")
//        textParam ('Description',"")
//    }
//    scm {
//        git {
//            remote {
//                url("https://github.com/Aaron1989/jenkins-groovy.git")
//                branch("*/master")
//                credentials(gitAuth)
//            }
//        }
//    }
//    steps {
//        shell(
//                cacheShell
//        )
//    }
//}
//
//freeStyleJob(Folder+'/'+slbJobNames[7]) {
//    logRotator(3, 3)
//    parameters {
//        stringParam ('Weight',"")
//        textParam ('Description',"")
//    }
//    scm {
//        git {
//            remote {
//                url("https://github.com/Aaron1989/jenkins-groovy.git")
//                branch("*/master")
//                credentials(gitAuth)
//            }
//        }
//    }
//    steps {
//        shell(
//                cacheShell
//        )
//    }
//}
