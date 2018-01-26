def COMPILE_JOB_NAME = 'Prod-Plugins-SyncHospital-Compile'
def DEPLOY_JOB_NAME = 'Prod-Plugins-SyncHospital-Deploy'

def TAG = ''
def OLD_VERSION = ''
def BRANCH = '*/release'
def WAR_NAME = 'Sync-Hospital-0.0.1.war'
properties ([
    parameters ([
        string (name:'TAG', defaultValue: "$TAG"),
        string (name:'OLD_VERSION', defaultValue: "$OLD_VERSION"),
        password (name:'JPS_PWD', defaultValue: ''),
        string (name:'BRANCH', defaultValue: "$BRANCH"),
        string (name:'WAR_NAME', defaultValue: "$WAR_NAME"),
        text (name:'Description', defaultValue: "$Description"),
    ])
])

node {
    def JobResult
    def pipelineStatus
    stage('Compile') {
        build job: "$COMPILE_JOB_NAME",parameters :[string(name: 'TAG', value:env.TAG),string(name: 'BRANCH', value:env.BRANCH), string(name: 'WAR_NAME', value:env.WAR_NAME),text(name: 'Description', value:env.Description)]
    }

    stage('Deploy') {
        input 'ok?'
        JobResult = build job : "$DEPLOY_JOB_NAME",parameters :[string(name: 'TAG', value:env.TAG), string(name: "OLD_VERSION", value:env.OLD_VERSION), string(name: 'WAR_NAME', value:env.WAR_NAME), password(name: 'JPS_PWD', value:env.JPS_PWD)]
        pipelineStatus = JobResult.result
        echo 'deployment result is: + pipelineStatus'
        }


}
