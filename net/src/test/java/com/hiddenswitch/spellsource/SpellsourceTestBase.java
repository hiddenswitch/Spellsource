package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.AccountsImpl;
import com.hiddenswitch.spellsource.impl.SpellsourceAuthHandler;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.util.Mongo;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.core.json.Json.decodeValue;
import static io.vertx.core.json.Json.encodeToBuffer;

@RunWith(VertxUnitRunner.class)
public class SpellsourceTestBase {
	protected static HazelcastInstance hazelcastInstance;
	protected static Vertx vertx;

	@BeforeClass
	public static void setUp(TestContext context) {
		if (hazelcastInstance == null) {
			hazelcastInstance = Hazelcast.newHazelcastInstance(Cluster.getConfig(5701));
			final Async async = context.async();

			Vertx.clusteredVertx(new VertxOptions().setClusterManager(new HazelcastClusterManager(hazelcastInstance)), context.asyncAssertSuccess(vertx -> {
				SpellsourceTestBase.vertx = vertx;
				vertx.executeBlocking(fut -> {
					Mongo.mongo().connectWithEnvironment(vertx);
					fut.complete();
				}, context.asyncAssertSuccess(v1 -> {
					Spellsource.spellsource().deployAll(vertx, context.asyncAssertSuccess(v2 -> {
						async.complete();
					}));
				}));
			}));
		}
	}

	@AfterClass
	public static void tearDown(TestContext context) {
		// Don't shut these things down at the end.
	}

}
