package com.hiddenswitch.spellsource;

import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.core.AtomixBuilder;
import io.atomix.core.profile.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the Atomix-based clustering and in-memory state management of Spellsource game servers
 */
public interface Cluster {
	Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

	static Atomix create(int port, Node... bootstrapNodes) {
		AtomixBuilder atomixBuilder = Atomix.builder()
				.withHost("0.0.0.0")
				.withPort(port);
		if (bootstrapNodes.length == 0) {
			atomixBuilder
					.withProfiles(Profile.dataGrid(1));
		} else {
			atomixBuilder
					.withMembershipProvider(BootstrapDiscoveryProvider
							.builder()
							.withNodes(bootstrapNodes).build());
		}
		return atomixBuilder
				.build();
	}
}
