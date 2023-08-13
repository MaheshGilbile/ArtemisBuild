package artemisbuilds

import org.jfrog.artifactory.client.Artifactory
import org.jfrog.buildinfo.Config
import artemisbuilds.LoggerUtil

class ArtifactoryUtils {
    static def uploadMavenArtifact() {
        def server = Artifactory.server('my-artifactory-server') // Replace with your server ID
        def buildInfo = Artifactory.newBuildInfo()
        def logger = new LoggerUtil()

        if (!fileExists('${WORKSPACE}/pom.xml')) { // Replace with your actual .csproj file name
            logger.logError("Neither CSPROJ nor Nuget.Spec not found for Dotnet build.")
        }

        def pomXml = readFile("${WORKSPACE}/pom.xml")
        def packagingMatcher = (pomXml =~ /<packaging>(.*?)<\/packaging>/)
        def packagingType = packagingMatcher ? packagingMatcher[0][1] : 'jar'

        def isSnapshotVersion = isSnapshotMavenVersion()

        def targetRepo = isSnapshotVersion ? "${buildInfo.name}-generic-local-dev" : "${buildInfo.name}-generic-local-release"

        def uploadSpec = """{
            "files": [
                {
                    "pattern": "target/*.${packagingType}",
                    "target": "${targetRepo}/"
                }
            ]
        }"""

        def uploadSpecFile = writeTempFile(uploadSpec)

        server.upload(uploadSpecFile, buildInfo)

        buildInfo.env.capture = true
        buildInfo.name = 'my-build'
        buildInfo.number = ${env.BUILD_NUMBER}

        server.publishBuildInfo(buildInfo)
    }

    static def uploadNpmArtifact() {
        def server = Artifactory.server('my-artifactory-server') // Replace with your server ID
        def buildInfo = Artifactory.newBuildInfo()
        def logger = new LoggerUtil()

        def uploadSpec = """{
            "files": [
                {
                    "pattern": "dist/**/*",
                    "target": "my-repo-local/npm/"
                }
            ]
        }"""

        def uploadSpecFile = writeTempFile(uploadSpec)

        server.upload(uploadSpecFile, buildInfo)

        buildInfo.env.capture = true
        buildInfo.name = 'my-build'
        buildInfo.number = ${env.BUILD_NUMBER}

        server.publishBuildInfo(buildInfo)
    }

    static def uploadDotnetArtifact() {
        def server = Artifactory.server('my-artifactory-server') // Replace with your server ID
        def buildInfo = Artifactory.newBuildInfo()
        def logger = new LoggerUtil()

        if (!fileExists('YourProject.csproj') || !fileExists('nuget.spec')) { // Replace with your actual .csproj file name
            logger.logError("Neither CSPROJ nor Nuget.Spec not found for Dotnet build.")
        }

        def isSnapshotVersion = isSnapshotDotnetVersion()

        def targetRepo = isSnapshotVersion ? "${buildInfo.name}-generic-local-dev" : "${buildInfo.name}-generic-local-release"

        def uploadSpec = """{
            "files": [
                {
                    "pattern": "bin/Debug/**/*.dll",
                    "target": "${targetRepo}/"
                }
            ]
        }"""

        def uploadSpecFile = writeTempFile(uploadSpec)

        server.upload(uploadSpecFile, buildInfo)

        buildInfo.env.capture = true
        buildInfo.name = 'my-build'
        buildInfo.number = ${env.BUILD_NUMBER}

        server.publishBuildInfo(buildInfo)
    }

    private static boolean fileExists(fileName) {
        return new File(fileName).exists()
    }

    private static boolean isSnapshotMavenVersion() {
        def pomXml = readFile('pom.xml')
        def matcher = (pomXml =~ /<version>.*-SNAPSHOT<\/version>/)
        return matcher.find()
    }
    private static boolean isSnapshotDotnetVersion() {
        def nugetSpec = readFile('nuget.spec')
        def matcher = (nugetSpec =~ /<version>.*-SNAPSHOT<\/version>/)
        return matcher.find()
    }
}
