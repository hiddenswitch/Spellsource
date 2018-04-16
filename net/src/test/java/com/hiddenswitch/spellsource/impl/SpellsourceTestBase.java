package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hiddenswitch.spellsource.Accounts;
import com.hiddenswitch.spellsource.Cluster;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.models.CreateAccountRequest;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.util.Mongo;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import static io.vertx.core.json.Json.decodeValue;

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

	protected CreateAccountResponse createRandomAccount() throws SuspendExecution, InterruptedException {
		return Accounts.createAccountInner(new CreateAccountRequest().withEmailAddress("test-" + RandomStringUtils.randomAlphanumeric(32) + "@test.com")
						.withName("username" + RandomStringUtils.randomAlphanumeric(32)).withPassword("password"));
	}

	@AfterClass
	public static void tearDown(TestContext context) {
		// Don't shut these things down at the end.
	}

}
