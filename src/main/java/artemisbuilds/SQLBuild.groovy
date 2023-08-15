package artemisbuilds

def call(buildConfig) {
    def buildTechno = buildConfig.Techno
    def artifactoryDeploy = buildConfig.ArtifactoryDeploy

    def logger = new LoggerUtil()

    if (buildTechno == 'sql') {
        stage('SQL Build') {
            steps {
                script {
                    // Replace this with the actual logic to package SQL files
                    // For example, you might use a shell command or another tool
                    def localPath = buildConfig.Techno.localPath
                    def appName = buildConfig.name
                    def sqlFilesPath = "${WORKSPACE}/${localPath}"
                    def sqlPackageName = "${appName}-sql-package-${env.BUILD_NUMBER}.zip"
                    sh "zip -r ${sqlPackageName} ${sqlFilesPath}"

                    // Upload the SQL package to Artifactory if ArtifactoryDeploy is true
                    if (artifactoryDeploy) {
                        def server = Artifactory.server('my-artifactory-server') // Replace with your server ID
                        def buildInfo = Artifactory.newBuildInfo()

                        def uploadSpec = """{
                            "files": [
                                {
                                    "pattern": "${sqlPackageName}",
                                    "target": "my-repo-local/sql/"
                                }
                            ]
                        }"""

                        def uploadSpecFile = writeTempFile(uploadSpec)
                        server.upload(uploadSpecFile, buildInfo)
                    }
                }
            }
        }
    } else {
        logger.logError("Unsupported build technology: ${buildTechno}")
        currentBuild.result = 'FAILURE' // Mark the build as failed
        return // Exit the script
    }
}
