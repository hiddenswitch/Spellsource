import ch.qos.logback.classic.filter.ThresholdFilter

import static ch.qos.logback.classic.Level.ERROR
import static ch.qos.logback.classic.Level.INFO

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyyMMdd'T'HHmmss} %thread %level %logger{15} %msg%n"
    }

    filter(ThresholdFilter) {
        level = TRACE
    }
}


root(DEBUG, ["STDOUT"])

logger("co.paralleluniverse", ERROR)
logger("io.vertx", INFO)
logger("io.jaegertracing", ERROR)
logger("com.hiddenswitch.spellsource.common.Tracing", ERROR)

// Production group
logger("com.hiddenswitch.spellsource.util.Simulation", INFO)
logger("com.hiddenswitch", ERROR)
logger("net.demilich", ERROR)
logger("com.hiddenswitch.cluster.applications", DEBUG)
