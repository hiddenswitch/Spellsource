/**
 * Logging settings for Spellsource.
 *
 * To set a specific minimum logging level for all game code, set the environment variable SPELLSOURCE_LOGGING_LEVEL.
 * For example, to see all tracing while running a server:
 *
 * SPELLSOURCE_LOGGING_LEVEL=TRACE gradle net:run
 */
import ca.pjer.logback.AwsLogsAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter

import static ch.qos.logback.classic.Level.*

def date = timestamp("yyyyMMdd")
def isAWS = System.getenv("SPELLSOURCE_APPLICATION") != null || System.getenv("AWS_ACCESS_KEY_ID") != null
Level defaultLevel = null;

if (System.getenv().containsKey("SPELLSOURCE_LOGGING_LEVEL")) {
    defaultLevel = Level.valueOf(System.getenv("SPELLSOURCE_LOGGING_LEVEL"))
} else {
    defaultLevel = WARN;
}

if (isAWS) {
    appender("ASYNC_AWS_LOGS", AwsLogsAppender) {
        filter(ThresholdFilter) {
            level = TRACE
        }

        layout(PatternLayout) {
            pattern = "%d{yyyyMMdd'T'HHmmss} %thread %level %logger{15} %msg%n"
        }

        logGroupName = "/com/hiddenswitch/spellsource"
        logStreamName = "stream-${date}"
        logRegion = "us-west-2"
        maxBatchLogEvents = 1000
        maxFlushTimeMillis = 1000
        maxBlockTimeMillis = 0
    }
}

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyyMMdd'T'HHmmss} %thread %level %logger{15} %msg%n"
    }

    filter(ThresholdFilter) {
        level = isAWS ? defaultLevel : TRACE
    }
}

root(DEBUG, ["STDOUT"])

logger("io.netty", ERROR)
logger("io.atomix", WARN)
logger("org.reflections", ERROR)
logger("co.paralleluniverse", ERROR)
logger("net.demilich", WARN)
logger("io.vertx", INFO)
logger("io.jaegertracing", ERROR)
logger("com.hiddenswitch.spellsource.common.Tracing", ERROR)

// Production group
logger("com.hiddenswitch", INFO)
logger("io.atomix.cluster.messaging.impl", ERROR)
logger("io.atomix.cluster.discovery", DEBUG)

// Test group
logger("com.hiddenswitch.spellsource.util.UnityClient", defaultLevel)
logger("com.hiddenswitch.spellsource.SimultaneousGamesTest", defaultLevel)
logger("com.hiddenswitch.spellsource.ClusterTest", defaultLevel)
logger("com.neovisionaries.ws.client", WARN)
logger("org.asynchttpclient", WARN)