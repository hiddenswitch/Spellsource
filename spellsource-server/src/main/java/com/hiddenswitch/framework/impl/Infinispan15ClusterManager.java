package com.hiddenswitch.framework.impl;

import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jgroups.stack.Protocol;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

public class Infinispan15ClusterManager extends InfinispanClusterManager {

	public Infinispan15ClusterManager(DefaultCacheManager cacheManager) {
		super(cacheManager);
	}

	@Override
	public List<String> getNodes() {
		return getCacheManager().getMembers().stream().map(Object::toString).collect(Collectors.toList());
	}

	public DefaultCacheManager getCacheManager() {
		return (DefaultCacheManager) this.getCacheContainer();
	}

	@Override
	public String clusterHost() {
		return this.getHostFromTransportProtocol("bind_addr");
	}

	@Override
	public String clusterPublicHost() {
		return this.getHostFromTransportProtocol("external_addr");
	}

	private String getHostFromTransportProtocol(String fieldName) {
		// Get the transport field using reflection
		Field transportField = null;
		try {
			transportField = getCacheManager().getClass().getDeclaredField("transport");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		transportField.setAccessible(true);

		// Get the transport instance
		JGroupsTransport transport = null;
		try {
			transport = (JGroupsTransport) transportField.get(getCacheManager());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		Protocol bottomProtocol = transport.getChannel().getProtocolStack().getBottomProtocol();

		try {
			InetAddress external_addr = (InetAddress) bottomProtocol.getValue(fieldName);
			String str = external_addr.toString();
			return str.charAt(0) == '/' ? str.substring(1) : str.substring(0, str.indexOf(47));
		} catch (Exception var6) {
			return null;
		}
	}
}
