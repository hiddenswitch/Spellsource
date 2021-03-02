import ch.qos.logback.classic.filter.ThresholdFilter

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger{24} %msg%n"
    }

    filter(ThresholdFilter) {
        level = TRACE
    }
}


scan("10 seconds")

root(ERROR, ["STDOUT"])
logger("io.grpc.netty", OFF)
logger("com.hiddenswitch", ERROR)
logger("com.hiddenswitch.framework", INFO)
logger("com.hiddenswitch.framework.impl", ERROR)
logger("com.hiddenswitch.framework.impl.ClusteredGames", INFO)
logger("com.hiddenswitch.containers", INFO)
logger("com.hiddenswitch.framework.Application", INFO)
logger("com.hiddenswitch.diagnostics.Tracing", INFO)