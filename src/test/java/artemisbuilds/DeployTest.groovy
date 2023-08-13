import groovy.mock.interceptor.MockFor
import org.junit.Test

class DeployTest extends GroovyTestCase {

    @Test
    void testDeploy() {
        def mockParams = [DEPLOY_ENVIRONMENT: 'staging', CHANGE_TICKET: '']

        def mockInput = { Map params, String message -> params.DEPLOY_ENVIRONMENT }

        def mockStage = { String stageName, Closure closure -> closure.call() }

        def mockSh = { String command -> println "Mocked sh: $command" }

        def deployConfig = [
                environmentList: ['staging', 'production'],
                defaultEnv: 'staging',
                name: 'my-app',
                AnsibleRepo: 'https://github.com/ansible/repo',
                AnsibleRepoBranch: 'main',
                skip: false,
                agent: 'docker'
        ]

        def deploy = new Deploy()
        deploy.with {
            setInputAndCheck = mockInput
            checkoutAndDeploy = mockStage
            sh = mockSh
        }

        deploy.call(deployConfig)

        // Verify the behavior of setInputAndCheck and checkoutAndDeploy
        assert deploy.setInputAndCheck.call(mockParams, 'Proceed with deployment?') == 'staging'
        assert deploy.checkoutAndDeploy.call() == null  // The mock stage should not return a value

        // Verify that sh is called with the expected command
        def expectedAnsibleCommand = "ansible-playbook -i staging.inventory https://github.com/ansible/repo/deploy.yml"
        mockSh.verify { String command -> command == expectedAnsibleCommand }
    }
}
