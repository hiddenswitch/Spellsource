/**
 * Logging settings for Spellsource.
 *
 * To set a specific minimum logging level for all game code, set the environment variable SPELLSOURCE_LOGGING_LEVEL.
 * For example, to see all tracing while running a server:
 *
 * SPELLSOURCE_LOGGING_LEVEL=TRACE gradle net:run
 */

import static ch.qos.logback.classic.Level.*


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