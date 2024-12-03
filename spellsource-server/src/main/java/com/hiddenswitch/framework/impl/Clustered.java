package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Environment;
import org.infinispan.commons.util.FileLookupFactory;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public interface Clustered {
	Logger LOGGER = LoggerFactory.getLogger(Clustered.class);
	String VERTX_INFINISPAN_CONFIG_PROP_NAME = "vertx.infinispan.config";
	String INFINISPAN_XML = "infinispan.xml";
	String DEFAULT_INFINISPAN_XML = "default-infinispan.xml";
	String VERTX_JGROUPS_CONFIG_PROP_NAME = "vertx.jgroups.config";
	String JGROUPS_XML = "jgroups.xml";

	String JGROUPS_BIND_ADDRESS = "jgroups.bind.address";
	String JGROUPS_BIND_PORT = "jgroups.bind.port";
	String JGROUPS_MULTICAST_ADDRESS = "jgroups.mcast_addr";

	static DefaultCacheManager infinispanClusterManagerUdp() {
		return infinispanClusterManagerUdp(0);
	}

	static DefaultCacheManager infinispanClusterManagerUdp(int port) {
		return infinispanClusterManager("udp", Environment.getHostIpAddress(), port);
	}

	static DefaultCacheManager infinispanClusterManagerTcp(int port) {
		return infinispanClusterManager("tcp", Environment.getHostIpAddress(), port);
	}

	static DefaultCacheManager infinispanClusterManagerKubernetes(int port) {
		return infinispanClusterManager("kubernetes", "GLOBAL", port);
	}

	static DefaultCacheManager infinispanClusterManagerKubernetes() {
		return infinispanClusterManagerKubernetes(7800);
	}

	private static ClassLoader getCTCCL() {
		return Thread.currentThread().getContextClassLoader();
	}

	static DefaultCacheManager infinispanClusterManager(String stack, String bindAddress, int port) {
		var ispnConfigPath = System.getProperty(VERTX_JGROUPS_CONFIG_PROP_NAME, INFINISPAN_XML);

		var properties = new Properties();
		properties.putAll(System.getProperties());
		properties.put(JGROUPS_BIND_ADDRESS, bindAddress);
		properties.put(JGROUPS_BIND_PORT, port);


		var classLoader = getCTCCL();
		var fileLookup = FileLookupFactory.newInstance();

		var ispnConfig = fileLookup.lookupFileLocation(ispnConfigPath, getCTCCL());
		if (ispnConfig == null) {
			LOGGER.warn("Cannot find Infinispan config '" + ispnConfigPath + "', using default");
			ispnConfig = fileLookup.lookupFileLocation(DEFAULT_INFINISPAN_XML, getCTCCL());
		}
		ConfigurationBuilderHolder builderHolder = null;
		var parser = new ParserRegistry(classLoader, false, properties);
		try {
			builderHolder = parser.parse(ispnConfig);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		builderHolder.getGlobalConfigurationBuilder().transport().stack(stack);
		// improve reliability for large games
		builderHolder.getDefaultConfigurationBuilder().memory().storage(StorageType.OFF_HEAP).maxSize("2GB");

		return new DefaultCacheManager(builderHolder, true);
	}
}
