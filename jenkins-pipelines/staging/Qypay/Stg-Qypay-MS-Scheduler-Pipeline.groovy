package jenkins.deploy


def COMPILE_JOB_NAME = 'Stg-Qypay-MS-Scheduler-Compile'
def DEPLOY_JOB_NAME = 'Stg-Qypay-MS-Scheduler-Deploy'
def BRANCH = '*/staging'
def WAR_NAME = 'ms-scheduler.war'
def SHELL_NAME = 'Prod-MicroService'


properties ([
        parameters ([
                string (
                        defaultValue: '',
                        description: '',
                        name: 'TAG'),
                password  (
                        defaultValue: '',
                        description: 'JPS_PWD',
                        name: 'JPS_PWD')
        ])
])

node {
    stage('compile') {
        build job: "$COMPILE_JOB_NAME", parameters: [string(name: 'TAG', value:env.TAG), string(name: 'BRANCH', value: BRANCH), string(name: 'WAR_NAME', value: WAR_NAME)]
    }
    stage('deploy'){
        build job: "$DEPLOY_JOB_NAME",parameters :[string(name: 'TAG', value:env.TAG), password(name: 'JPS_PWD', value:env.JPS_PWD),string(name: 'WAR_NAME', value: WAR_NAME)]
    }
}