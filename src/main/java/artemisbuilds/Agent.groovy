// agent configuration
package artemisbuilds

import org.yaml.snakeyaml.Yaml

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
            stage('SecurityScanSAST') {
                agent {
                    label agentConfig
                }
                steps {
                    script {
                        appName.SecurityScanSAST(securityScanConfig)
                    }
                }
            }
            stage('Build') {
                agent {
                    label agentConfig
                }
                steps {
                    script {
                        appName.Build(buildConfig)
                    }
                }
            }
            stage('Sonar') {
                agent {
                    label agentConfig
                }
                steps {
                    script {
                        appName.Sonar(sonarConfig)
                    }
                }
            }
            stage('SecurityScanSCA') {
                agent {
                    label agentConfig
                }
                steps {
                    script {
                        appName.SecurityScanSCA(securityScanConfig)
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
