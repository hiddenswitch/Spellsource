package io.vertx.test.core;

import com.hiddenswitch.containers.RedisContainer;
import org.junit.ClassRule;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.DeletedObjectListener;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.CountDownLatch;

public class RedisKeyspaceEventsConfigurationTest {
	@ClassRule
	public static RedisContainer redisContainer = new RedisContainer();

	@Test(timeout = 8000L)
	public void testKeyspaceEvents() throws InterruptedException {
		Config config = new Config();
		config.useSingleServer().setAddress(redisContainer.getRedisUrl());
		RedissonClient redisson = Redisson.create(config);
		RBucket<String> bucket = redisson.getBucket("bucket");
		CountDownLatch latch = new CountDownLatch(1);
		int listener = bucket.addListener((DeletedObjectListener) name -> latch.countDown());
		bucket.set("test");
		bucket.delete();
		latch.await();
	}
}
