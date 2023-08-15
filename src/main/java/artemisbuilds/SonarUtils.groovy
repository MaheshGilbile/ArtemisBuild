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

                def sonarScannerPath = "${scannerHome}/bin/sonar-scanner"

                if (isUnix()) {
                    if (!fileExists(sonarScannerPath)) {
                        downloadAndInstallSonarScanner(scannerHome, 'linux')
                    }
                } else {
                    if (!fileExists(sonarScannerPath + ".bat")) {
                        downloadAndInstallSonarScanner(scannerHome, 'windows')
                    }
                }

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

                    sh "${sonarScannerPath} ${sonarCmd}"
                }
            }
        }
    }
}

def isUnix() {
    return !System.getProperty("os.name").toLowerCase().contains("win")
}

def downloadAndInstallSonarScanner(installationDir, platform) {
    def sonarScannerZipUrl
    def sonarScannerDir
    if (platform == 'linux') {
        sonarScannerZipUrl = "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.6.0.2311-linux.zip"
        sonarScannerDir = "${installationDir}/bin"
    } else if (platform == 'windows') {
        sonarScannerZipUrl = "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.6.0.2311-windows.zip"
        sonarScannerDir = "${installationDir}/bin"
    } else {
        error "Unsupported platform: ${platform}"
    }

    def downloadDir = "${installationTokenDir}/download"
    def sonarScannerZip = "${downloadDir}/sonar-scanner.zip"

    sh "mkdir -p ${downloadDir}"
    sh "curl -L -o ${sonarScannerZip} ${sonarScannerZipUrl}"
    sh "unzip -o ${sonarScannerZip} -d ${sonarScannerDir}"
}

def fileExists(fileName) {
    return new File(fileName).exists()
}
