def jsonParse(def json) {
    new groovy.json.JsonSlurperClassic().parseText(json)
}

def JPS_API = 'http:// :8002'
def SLB_AK = 'InnovationSlb'
def RDS_AK = 'InnovationRds'
def LOG_AK = 'InnovationLog'
def CLOUD_MONITOR_AK = 'InnovationCM'
def OSS_AK = 'InnovationOss'

def APP_ID = 'plugins-ms-doctor'
def ENVIRONMENT = 'PRO'
def NAMESPACE = 'Ops.deploy-public,prod,gray,gray_cloud,prod_cloud'

properties([
        parameters([
                [$class: 'DynamicParameter',
                 appValue: "$APP_ID",
                 dynamicValue: "$ENVIRONMENT",
                 spaceValue: "$NAMESPACE"],
                password(defaultValue: '', description: '', name: 'JPS_PWD'),
                text (name:'Description', defaultValue: "")
        ]),
        pipelineTriggers([])
])

node('base') {
    stage('get-parameters') {
        prod_param = env["$APP_ID+$ENVIRONMENT+prod"]
        gray_param = env["$APP_ID+$ENVIRONMENT+gray"]
        com_param = env["$APP_ID+$ENVIRONMENT"+"+Ops.deploy-public"]
        gray_cloud_param = env["$APP_ID+$ENVIRONMENT+gray_cloud"]
        prod_cloud_param = env["$APP_ID+$ENVIRONMENT+prod_cloud"]

        print prod_param
        print gray_param
        print com_param
        print gray_cloud_param
        print prod_cloud_param

        com_config = jsonParse(com_param)['configurations']
        gray_config = jsonParse(gray_param)['configurations']
        prod_config = jsonParse(prod_param)['configurations']
        gray_cloud_config = jsonParse(gray_cloud_param)['configurations']
        prod_cloud_config = jsonParse(prod_cloud_param)['configurations']


        PROD_SERVER = prod_config['service'].toString()
        PRODUCTION = PROD_SERVER
        GRAY_SERVER = gray_config['service'].toString()

        prod_info = sh returnStdout: true, script: 'curl "' + JPS_API + '/api/jplay/?app={' + PROD_SERVER + '}"'
        print prod_info

        gray_info = sh returnStdout: true, script: 'curl "' + JPS_API + '/api/jplay/?app={' + GRAY_SERVER + '}"'
        print gray_info

        G_BALANCER_ID = gray_cloud_config['balancer_id'].split(',').toList()
        G_VIRTUAL_GROUP = gray_cloud_config['virtual_group'].split(',').toList()
        G_SECURITY_GROUP = gray_cloud_config['security_group_id'].split(',').toList()
        G_RDS_ID = gray_cloud_config['rds_id'].split(',').toList()
        G_LOG_GROUP = gray_cloud_config['log_group']
        G_LOG_ENDIPOINT = gray_cloud_config['log_endpoint']
        G_LOG_PROJECT = gray_cloud_config['log_project']

        P_BALANCER_ID = prod_cloud_config['balancer_id'].split(',').toList()
        P_VIRTUAL_GROUP = prod_cloud_config['virtual_group'].split(',').toList()
        P_SECURITY_GROUP = prod_cloud_config['security_group_id'].split(',').toList()
        P_RDS_ID = prod_cloud_config['rds_id'].split(',').toList()
        P_LOG_GROUP = prod_cloud_config['log_group']
        P_LOG_ENDIPOINT = prod_cloud_config['log_endpoint']
        P_LOG_PROJECT = prod_cloud_config['log_project']

        OSS_BUCKET = com_config['bucket_name']
        TAG = prod_config['tag']
        OLD_VERSION = prod_config['old_version']
        DESCRIPTION = prod_config['description']
        ENV = prod_config['env']
        GROUP = prod_config['group']
        WAR_NAME = prod_config['war_name']
        GIT_URL = prod_config['git_url']
        GIT_AUTH = prod_config['git_auth']
        BRANCH = prod_config['branch']



        PROD_INFO = jsonParse(prod_info)
        GRAY_INFO = jsonParse(gray_info)

        PREFIX = 'PROD/' + GROUP + '/' + PRODUCTION
        COMPILE_JOB_NAME = 'Prod-' + GROUP + '-New/Prod-' + GROUP + '-New-' + PRODUCTION + '-Compile'

    }
}

node('docker-python') {
    stage('Gray-Cloud-Check') {
        slb = []
        instancelist = []
        iplist = []
        slbv = []
        sgroup = []
        rdslist = []

        for (def item : GRAY_INFO) {
            a = '"' + item['instance_id'] + '"'
            instancelist.add(a)
        }
        for (def item : GRAY_INFO) {
            a = '"' + item['ip'] + '"'
            iplist.add(a)
        }

        parallel(
                'check slb': {
                    //确认被部署服务器是否存在于指定SLB后端
                    for (def item : G_BALANCER_ID) {
                        slb.add(item)
                    }
                    for (def item : slb) {
                        result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' + instancelist.toString() + ',"balancer_id":"' + item + '","ak_name":"' + SLB_AK + '"}', url: JPS_API + '/api/plugin/?plugin=check_slb_backend_server'
                        println('Check SLB:' + result.getContent())
                        if (result.getContent().contains('false')) {
                            sh 'exit 1'
                        }
                    }
                },
                'check virtual group': {
                    //确认被部署服务器是否存在于指定虚拟组里
                    for (def item : G_VIRTUAL_GROUP) {
                        slbv.add(item)
                    }
                    for (def item : slbv) {
                        result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' + instancelist.toString() + ',"group_id":"' + item + '","ak_name":"' + SLB_AK + '"}', url: JPS_API + '/api/check_slb_virtual_server/'
                        println('Check Virtual Group:' + result.getContent())
                        if (result.getContent().contains('false')) {
                            sh 'exit 1'
                        }
                    }
                },
                'check security group': {
                    //确认被部署服务器是否已经加入指定安全组里
                    for (def item : G_SECURITY_GROUP) {
                        sgroup.add(item)
                    }
                    for (def item : sgroup) {
                        result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' + instancelist.toString() + ',"group_id":"' + item + '","ak_name":"' + SLB_AK + '"}', url: JPS_API + '/api/check_security_group/'
                        println('Check Security Group:' + result.getContent())
                        if (result.getContent().contains('false')) {
                            sh 'exit 1'
                        }
                    }
                },
                'check rds white list': {
                    //确认被部署服务器是否已加入rds白名单
                    for (def item : G_RDS_ID) {
                        rdslist.add(item)
                    }
                    for (def item : rdslist) {
                        result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"ips": ''' + iplist.toString() + ',"rds_id":"' + item + '","ak_name":"' + RDS_AK + '"}', url: JPS_API + '/api/check_rds_white_list/'
                        println('Check RDS White List:' + result.getContent())
                        if (result.getContent().contains('false')) {
                            sh 'exit 1'
                        }
                    }
                },
                'check log group': {
                    //确认服务器是否已加入阿里云日志服务的机器组里
                    result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"ips": ''' + iplist.toString() + ',"group":"' + G_LOG_GROUP + '","endpoint":"' + G_LOG_ENDIPOINT + '","project":"' + G_LOG_PROJECT + '","ak_name":"' + LOG_AK + '"}', url: JPS_API + '/api/check_log_group/'
                    println('Check Log Group:' + result.getContent())
                    if (result.getContent().contains('false')) {
                        sh 'exit 1'
                    }
                },
                'check cloud monitor': {
                    //检查云监控是否安装
                    result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' + instancelist.toString() + ',"ak_name":"' + CLOUD_MONITOR_AK + '"}', url: JPS_API + '/api/plugin/?plugin=get_cloud_monitor'
                    println('Check Cloud Monitor:' + result.getContent())
                    if (result.getContent().contains('False')) {
                        sh 'exit 1'
                    }
                }
        )


    }
}
node('base') {
    stage('PackageCheck') {
        def userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip = 'skip' + userInput
        echo(skip)
        if (skip == 'skipfalse') {

//        input 'chekout and continue'

            //检查oss上回滚的war包是否存在
            print '{"bucket_name": "' + OSS_BUCKET + '", "path": "' + PREFIX + '", "old_version": "' + OLD_VERSION + '", "new_version": "' + TAG + '", "war_name": "' + WAR_NAME + '", "ak_name": "' + OSS_AK + '"}'
            result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '{"bucket_name": "' + OSS_BUCKET + '", "path": "' + PREFIX + '", "old_version": "' + OLD_VERSION + '", "new_version": "' + TAG + '", "war_name": "' + WAR_NAME + '", "ak_name": "' + OSS_AK + '"}', url: JPS_API + '/api/plugin/?plugin=oss_check'
            println('check oss:' + result.getContent())
//            if (result.getContent().contains('False')) {
//                sh 'exit 1'
//            }


        }
    }
}
node ('base'){
    stage('compile') {

        if (skip == 'skipfalse') {
//            input 'chekout and continue'
            git credentialsId: 'dingqishi', url: 'http://h.quyiyuan.com/scm/devops/jenkins-pipelines.git'

            jobDsl removedJobAction: 'DELETE', targets: 'prod/plugins-new/mavenjobseedjob.groovy'


//            input 'chekout and continue'
            build job: 'compile job', parameters: [string(name: 'TAG', value: TAG), string(name: 'BRANCH', value: BRANCH), string(name: 'WAR_NAME', value: WAR_NAME), string(name: 'GROUP', value: GROUP), string(name: 'PRODUCTION', value: PRODUCTION), text(name: 'GIT_URL', value: GIT_URL), text(name: 'Description', value: Description)]
        }
    }
}

node('deploy'){
    for (def item : GRAY_INFO){
        stage("gray deploy " + item['ip']){
            def userInput = input(
                    id: 'userInput', message: 'go on?', parameters: [
                    [$class: 'BooleanParameterDefinition', defaultValue: false,description: 'skip?', name: 'skip'],
            ])
            def skip = 'skip' + userInput
            echo (skip)
            if (skip == 'skipfalse'){
                print item
                print GRAY_SERVER
                print ([item['ip']])
                jpsdeploy jpsURL: '生产环境jps', appName: GRAY_SERVER, assets:[item['ip']]
            }
        }
    }
}

node('docker-python') {
    stage('Prod-Cloud-Check') {
        slb = []
        instancelist = []
        iplist = []
        slbv = []
        sgroup = []
        rdslist = []

        for (def item : PROD_INFO) {
            a = '"' + item['instance_id'] + '"'
            instancelist.add(a)
        }
        for (def item : PROD_INFO) {
            a = '"' + item['ip'] + '"'
            iplist.add(a)
        }

        parallel(
                'check slb': {
                    //确认被部署服务器是否存在于指定SLB后端
                    for (def item : P_BALANCER_ID) {
                        slb.add(item)
                    }
                    for (def item : slb) {
                        result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' + instancelist.toString() + ',"balancer_id":"' + item + '","ak_name":"' + SLB_AK + '"}', url: JPS_API + '/api/plugin/?plugin=check_slb_backend_server'
                        println('Check SLB:' + result.getContent())
                        if (result.getContent().contains('false')) {
                            sh 'exit 1'
                        }
                    }
                },
                'check virtual group': {
                    //确认被部署服务器是否存在于指定虚拟组里
                    for (def item : P_VIRTUAL_GROUP) {
                        slbv.add(item)
                    }

                    for (def item : slbv) {
                        result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' + instancelist.toString() + ',"group_id":"' + item + '","ak_name":"' + SLB_AK + '"}', url: JPS_API + '/api/check_slb_virtual_server/'
                        println('Check Virtual Group:' + result.getContent())
                        if (result.getContent().contains('false')) {
                            sh 'exit 1'
                        }
                    }
                },
                'check security group': {
                    //确认被部署服务器是否已经加入指定安全组里
                    for (def item : P_SECURITY_GROUP) {
                        sgroup.add(item)
                    }

                    for (def item : sgroup) {
                        result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' + instancelist.toString() + ',"group_id":"' + item + '","ak_name":"' + SLB_AK + '"}', url: JPS_API + '/api/check_security_group/'
                        println('Check Security Group:' + result.getContent())
                        if (result.getContent().contains('false')) {
                            sh 'exit 1'
                        }
                    }
                },
                'check rds white list': {
                    //确认被部署服务器是否已加入rds白名单
                    for (def item : P_RDS_ID) {
                        rdslist.add(item)
                    }

                    for (def item : rdslist) {
                        result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"ips": ''' + iplist.toString() + ',"rds_id":"' + item + '","ak_name":"' + RDS_AK + '"}', url: JPS_API + '/api/check_rds_white_list/'
                        println('Check RDS White List:' + result.getContent())
                        if (result.getContent().contains('false')) {
                            sh 'exit 1'
                        }
                    }
                },
                'check log group': {
                    //确认服务器是否已加入阿里云日志服务的机器组里
                    result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"ips": ''' + iplist.toString() + ',"group":"' + P_LOG_GROUP + '","endpoint":"' + P_LOG_ENDIPOINT + '","project":"' + P_LOG_PROJECT + '","ak_name":"' + LOG_AK + '"}', url: JPS_API + '/api/check_log_group/'
                    println('Check Log Group:' + result.getContent())
                    if (result.getContent().contains('false')) {
                        sh 'exit 1'
                    }
                },
                'check cloud monitor': {
                    //检查云监控是否安装
                    //检查云监控是否安装
                    result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' + instancelist.toString() + ',"ak_name":"' + CLOUD_MONITOR_AK + '"}', url: JPS_API + '/api/plugin/?plugin=get_cloud_monitor'
                    println('Check Cloud Monitor:' + result.getContent())
                    if (result.getContent().contains('false')) {
                        sh 'exit 1'
                    }

                }

        )
    }
}


//node('deploy'){
//    for (def item : PROD_INFO){
//        stage("prod deploy " + item['ip']){
//            input 'chekout and continue'
//            print item
//            print PROD_SERVER
//            print [item['ip']]
//            jpsdeploy jpsURL: '生产环境jps', appName: PROD_SERVER, assets:[item['ip']]
//        }
//    }
//}

node('deploy'){
    for (def item : PROD_INFO){
        stage("prod deploy " + item['ip']){
            input 'chekout and continue'
            build job : "Deploay",parameters :[string(name: 'app_name', value: PROD_SERVER), string(name: "ip", value:item['ip'], password(name: 'JPS_PWD', value:env.JPS_PWD))]
        }
    }
}