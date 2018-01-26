
package jenkins.deploy

/**
 * Created by huis on 2017/1/12.
 */
def credentialsId = 'stash_caoyi'
def baseUrl = 'http://s.kyee.com.cn/scm/nec/'

//stash url
def adminUrl = baseUrl + 'nec-admin-web.git'
def examUrl = baseUrl + 'nec-exam-web.git'
def mobileUrl = baseUrl + 'nec-mobile.git'
def mgtWebUrl = baseUrl + 'nec-mgt-web.git'
def msgUrl = baseUrl + 'nec-msg.git'
def mgtUrl = baseUrl + 'nec-mgt.git'
def mtsUrl = baseUrl + 'nec-mts.git'
def authUrl = baseUrl + 'nec-auth.git'
def serverUrl = baseUrl + 'nec-server.git'
def mgtMobileUrl=baseUrl + 'nec-mgt-mobile.git'

//base job
def admin_hotfix_job = "nec-admin-web-hfx"
def exam_hotfix_job = "nec-exam-web-hfx"
def mobile_hotfix_job = "nec-mobile-hfx"
def mgt_web_hotfix_job = "nec-mgt-web-hfx"
def msg_hotfix_job = "nec-msg-hfx"
def mgt_hotfix_job = "nec-mgt-hfx"
def server_hotfix_job = "nec-server-hfx"
def mts_hotfix_job = "nec-mts-hfx"
def auth_hotfix_job = "nec-auth-hfx"
def mgt_mobile_hotfix_job = "nec-mgt-mobile-hfx"

//deploy job
def prd_confirm_job = "nec-hotfix-deploy-confirm-prepare"

//variables
def userInput;
def version;
def src;
node('master') {
    stage('start') {
        userInput = input(
        id: 'userInput', message: '请输入构建参数?', parameters: [
        [$class: 'StringParameterDefinition', defaultValue: '', name: 'version', description: 'hotfix版本'],
        [$class: 'StringParameterDefinition', defaultValue: '', name: 'src', description: '源码版本tag号，比如：1.33.0']
        ])
        echo "start hotfix ${userInput['version']} from ${userInput['src']}"
    }
    stage('prepare') {
        version = userInput['version']
        src = userInput['src']
        parallel('prepare-admin': {
        dir('../' + admin_hotfix_job) {
            checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: adminUrl]]])
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
            sh """if [ \$(git branch -r |grep -c origin/hotfix/${version}) = 1 -o \$(git branch -r |grep -c origin/hotfix) = 0 ]
            then
            git checkout -B hotfix/${version}
            echo ${stash_user}
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-admin-web.git hotfix/${version} -f
            else
            echo "another hotfix branch exists!"
            exit 1
            fi"""
                }
            }
        }, 'prepare-exam': {
        dir('../' + exam_hotfix_job) {
            checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: examUrl]]])
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
            sh """if [ \$(git branch -r |grep -c origin/hotfix/${version}) = 1 -o \$(git branch -r |grep -c origin/hotfix) = 0 ]
            then
            git checkout -B hotfix/${version}
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-exam-web.git hotfix/${version} -f
            else
            echo "another hotfix branch exists!"
            exit 1
            fi"""
                }
            }
        }, 'prepare-mgt-web':{
        dir('../' + mgt_web_hotfix_job) {
            checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtWebUrl]]])
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
            sh """if [ \$(git branch -r |grep -c origin/hotfix/${version}) = 1 -o \$(git branch -r |grep -c origin/hotfix) = 0 ]
            then
            git checkout -B hotfix/${version}
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-web.git hotfix/${version} -f
            else
            echo "another hotfix branch exists!"
            exit 1
            fi"""
                }
            }
        }, 'prepare-mobile':{
        dir('../' + mobile_hotfix_job) {
            checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mobileUrl]]])
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
            sh """if [ \$(git branch -r |grep -c origin/hotfix/${version}) = 1 -o \$(git branch -r |grep -c origin/hotfix) = 0 ]
            then
            git checkout -B hotfix/${version}
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mobile.git hotfix/${version} -f
            else
            echo "another hotfix branch exists!"
            exit 1
            fi"""
                }
            }
        }, 'prepare-msg':{
        dir('../' + msg_hotfix_job) {
            checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: msgUrl]]])
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
            sh """if [ \$(git branch -r |grep -c origin/hotfix/${version}) = 1 -o \$(git branch -r |grep -c origin/hotfix) = 0 ]
            then
            git checkout -B hotfix/${version}
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-msg.git hotfix/${version} -f
            else
            echo "another hotfix branch exists!"
            exit 1
            fi"""
                }
            }
        }, 'prepare-server':{
        dir('../' + server_hotfix_job) {
            checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: serverUrl]]])
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
            sh """if [ \$(git branch -r |grep -c origin/hotfix/${version}) = 1 -o \$(git branch -r |grep -c origin/hotfix) = 0 ]
            then
            git checkout -B hotfix/${version}
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-server.git hotfix/${version} -f
            else
            echo "another hotfix branch exists!"
            exit 1
            fi"""
                }
            }
        },'prepare-auth':{
        dir('../' + auth_hotfix_job) {
            checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: authUrl]]])
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
            sh """if [ \$(git branch -r |grep -c origin/hotfix/${version}) = 1 -o \$(git branch -r |grep -c origin/hotfix) = 0 ]
            then
            git checkout -B hotfix/${version}
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-auth.git hotfix/${version} -f
            else
            echo "another hotfix branch exists!"
            exit 1
            fi"""
                }
            }
        },'prepare-mts':{
        dir('../' + mts_hotfix_job) {
            checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mtsUrl]]])
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
            sh """if [ \$(git branch -r |grep -c origin/hotfix/${version}) = 1 -o \$(git branch -r |grep -c origin/hotfix) = 0 ]
            then
            git checkout -B hotfix/${version}
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mts.git hotfix/${version} -f
            else
            echo "another hotfix branch exists!"
            exit 1
            fi"""
                }
            }
        }, 'prepare-mgt':{
        dir('../' + mgt_hotfix_job) {
            checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtUrl]]])
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
            sh """if [ \$(git branch -r |grep -c origin/hotfix/${version}) = 1 -o \$(git branch -r |grep -c origin/hotfix) = 0 ]
            then
            git checkout -B hotfix/${version}
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt.git hotfix/${version} -f
            else
            echo "another hotfix branch exists!"
            exit 1
            fi"""
            }
        }
        }, 'prepare-mgt-mobile':{
        dir('../' + mgt_mobile_hotfix_job) {
            checkout([$class: 'GitSCM', branches: [[name: "${src}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtMobileUrl]]])
            withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
            sh """if [ \$(git branch -r |grep -c origin/hotfix/${version}) = 1 -o \$(git branch -r |grep -c origin/hotfix) = 0 ]
            then
            git checkout -B hotfix/${version}
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-mobile.git hotfix/${version} -f
            else
            echo "another hotfix branch exists!"
            exit 1
            fi"""
            }
        }
        })
     }
    stage('deploy-confirm') {
        build(job: prd_confirm_job, parameters: [[$class: 'StringParameterValue', name: 'version', value:
        version], [$class: 'StringParameterValue', name: 'src', value:
        src]])
        }
}
