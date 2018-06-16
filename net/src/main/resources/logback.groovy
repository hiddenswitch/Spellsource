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
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.boolex.OnMarkerEvaluator
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.core.filter.EvaluatorFilter

import static ch.qos.logback.classic.Level.*

def date = timestamp("yyyyMMdd")
def isAWS = System.getenv("SPELLSOURCE_APPLICATION") != null

if (isAWS) {
    appender("ASYNC_AWS_LOGS", AwsLogsAppender) {
        filter(ThresholdFilter) {
            level = INFO
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
        level = isAWS ? WARN : TRACE
    }
}

root(DEBUG, isAWS ? ["STDOUT", "ASYNC_AWS_LOGS"] : ["STDOUT"])

logger("io.netty", ERROR)
logger("com.hazelcast", ERROR)
logger("org.reflections", ERROR)
logger("co.paralleluniverse", ERROR)
logger("net.demilich", WARN)
logger("io.vertx", WARN)
logger("com.hiddenswitch", WARN)
logger("com.hiddenswitch.spellsource.Matchmaking", TRACE)