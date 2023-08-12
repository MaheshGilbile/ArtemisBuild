def call(buildConfig) {
    def buildTechno = buildConfig.Techno
    def buildOptions = buildConfig.BuildOptions
    def artifactoryDeploy = buildConfig.ArtifactoryDeploy

    stage('Build') {
        steps {
            script {
                switch (buildTechno) {
                    case 'mvn':
                        // Maven build
                        sh "mvn clean install ${buildOptions}"
                        break
                    case 'npm':
                        // npm build
                        sh "npm install"
                        sh "npm run build"
                        break
                    case 'dotnet':
                        // .NET build
                        sh "dotnet build"
                        break
                    default:
                        error "Unsupported build technology: ${buildTechno}"
                }
            }
        }
    }

    if (artifactoryDeploy) {
        stage('Artifactory Deploy') {
            steps {
                script {
                    def server = Artifactory.server('my-artifactory-server') // Replace with your server ID
                    def buildInfo = Artifactory.newBuildInfo()

                    def uploadSpec = """{
                        "files": [
                            {
                                "pattern": "target/*.jar", // Replace with the appropriate file pattern
                                "target": "my-repo-local/"
                            }
                        ]
                    }"""

                    def uploadSpecFile = writeTempFile(uploadSpec)

                    server.upload(uploadSpecFile, buildInfo)

                    buildInfo.env.capture = true
                    buildInfo.name = 'my-build'
                    buildInfo.number = env.BUILD_NUMBER

                    server.publishBuildInfo(buildInfo)
                }
            }
        }
    }
}
