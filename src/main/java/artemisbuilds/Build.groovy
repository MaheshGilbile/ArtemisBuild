package artemisbuilds

import artemisbuilds.LoggerUtil
import artemisbuilds.*

def call(buildConfig) {
    def buildTechno = buildConfig.Techno
    //def slnFileName = buildConfig.SlnFileName
    def logger = new artemisbuilds.LoggerUtil()
    def mavenBuild = new artemisbuilds.MavenBuild()
    def npmBuild = new artemisbuilds.NpmBuild()
    def dotnetBuild = new artemisbuilds.DotnetBuild()

    if (buildTechno == 'mvn') {
        mavenBuild.build(buildConfig)
    } else if (buildTechno == 'npm') {
        npmBuild.build(buildConfig)
    } else if (buildTechno == 'dotnet') {
        dotnetBuild.build(buildConfig)
    } else {
        logger.logError("Unsupported build technology: ${buildTechno}")
        currentBuild.result = 'FAILURE' // Mark the build as failed
    }
}
