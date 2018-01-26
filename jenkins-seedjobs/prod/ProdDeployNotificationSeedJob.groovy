def GITURL = 'http://h.quyiyuan.com/scm/devops/devops-shell.git' //git仓库地址
def Git_AUTH = 'dingqishi' //git仓库认证账号


def JOB_NAME = 'Prod-Deploy-Notification' //发布job名称，不可以使用中文

freeStyleJob("$JOB_NAME") {
    logRotator(7, 7)
    parameters {
        stringParam ('TAG',"")
        textParam ('Description', "")
        stringParam ('JiraID', "")
        stringParam ('group', "")
        stringParam ('app_name', "")
//        stringParam ('name', "") //默认是发布人员姓名拼音
        stringParam ('stage', "") //发布开始通知写start，发布完成写end
    }
  	label('docker-python')
    scm {
            git {
                remote {
                    url("$GITURL")
                    branch('*/master')
                    credentials("$Git_AUTH")
                }
            }
        }
    steps {
        shell('python deploy_notify.py $group $Description $JiraID $app_name $TAG $stage')
    }

}