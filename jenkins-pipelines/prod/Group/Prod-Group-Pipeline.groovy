def COMPILE_JOB_NAME = 'Prod-Group-Compile'
def DEPLOY_JOB_NAME = 'Prod-Group-Deploy'

properties ([
    parameters ([
        string (
            defaultValue: '',
            description: '',
            name: 'TAG'),
        text (
            defaultValue: '',
            description: '',
            name: 'Description'
            )
    ])
])

node() {
    stage(COMPILE_JOB_NAME) {
        build job: 'Prod-Group/'+COMPILE_JOB_NAME,parameters :[string(name: 'TAG', value:env.TAG), text(name: 'Description', value:env.Description)]
    }


    stage(DEPLOY_JOB_NAME) {
        build job: 'Prod-Group/'+DEPLOY_JOB_NAME,parameters :[string(name: 'tag', value:env.TAG)]
    }


}