package artemisbuilds

package artemisbuilds

import spock.lang.Specification

class SecurityScanSCATest extends Specification {
    def "test Nexus IQ scan when SCA is enabled and buildTechno is mvn"() {
        given:
        def securityScanConfig = [
                SCA: true
        ]
        def buildTechno = 'mvn'

        def securityScanSCA = new SecurityScanSCA()

        when:
        securityScanSCA.call(securityScanConfig, buildTechno)

        then:
        1 * securityScanSCA.extractArtifactExtensionFromPom(_) >> 'jar'
        1 * securityScanSCA.nexusIQScan(_, 'jar')
    }

    def "test Nexus IQ scan when SCA is enabled and buildTechno is dotnet"() {
        given:
        def securityScanConfig = [
                SCA: true
        ]
        def buildTechno = 'dotnet'

        def securityScanSCA = new SecurityScanSCA()

        when:
        securityScanSCA.call(securityScanConfig, buildTechno)

        then:
        1 * securityScanSCA.nexusIQScan(_, 'nupkg')
    }

    def "test Nexus IQ scan skipped when SCA is disabled"() {
        given:
        def securityScanConfig = [
                SCA: false
        ]
        def buildTechno = 'mvn'

        def securityScanSCA = new SecurityScanSCA()

        when:
        securityScanSCA.call(securityScanConfig, buildTechno)

        then:
        0 * _
    }

    def "test Nexus IQ scan error when unsupported build technology"() {
        given:
        def securityScanConfig = [
                SCA: true
        ]
        def buildTechno = 'invalid'

        def securityScanSCA = new SecurityScanSCA()

        when:
        securityScanSCA.call(securityScanConfig, buildTechno)

        then:
        1 * securityScanSCA.nexusIQScan(_, _)
    }
}
