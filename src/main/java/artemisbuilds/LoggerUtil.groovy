package artemisbuilds

class LoggerUtil {
    static void log(message) {
        echo "[${new Date()}] $message"
    }

    static void logError(errorMessage) {
        error "[${new Date()}] $errorMessage"
    }
}
