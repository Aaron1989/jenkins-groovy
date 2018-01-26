def appName = params.APP_NAME
def ip = params.IP

node(){
    stage("Deploy"){
        print appName
        print ip
        jpsdeploy jpsURL: '生产环境jps', appName: appName, assets: ["$ip"]
    }
}