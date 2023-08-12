import spock.lang.Specification
import groovy.transform.Field

class AgentTest extends Specification {

    @Field def agentConfig = [
            agent: 'mydockeragent'
    ]

    def 'Test Agent configuration'() {
        given:
        def agentScript = new File('Agent.groovy')
        def binding = new Binding(agentConfig)
        def shell = new GroovyShell(binding)

        when:
        shell.evaluate(agentScript.text)

        then:
        label == agentConfig.agent
    }
}
