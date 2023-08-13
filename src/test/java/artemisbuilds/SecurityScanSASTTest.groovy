package artemisbuilds

package artemisbuilds

import spock.lang.Specification

class SecurityScanSASTTest extends Specification {
    def "test SAST scan initiated when conditions are met"() {
        given:
        def securityScanConfig = [
                SAST: true,
                SASTConfig: [
                        Refversion: '1.0.0',
                        CustomTargetVersion: null
                ]
        ]
        def buildTechno = 'mvn'
        def branchName = 'feature/123'

        def loggerMock = Mock(LoggerUtil)
        def securityScanSAST = new SecurityScanSAST()

        securityScanSAST.logger = loggerMock

        when:
        securityScanSAST.call(securityScanConfig, buildTechno, branchName)

        then:
        1 * loggerMock.logInfo("SAST scan initiated.")
    }

    def "test SAST scan skipped when conditions are not met"() {
        given:
        def securityScanConfig = [
                SAST: false,
                SASTConfig: [
                        Refversion: '1.0.0',
                        CustomTargetVersion: null
                ]
        ]
        def buildTechno = 'mvn'
        def branchName = 'feature/123'

        def loggerMock = Mock(LoggerUtil)
        def securityScanSAST = new SecurityScanSAST()

        securityScanSAST.logger = loggerMock

        when:
        securityScanSAST.call(securityScanConfig, buildTechno, branchName)

        then:
        1 * loggerMock.logInfo("SAST scan skipped.")
    }
}

