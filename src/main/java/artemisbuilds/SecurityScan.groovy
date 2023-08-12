def call(securityScanConfig, buildTechno) {
    def sastEnabled = securityScanConfig.SAST
    def scaEnabled = securityScanConfig.SCA

    stage('Security Scan') {
        steps {
            script {
                if (sastEnabled) {
                    fortifyScan()
                }

                if (scaEnabled) {
                    nexusIQ()
                }
            }
        }
    }
}
def fortifyScan(){
    def refVersion = securityScanConfig.SASTConfig.Refversion
    def customTargetVersion = securityScanConfig.SASTConfig.CustomTargetVersion

    def targetVersion
    if (buildTechno == 'mvn') {
        targetVersion = readPomVersion()
    } else if (buildTechno == 'dotnet') {
        targetVersion = readNugetSpecVersion()
    } else {
        error "Unsupported build technology: ${buildTechno}"
    }

    if (customTargetVersion) {
        targetVersion = customTargetVersion
    }

    sh "fortifycommand ${refVersion} ${targetVersion}"
}
def nexusIQ() {
    stage('Nexus IQ Scan') {
        steps {
            script {
                // Run Nexus IQ scan command
                sh 'nexus-iq-command'
            }
        }
    }
}

def readPomVersion() {
    // Logic to read version from pom.xml
    def pomXml = readFile('pom.xml')
    def versionMatch = pomXml =~ /<version>(.*?)<\/version>/
    return versionMatch ? versionMatch[0][1] : null
}

def readNugetSpecVersion() {
    // Logic to read version from nuget.spec
    def nugetSpec = readFile('nuget.spec')
    def versionMatch = nugetSpec =~ /version\s*=\s*"(.*?)"/
    return versionMatch ? versionMatch[0][1] : null
}
