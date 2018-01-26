def version = params.VERSION
def oldVersion = params.OLD_VERSION
def ossBucket = params.OSS_BUCKET
def warName = params.WAR_NAME
def prefix = params.PREFIX
def jpsApi = 'http:// :8002'
def ossAk = 'InnovationOss'


node() {
    stage('Package check') {
        result = httpRequest consoleLogResponseBody: true, httpMode: 'POST', requestBody: '{"bucket_name": "' +
                ossBucket + '", "path": "' + prefix + '", "old_version": "' + oldVersion + '", "new_version": "' +
                version + '", "war_name": "' + warName + '", "ak_name": "' + ossAk + '"}', url: jpsApi +
                '/api/plugin/?plugin=oss_check'
        if (! result.getContent().contains('true') || result.getContent().contains('false')) {
            sh 'exit 1'
        }
    }
}