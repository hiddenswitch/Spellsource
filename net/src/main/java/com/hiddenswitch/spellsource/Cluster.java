package com.hiddenswitch.spellsource;

import com.hazelcast.config.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

public interface Cluster {
	static Config getConfig(int port) {
		Config config = new Config();
		config.setNetworkConfig(new NetworkConfig()
				.setPort(port)
				.setJoin(new JoinConfig()
						.setMulticastConfig(new MulticastConfig()
								.setEnabled(false))
						.setTcpIpConfig(new TcpIpConfig()
								.setEnabled(true)
								.setMembers(Arrays.asList("localhost:5701", "localhost:5702")))));
		appendVertxConfig(config);
		return config;
	}

	static Config getAwsConfig(String region) {
		Config config = new Config();
		// From https://github.com/hazelcast/hazelcast-aws
		final DiscoveryConfig discoveryConfig = new DiscoveryConfig();
		final DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig("com.hazelcast.aws.AwsDiscoveryStrategy");
		discoveryConfig.addDiscoveryStrategyConfig(discoveryStrategyConfig);
		discoveryStrategyConfig.addProperty("region", region);
		discoveryStrategyConfig.addProperty("host-header", "ec2.amazonaws.com");


		config.setNetworkConfig(new NetworkConfig()
				.setOutboundPortDefinitions(Collections.singletonList(System.getProperty("outbound.ephemeral.ports", "33000-35000")))
				.setJoin(new JoinConfig()
						.setTcpIpConfig(new TcpIpConfig()
								.setEnabled(false))
						.setMulticastConfig(new MulticastConfig()
								.setEnabled(false))
						.setDiscoveryConfig(discoveryConfig)));
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
