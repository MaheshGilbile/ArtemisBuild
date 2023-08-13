package artemisbuilds

import static spock.lang.Specification

class LoggerUtilTest extends Specification {
    def "test log method"() {
        given:
        def message = "Test message"
        def outputStream = new ByteArrayOutputStream()
        System.setOut(new PrintStream(outputStream))

        when:
        LoggerUtil.log(message)

        then:
        outputStream.toString() == "[${new Date()}] $message\n"
    }

    def "test logError method"() {
        given:
        def errorMessage = "Test error message"
        def errorOutputStream = new ByteArrayOutputStream()
        System.setErr(new PrintStream(errorOutputStream))

        when:
        LoggerUtil.logError(errorMessage)

        then:
        errorOutputStream.toString() == "[${new Date()}] $errorMessage\n"
    }
}
