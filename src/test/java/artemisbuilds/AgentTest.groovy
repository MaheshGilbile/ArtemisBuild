import groovy.mock.interceptor.MockFor
import org.junit.Test

class AgentTest extends GroovyTestCase {

    @Test
    void testPipeline() {
        def appName = [Build: { config -> println "Build stage logic" },
                       Sonar: { config -> println "Sonar stage logic" },
                       SecurityScanSAST: { config -> println "SecurityScanSAST stage logic" },
                       SecurityScanSCA: { config -> println "SecurityScanSCA stage logic" },
                       Deploy: { config -> println "Deploy stage logic" }]

        def mock = new MockFor(appName)
        mock.demand.Build(1..1) { config -> println "Mocked Build stage logic" }
        mock.demand.Sonar(1..1) { config -> println "Mocked Sonar stage logic" }
        mock.demand.SecurityScanSAST(1..1) { config -> println "Mocked SecurityScanSAST stage logic" }
        mock.demand.SecurityScanSCA(1..1) { config -> println "Mocked SecurityScanSCA stage logic" }
        mock.demand.Deploy(1..1) { config -> println "Mocked Deploy stage logic" }

        def yamlFilePath = 'path_to_your_config.yaml'

        def agent = new Agent()
        agent.call(yamlFilePath)

        mock.verify()
    }
}

// This is a mock implementation of Agent.groovy
class Agent {
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
}
