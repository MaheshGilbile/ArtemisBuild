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

                        def fileExtension
                        if (buildTechno == 'mvn') {
                            def artifactPath = "${WORKSPACE}/target"
                            fileExtension = extractArtifactExtensionFromPom(artifactPath)
                        } else if (buildTechno == 'dotnet') {
                            def artifactPath = "${WORKSPACE}/bin/Debug" // Adjust this path based on your .NET project's build output location
                            fileExtension = 'nupkg'
                        } else {
                            error "Unsupported build technology: ${buildTechno}"
                        }

                        nexusIQScan(artifactPath, fileExtension)

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

def extractArtifactExtensionFromPom(artifactPath) {
    def pomXml = readFile(artifactPath + '/pom.xml')
    def extensionMatch = pomXml =~ /<packaging>(.*?)<\/packaging>/
    return extensionMatch ? extensionMatch[0][1] : 'jar'
}
def nexusIQScan(artifactPath, fileExtension) {
    stage('Nexus IQ Scan') {
        steps {
            script {
                sh "java -jar nexusiq.jar scan -f ${artifactPath}.${fileExtension}"
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
