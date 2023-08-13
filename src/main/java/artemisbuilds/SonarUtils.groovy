import hudson.plugins.sonar.SonarGlobalConfiguration
def call(sonarConfig, buildTechno) {
    def sonarID = sonarConfig.SonarID
    def sonarCredentials = sonarConfig.SonarCredentials
    def sonarOptions = sonarConfig.SonarOptions


    stage('Sonar Analysis') {
        steps {
            script {
                def scannerHome = tool name: 'SonarScanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
                def sonarCmd = ''

                def sonarScannerOpts = [
                        projectKey: sonarID,
                        credentialsId: sonarCredentials,
                ]

                withSonarQubeEnv('SonarQubeServer') {
                    if (buildTechno == 'mvn') {
                        sonarScannerOpts += [
                                extraProperties: "sonar.cs.dotcover.reportsPaths=**/coverage.xml"
                        ]
                    } else if (buildTechno == 'dotnet') {
                        sonarScannerOpts += [
                                extraProperties: "sonar.cs.dotcover.reportsPaths=**/coverage.xml"
                        ]
                    } else {
                        error "Unsupported build technology: ${buildTechno}"
                    }

                    sonarScannerOpts.each { key, value ->
                        sonarCmd += "-D${key}=${value} "
                    }

                    sh "${scannerHome}/bin/sonar-scanner ${sonarCmd}"
                }
            }
        }
    }
}
