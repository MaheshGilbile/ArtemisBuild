import spock.lang.Specification
import groovy.transform.Field

class DeployTest extends Specification {

    @Field def deployConfig = [
            agent: 'myansibleagent',
            environmentList: ['dev', 'uat', 'prod'],
            defaultEnv: 'dev',
            AnsibleRepo: 'deployment',
            AnsibleRepoBranch: 'main',
            skip: false
    ]

    def 'Test Deploy configuration'() {
        given:
        def deployScript = new File('Deploy.groovy')
        def binding = new Binding(deployConfig)
        def shell = new GroovyShell(binding)

        when:
        shell.evaluate(deployScript.text)

        then:
        agent == deployConfig.agent
    }
}
