def compile_job_name = 'Prod-plugins-New/compile job'
def git_auth = "dingqishi"

//发布job初始化
mavenJob(compile_job_name) {
    logRotator(3, 3)       //参数为job保留时间，队列最大job构建数
    parameters {
        stringParam ('TAG',)
        stringParam ('BRANCH',)
        stringParam ('WAR_NAME',)
        stringParam ('GROUP',)
        stringParam ('PRODUCTION',)
        textParam ('GIT_URL',)
        textParam ('Description',)
    }

    label('compile')   //构建job节点选择
    goals('clean install -Dmaven.test.skip=true')
    rootPOM('pom.xml')
    mavenInstallation('maven-3.2.5')

    scm {
        git {
            remote {
                url('$GIT_URL')
                branch('$BRANCH')
                credentials(git_auth)
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

    //    publishers {
    //        gitPublisher{
    //          tagsToPush {
    //            tagToPush {
    //              targetRepoName('origin')
    //              tagName('$TAG')
    //              tagMessage('$Description')
    //              forcePush(false)
    //              createTag(true)webapp/
    //              updateTag(true)
    //           }
    //          }
    //          pushOnlyIfSuccess(true)
    //          pushMerge(true)
    //        		}
    //            }
    publishers {
        aliyunOSSPublisher {
            bucketName('deployment-package')
            filesPath('webapp/target/$WAR_NAME')
            objectPrefix('release/$GROUP/$PRODUCTION/$TAG')
        }
    }
}