import groovy.json.JsonOutput

def f(input){
    output = []
    if (input == null)
        return output
    return input.split(',').toList()
}

def slbs = f(params.SLBS)
def vGroups = f(params.VGROUPS)
def sGroups = f(params.SGROUPS)
def rdss = f(params.RDSS)

def lGroup = params.LGROUP
def lProject = params.LPROJECT

def ips = JsonOutput.toJson(params.IPS.split(',').toList())
def instances = JsonOutput.toJson(params.INSTANCES.split(',').toList())

def jpsApi = 'http:// :8002'
def logEndpoint = 'cn-qingdao.sls.aliyuncs.com'
def slbAk = 'InnovationSlb'
def rdsAk = 'InnovationRds'
def logAk = 'InnovationLog'
def cloudMonitorAk = 'InnovationCM'
def pass = true


node() {
    try {
        stage('Check SLB') {
            for (def item: slbs) {
                if (item == '') {
                    break
                }
                result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' +
                        instances +  ',"balancer_id":"' + item + '","ak_name":"' + slbAk + '"}', url:
                        jpsApi + '/api/plugin/?plugin=check_slb_backend_server'
                print('Check SLB:' + result.getContent())
                if (! result.getContent().contains('true') || result.getContent().contains('false')) {
                    sh 'exit 1'
                }
            }
        }
    } catch(Exception ex) {
        pass = false
    }

    try {
        stage('Check virtual group') {
            for (def item: vGroups) {
                if (item == '') {
                    break
                }
                result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' +
                        instances + ',"group_id":"' + item + '","ak_name":"' + slbAk + '"}', url:
                        jpsApi + '/api/check_slb_virtual_server/'
                print('Check virtual group:' + result.getContent())
                if (! result.getContent().contains('true') || result.getContent().contains('false')) {
                    sh 'exit 1'
                }
            }
        }
    } catch(Exception ex) {
        pass = false
    }

    try {
        stage('Check security group') {
            for (def item: sGroups) {
                if (item == '') {
                    break
                }
                result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' +
                        instances + ',"group_id":"' + item + '","ak_name":"' + slbAk + '"}', url:
                        jpsApi + '/api/check_security_group/'
                print('Check SLB:' + result.getContent())
                if (! result.getContent().contains('true') || result.getContent().contains('false')) {
                    sh 'exit 1'
                }
            }
        }
    } catch(Exception ex) {
        pass = false
    }

    try {
        stage('Check rds white list') {
            for (def item: rdss) {
                if (item == '') {
                    break
                }
                result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"ips": ''' + ips +
                        ',"rds_id":"' + item + '","ak_name":"' + rdsAk + '"}', url: jpsApi + '/api/check_rds_white_list/'
                print('Check rds white list:' + result.getContent())
                if (! result.getContent().contains('true') || result.getContent().contains('false')) {
                    sh 'exit 1'
                }
            }
        }
    } catch(Exception ex) {
        pass = false
    }

    try {
        stage('Check log group') {
            if (lProject != null && lProject != '') {
                result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"ips": ''' + ips +
                        ',"group":"' + lGroup + '","endpoint":"' + logEndpoint + '","project":"' + lProject +
                        '","ak_name":"' + logAk + '"}', url: jpsApi + '/api/check_log_group/'
                println('Check Log Group:' + result.getContent())
                if (! result.getContent().contains('true') || result.getContent().contains('false')) {
                    sh 'exit 1'
                }
            }
        }
    } catch(Exception ex) {
        pass = false
    }

    try {
        stage('Check cloud monitor') {
            result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '''{"instances": ''' +
                    instances + ',"ak_name":"' + cloudMonitorAk + '"}', url: jpsApi +
                    '/api/plugin/?plugin=get_cloud_monitor'
            println('Check Cloud Monitor:' + result.getContent())
            if (! result.getContent().contains('true') || result.getContent().contains('false')) {
                sh 'exit 1'
            }
        }
    } catch(Exception ex) {
        print 'monitor not installed'
    }

    stage('Pass') {
        if (! pass) {
            sh 'exit 1'
        }
    }
}