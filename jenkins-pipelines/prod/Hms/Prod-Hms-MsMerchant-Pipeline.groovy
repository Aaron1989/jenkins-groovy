def COMPILE_JOB_NAME = 'Prod-Hms-MSMerchant-Compile'
def DEPLOY_JOB_NAME1 = 'Prod-Hms-MSMerchant-Deploy1'
def DEPLOY_JOB_NAME2 = 'Prod-Hms-MSMerchant-Deploy2'

def TAG = ' '
def OLD_VERSION = ' '
def BRANCH = '*/release'
def WAR_NAME = ''

properties ([
        parameters ([
                string (
                        defaultValue: '',
                        description: '',
                        name: 'TAG'),
                string (
                        defaultValue: 'v1.0.0',
                        description: 'v1.0.0',
                        name: 'OLD_VERSION'),
                string (
                        defaultValue: '',
                        description: '',
                        name: 'BRANCH'),
                password  (
                        defaultValue: '',
                        description: 'JPS_PWD',
                        name: 'JPS_PWD')
        ])
])

node {
    stage('Compile') {
        build job: "$COMPILE_JOB_NAME", parameters: [string(name: 'TAG', value:env.TAG), string(name: 'BRANCH', value: BRANCH), string(name: 'WAR_NAME', value: WAR_NAME)]
    }

    stage ('input'){
        def userInput = input(
                id: 'userInput', message: 'go on?', parameters: [
                [$class: 'StringParameterDefinition', defaultValue: '',description: 'war_name', name: 'war_name'],
        ])
        WAR_NAME = userInput
    }
    stage('Deploy1') {
        //input 'Whether to go on?'
        build job: "$DEPLOY_JOB_NAME1",parameters :[string(name: 'WAR_NAME',value: WAR_NAME),string(name: 'TAG', value:env.TAG), string(name: 'OLD_VERSION', value:env.OLD_VERSION), password(name: 'JPS_PWD', value:env.JPS_PWD)]
    }
    stage('Deploy2') {
        input 'Whether to go on?'
        build job: "$DEPLOY_JOB_NAME2",parameters :[string(name: 'WAR_NAME',value: WAR_NAME),string(name: 'TAG', value:env.TAG), string(name: 'OLD_VERSION', value:env.OLD_VERSION), password(name: 'JPS_PWD', value:env.JPS_PWD)]
    }
}
