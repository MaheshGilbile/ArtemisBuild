import groovy.mock.interceptor.MockFor
import org.junit.Test

class UtilsTest extends GroovyTestCase {

    @Test
    void testValidateYaml() {
        def utils = new Utils()

        def mockFileExists = { String path -> path == 'existing_file.yaml' }
        def mockYamlLoad = { Object input -> println "Mocked Yaml.load($input)" }

        def mock = new MockFor(utils)
        mock.demand.fileExists { String path -> mockFileExists(path) }
        mock.demand.new Yaml() { -> [load: mockYamlLoad] }

        def result = utils.validateYaml('existing_file.yaml')

        mockFileExists = { String path -> path != 'existing_file.yaml' }
        def resultInvalid = utils.validateYaml('non_existing_file.yaml')

        assert result == true
        assert resultInvalid == false

        mock.verify()
    }
}

// This is a mock implementation of Utils.groovy
class Utils {
    def validateYaml(yamlFilePath) {
        def configFile = new File(yamlFilePath)

        if (!fileExists(yamlFilePath)) {
            return false
        }

        try {
            new Yaml().load(configFile)
            return true
        } catch (Exception e) {
            return false
        }
    }

    def fileExists(fileName) {
        return new File(fileName).exists()
    }
}
