import spock.lang.Specification
import groovy.transform.Field

class BuildTest extends Specification {

    @Field def buildConfig = [
            Techno: 'mvn'
    ]

    def 'Test Build configuration'() {
        given:
        def buildScript = new File('Build.groovy')
        def binding = new Binding(buildConfig)
        def shell = new GroovyShell(binding)

        when:
        shell.evaluate(buildScript.text)

        then:
        buildTechno == buildConfig.Techno
    }
}
