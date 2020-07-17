# vertx-redis-cluster

A cluster manager for Vertx that uses Redisson (Redis clustering).

This passes all the Vertx cluster manager tests and is used in a real, production application. All features are supported. Cannot share a Redis keyspace with other vertx applications.

### Usage

Requires Vertx 3.9.2 but is compatible with any of the 3.6+ clustering SPI.

Create the cluster manager and add it to the options in your `Vertx.clusteredVertx` call.

```java
public class Application {
  public static void main(String[] args) {
    RedisClusterManager clusterManager = new RedisClusterManager("redis://redis:6379");
    Vertx.clusteredVertx(new VertxOptions().setClusterManager(clusterManager), vertx -> { /*...*/ });
  } 
}
```

Please fork if you need to downgrade your Vertx version. Note earlier versions of Vertx did not have as complete a cluster management test suite as the current Vertx.

### Testing

Use [Testcontainers](https://www.testcontainers.org). Here is a Redis test container:

```java
public class RedisContainer extends GenericContainer<RedisContainer> {

	private static final int REDIS_PORT = 6379;

	public RedisContainer() {
		super("redis:6.0.5");
		withExposedPorts(REDIS_PORT);
		waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));
	}

	public void clear() throws IOException, InterruptedException {
		execInContainer("redis-cli", "FLUSHALL");
	}

	public String getRedisUrl() {
		return "redis://" + getHost() + ":" + getMappedPort(6379);
	}
}
```

For JUnit4, add the following rules:

```java
public class TestClass {
	@ClassRule
	public static RedisContainer redisContainer = new RedisContainer();

	@Before
	public void before()  {
		redisContainer.clear();
	}
}
```