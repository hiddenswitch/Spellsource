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

Redis should be configured to enable keyspace events. In the CLI, use:

```shell script
redis-cli CONFIG SET notify-keyspace-events Exg
```

This is optional.

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

	@Override
	protected void containerIsStarted(InspectContainerResponse containerInfo) {
		super.containerIsStarted(containerInfo);
		try {
			ExecResult res = execInContainer("redis-cli", "CONFIG", "SET", "notify-keyspace-events", "Exg");
			if (!res.getStdout().contains("OK")) {
				throw new AssertionError(res.getStdout());
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
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