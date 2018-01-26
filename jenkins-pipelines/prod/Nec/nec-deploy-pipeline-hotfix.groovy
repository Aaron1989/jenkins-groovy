package jenkins.deploy

/**
 * Created by huis on 2017/1/12.
 */
def credentialsId = 'stash_huis'
def baseUrl = 'http://zhangshaohui_cp@s.kyee.com.cn/scm/nec/'
def imageBaseUrl = 'registry-vpc.cn-hangzhou.aliyuncs.com'

//stash url
def adminUrl = baseUrl + 'nec-admin-web.git'
def examUrl = baseUrl + 'nec-exam-web.git'
def mgtWebUrl = baseUrl + 'nec-mgt-web.git'
def mobileUrl = baseUrl + 'nec-mobile.git'
def msgUrl = baseUrl + 'nec-msg.git'
def serverUrl = baseUrl + 'nec-server.git'
def mtsUrl = baseUrl + 'nec-mts.git'
def mgtUrl = baseUrl + 'nec-mgt.git'
def authUrl = baseUrl + 'nec-auth.git'
def mgtMobileUrl=baseUrl + 'nec-mgt-mobile.git'

//base job
def admin_hfx_job = "nec-admin-web-hfx"
def exam_hfx_job = "nec-exam-web-hfx"
def mgt_web_hfx_job = "nec-mgt-web-hfx"
def mobile_hfx_job = "nec-mobile-hfx"
def msg_hfx_job = "nec-msg-hfx"
def server_hfx_job = "nec-server-hfx"
def auth_hfx_job = "nec-auth-hfx"
def mts_hfx_job = "nec-mts-hfx"
def mgt_hfx_job = "nec-mgt-hfx"
def mgt_mobile_hfx_job = "nec-mgt-mobile-hfx"
def prd_deploy_job = "nec-prd-deploy"

def version = params.version;
def src = params.src;
if (!version || !src) {
  node('master') {
    def userInput = input(
      id: 'userInput', message: '请输入构建参数?', parameters: [
      [$class: 'StringParameterDefinition', defaultValue: '', name: 'version', description: 'hotfix版本'],
    [$class: 'StringParameterDefinition', defaultValue: '', name: 'src', description: '源码版本，比如：dev或0.1.0']
  ])
    echo "start hotfix ${userInput['version']} from ${userInput['src']}"
    version = userInput['version']
    src = userInput['src']
  }
}
node('slave-nodejs') {
  stage('tag') {
    parallel('admin-tag': {
      dir('../' + admin_hfx_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/hotfix/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: adminUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/hotfix) = 1
          git tag -a -f -m "${version} hotfix!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-admin-web.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-admin-web.git :hotfix/${version}
            """
        }
      }
    },'exam-tag': {
      dir('../' + exam_hfx_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/hotfix/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: examUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/hotfix) = 1
          git tag -a -f -m "${version} hotfix!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-exam-web.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-exam-web.git :hotfix/${version}
            """
        }
      }
    },'mgt-web-tag': {
      dir('../' + mgt_web_hfx_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/hotfix/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtWebUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/hotfix) = 1
          git tag -a -f -m "${version} hotfix!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-web.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-web.git :hotfix/${version}
            """
        }
      }
    }, 'mobile-tag': {
      dir('../' + mobile_hfx_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/hotfix/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mobileUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/hotfix) = 1
          git tag -a -f -m "${version} hotfix!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mobile.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mobile.git :hotfix/${version}
            """
        }
      }
    }, 'msg-tag': {
      dir('../' + msg_hfx_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/hotfix/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: msgUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/hotfix) = 1
          git tag -a -f -m "${version} hotfix!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-msg.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-msg.git :hotfix/${version}
            """
        }
      }
    }, 'server-tag': {
      dir('../' + server_hfx_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/hotfix/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: serverUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/hotfix) = 1
          git tag -a -f -m "${version} hotfix!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-server.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-server.git :hotfix/${version}
            """
        }
      }
    }, 'auth-tag': {
      dir('../' + auth_hfx_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/hotfix/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: authUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/hotfix) = 1
          git tag -a -f -m "${version} hotfix!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-auth.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-auth.git :hotfix/${version}
            """
        }
      }
    },'mts-tag': {
      dir('../' + mts_hfx_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/hotfix/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mtsUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/hotfix) = 1
          git tag -a -f -m "${version} hotfix!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mts.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mts.git :hotfix/${version}
            """
        }
      }
    },'mgt-tag': {
      dir('../' + mgt_hfx_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/hotfix/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/hotfix) = 1
          git tag -a -f -m "${version} hotfix!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt.git :hotfix/${version}
            """
        }
      }
    },'mgt-mobile-tag': {
      dir('../' + mgt_mobile_hfx_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/hotfix/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtMobileUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/hotfix) = 1
          git tag -a -f -m "${version} hotfix!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-mobile.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-mobile.git :hotfix/${version}
            """
        }
      }
    })
  }
  stage('push') {
    sh """
        #nec镜像
    docker pull ${imageBaseUrl}/ky-edu/nec-server:hfx
    docker pull ${imageBaseUrl}/ky-edu/nec-exam-web:hfx
    docker pull ${imageBaseUrl}/ky-edu/nec-mgt-web:hfx
    docker pull ${imageBaseUrl}/ky-edu/nec-admin-web:hfx
    docker pull ${imageBaseUrl}/ky-edu/nec-mobile:hfx
    docker pull ${imageBaseUrl}/ky-edu/nec-msg:hfx
    docker pull ${imageBaseUrl}/ky-edu/nec-mts:hfx
    docker pull ${imageBaseUrl}/ky-edu/nec-mgt:hfx
    docker pull ${imageBaseUrl}/ky-edu/nec-auth:hfx
    docker pull ${imageBaseUrl}/ky-edu/nec-mgt-mobile:hfx

    docker tag ${imageBaseUrl}/ky-edu/nec-server:hfx ${imageBaseUrl}/ky-edu/nec-server:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-server:hfx ${imageBaseUrl}/ky-edu/nec-server:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-exam-web:hfx ${imageBaseUrl}/ky-edu/nec-exam-web:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-exam-web:hfx ${imageBaseUrl}/ky-edu/nec-exam-web:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-mgt-web:hfx ${imageBaseUrl}/ky-edu/nec-mgt-web:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-mgt-web:hfx ${imageBaseUrl}/ky-edu/nec-mgt-web:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-admin-web:hfx ${imageBaseUrl}/ky-edu/nec-admin-web:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-admin-web:hfx ${imageBaseUrl}/ky-edu/nec-admin-web:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-mobile:hfx ${imageBaseUrl}/ky-edu/nec-mobile:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-mobile:hfx ${imageBaseUrl}/ky-edu/nec-mobile:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-msg:hfx ${imageBaseUrl}/ky-edu/nec-msg:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-msg:hfx ${imageBaseUrl}/ky-edu/nec-msg:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-mts:hfx ${imageBaseUrl}/ky-edu/nec-mts:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-mts:hfx ${imageBaseUrl}/ky-edu/nec-mts:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-mgt:hfx ${imageBaseUrl}/ky-edu/nec-mgt:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-mgt:hfx ${imageBaseUrl}/ky-edu/nec-mgt:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-auth:hfx ${imageBaseUrl}/ky-edu/nec-auth:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-auth:hfx ${imageBaseUrl}/ky-edu/nec-auth:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-mgt-mobile:hfx ${imageBaseUrl}/ky-edu/nec-mgt-mobile:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-mgt-mobile:hfx ${imageBaseUrl}/ky-edu/nec-mgt-mobile:latest

    docker push ${imageBaseUrl}/ky-edu/nec-server:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-server:latest

    docker push ${imageBaseUrl}/ky-edu/nec-exam-web:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-exam-web:latest

    docker push ${imageBaseUrl}/ky-edu/nec-mgt-web:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-mgt-web:latest

    docker push ${imageBaseUrl}/ky-edu/nec-admin-web:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-admin-web:latest

    docker push ${imageBaseUrl}/ky-edu/nec-mobile:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-mobile:latest

    docker push ${imageBaseUrl}/ky-edu/nec-msg:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-msg:latest

    docker push ${imageBaseUrl}/ky-edu/nec-auth:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-auth:latest

    docker push ${imageBaseUrl}/ky-edu/nec-mts:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-mts:latest

    docker push ${imageBaseUrl}/ky-edu/nec-mgt:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-mgt:latest

    docker push ${imageBaseUrl}/ky-edu/nec-mgt-mobile:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-mgt-mobile:latest
    """
  }
}

node('master') {
  stage('deploy') {
    build(job: prd_deploy_job, parameters: [[$class: 'StringParameterValue', name: 'type', value: 'hfx']])
  }
}
