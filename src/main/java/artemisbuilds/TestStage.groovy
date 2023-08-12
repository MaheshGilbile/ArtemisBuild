// TestStage.groovy
package artemisbuilds

class TestStage {
    def static execute(parsedConfig) {
        return {
            stage('Test') {
                steps {
                    script {
                        def testType = parsedConfig.Test.Type ?: 'unit'
                        if (testType == 'unit') {
                            sh 'mvn test'
                        } else if (testType == 'integration') {
                            sh 'mvn integration-test'
                        }
                    }
                }
            }
        }
    }
}

