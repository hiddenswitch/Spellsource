package com.hiddenswitch.spellsource.trainer;

import com.google.gson.Gson;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Redis-backed work queue for distributed evaluation.
 * Coordinator pushes EvalRequests; workers pop them, run games, push results.
 */
public class RedisQueue implements AutoCloseable {
	private static final Logger LOG = Logger.getLogger(RedisQueue.class.getName());
	private static final String REQUEST_QUEUE = "eval:requests";
	private static final String RESULT_PREFIX = "eval:results:";
	private static final String DONE_KEY = "training:done";
	private static final int RESULT_TTL_SECONDS = 3600;

	private final JedisPool pool;
	private final Gson gson = new Gson();

	public RedisQueue(String redisUri) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(16);
		config.setMaxIdle(8);
		this.pool = new JedisPool(config, URI.create(redisUri));
		LOG.info("Connected to Redis at " + redisUri);
	}

	// --- Coordinator side ---

	/**
	 * Pushes an evaluation request to the work queue.
	 */
	public void pushRequest(EvalRequest request) {
		try (var jedis = pool.getResource()) {
			jedis.lpush(REQUEST_QUEUE, gson.toJson(request));
		}
	}

	/**
	 * Waits for a result with the given ID, polling with backoff.
	 */
	public EvalResult waitForResult(String id, Duration timeout) {
		long deadline = System.currentTimeMillis() + timeout.toMillis();
		String key = RESULT_PREFIX + id;
		long sleepMs = 100;

		while (System.currentTimeMillis() < deadline) {
			try (var jedis = pool.getResource()) {
				String json = jedis.get(key);
				if (json != null) {
					jedis.del(key);
					return gson.fromJson(json, EvalResult.class);
				}
			}
			try {
				Thread.sleep(sleepMs);
				sleepMs = Math.min(sleepMs * 2, 5000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Interrupted waiting for result " + id, e);
			}
		}
		throw new RuntimeException("Timeout waiting for result " + id);
	}

	/**
	 * Signals that training is complete.
	 */
	public void signalDone() {
		try (var jedis = pool.getResource()) {
			jedis.set(DONE_KEY, "true");
			LOG.info("Signaled training complete");
		}
	}

	/**
	 * Clears all training keys (for fresh start).
	 */
	public void clear() {
		try (var jedis = pool.getResource()) {
			jedis.del(REQUEST_QUEUE);
			jedis.del(DONE_KEY);
			// Results expire naturally via TTL
		}
	}

	// --- Worker side ---

	/**
	 * Pops an evaluation request from the work queue (blocking).
	 */
	public Optional<EvalRequest> popRequest(Duration timeout) {
		try (var jedis = pool.getResource()) {
			List<String> result = jedis.brpop((int) timeout.getSeconds(), REQUEST_QUEUE);
			if (result == null || result.size() < 2) {
				return Optional.empty();
			}
			return Optional.of(gson.fromJson(result.get(1), EvalRequest.class));
		}
	}

	/**
	 * Pushes an evaluation result.
	 */
	public void pushResult(EvalResult result) {
		String key = RESULT_PREFIX + result.id();
		try (var jedis = pool.getResource()) {
			jedis.setex(key, RESULT_TTL_SECONDS, gson.toJson(result));
		}
	}

	/**
	 * Checks if training is done.
	 */
	public boolean isDone() {
		try (var jedis = pool.getResource()) {
			return jedis.exists(DONE_KEY);
		}
	}

	@Override
	public void close() {
		pool.close();
	}
}
