package jenkins.deploy

/**
 * Created by Andy on 2017/06/19.
 */

def COMPILE_JOB_NAME = 'Prod-MiddleLayer-MsSearch-Compile'
def DEPLOY_JOB_NAMES = ['Gray-MiddleLayer-MsSearch-Deploy1','Prod-MiddleLayer-MsSearch-Deploy1','Prod-MiddleLayer-MsSearch-Deploy2','Prod-MiddleLayer-MsSearch-Deploy3']
def BRANCH = '*/release'
def WAR_NAME = 'search-0.0.1-SNAPSHOT.war'
//def SHELL_NAME = 'Prod-MicroService'

properties ([
        parameters ([
                string (
                        defaultValue: '',
                        description: '',
                        name: 'TAG'),
                string (
                        defaultValue: '',
                        description: '',
                        name: 'OLD_VERSION'),
                password  (
                        defaultValue: '',
                        description: 'JPS_PWD',
                        name: 'JPS_PWD')
        ])
])

node() {
    stage('compile') {
        build job: "$COMPILE_JOB_NAME", parameters: [string(name: 'TAG', value:env.TAG), string(name: 'BRANCH', value: BRANCH), string(name: 'WAR_NAME', value: WAR_NAME)]
    }
    for (def item : DEPLOY_JOB_NAMES){
        stage('Prod-MiddleLayer/'+item){
            input 'chekout and continue'
            build job: item,parameters :[string(name: 'TAG', value:env.TAG), string(name: 'OLD_VERSION', value:env.OLD_VERSION), password(name: 'JPS_PWD', value:env.JPS_PWD)]
        }
    }
}
