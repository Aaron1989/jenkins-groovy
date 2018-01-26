def COMPILE_JOB_NAME = 'Prod-Insurance-Manager-Schedule-Compile' //编译job名称，不可以使用中文
def DEPLOY_JOB_NAME = 'Prod-Insurance-Manager-Schedule-Deploy' //发布job名称，不可以使用中文

def TAG = ''
def OLD_VERSION = ''
def BRANCH = ''
def WAR_NAME = 'insurance-schedule.war'
properties ([
    parameters ([
        string (name:'TAG', defaultValue: "$TAG"),
        string (name:'OLD_VERSION', defaultValue: "$OLD_VERSION"),
        password (name:'JPS_PWD', defaultValue: ''),
        string (name:'BRANCH', defaultValue: ""),
        string (name:'WAR_NAME', defaultValue: "$WAR_NAME"),
        text (name:'Description', defaultValue: "$Description"),
    ])
])

node {
    def JobResult
    def pipelineStatus
    stage('Compile') {
        build job: "$COMPILE_JOB_NAME", parameters: [string(name: 'TAG', value: env.TAG), string(name: 'BRANCH', value: env.BRANCH), string(name: 'WAR_NAME', value: env.WAR_NAME), text(name: 'Description', value: env.Description)]
    }
}
node('deploy'){
        stage('Deploy') {
        input 'ok?'
        JobResult = build job : "$DEPLOY_JOB_NAME",parameters :[string(name: 'TAG', value:env.TAG), string(name: "OLD_VERSION", value:env.OLD_VERSION), string(name: 'WAR_NAME', value:env.WAR_NAME), password(name: 'JPS_PWD', value:env.JPS_PWD)]
        pipelineStatus = JobResult.result
        echo 'deployment result is: + pipelineStatus'
        }
}



