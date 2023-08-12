def call(sonarConfig, buildTechno) {
    def sonarID = sonarConfig.SonarID
    def sonarCredentials = sonarConfig.SonarCredentials

    stage('Sonar Analysis') {
        steps {
            script {
                def scannerHome = tool name: 'SonarScanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
                def sonarCmd = ''

                if (buildTechno == 'mvn') {
                    sonarCmd = "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${sonarID} -Dsonar.login=${sonarCredentials}"
                } else if (buildTechno == 'dotnet') {
                    sonarCmd = "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${sonarID} -Dsonar.login=${sonarCredentials} -Dsonar.cs.dotcover.reportsPaths=**/coverage.xml"
                } else {
                    error "Unsupported build technology: ${buildTechno}"
                }

                sh sonarCmd
            }
        }
    }
}
