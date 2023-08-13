package artemisbuilds
import org.yaml.snakeyaml.Yaml

def validateYaml(yamlFilePath) {
    def configFile = new File(yamlFilePath)

    if (!configFile.exists()) {
        return false
    }

    try {
        new Yaml().load(configFile)
        return true
    } catch (Exception e) {
        return false
    }
}
