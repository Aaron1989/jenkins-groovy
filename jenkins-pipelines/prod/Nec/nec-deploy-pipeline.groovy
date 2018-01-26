def credentialsId = 'stash_huis'
def baseUrl = 'http://zhangshaohui_cp@s.kyee.com.cn/scm/nec/'
def imageBaseUrl = 'registry-vpc.cn-hangzhou.aliyuncs.com'

//stash url
def adminUrl = baseUrl + 'nec-admin-web.git'
def examUrl = baseUrl + 'nec-exam-web.git'
def mobileUrl = baseUrl + 'nec-mobile.git'
def mgtWebUrl = baseUrl + 'nec-mgt-web.git'
def msgUrl = baseUrl + 'nec-msg.git'
def serverUrl = baseUrl + 'nec-server.git'
def mtsUrl=baseUrl + 'nec-mts.git'
def mgtUrl=baseUrl + 'nec-mgt.git'
def authUrl=baseUrl + 'nec-auth.git'
def mgtMobileUrl=baseUrl + 'nec-mgt-mobile.git'

//base job
def admin_rls_job = "nec-admin-rls-web"
def exam_rls_job = "nec-exam-rls-web"
def mobile_rls_job = "nec-mobile-rls-web"
def mgt_web_rls_job = "nec-mgt-web-rls"
def msg_rls_job = "nec-msg-rls"
def server_rls_job = "nec-server-rls"
def auth_rls_job="nec-auth-rls"
def mts_rls_job="nec-mts-rls"
def mgt_rls_job="nec-mgt-rls"
def mgt_mobile_rls_job = "nec-mgt-mobile-rls"
def prd_deploy_job = "nec-prd-deploy"

def version = params.version;
def src = params.src;
if (!version || !src) {
  node('master') {
    def userInput = input(
      id: 'userInput', message: '请输入构建参数?', parameters: [
      [$class: 'StringParameterDefinition', defaultValue: '', name: 'version', description: 'release版本'],
    [$class: 'StringParameterDefinition', defaultValue: '', name: 'src', description: '源码版本，比如：dev或0.1.0']
  ])
    echo "start release ${userInput['version']} from ${userInput['src']}"
    version = userInput['version']
    src = userInput['src']
  }
}
node('slave-nodejs') {
  stage('tag') {
    parallel('admin-tag': {
      dir('../' + admin_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/release/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: adminUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/release) = 1
          git tag -a -f -m "${version} release!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-admin-web.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-admin-web.git :release/${version}
            """
        }
      }
    },'exam-tag': {
      dir('../' + exam_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/release/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: examUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/release) = 1
          git tag -a -f -m "${version} release!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-exam-web.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-exam-web.git :release/${version}
            """
        }
      }
    }, 'mobile-tag': {
      dir('../' + mobile_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/release/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mobileUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/release) = 1
          git tag -a -f -m "${version} release!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mobile.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mobile.git :release/${version}
            """
        }
      }
    }, 'mgt-web-tag':{
        dir('../' + mgt_web_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/release/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtWebUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/release) = 1
          git tag -a -f -m "${version} release!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-web.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-web.git :release/${version}
            """
        }
      }
    },'msg-tag': {
      dir('../' + msg_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/release/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: msgUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/release) = 1
          git tag -a -f -m "${version} release!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-msg.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-msg.git :release/${version}
            """
        }
      }
    }, 'server-tag': {
      dir('../' + server_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/release/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: serverUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/release) = 1
          git tag -a -f -m "${version} release!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-server.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-server.git :release/${version}
            """
        }
      }
    },'auth-tag': {
      dir('../' + auth_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/release/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: authUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/release) = 1
          git tag -a -f -m "${version} release!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-auth.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-auth.git :release/${version}
            """
        }
      }
    },'mts-tag': {
      dir('../' + mts_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/release/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mtsUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/release) = 1
          git tag -a -f -m "${version} release!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mts.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mts.git :release/${version}
            """
        }
      }
    },'mgt-tag': {
      dir('../' + mgt_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/release/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/release) = 1
          git tag -a -f -m "${version} release!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt.git :release/${version}
            """
        }
      }
    },'mgt-mobile-tag': {
      dir('../' + mgt_mobile_rls_job) {
        checkout([$class: 'GitSCM', branches: [[name: "origin/release/${version}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PruneStaleBranch']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: credentialsId, url: mgtMobileUrl]]])
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'stash_pass', usernameVariable: 'stash_user')]) {
          sh """
          test \$(git branch -r |grep -c origin/release) = 1
          git tag -a -f -m "${version} release!" ${version}
          git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-mobile.git ${version} -f
            git push http://${stash_user}:${stash_pass}@s.kyee.com.cn/scm/nec/nec-mgt-mobile.git :release/${version}
            """
        }
      }
    })
  }
  stage('push') {
    sh """
        #nec镜像
    docker pull ${imageBaseUrl}/ky-edu/nec-server:rls
    docker pull ${imageBaseUrl}/ky-edu/nec-exam-web:rls
    docker pull ${imageBaseUrl}/ky-edu/nec-admin-web:rls
    docker pull ${imageBaseUrl}/ky-edu/nec-mobile:rls
    docker pull ${imageBaseUrl}/ky-edu/nec-mgt-web:rls
    docker pull ${imageBaseUrl}/ky-edu/nec-msg:rls
    docker pull ${imageBaseUrl}/ky-edu/nec-mts:rls
    docker pull ${imageBaseUrl}/ky-edu/nec-mgt:rls
    docker pull ${imageBaseUrl}/ky-edu/nec-auth:rls
    docker pull ${imageBaseUrl}/ky-edu/nec-mgt-mobile:rls

    docker tag ${imageBaseUrl}/ky-edu/nec-exam-web:rls ${imageBaseUrl}/ky-edu/nec-exam-web:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-exam-web:rls ${imageBaseUrl}/ky-edu/nec-exam-web:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-mgt-web:rls ${imageBaseUrl}/ky-edu/nec-mgt-web:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-mgt-web:rls ${imageBaseUrl}/ky-edu/nec-mgt-web:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-admin-web:rls ${imageBaseUrl}/ky-edu/nec-admin-web:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-admin-web:rls ${imageBaseUrl}/ky-edu/nec-admin-web:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-mobile:rls ${imageBaseUrl}/ky-edu/nec-mobile:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-mobile:rls ${imageBaseUrl}/ky-edu/nec-mobile:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-server:rls ${imageBaseUrl}/ky-edu/nec-server:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-server:rls ${imageBaseUrl}/ky-edu/nec-server:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-msg:rls ${imageBaseUrl}/ky-edu/nec-msg:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-msg:rls ${imageBaseUrl}/ky-edu/nec-msg:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-auth:rls ${imageBaseUrl}/ky-edu/nec-auth:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-auth:rls ${imageBaseUrl}/ky-edu/nec-auth:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-mts:rls ${imageBaseUrl}/ky-edu/nec-mts:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-mts:rls ${imageBaseUrl}/ky-edu/nec-mts:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-mgt:rls ${imageBaseUrl}/ky-edu/nec-mgt:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-mgt:rls ${imageBaseUrl}/ky-edu/nec-mgt:latest

    docker tag ${imageBaseUrl}/ky-edu/nec-mgt-mobile:rls ${imageBaseUrl}/ky-edu/nec-mgt-mobile:${version}
    docker tag ${imageBaseUrl}/ky-edu/nec-mgt-mobile:rls ${imageBaseUrl}/ky-edu/nec-mgt-mobile:latest

    docker push ${imageBaseUrl}/ky-edu/nec-exam-web:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-exam-web:latest

    docker push ${imageBaseUrl}/ky-edu/nec-admin-web:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-admin-web:latest

    docker push ${imageBaseUrl}/ky-edu/nec-mobile:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-mobile:latest

    docker push ${imageBaseUrl}/ky-edu/nec-mgt-web:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-mgt-web:latest

    docker push ${imageBaseUrl}/ky-edu/nec-server:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-server:latest

    docker push ${imageBaseUrl}/ky-edu/nec-msg:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-msg:latest

    docker push ${imageBaseUrl}/ky-edu/nec-mts:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-mts:latest

    docker push ${imageBaseUrl}/ky-edu/nec-mgt:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-mgt:latest

    docker push ${imageBaseUrl}/ky-edu/nec-auth:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-auth:latest

    docker push ${imageBaseUrl}/ky-edu/nec-mgt-mobile:${version}
    docker push ${imageBaseUrl}/ky-edu/nec-mgt-mobile:latest
    """
  }
}
node('master') {
  stage('deploy') {
    build(job: prd_deploy_job, parameters: [[$class: 'StringParameterValue', name: 'type', value: 'rls']])
  }
}
