import ch.qos.logback.classic.filter.ThresholdFilter

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyyMMdd'T'HHmmss} %thread %level %logger{15} %msg%n"
    }

    filter(ThresholdFilter) {
        level = DEBUG
    }
}

root(DEBUG, ["STDOUT"])

// Test group
logger("com.hiddenswitch.spellsource.util.UnityClient", INFO)
logger("com.hiddenswitch.spellsource.SimultaneousGamesTest", INFO)
logger("com.hiddenswitch.spellsource.ClusterTest", INFO)
logger("com.neovisionaries.ws.client", WARN)
logger("org.asynchttpclient", WARN)
logger("org.testcontainers", WARN)
logger("com.github.dockerjava", WARN)
logger("org.redisson", ERROR)