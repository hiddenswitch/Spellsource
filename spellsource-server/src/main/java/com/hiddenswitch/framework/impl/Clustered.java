package com.hiddenswitch.framework.impl;

import io.vertx.core.VertxOptions;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import org.infinispan.commons.util.FileLookup;
import org.infinispan.commons.util.FileLookupFactory;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public interface Clustered {
	Logger log = LoggerFactory.getLogger(Clustered.class);
	String VERTX_INFINISPAN_CONFIG_PROP_NAME = "vertx.infinispan.config";
	String INFINISPAN_XML = "infinispan.xml";
	String DEFAULT_INFINISPAN_XML = "default-infinispan.xml";
	String VERTX_JGROUPS_CONFIG_PROP_NAME = "vertx.jgroups.config";
	String JGROUPS_XML = "jgroups.xml";

	String JGROUPS_BIND_ADDRESS = "jgroups.bind.address";
	String JGROUPS_BIND_PORT = "jgroups.bind.port";

	static VertxOptions clusteredOptions(VertxOptions other) {
		var newOptions = new VertxOptions(other);

		return newOptions;
	}

	static DefaultCacheManager infinispanClusterManagerUdp() {
		return infinispanClusterManagerUdp(0);
	}

	static DefaultCacheManager infinispanClusterManagerUdp(int port) {
		return infinispanClusterManager("default-configs/default-jgroups-udp.xml", "GLOBAL", port);
	}

	static DefaultCacheManager infinispanClusterManagerKubernetes(int port) {
		return infinispanClusterManager("default-configs/default-jgroups-kubernetes.xml", "GLOBAL", port);
	}

	static DefaultCacheManager infinispanClusterManagerKubernetes() {
		return infinispanClusterManagerKubernetes(7800);
	}

	static DefaultCacheManager infinispanClusterManager(String configPath, String bindAddress, int port) {
		var ctccl = Thread.currentThread().getContextClassLoader();
		var fileLookup = FileLookupFactory.newInstance();

		var ispnConfig = fileLookup.lookupFileLocation(INFINISPAN_XML, ctccl);
		if (ispnConfig == null) {
			log.warn("Cannot find Infinispan config '" + INFINISPAN_XML + "', using default");
			ispnConfig = fileLookup.lookupFileLocation(DEFAULT_INFINISPAN_XML, ctccl);
		}
		var properties = new Properties();
		properties.put(JGROUPS_BIND_ADDRESS, bindAddress);
		properties.put(JGROUPS_BIND_PORT, port);

		ConfigurationBuilderHolder builderHolder = null;
		try {
			builderHolder = new ParserRegistry(ctccl, false, properties).parse(ispnConfig);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Workaround Launcher in fatjar issue (context classloader may be null)
		var classLoader = ctccl;
		if (classLoader == null) {
			classLoader = InfinispanClusterManager.class.getClassLoader();
		}
		builderHolder.getGlobalConfigurationBuilder().classLoader(classLoader);

		if (fileLookup.lookupFileLocation(configPath, ctccl) != null) {
			log.warn("Forcing JGroups config to '" + configPath + "'");
			builderHolder.getGlobalConfigurationBuilder().transport().defaultTransport().removeProperty(JGroupsTransport.CHANNEL_CONFIGURATOR).addProperty(JGroupsTransport.CONFIGURATION_FILE, configPath);
		}
		builderHolder.getDefaultConfigurationBuilder().memory().storage(StorageType.OFF_HEAP).maxSize("2GB");

		return new DefaultCacheManager(builderHolder, true);
	}
}
