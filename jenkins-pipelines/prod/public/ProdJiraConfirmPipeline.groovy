def version = params.VERSION
def oldVersion = params.OLD_VERSION
def jiraId = params.JIRA_ID
def appName = params.APP_NAME
def jpsApi = 'http:// :8002'
def jiraRefused = params.JIRA_REFUSED
def refusedReason = params.REFUSED_REASON
def succeed = params.SUCCEED
def failReason = params.FAIL_REASON
def rollback = params.ROLLBACK
def rollbackReason = params.ROLLBACK_REASON

node() {
    stage('Jira Confirm'){
        wrap([$class: 'BuildUser']) {
            releaser = env.BUILD_USER

        }
        result = httpRequest acceptType: 'APPLICATION_JSON_UTF8', contentType: 'APPLICATION_JSON_UTF8',
                consoleLogResponseBody: true, httpMode: 'POST', requestBody: '{"applicationName": "' + appName + '", ' +
                '"rollback": "' + rollback + '", "rollbackReason": "' + rollbackReason + '", "succeed": "'+ succeed +
                '", "JIRARefused": "' + jiraRefused + '", "releaser": "' + releaser + '", "JIRAId": "' + jiraId + '", ' +
                '"failReason": "' + failReason + '", "version": "' + version + '", "rollbackVersion": "' + oldVersion +
                '", "JIRARefusedReason": "' + refusedReason + '"}', url: jpsApi + '/api/add_release_record/'
        if (! result.getContent().contains('true') || result.getContent().contains('false')) {
            sh 'exit 1'
        }

    }
}
