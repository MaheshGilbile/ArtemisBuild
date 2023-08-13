import groovy.mock.interceptor.MockFor
import org.junit.Test

class BuildTest extends GroovyTestCase {

    @Test
    void testBuildMaven() {
        def mockUtils = new MockFor(ArtifactoryUtils)
        mockUtils.demand.uploadMavenArtifact { -> }

        def mockFileExists = { String path -> true }
        def mockLoggerUtil = [logError: { msg -> println "Mocked logError: $msg" }]

        def mock = new MockFor(LoggerUtil)
        mock.demand.fileExists { String path -> mockFileExists(path) }
        mock.demand.new artemisbuilds.LoggerUtil { -> mockLoggerUtil }

        def buildConfig = [Techno: 'mvn', BuildOptions: '', ArtifactoryDeploy: true]

        def mockWorkspace = "mockedWorkspace"
        def mockEnv = [WORKSPACE: mockWorkspace, MAVEN_LOCAL_REPO: 'mockedRepo']
        def mockBuild = [sh: { command -> println "Mocked sh: $command" }]

        def mockGroovyScript = [env: mockEnv, BUILD_NUMBER: '123', WORKSPACE: mockWorkspace, sh: mockBuild]

        def build = new Build()
        build.with {
            ArtifactoryUtils.metaClass.static.uploadMavenArtifact = mockUtils.uploadMavenArtifact
            LoggerUtil.metaClass.static.new = { -> mockLoggerUtil }
            LoggerUtil.metaClass.static.fileExists = mockLoggerUtil.fileExists

            setProperty('ArtifactoryUtils', ArtifactoryUtils)
            setProperty('env', mockEnv)
            setProperty('sh', mockBuild)
            setProperty('workspace', mockWorkspace)
        }

        build.call(buildConfig)

        mockUtils.verify()
        mock.verify()
    }

    @Test
    void testBuildNpm() {
        // Similar test setup as testBuildMaven, but mocking NPM-specific logic
    }

    @Test
    void testBuildDotnet() {
        // Similar test setup as testBuildMaven, but mocking Dotnet-specific logic
    }
}
