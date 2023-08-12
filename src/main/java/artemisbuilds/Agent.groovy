import groovy.yaml.Yaml
import groovy.json.JsonSlurperClassic

def call(yamlFilePath) {
    def configFile = new File(yamlFilePath)

    if (!configFile.exists()) {
        error "Configuration file not found: ${yamlFilePath}"
    }

    def config = new Yaml().load(configFile)

    def appName = config.name
    def agentConfig = config.Agent
    def buildConfig = config.Build
    def sonarConfig = config.Sonar
    def securityScanConfig = config.SecurityScan
    def deployConfig = config.Deploy

    pipeline {
        agent {
            label agentConfig
        }
        stages {
            stage('Build, Sonar, SecurityScan') {
                agent {
                    label agentConfig
                }
                stages {
                    stage('Build') {
                        steps {
                            script {
                                appName.Build(buildConfig)
                            }
                        }
                    }
                    stage('Sonar') {
                        steps {
                            script {
                                appName.Sonar(sonarConfig)
                            }
                        }
                    }
                    stage('SecurityScan') {
                        steps {
                            script {
                                appName.SecurityScan(securityScanConfig)
                            }
                        }
                    }
                }
            }
            stage('Deploy') {
                agent {
                    label deployConfig.agent
                }
                steps {
                    script {
                        appName.Deploy(deployConfig)
                    }
                }
            }
        }
    }
}
