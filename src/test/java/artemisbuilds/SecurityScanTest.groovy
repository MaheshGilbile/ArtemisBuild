import spock.lang.Specification
import groovy.transform.Field

class SecurityScanTest extends Specification {

    @Field def securityScanConfig = [
            SAST: true,
            SCA: true
    ]

    def 'Test Security Scan configuration'() {
        given:
        def securityScanScript = new File('SecurityScan.groovy')
        def binding = new Binding(securityScanConfig)
        def shell = new GroovyShell(binding)

        when:
        shell.evaluate(securityScanScript.text)

        then:
        sastEnabled == securityScanConfig.SAST
    }
}
