
def JOB_NAME = 'Prod-Deploy-Record1' //发布job名称，不可以使用中文

freeStyleJob("$JOB_NAME") {
    logRotator(7, 7)
    parameters {
        textParam ('deploy_record', "")
    }
  	type = 'APPLICATION_JSON_UTF8'
  	steps {
        httpRequest('http:// :8002/api/add_release_record/') {
            httpMode('POST')
            acceptType(type.toString())
            contentType(type.toString())
            ignoreSslErrors(true)
            requestBody(deploy_record)
            consoleLogResponseBody(true)
        }
    }

}