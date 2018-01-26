s#!/usr/bin/env groovy

def gitUrl = "http:// <git>/scm/kyeeweb/kyeeweb.git"
def gitBranch = "*/release"
def gitAuth = "dingqishi"

def OSSBucket = "deployment-package"
def artifacts = "target/*.jar"
def objectPre = "distrelease/group/web/\$TAG"

def Folder = "Prod-Group"
def compileJobName = Folder+'/'+'Prod-Group-Compile'
def deployJobName = Folder+'/'+'Prod-Group-Deploy'
def pipelineJobName = Folder+ '/' +'Prod-Group-Pipeline'

mavenJob(compileJobName) {
    logRotator(5, 5)

    parameters {
        stringParam ('TAG',"")
        textParam ('Description',"")
		stringParam ('host',"oss-cn-qingdao-internal.aliyuncs.com")
		stringParam ('id',"  ")
		stringParam ('key',"   ")
    }

	label('compile')
    goals('clean install -Pmono-mode -Dmaven.test.skip=true')
    rootPOM('pom.xml')
    mavenInstallation('maven-3.2.5')

	preBuildSteps {
	    shell( "#npm install gulp-autoprefixer gulp-clean gulp-clean-css gulp-concat gulp-group-files gulp-rev gulp-rev-collector gulp-sass gulp-sequence gulp-uglify gulp-util gulp.spritesmith gulp-watch\n" +
               "#npm install gulp\n" + "gulp deploy\n" +
               "python /opt/OSS_Python_API/osscmd uploadfromdir src/main/resources/public/ oss://ky-group-prd --host=\$host --id=\$id --key=\$key --check_md5=true --check_point=cp_test.txt")
	}




    triggers {
        snapshotDependencies(true)
    }

	scm {
	    git {
		    remote {
			    url(gitUrl)
				branch(gitBranch)
				credentials(gitAuth)
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
                    tagName('$TAG')
                    tagMessage('$Description')
                    forcePush(false)
                    createTag(true)
                    updateTag(true)
                }
            }
            pushOnlyIfSuccess(true)
            pushMerge(true)
        }
        aliyunOSSPublisher {
            bucketName(OSSBucket)
            filesPath(artifacts)
            objectPrefix(objectPre)
        }
    }
}


freeStyleJob(deployJobName) {
    logRotator(5, 5)
	parameters {
        stringParam ('tag',"")
		stringParam ('deploy',"false")

    }

	label('docker-python')

	steps {
	    shell(
            "if [ \$deploy = 'true' ];then\n" +
	        "    " + "rm -f KyeeWeb-0.0.1-SNAPSHOT.jar*\n" +
            "    " +  "echo 'removed'\n" +
            "    " +  "sleep 20\n" +
	        "    " +  "wget http://deployment-package.oss-cn-qingdao-internal.aliyuncs.com/distrelease/group/web/\${tag}/KyeeWeb-0.0.1-SNAPSHOT.jar\n" +
            "fi"
		)
		shell(
			"nohup java -jar KyeeWeb-0.0.1-SNAPSHOT.jar --spring.profiles.active=release> log.log 2>& 1 &\n" +
            "sleep 60\n" +
            "python main.py http://localhost:8081/error.html\n" +
            "sleep 30\n" +
            "python  /opt/OSS_Python_API/osscmd uploadfromdir output/  oss://ky-group-prd --host=oss-cn-qingdao-internal.aliyuncs.com --id=   --key=    --check_md5=true --check_point=cpcheck.txt\n" +
		    "for pid in \$(ps -ef|grep KyeeWeb-0.0.1-SNAPSHOT.jar|grep -v grep|grep -v bash|cut -c 10-15);\n" +
            "        " + "do\n" +
            "            " + "echo \$pid;\n" +
            "            " + "kill -9 \$pid;\n" +
            "        " + "done;"
        )


	}
    scm {
      git{
        remote {
            url("http:// <git>/scm/kyeeweb/crawlerimage.git")
		    branch("*/master")
		    credentials(gitAuth)
        }
      }

    }

    triggers {
        scm('H H/4 * * *')
    }
}
pipelineJob(pipelineJobName) {
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url("https://github.com/Aaron1989/jenkins-groovy.git")
                        branch("*/master")
                        credentials(gitAuth)
                    }
                }
            }
            scriptPath("jenkins-pipelines/prodd/Prod/Prod-Group-Pipeline.groovy")
        }
    }
}