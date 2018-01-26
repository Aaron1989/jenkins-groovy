//发布job初始化
def Folder = 'Prod-Hms'

def COMPILE_JOB_NAME = Folder+'/'+'Prod-Hms-Component-Web-Compile1' //编译job名称，不可以使用中文
def PIPELINE_JOB_NAME1 =  Folder+'/'+'Prod-Hms-Component-Web-Pipeline'
def GIT_URL = '$GIT_URL'
def BRANCH = '*/$BRANCH'
def Git_AUTH = 'GIT_2060'
def WAR_NAME = '$WAR_NAME'
def PATH = '$PREFIX/$VERSION'

mavenJob("${COMPILE_JOB_NAME}") {
    logRotator(3, 5)       //参数为job保留时间，队列最大job构建数
    parameters {
        stringParam ('TAG',"")
        stringParam ('BRANCH',"")
        stringParam ('WAR_NAME',"")
		stringParam ('PREFIX',"")
		stringParam ('GIT_URL',"")
		stringParam ('VERSION',"")
    }

    label('insurance')   //构建job节点选择
    goals('clean install -Dmaven.test.skip=true')
    rootPOM('pom.xml')
    mavenInstallation('maven-3.2.5')
    preBuildSteps {
        shell(
                "#npm config set registry https://registry.npm.taobao.org\n" +
                        "#npm install\n" +
                        "#npm install ng-zorro-antd@0.5.3\n" +
                        "npm run release\n" +
//                      "cd dist \n" +
                        "tar -cvf /tmp/component-web.tar *"

        )
    }
    scm {
        git {
            remote {
                url("$GIT_URL")
                branch("$BRANCH")
                credentials("$Git_AUTH")
            }
        }
    }
    publishers {
        aliyunOSSPublisher {
            bucketName("deployment-package-new")
            filesPath('/tmp/$WAR_NAME')
            objectPrefix("$PATH")
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
        textParam('DESCRIPTION',"")
        stringParam ('JIRA_ID',"")
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url("https://github.com/Aaron1989/jenkins-groovy.git")
                        branch("*/master")
                        credentials("6b5de867-3f96-4162-9384-4c81e0e7063c")
                    }
                }
            }
            scriptPath("jenkins-pipelines/prodd/NewHms/Prod-Hms-Component-Web-Pipeline.groovy")
        }
    }
}