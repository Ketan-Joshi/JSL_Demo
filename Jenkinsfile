// Pipeline starts from here 
pipeline {
	agent any
	stages {
    	stage('Fetching Code') {
        	steps {
                echo "Cleaning Workspace"
                deleteDir()
                echo "Cleaning @tmp Directory"
                dir("${WORKSPACE}@tmp") {
                    deleteDir()
                }
            	echo "Fetching Code from Bit-Bucket"
                checkout scm
        	}
        }
		stage ('Fetching Environment') {
	    	steps {
				script {
					echo "Fetching Environment"
                    branchName = env.BRANCH_NAME.toLowerCase()
                    if ( branchName == "develop" ) {
                        environment = "dev"
                        echo "Running in ${environment} environment"
                    } else if ( branchName == "qa" ) {
                        environment = "qa"
                        echo "Running in ${environment} environment"
                    } else if ( branchName == "uat" ) {
                        environment = "uat"
                        echo "Running in ${environment} environment"
                    } else if ( branchName == "main" ) {
                        environment = "prod"
                        echo "Running in ${environment} environment"
                    } else {
                        try {
                            timeout(time: 300, unit: 'SECONDS') {
                                def userInput = input message: 'Environment Selection', ok: 'Next',
                                    parameters: [
                                        choice(name: 'ENVIRONMENT', choices: ['dev', 'qa'], description: 'Choose Environment')
                                    ]
                                environment = "${userInput}"
                                echo "Running in ${environment} environment"
                            }
                        }
                        catch(err){
                            def user = err.getCauses()[0].getUser().toString()
                            if('SYSTEM' == user.toString()) { // SYSTEM means timeout.
                                echo "Timed Out!"
                            } else {
                                currentBuild.result = 'ABORTED'
                                abortBuild("This build was aborted by [${user}]")
                            }
                        }
                    }
                    // Setting Up EKS Cluster Environment
                    if ( "${environment}" == "dev" || "${environment}" == "qa" ) {
                        clusterEnvironment = "dev"
                        echo "EKS Cluster Environment is ${clusterEnvironment}"
                    } else {
                        clusterEnvironment = "${environment}"
                        echo "EKS Cluster Environment is ${clusterEnvironment}"
                    }
				}
			}	
		}
        stage ('Docker Build and Push') {
            steps {
                script {
                    if ( "${environment}" == "dev" || "${environment}" == "qa" ) {
                        //Creating Git Commit ID Variable
                        def commitID = env.GIT_COMMIT.take(7)
                        // Passing environment variables in Dockerfile
                        sh "bash kubernetes/setup-dockerfile.sh ${environment} ${repoName}"
                        //sh("eval \$(aws ecr get-login --region ${region} | sed 's|-e none https://||')")
                        branchName = branchName.replace("/", "-");
                        //tags.add("${branchName}-${commitID}")
                        //docker.withRegistry("${ecrUrl}","") {
                        //    image = docker.build("${repoName}")
                        //    tags.each{
                        //        docker.image("${repoName}").push("${it}")                            
                        //    }
                        //}
                        sh "docker build -t ${localImageUrl}/${repoName}:${branchName}-${commitID} ."
                        sh "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${localImageUrl}"
                        sh "docker push ${localImageUrl}/${repoName}:${branchName}-${commitID}"
                    } else if ( "${environment}" == "uat" || "${environment}" == "prod" ) {
                        def commitID = env.GIT_COMMIT.take(7)
                        def buildNumber = env.BUILD_NUMBER
                        branchName = branchName.replace("/", "-");
                        sh "docker build -t ${localImageUrl}/${repoName}:${branchName}-${buildNumber}-${commitID} -f octopus-pre-epic-portal/Dockerfile ."
                        sh "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${localImageUrl}"
                        sh "docker push ${localImageUrl}/${repoName}:${branchName}-${buildNumber}-${commitID}"
                    } else {
                        echo "Environment Not Matched"
                    }
                }
            }
        }
        stage ('Removing Local Image') {
            steps {
                script {
                    def commitID = env.GIT_COMMIT.take(7)
                    sh "docker rmi ${repoName} | true"
                    if ( "${environment}" == "dev" || "${environment}" == "qa") {
                        sh "docker rmi ${localImageUrl}/${repoName}:${branchName}-${commitID} | true"
                    } else if ( "${environment}" == "uat" || "${environment}" == "prod") {
                        def buildNumber = env.BUILD_NUMBER
                        sh "docker rmi ${localImageUrl}/${repoName}:${branchName}-${buildNumber}-${commitID} | true"   
                    }
                }
            }
        }
        stage ('Deploying Kong Api-Gateway and Konga') {
            when {
                anyOf {
                    expression { "${environment}" == "dev" }
                    expression { "${environment}" == "qa" }
                }
            }
            steps {
                script {
                    sh "bash kubernetes/kong-setup.sh ${clusterEnvironment} ${environment} ${appName}"
                }
            }
        }
        stage ('Deploying Application') {
            when {
                anyOf {
                    expression { "${environment}" == "dev" }
                    expression { "${environment}" == "qa" }
                }
            }
            steps {
                script {
                    def commitID = env.GIT_COMMIT.take(7)
                    sh "bash kubernetes/eks-setup.sh ${clusterEnvironment} ${environment} ${appName} ${commitID} ${repoName} ${branchName}"
                }
            }
        }
   	}
}
