package com.hiddenswitch.containers;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;

import java.util.Optional;

/**
 * A docker network with a fixed name that is created on demand and never deleted.
 * <p>
 * {@link Network#SHARED} creates a new, Ryuk-managed network per JVM, so its id changes every run. Containers that opt
 * into testcontainers reuse include the network id in their reuse hash, which means a per-JVM network defeats reuse.
 * Attaching reused containers to this network instead keeps the hash stable across JVM runs, and lets non-reused
 * containers (like the GraphQL dev server) find reused ones by network alias.
 */
public class PersistentNetwork implements Network {
	private final String name;
	private volatile String id;

	public PersistentNetwork(String name) {
		this.name = name;
	}

	@Override
	public String getId() {
		if (id == null) {
			synchronized (this) {
				if (id == null) {
					Optional<String> existing = find();
					if (existing.isPresent()) {
						id = existing.get();
					} else {
						try {
							id = DockerClientFactory.instance().client().createNetworkCmd().withName(name).exec().getId();
						} catch (RuntimeException e) {
							// lost a creation race with another process
							id = find().orElseThrow(() -> e);
						}
					}
				}
			}
		}
		return id;
	}

	private Optional<String> find() {
		return DockerClientFactory.instance().client().listNetworksCmd().withNameFilter(name).exec().stream()
				.filter(network -> name.equals(network.getName()))
				.map(com.github.dockerjava.api.model.Network::getId)
				.findFirst();
	}

	@Override
	public void close() {
		// persistent by design, remove with docker network rm
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return base;
	}
}
