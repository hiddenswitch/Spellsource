/**
 * Logging settings for Spellsource.
 *
 * To set a specific minimum logging level for all game code, set the environment variable SPELLSOURCE_LOGGING_LEVEL.
 * For example, to see all tracing while running a server:
 *
 * SPELLSOURCE_LOGGING_LEVEL=TRACE gradle net:run
 */


import ch.qos.logback.classic.Level
import ch.qos.logback.classic.filter.ThresholdFilter

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyyMMdd'T'HHmmss} %thread %level %logger{15} %msg%n"
    }

    filter(ThresholdFilter) {
        level = TRACE
    }
}


root(ERROR, ["STDOUT"])
logger("net.demilich", ERROR)
logger("com.hiddenswitch.spellsource.tests", DEBUG)
logger("com.hiddenswitch.spellsource.common", OFF)
logger("io.vertx.spi.cluster.redis", ERROR)