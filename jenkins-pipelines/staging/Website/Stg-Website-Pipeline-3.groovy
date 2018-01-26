def DEPLOY_JOB_NAME1 = 'Stg-Website-Compile-3'
def DEPLOY_JOB_NAME2 = 'Stg-Website-Compose-Build-3'
def DEPLOY_JOB_NAME3 = 'Stg-Website-Image-Push-3'
def DEPLOY_JOB_NAME4 = 'Stg-Website-Compose-Up-3'
def DEPLOY_JOB_NAME5 = 'Stg-Website-Import-Compile-3'
def OSS_BUCKET = 'deployment-package'
def OSS_OBJECT = 'dev/website/web'
def PROFILE_ACTIVE = 'diststaging'
def APPLICATION_NAME = 'docker-stg3'
def domainName = 'www.quyiyuan.net'
def subName = 'quyiyuan.net'


properties ([
        parameters ([
                string (
                        defaultValue: '',
                        description: '官网编译git分支',
                        name: 'branch'),
                string (
                        defaultValue: '',
                        description: 'docker的版本号，也是包上传的地址，每次需要新的未曾使用的版本号，请严格按照月日时分格式，如05201709',
                        name: 'DOCKER_TAG_VERSION'),
                string (
                        defaultValue: '',
                        description: '测试地址为www.quyiyuan.net',
                        name: 'platform'),
                string (
                        defaultValue: 'false',
                        description: '发布时有import填true,没有import填false',
                        name: 'HAS_IMPORT'),
                string (
                        defaultValue: '',
                        description: 'import的git分支,不发import为空',
                        name: 'IMPORT_BRANCH'),
                string (
                        defaultValue: 'feature/new-stg',
                        description: '发布时有import在默认值之后加上-import',
                        name: 'BRANCH_DOCKER')
        ])
])

node {
    stage('Ms-import-Compile'){
        if (env.HAS_IMPORT == 'true'){
            build job: "$DEPLOY_JOB_NAME5",parameters :[string(name: 'DOCKER_TAG_VERSION', value:'wqyynet2017'+env.DOCKER_TAG_VERSION), string(name:'IMPORT_BRANCH', value:env.IMPORT_BRANCH)]
        }
    }

    stage('Compile') {
        build job: "$DEPLOY_JOB_NAME1",parameters :[string(name: 'branch', value:env.BRANCH), string(name: 'DOCKER_TAG_VERSION', value:'wqyynet2017'+env.DOCKER_TAG_VERSION)]
    }

    stage('Compose-Bulid') {
        build job: "$DEPLOY_JOB_NAME2",parameters :[string(name: 'DOCKER_TAG_VERSION', value:'wqyynet2017'+env.DOCKER_TAG_VERSION), string(name: 'OSS_OBJECT', value:"$OSS_OBJECT"), string(name: 'OSS_BUCKET', value:"$OSS_BUCKET"), string(name: 'PROFILE_ACTIVE', value:"$PROFILE_ACTIVE"), string(name: 'platform', value:env.platform), string(name:'BRANCH_DOCKER', value:env.BRANCH_DOCKER)]
    }

    sh 'sleep 20'

    stage('Push-Images') {
        build job: "$DEPLOY_JOB_NAME3",parameters :[string(name: 'HAS_IMPORT', value:env.HAS_IMPORT), string(name: 'DOCKER_TAG_VERSION', value:'wqyynet2017'+env.DOCKER_TAG_VERSION)]
    }

    sh 'sleep 20'

    stage('Compose-Up') {
        build job: "$DEPLOY_JOB_NAME4",parameters :[string(name: 'DOCKER_TAG_VERSION', value:'wqyynet2017'+env.DOCKER_TAG_VERSION), string(name: 'OSS_OBJECT', value:"$OSS_OBJECT"), string(name: 'OSS_BUCKET', value:"$OSS_BUCKET"), string(name: 'PROFILE_ACTIVE', value:"$PROFILE_ACTIVE"), string(name: 'platform', value:env.platform), string(name: 'APPLICATION_NAME', value:"$APPLICATION_NAME"), string(name: 'domainName', value:"$domainName"), string(name: 'subName', value:"$subName"), string(name:'BRANCH_DOCKER', value:env.BRANCH_DOCKER)]
    }
}
    
    