package artemisbuilds

def call(buildConfig) {
    def artifactoryDeploy = buildConfig.ArtifactoryDeploy
    //@GrabResolver(name='LoggerUtil', root='http://<LOGGERUTIL_REPO_URL>/libs-release')
    //@Grab(group='com.yourcompany', module='LoggerUtil', version='1.0.0')
    def logger = new artemisbuilds.LoggerUtil()
    stage('Build') {
        steps {
            script {
                if (buildTechno == 'mvn') {
                    if (artifactoryDeploy) {
                        logger.logInfo("Uploading Maven artifact to Artifactory...")
                        ArtifactoryUtils.uploadMavenArtifact()
                        logger.logInfo("Maven artifact upload completed.")
                    }
                } else if (buildTechno == 'npm') {
                    if (artifactoryDeploy) {
                        logger.logInfo("Uploading NPM artifact to Artifactory...")
                        ArtifactoryUtils.uploadNpmArtifact()
                        logger.logInfo("NPM artifact upload completed.")
                    }
                } else if (buildTechno == 'dotnet') {
                    if (artifactoryDeploy) {
                        logger.logInfo("Uploading Dotnet artifact to Artifactory...")
                        ArtifactoryUtils.uploadDotnetArtifact()
                        logger.logInfo("Dotnet artifact upload completed.")
                    }
                } else
                {
                    logger.logError("Unsupported build technology: ${buildTechno}")
                }
            }
        }
    }
}

