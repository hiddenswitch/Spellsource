import ch.qos.logback.classic.filter.ThresholdFilter

import static ch.qos.logback.classic.Level.*

/**
 * Logging settings for Spellsource.
 *
 * To set a specific minimum logging level for all game code, set the environment variable SPELLSOURCE_LOGGING_LEVEL.
 * For example, to see all tracing while running a server:
 *
 * SPELLSOURCE_LOGGING_LEVEL=TRACE gradle net:run
 */
appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyyMMdd'T'HHmmss} %thread %level %logger{15} %msg%n"
    }

    filter(ThresholdFilter) {
        level = DEBUG
    }
}

root(DEBUG, ["STDOUT"])

logger("io.netty", ERROR)
logger("org.reflections", ERROR)
logger("co.paralleluniverse", ERROR)
logger("net.demilich", WARN)
logger("io.vertx", INFO)
logger("io.jaegertracing", ERROR)
logger("com.hiddenswitch.spellsource.common.Tracing", ERROR)
logger("org.mongodb.driver", ERROR)

// Production group
logger("com.hiddenswitch", INFO)
logger("io.vertx.spi.cluster.redis", WARN)

// Test group
logger("com.hiddenswitch.spellsource.util.UnityClient", INFO)
logger("com.hiddenswitch.spellsource.net.impl.MigrationsImpl", INFO)
logger("com.hiddenswitch.spellsource.SimultaneousGamesTest", INFO)
logger("com.hiddenswitch.spellsource.ClusterTest", INFO)
logger("com.neovisionaries.ws.client", WARN)
logger("org.asynchttpclient", WARN)
logger("org.testcontainers", WARN)
logger("com.github.dockerjava", WARN)
logger("org.redisson", ERROR)