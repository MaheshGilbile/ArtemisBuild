package artemisbuilds
import artemisbuilds.LoggerUtil
def call(securityScanConfig, buildTechno, branchName) {
    def logger = new artemisbuilds.LoggerUtil()
    def sastEnabled = securityScanConfig.SAST

        stage('Security Scan SAST') {
            when {
                expression {
                    (branchName.startsWith('release') || branchName.startsWith('master')) ||
                            (sastEnabled && (branchName.startsWith('feature') || branchName.startsWith('develop') || branchName.startsWith('bugfix') || isCustomBranch(branchName)))
                }
            }
            steps {
                script {
                    if (sastEnabled || (branchName.startsWith('release') || branchName.startsWith('master'))) {
                        fortifyScan(buildTechno)
                        logger.logInfo("SAST scan initiated.")
                    } else {
                        logger.logInfo("SAST scan skipped.")
                    }
                }
            }
        }
    }

def fortifyScan(buildTechno) {
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
def isCustomBranch(branchName) {
    // Logic to determine if the branch is a custom branch
    // For example, you can check if it doesn't start with any of the predefined prefixes
    def predefinedPrefixes = ['release', 'master', 'feature', 'develop', 'bugfix']
    return !predefinedPrefixes.any { prefix -> branchName.startsWith(prefix) }
}