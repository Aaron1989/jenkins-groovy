node() {
    stage('merge') {
        checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/release']],
            doGenerateSubmoduleConfigurations: false, extensions: [[$class:
            'PreBuildMerge', options: [fastForwardMode: 'FF', mergeRemote: 'origin', mergeStrategy:
           '<object of type org.jenkinsci.plugins.gitclient.MergeCommand.Strategy>', mergeTarget: 'master']]],
              submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'dingqishi',
             url: 'http://h.quyiyuan.com/scm/bigdata/max-compute.git']]]
    }
}