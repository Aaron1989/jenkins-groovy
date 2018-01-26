def version = params.VERSION
def oldVersion = params.OLD_VERSION
def jiraId = params.JIRAID
def appName = params.APP_NAME
def jpsApi = 'http:// :8002'
def jiraRefused = params.JIRA_REFUSED
def refusedReason = params.REFUSED_REASION
def succeed = params.SUCCEED
def failReason = params.FAIL_REASION
def rollback = params.ROOLBACK
def rollbackReason = params.ROOLBACK_REASION

node() {
   stage('jira-confirm'){
        wrap([$class: 'BuildUser']) {
             releaser = env.BUILD_USER

             }
        jiraRefused = jiraRefused.toString().replace('false','否').replace('true','是')
        succeed = succeed.toString().replace('false','否').replace('true','是')
        rollback = rollback.toString().replace('false','否').replace('true','是')
        if (refusedReason == '') {
            refusedReason = '/'
        }
        if (failReason == '') {
            failReason = '/'
        }
        if (rollbackReason == '') {
            rollbackReason = '/'
        }
        print(jpsApi + '/api/add_release_record/')
        test = '{"applicationName": "' + appName + '", "rollback": "' + rollback + '", "rollbackReason": "' + rollbackReason + '", "succeed": "'+ succeed + '", "JIRARefused": "' + jiraRefused + '", "releaser": "' + releaser + '", "JIRAId": "' + jiraId + '", "failReason": "' + failReason + '", "version": "' + version + '", "rollbackVersion": "' + oldVersion + '", "JIRARefusedReason": "' + refusedReason + '"}'
        print test
        result = httpRequest acceptType: 'APPLICATION_JSON_UTF8', contentType: 'APPLICATION_JSON_UTF8', consoleLogResponseBody: true, httpMode: 'POST', requestBody: '{"applicationName": "' + appName + '", "rollback": "' + rollback + '", "rollbackReason": "' + rollbackReason + '", "succeed": "'+ succeed + '", "JIRARefused": "' + jiraRefused + '", "releaser": "' + releaser + '", "JIRAId": "' + jiraId + '", "failReason": "' + failReason + '", "version": "' + version + '", "rollbackVersion": "' + oldVersion + '", "JIRARefusedReason": "' + refusedReason + '"}', url: jpsApi + '/api/add_release_record/'
        if (! result.getContent().contains('true') || result.getContent().contains('false')) {
              sh 'exit 1'
        }

   }
 }
