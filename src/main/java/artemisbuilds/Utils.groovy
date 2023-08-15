package artemisbuilds
import org.yaml.snakeyaml.Yaml

class Utils {

    static boolean validateYaml(yamlFilePath) {
        def configFile = new File(yamlFilePath)

        if (!configFile.exists()) {
            return false
        }

        try {
            def yamlContent = configFile.text
            def yaml = new Yaml().load(yamlContent)

            // Validate keys and structure
            if (validateKeysAndStructure(yaml)) {
                return true
            } else {
                return false
            }
        } catch (Exception e) {
            return false
        }
    }

    static boolean validateKeysAndStructure(yaml) {
        // Define the expected keys and structure here
        def expectedKeys = ['name', 'Ecosystem', 'Agent', 'Build', 'Sonar', 'SecurityScan', 'Deploy']
        def buildTechnoKeys = ['Techno', 'BuildOptions', 'SlnFileName', 'ArtifactoryDeploy']
        def sonarKeys = ['SonarID', 'SonarCredentials', 'SonarOptions']
        def securityScanKeys = ['SAST', 'SASTConfig', 'SCA']
        def deployKeys = ['skip', 'agent', 'environmentList', 'defaultEnv', 'AnsibleRepo', 'AnsibleRepoBranch']

        if (!expectedKeys.every { yaml.containsKey(it) }) {
            return false
        }

        def build = yaml['Build']
        if (build) {
            // Check that only one buildTechno is specified
            if (!(buildTechnoKeys.every { build.containsKey(it) } && build.size() == 1)) {
                return false
            }
        }

        if (yaml['Sonar']) {
            if (!sonarKeys.every { yaml['Sonar'].containsKey(it) }) {
                return false
            }
        }

        if (yaml['SecurityScan']) {
            if (!securityScanKeys.every { yaml['SecurityScan'].containsKey(it) }) {
                return false
            }
        }

        if (yaml['Deploy']) {
            if (!deployKeys.every { yaml['Deploy'].containsKey(it) }) {
                return false
            }
        }

        return true
    }
}
