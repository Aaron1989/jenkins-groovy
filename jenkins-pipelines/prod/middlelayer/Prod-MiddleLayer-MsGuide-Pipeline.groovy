
def COMPILE_JOB_NAME = 'Prod-MiddleLayer-MsGuide-Compile'
def DEPLOY_JOB_NAMES = ['Prod-MiddleLayer-MsGuide-Deploy']
def BRANCH = '*/release'
def WAR_NAME = 'MS-Guide-1.0-SNAPSHOT.war'
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
