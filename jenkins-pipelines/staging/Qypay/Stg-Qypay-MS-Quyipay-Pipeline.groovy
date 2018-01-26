package jenkins.deploy

/**
 * Created by Andy on 2017/06/19.
 */

def COMPILE_JOB_NAME = 'Stg-Qypay-MS-Quyipay-Compile'
def DEPLOY_JOB_NAME = 'Stg-Qypay-MS-Quyipay-Deploy'
def BRANCH = '*/staging'
def WAR_NAME = 'ms-quyipay.war'
def SHELL_NAME = 'Prod-MicroService'


properties ([
        parameters ([
                string (
                        defaultValue: '',
                        description: '',
                        name: 'TAG'),
                /*string (
                        defaultValue: '',
                        description: '',
                        name: 'OLD_VERSION'),*/
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
        build job: "$DEPLOY_JOB_NAME",parameters :[string(name: 'TAG', value:env.TAG), string(name: 'WAR_NAME', value: WAR_NAME), password(name: 'JPS_PWD', value:env.JPS_PWD)]
    }
}