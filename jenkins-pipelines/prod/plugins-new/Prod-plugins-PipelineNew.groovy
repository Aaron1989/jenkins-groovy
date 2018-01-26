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
def NAMESPACE = 'DevOps.deploy-public,prod,gray,gray_cloud,prod_cloud'

def GROUP = 'devops'
def deploy_notification_job = 'Prod-Deploy-Notification'
def deploy_record_job = 'Prod-Deploy-Record'
def deploy_email_job = 'Prod-Email-Notification'

properties([
        parameters([
                [$class: 'DynamicParameter',
                 appValue: "$APP_ID",
                 dynamicValue: "$ENVIRONMENT",
                 spaceValue: "$NAMESPACE"],
                password(defaultValue: '', description: '', name: 'JPS_PWD'),
                text (name:'Description', defaultValue: ""),
                string (name:'JiraID', defaultValue: ""),
                string (name:'APP_NAME', defaultValue: ""),
        ]),
        pipelineTriggers([])
])


node('base') {
    stage('get-parameters') {
        prod_param = env["$APP_ID+$ENVIRONMENT+prod"]
        gray_param = env["$APP_ID+$ENVIRONMENT+gray"]
        com_param = env["$APP_ID+$ENVIRONMENT"+"+DevOps.deploy-public"]
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
   stage('deploy notification'){
        def userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip = 'skip' + userInput
        if (skip == 'skipfalse') {
        description = Description.replace("\r","#").replace("\n","#")
        build job: deploy_notification_job, parameters: [string(name: 'TAG', value: TAG), string(name: 'JiraID', value: JiraID), text(name: 'Description', value: description), string(name: 'group', value: GROUP), string(name: 'app_name', value: APP_NAME), string(name: 'stage', value: 'start')]

        }
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
                        if (! result.getContent().contains('true')) {
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
                        if (! result.getContent().contains('true')) {
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
                        if (! result.getContent().contains('true')) {
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
                        if (! result.getContent().contains('true')) {
                            sh 'exit 1'
                        }
                    }
                },
                'check log group': {
                    //确认服务器是否已加入阿里云日志服务的机器组里
                    result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"ips": ''' + iplist.toString() + ',"group":"' + G_LOG_GROUP + '","endpoint":"' + G_LOG_ENDIPOINT + '","project":"' + G_LOG_PROJECT + '","ak_name":"' + LOG_AK + '"}', url: JPS_API + '/api/check_log_group/'
                    println('Check Log Group:' + result.getContent())
                    if (! result.getContent().contains('true')) {
                        sh 'exit 1'
                    }
                },
                'check cloud monitor': {
                    //检查云监控是否安装
                    result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' + instancelist.toString() + ',"ak_name":"' + CLOUD_MONITOR_AK + '"}', url: JPS_API + '/api/plugin/?plugin=get_cloud_monitor'
                    println('Check Cloud Monitor:' + result.getContent())
                    if (! result.getContent().contains('true')) {
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
          if (! result.getContent().contains('True')) {
              sh 'exit 1'
          }


        }
    }
}

node ('base'){
    stage('compile') {

        if (skip == 'skipfalse') {
            input 'chekout and continue'
            build job: 'compile job', parameters: [string(name: 'TAG', value: TAG), string(name: 'BRANCH', value: BRANCH), string(name: 'WAR_NAME', value: WAR_NAME), string(name: 'GROUP', value: GROUP), string(name: 'PRODUCTION', value: PRODUCTION), text(name: 'GIT_URL', value: GIT_URL), text(name: 'Description', value: Description)]
        }
    }
}

node('deploy'){
    for (def item : GRAY_INFO){
        stage("prod deploy " + item['ip']){
            def userInput = input(
                    id: 'userInput', message: 'go on?', parameters: [
                    [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
            ])
            def skip = 'skip' + userInput
            echo(skip)
            if (skip == 'skipfalse') {
                ip = item['ip']
                println(ip)
                println(GRAY_SERVER)
                build job: "Deploy", parameters: [string(name: 'app_name', value: GRAY_SERVER), string(name: "ip", value: ip), password(name: 'JPS_PWD', value: JPS_PWD)]
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
                        if (! result.getContent().contains('true')) {
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
                        if (! result.getContent().contains('true')) {
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
                        if (! result.getContent().contains('true')) {
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
                        if (! result.getContent().contains('true')) {
                            sh 'exit 1'
                        }
                    }
                },
                'check log group': {
                    //确认服务器是否已加入阿里云日志服务的机器组里
                    result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"ips": ''' + iplist.toString() + ',"group":"' + P_LOG_GROUP + '","endpoint":"' + P_LOG_ENDIPOINT + '","project":"' + P_LOG_PROJECT + '","ak_name":"' + LOG_AK + '"}', url: JPS_API + '/api/check_log_group/'
                    println('Check Log Group:' + result.getContent())
                    if (! result.getContent().contains('true')) {
                        sh 'exit 1'
                    }
                },
                'check cloud monitor': {
                    //检查云监控是否安装
                    //检查云监控是否安装
                    result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' + instancelist.toString() + ',"ak_name":"' + CLOUD_MONITOR_AK + '"}', url: JPS_API + '/api/plugin/?plugin=get_cloud_monitor'
                    println('Check Cloud Monitor:' + result.getContent())
                    if (! result.getContent().contains('true')) {
                        sh 'exit 1'
                    }

                }

        )

    }
}

node('deploy'){
    for (def item : PROD_INFO){
        stage("prod deploy " + item['ip']){
            input 'chekout and continue'
            ip = item['ip']
            println(ip)
            println(PROD_SERVER)
            build job : "Deploy",parameters :[string(name: 'app_name', value:PROD_SERVER), string(name: "ip", value:ip), password(name: 'JPS_PWD', value:JPS_PWD)]
        }
    }
}

node('base'){
    stage('complete notification') {
        def userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip = 'skip' + userInput
        if (skip == 'skipfalse') {
        description = Description.replace("\r","#").replace("\n","#")
        build job: deploy_notification_job, parameters: [string(name: 'TAG', value: TAG), string(name: 'JiraID', value: JiraID), text(name: 'Description', value: description), string(name: 'group', value: GROUP), string(name: 'app_name', value: APP_NAME), string(name: 'stage', value: 'end')]
        }
    }

    stage('email notification') {
        def userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'skip?', name: 'skip'],
        ])
        skip = 'skip' + userInput
        if (skip == 'skipfalse') {
        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'dingqishi', url: 'http://h.quyiyuan.com/scm/devops/devops-shell.git']]])
        RECIPIENT = sh returnStdout: true, script: "python deploy_email_notify.py $GROUP"
        print RECIPIENT
        T = sh returnStdout: true, script: 'date +"%Y-%m-%d %H:%M"'
        TIME = T.replace('\n','')
        build job: deploy_email_job, parameters: [string(name: 'TAG', value: TAG), string(name: 'TIME', value: TIME), string(name: 'JiraID', value: JiraID), text(name: 'CONTENT', value: Description), string(name: 'APP_NAME', value: APP_NAME), string(name: 'RECIPIENT', value: RECIPIENT)]
        }
    }

   stage('jira-confirm'){
          def userInput = input(
            id: 'userInput', message: '发布记录', parameters: [
              [$class: 'BooleanParameterDefinition', defaultValue: false, description: '默认为否', name: 'jira是否打回'],
              text(defaultValue: '', description: 'JIRA打回原因', name: 'jira_reason'),
              [$class: 'BooleanParameterDefinition', defaultValue: true, description: '默认为是', name: '发布是否成功'],
              text(defaultValue: '', description: '发布失败原因', name: 'deploy_reason'),
              [$class: 'BooleanParameterDefinition', defaultValue: false, description: '默认为否', name: '是否回滚'],
              text(defaultValue: '', description: '回滚原因', name: 'rollback_reason'),
            ])
          wrap([$class: 'BuildUser']) {
             releaser = env.BUILD_USER

             }

        JIRARefused = userInput['jira是否打回'].toString().replace('false','否').replace('true','是')
        JIRARefusedReason = userInput['jira_reason']
        if (JIRARefusedReason == '') {
            JIRARefusedReason = '/'
        }
        succeed = userInput['发布是否成功'].toString().replace('false','否').replace('true','是')
        failReason = userInput['deploy_reason']
        if (failReason == '') {
            failReason = '/'
        }
        rollback = userInput['是否回滚'].toString().replace('false','否').replace('true','是')
        rollbackReason = userInput['rollback_reason']
        if (rollbackReason == '') {
            rollbackReason = '/'
        }

        deploy_record = '{"applicationName": "' + APP_NAME + '", "rollback": "' + rollback + '", "rollbackReason": "' + rollbackReason + '", "succeed": "'+ succeed + '", "JIRARefused": "' + JIRARefused + '", "releaser": "' + releaser + '", "JIRAId": "' + JiraID + '", "failReason": "' + failReason + '", "version": "' + TAG + '", "rollbackVersion": "' + OLD_VERSION + '", "JIRARefusedReason": "' + JIRARefusedReason + '"}'
        build job: deploy_record_job, parameters: [text(name: 'deploy_record', value: deploy_record)]
   }
}