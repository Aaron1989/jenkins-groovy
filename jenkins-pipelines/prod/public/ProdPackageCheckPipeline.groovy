def version = params.VERSION
def oldVersion = params.OLD_VERSION
def ossBucket = params.OSS_BUCKET
def warName = params.WAR_NAME
def prefix = params.PREFIX
def JPS_API = 'http:// :8002'
def OSS_AK = 'InnovationOss'




node() {
    stage('Package check') {
        if (oldVersion == '') {
                    break
                }
        result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '{"bucket_name": "' +
                ossBucket + '", "path": "' + prefix + '", "old_version": "' + oldVersion + '", "new_version": "' +
                version + '", "war_name": "' + warName + '", "ak_name": "' + OSS_AK + '"}', url: JPS_API +
                '/api/plugin/?plugin=oss_check'
        if (! result.getContent().contains('true')) {
            sh 'exit 1'
        }
        if (result.getContent().contains('false')) {
            sh 'exit 1'
        }
    }
}