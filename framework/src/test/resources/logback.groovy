import ch.qos.logback.classic.filter.ThresholdFilter

import static ch.qos.logback.classic.Level.ERROR

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger{24} %msg%n"
    }

    filter(ThresholdFilter) {
        level = TRACE
    }
}


root(ERROR, ["STDOUT"])
logger("com.hiddenswitch", ERROR)
logger("com.hiddenswitch.containers", INFO)