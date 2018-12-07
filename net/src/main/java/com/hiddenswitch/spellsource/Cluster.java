package com.hiddenswitch.spellsource;

import com.hazelcast.config.*;
import org.bitsofinfo.hazelcast.discovery.consul.ConsulDiscoveryConfiguration;
import org.bitsofinfo.hazelcast.discovery.consul.ConsulDiscoveryStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Manages the Hazelcast-based clustering and in-memory state management of Spellsource game servers
 */
public interface Cluster {
	Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

	static Config getTcpDiscoverabilityConfig(int... ports) {
		Config config = new Config();
		config.setNetworkConfig(new NetworkConfig()
				.setPort(ports[0])
				.setJoin(new JoinConfig()
						.setMulticastConfig(new MulticastConfig()
								.setEnabled(false))
						.setTcpIpConfig(new TcpIpConfig()
								.setEnabled(true)
								.setMembers(IntStream.of(ports).mapToObj((int port) -> String.format("localhost:%d", port)).collect(toList())))));
		appendVertxConfig(config);
		return config;
	}

	static Config getDiscoverySPIConfig(String region) {
		Config config = new Config();
		if (!System.getenv().getOrDefault("HAZELCAST_MANCENTER_URL", "").isEmpty()) {
			String mancenterUrl = System.getenv().get("HAZELCAST_MANCENTER_URL");
			config.setManagementCenterConfig(new ManagementCenterConfig()
					.setEnabled(true)
					.setUrl(mancenterUrl));
		}
		DiscoveryConfig discoveryConfig = new DiscoveryConfig();
		NetworkConfig networkConfig = new NetworkConfig()
				.setOutboundPortDefinitions(Collections.singletonList(System.getProperty("outbound.ephemeral.ports", "33000-35000")));
		int hazelcastClusterPort = Integer.parseInt(System.getenv().getOrDefault("HAZELCAST_PORT", "5701"));
		networkConfig.setPort(hazelcastClusterPort);

		if (!System.getenv().getOrDefault("HAZELCAST_URLS", "").isEmpty()) {
			List<String> urls = Arrays.asList(System.getenv("HAZELCAST_URLS").split(","));

			int port;
			try {
				port = Integer.parseInt(urls.get(0).split(":")[1]);
			} catch (Throwable any) {
				port = hazelcastClusterPort;
			}

			networkConfig
					.setJoin(new JoinConfig()
							.setMulticastConfig(new MulticastConfig().setEnabled(false))
							.setTcpIpConfig(new TcpIpConfig()
									.setEnabled(true)
									.setMembers(urls)));
		} else if (System.getenv().containsKey("CONSUL_URL")) {
			URL consulUrl;
			try {
				consulUrl = new URL(System.getenv("CONSUL_URL"));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}

			DiscoveryStrategyConfig consul = new DiscoveryStrategyConfig(new ConsulDiscoveryStrategyFactory());
			consul.addProperty(ConsulDiscoveryConfiguration.CONSUL_HOST.key(), consulUrl.getHost());
			consul.addProperty(ConsulDiscoveryConfiguration.CONSUL_PORT.key(), Integer.toString(consulUrl.getPort()));
			consul.addProperty(ConsulDiscoveryConfiguration.CONSUL_SERVICE_NAME.key(), "spellsource-hazelcast");
			consul.addProperty(ConsulDiscoveryConfiguration.CONSUL_SSL_ENABLED.key(), "false");
			consul.addProperty(ConsulDiscoveryConfiguration.CONSUL_DISCOVERY_DELAY_MS.key(), "4000");
			consul.addProperty(ConsulDiscoveryConfiguration.CONSUL_SERVICE_TAGS.key(), "spellsource");
			consul.addProperty(ConsulDiscoveryConfiguration.CONSUL_HEALTHY_ONLY.key(), "true");
			consul.addProperty(ConsulDiscoveryConfiguration.CONSUL_REGISTRATOR.key(), "org.bitsofinfo.hazelcast.discovery.consul.LocalDiscoveryNodeRegistrator");
			consul.addProperty(ConsulDiscoveryConfiguration.CONSUL_REGISTRATOR_CONFIG.key(), "{\"preferPublicAddress\":false,\"healthCheckProvider\":\"org.bitsofinfo.hazelcast.discovery.consul.TcpHealthCheckBuilder\",\"healthCheckTcp\":\"#MYIP:#MYPORT\",\"healthCheckTcpIntervalSeconds\":10}");
			discoveryConfig.addDiscoveryStrategyConfig(consul);

			networkConfig.setJoin(new JoinConfig()
					.setTcpIpConfig(new TcpIpConfig()
							.setEnabled(false))
					.setMulticastConfig(new MulticastConfig()
							.setEnabled(false))
					.setDiscoveryConfig(discoveryConfig));
		} else {
			// From https://github.com/hazelcast/hazelcast-aws
			// Although this probably hasn't been working
			DiscoveryStrategyConfig awsDiscoveryStrategy = new DiscoveryStrategyConfig("com.hazelcast.aws.AwsDiscoveryStrategy");
			awsDiscoveryStrategy.addProperty("region", region);
			awsDiscoveryStrategy.addProperty("host-header", "ec2.amazonaws.com");
			discoveryConfig.addDiscoveryStrategyConfig(awsDiscoveryStrategy);

			networkConfig
					.setJoin(new JoinConfig()
							.setTcpIpConfig(new TcpIpConfig()
									.setEnabled(false))
							.setMulticastConfig(new MulticastConfig()
									.setEnabled(false))
							.setDiscoveryConfig(discoveryConfig));
		}

		config.setNetworkConfig(networkConfig);
		appendVertxConfig(config);
		return config;
	}

	@SuppressWarnings("deprecation")
	static void appendVertxConfig(Config config) {
		final Properties properties = new Properties();
		properties.setProperty("hazelcast.shutdownhook.enabled", "false");
		properties.setProperty("hazelcast.logging.type", "slf4j");
		config.setProperties(properties)
				.addMultiMapConfig(new MultiMapConfig()
						.setBackupCount(1)
						.setName("__vertx.subs"))
				.addMapConfig(new MapConfig()
						.setName("__vertx.haInfo")
						.setTimeToLiveSeconds(0)
						.setMaxIdleSeconds(0)
						.setEvictionPolicy(EvictionPolicy.NONE)
						.setMaxSizeConfig(new MaxSizeConfig().setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.PER_NODE).setSize(0))
						.setEvictionPercentage(25)
						.setMergePolicy("com.hazelcast.map.merge.LatestUpdateMapMergePolicy"))
				.addSemaphoreConfig(new SemaphoreConfig()
						.setName("__vertx.*")
						.setInitialPermits(1));
	}
}
