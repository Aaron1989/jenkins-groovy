def folder = 'Prod-Public'

def mergeJobName = folder+'/'+'Prod-Public-Merge-Job'
def gitAuth = 'dingqishi'


freeStyleJob("$mergeJobName") {
    logRotator(30, 300)
    parameters {
        stringParam ('VERSION',"")
        stringParam ('REPOSITORY_URL',"")
        stringParam ('BRANCH',"")
        textParam ('DESCRIPTION',"")
    }
    scm {
        git {
            remote {
              url('${REPOSITORY_URL}')
              branch('*/${BRANCH}')
              credentials("$gitAuth")
            }
        extensions{
              preBuildMerge {
                options{
                  mergeRemote('origin')
                  mergeTarget('master')
                  mergeStrategy('default')
                  fastForwardMode('FF')
                }
              }
            }
        }
    }

    publishers {
        gitPublisher{
          tagsToPush {
            tagToPush {
              targetRepoName('origin')
              tagName('$VERSION')
              tagMessage('$DESCRIPTION')
              forcePush(false)
              createTag(true)
              updateTag(true)
            }
          }
          branchesToPush{
            branchToPush {
                targetRepoName('origin')
                branchName('master')
            }
          }
          pushOnlyIfSuccess(true)
          pushMerge(true)
        		}
     }

}
