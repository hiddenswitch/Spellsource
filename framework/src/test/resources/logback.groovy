import ch.qos.logback.classic.filter.ThresholdFilter

import static ch.qos.logback.classic.Level.ERROR

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyyMMdd'T'HHmmss} %thread %level %logger{15} %msg%n"
    }

    filter(ThresholdFilter) {
        level = TRACE
    }
}


root(ERROR, ["STDOUT"])
logger("com.hiddenswitch.containers", INFO)