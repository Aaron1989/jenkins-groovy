def Folder = 'Prod-MiddleLayer-New'
def DEPLOY_JOB_NAME = Folder+'/'+'deploy'


pipelineJob("$DEPLOY_JOB_NAME") {
    logRotator(14, 40)
    parameters {
        password {
            name('JPS_PWD')
            defaultValue('')
            description('')
        }
        stringParam('app_name', "")
        stringParam('ip', "")
    }
    definition {
        cps {
            script(
                    '''
node('deploy'){
    stage("Deploy"){
        print app_name
        print ip
        jpsdeploy jpsURL: '生产环境jps', appName: "$app_name", assets: ["$ip"]
                    }
                }
'''
            )
            sandbox()
        }
    }
}
