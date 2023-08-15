@Library('ArtemisBuilds') _

def yamlFilePath = 'path/to/your/project.yaml'
if (Utils.validateYaml(yamlFilePath)) {
    println("YAML is valid.")
} else {
    println("YAML is invalid.")
}

pipeline {
    agent any

    stages {
        stage('Initialize') {
            steps {
                script {
                    appName.PipelineRunner('path/to/your/config.yaml')
                }
            }
        }
    }
}
