package artemisbuilds

import artemisbuilds.LoggerUtil

class NpmBuild {
    def build(buildConfig) {
        def logger = new artemisbuilds.LoggerUtil()

        stage('NPM Build') {
            steps {
                script {
                    if (!fileExists('package.json') || !fileExists('package-lock.json')) {
                        logger.logError("package.json or package-lock.json not found for npm build.")
                        currentBuild.result = 'FAILURE' // Mark the build as failed
                        return // Exit the script
                    }
                    sh "npm install"
                    def npmExitCode = sh script: "npm run build", returnStatus: true
                    if (npmExitCode != 0) {
                        currentBuild.result = 'FAILURE' // Mark the build as failed
                        return // Exit the script
                    }
                }
            }
        }
    }

    def fileExists(fileName) {
        return new File(fileName).exists()
    }
}
