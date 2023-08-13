import groovy.mock.interceptor.MockFor
import org.junit.Test

class ArtifactoryUtilsTest extends GroovyTestCase {

    @Test
    void testUploadMavenArtifact() {
        def utils = new ArtifactoryUtils()

        def mockFileExists = { String path -> path == '${WORKSPACE}/pom.xml' }
        def mockReadFile = { String path -> "<project><version>1.0.0-SNAPSHOT</version></project>" }
        def mockServer = [upload: { uploadSpecFile, buildInfo -> println "Mocked server.upload($uploadSpecFile, $buildInfo)" },
                          publishBuildInfo: { buildInfo -> println "Mocked server.publishBuildInfo($buildInfo)" }]
        def mockArtifactory = [server: { String serverId -> mockServer }]

        def mock = new MockFor(utils)
        mock.demand.fileExists { String path -> mockFileExists(path) }
        mock.demand.readFile { String path -> mockReadFile(path) }
        mock.demand.Artifactory.newBuildInfo { -> [:] }
        mock.demand.Artifactory.server { String serverId -> mockServer }

        utils.uploadMavenArtifact()

        mock.verify()
    }

    @Test
    void testUploadNpmArtifact() {
        def utils = new ArtifactoryUtils()

        def mockServer = [upload: { uploadSpecFile, buildInfo -> println "Mocked server.upload($uploadSpecFile, $buildInfo)" },
                          publishBuildInfo: { buildInfo -> println "Mocked server.publishBuildInfo($buildInfo)" }]
        def mockArtifactory = [server: { String serverId -> mockServer }]

        def mock = new MockFor(utils)
        mock.demand.Artifactory.newBuildInfo { -> [:] }
        mock.demand.Artifactory.server { String serverId -> mockServer }

        utils.uploadNpmArtifact()

        mock.verify()
    }

    @Test
    void testUploadDotnetArtifact() {
        def utils = new ArtifactoryUtils()

        def mockFileExists = { String path -> path == 'YourProject.csproj' || path == 'nuget.spec' }
        def mockServer = [upload: { uploadSpecFile, buildInfo -> println "Mocked server.upload($uploadSpecFile, $buildInfo)" },
                          publishBuildInfo: { buildInfo -> println "Mocked server.publishBuildInfo($buildInfo)" }]
        def mockArtifactory = [server: { String serverId -> mockServer }]

        def mock = new MockFor(utils)
        mock.demand.fileExists { String path -> mockFileExists(path) }
        mock.demand.Artifactory.newBuildInfo { -> [:] }
        mock.demand.Artifactory.server { String serverId -> mockServer }

        utils.uploadDotnetArtifact()

        mock.verify()
    }
}
