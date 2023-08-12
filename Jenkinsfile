@Library('ArtemisBuilds') _

def yamlFilePath = 'path/to/your/config.yaml'

def isValidYaml = validateYaml(yamlFilePath)

if (!isValidYaml) {
    error "Invalid YAML configuration"
}

pipeline {
    agent any

    stages {
        stage('Initialize') {
            steps {
                script {
                    appName.Agent('path/to/your/config.yaml')
                }
            }
        }
    }
}
