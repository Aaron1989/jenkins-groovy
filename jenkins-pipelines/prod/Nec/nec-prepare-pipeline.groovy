def credentialsId = 'stash_huis'
def baseUrl = 'http://zhangshaohui_cp@s.kyee.com.cn/scm/nec/'

//stash url
def adminUrl = baseUrl + 'nec-admin-web.git'
def examUrl = baseUrl + 'nec-exam-web.git'
def mobileUrl = baseUrl + 'nec-mobile.git'
def mgtWebUrl = baseUrl + 'nec-mgt-web.git'
def msgUrl = baseUrl + 'nec-msg.git'
def serverUrl = baseUrl + 'nec-server.git'
def mtsUrl= baseUrl + 'nec-mts.git'
def mgtUrl= baseUrl + 'nec-mgt.git'
def authUrl = baseUrl + 'nec-auth.git'
def mgtMobileUrl=baseUrl + 'nec-mgt-mobile.git'

//base job
def admin_rls_job = "nec-admin-rls-web"
def exam_rls_job = "nec-exam-rls-web"
def mobile_rls_job = "nec-mobile-rls-web"
def mgt_web_rls_job = "nec-mgt-web-rls"
def msg_rls_job = "nec-msg-rls"
def server_rls_job = "nec-server-rls"
def mts_rls_job = "nec-mts-rls"
def mgt_rls_job = "nec-mgt-rls"
def auth_rls_job = "nec-auth-rls"
def mgt_mobile_rls_job = "nec-mgt-mobile-rls"


//deploy job
def prd_confirm_job = "nec-deploy-confirm-prepare"

//variables
def userInput;
def version;
def src;
node('master') {
  stage('start') {
    userInput = input(
      id: 'userInput', message: '请输入构建参数?', parameters: [
      [$class: 'StringParameterDefinition', defaultValue: '', name: 'version', description: 'release版本'],
    [$class: 'StringParameterDefinition', defaultValue: '', name: 'src', description: '源码版本，比如：dev或0.1.0']
  ])
    echo "start release ${userInput['version']} from ${userInput['src']}"
  }
}

node('slave-nodejs') {
  stage('prepare') {
    version = userInput['version']
    src = userInput['src']
    parallel('prepare-admin': {
      dir('../' + admin_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: adminUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """if [ \$(git branch -r |grep -c origin/release/${version}) = 1 -o \$(git branch -r |grep -c origin/release) = 0 ]
          then
          git checkout -B release/${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-admin-web.git release/${version} -f
            else
          echo "another release branch exists!"
          exit 1
          fi"""
        }
      }
    }, 'prepare-exam': {
      dir('../' + exam_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: examUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """if [ \$(git branch -r |grep -c origin/release/${version}) = 1 -o \$(git branch -r |grep -c origin/release) = 0 ]
          then
          git checkout -B release/${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-exam-web.git release/${version} -f
            else
          echo "another release branch exists!"
          exit 1
          fi"""
        }
      }
    }, 'prepare-mgt-web': {
        dir('../' + mgt_web_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtWebUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """if [ \$(git branch -r |grep -c origin/release/${version}) = 1 -o \$(git branch -r |grep -c origin/release) = 0 ]
          then
          git checkout -B release/${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-web.git release/${version} -f
            else
          echo "another release branch exists!"
          exit 1
          fi"""
        }
      }
    },'prepare-mobile':{
      dir('../' + mobile_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mobileUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """if [ \$(git branch -r |grep -c origin/release/${version}) = 1 -o \$(git branch -r |grep -c origin/release) = 0 ]
          then
          git checkout -B release/${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mobile.git release/${version} -f
            else
          echo "another release branch exists!"
          exit 1
          fi"""
        }
      }
    }, 'prepare-msg':{
      dir('../' + msg_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: msgUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """if [ \$(git branch -r |grep -c origin/release/${version}) = 1 -o \$(git branch -r |grep -c origin/release) = 0 ]
          then
          git checkout -B release/${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-msg.git release/${version} -f
            else
          echo "another release branch exists!"
          exit 1
          fi"""
        }
      }
    }, 'prepare-server':{
      dir('../' + server_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: serverUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """if [ \$(git branch -r |grep -c origin/release/${version}) = 1 -o \$(git branch -r |grep -c origin/release) = 0 ]
          then
          git checkout -B release/${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-server.git release/${version} -f
            else
          echo "another release branch exists!"
          exit 1
          fi"""
        }
      }
    },'prepare-auth':{
      dir('../' + auth_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: authUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """if [ \$(git branch -r |grep -c origin/release/${version}) = 1 -o \$(git branch -r |grep -c origin/release) = 0 ]
          then
          git checkout -B release/${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-auth.git release/${version} -f
            else
          echo "another release branch exists!"
          exit 1
          fi"""
        }
      }
    },'prepare-mts':{
      dir('../' + mts_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mtsUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """if [ \$(git branch -r |grep -c origin/release/${version}) = 1 -o \$(git branch -r |grep -c origin/release) = 0 ]
          then
          git checkout -B release/${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mts.git release/${version} -f
            else
          echo "another release branch exists!"
          exit 1
          fi"""
        }
      }
    },'prepare-mgt':{
      dir('../' + mgt_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """if [ \$(git branch -r |grep -c origin/release/${version}) = 1 -o \$(git branch -r |grep -c origin/release) = 0 ]
          then
          git checkout -B release/${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt.git release/${version} -f
            else
          echo "another release branch exists!"
          exit 1
          fi"""
        }
      }
    },'prepare-mgt-mobile':{
      dir('../' + mgt_mobile_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtMobileUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """if [ \$(git branch -r |grep -c origin/release/${version}) = 1 -o \$(git branch -r |grep -c origin/release) = 0 ]
          then
          git checkout -B release/${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-mobile.git release/${version} -f
            else
          echo "another release branch exists!"
          exit 1
          fi"""
        }
      }
    })
  }
}
node('master') {
  stage('deploy-confirm') {
    build(job: prd_confirm_job, parameters: [[$class: 'StringParameterValue', name: 'version', value:
    version], [$class: 'StringParameterValue', name: 'src', value:
    src]])
  }
}
