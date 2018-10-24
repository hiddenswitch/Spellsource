//
// Built on Fri Mar 09 18:42:29 CET 2018 by logback-translator
// For more information on configuration files in Groovy
// please see http://logback.qos.ch/manual/groovy.html

// For assistance related to this tool or configuration files
// in general, please contact the logback user mailing list at
//    http://qos.ch/mailman/listinfo/logback-user

// For professional support please see
//   http://www.qos.ch/shop/products/professionalSupport

import ca.pjer.logback.AwsLogsAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter

import static ch.qos.logback.classic.Level.*

def date = timestamp("yyyyMMdd")
def isAWS = System.getenv("SPELLSOURCE_APPLICATION") != null
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

root(DEBUG, isAWS ? ["STDOUT", "ASYNC_AWS_LOGS"] : ["STDOUT"])

logger("io.netty", ERROR)
logger("com.hazelcast", ERROR)
logger("org.reflections", ERROR)
logger("com.github.fromage.quasi", ERROR)
logger("net.demilich", defaultLevel)
logger("io.vertx", INFO)
logger("com.hiddenswitch", INFO)
logger("org.asynchttpclient", defaultLevel)
logger("com.hiddenswitch.spellsource.Matchmaking", DEBUG)
logger("com.hiddenswitch.spellsource.Games", DEBUG)
logger("com.hiddenswitch.spellsource.impl.util.ServerGameContext", DEBUG)
logger("com.hiddenswitch.spellsource.Gateway", INFO)
logger("com.hiddenswitch.spellsource.GatewayTest", TRACE)
