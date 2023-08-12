import spock.lang.Specification
import groovy.transform.Field

class SonarTest extends Specification {

    @Field def sonarConfig = [
            SonarID: 'app1',
            SonarCredentials: 'sonar-api-key'
    ]

    def 'Test Sonar configuration'() {
        given:
        def sonarScript = new File('Sonar.groovy')
        def binding = new Binding(sonarConfig)
        def shell = new GroovyShell(binding)

        when:
        shell.evaluate(sonarScript.text)

        then:
        sonarID == sonarConfig.SonarID
    }
}
