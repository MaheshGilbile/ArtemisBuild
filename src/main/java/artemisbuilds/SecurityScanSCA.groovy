package artemisbuilds

def call(securityScanConfig, buildTechno) {
    def scaEnabled = securityScanConfig.SCA

    stage('Security Scan SCA') {
        steps {
            script {
                if (scaEnabled) {

                    def artifactPath
                    def fileExtension
                    if (buildTechno == 'mvn') {
                        artifactPath = "${WORKSPACE}/target"
                        fileExtension = extractArtifactExtensionFromPom(artifactPath)
                    } else if (buildTechno == 'dotnet') {
                        artifactPath = "${WORKSPACE}/bin/Debug" // Adjust this path based on your .NET project's build output location
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

def extractArtifactExtensionFromPom(artifactPath) {
    def pomXml = readFile(artifactPath + '/pom.xml')
    def extensionMatch = pomXml =~ /<packaging>(.*?)<\/packaging>/
    return extensionMatch ? extensionMatch[0][1] : 'jar'
}

def nexusIQScan(artifactPath, fileExtension) {
    stage('Nexus IQ Scan') {
        steps {
            script {
                sh "java -jar nexusiq.jar scan -f ${WORKSPACE}/target/${artifactPath}.${fileExtension}"
            }
        }
    }
}
