package com.hiddenswitch.spellsource.net;

import com.google.common.collect.Streams;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.core.AtomixBuilder;
import io.atomix.core.profile.ConsensusProfile;
import io.atomix.core.profile.ConsensusProfileBuilder;
import io.atomix.core.profile.ConsensusProfileConfig;
import io.atomix.core.profile.Profile;
import io.atomix.primitive.partition.ManagedPartitionGroup;
import io.atomix.primitive.partition.MemberGroupStrategy;
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Manages the Atomix-based clustering and in-memory state management of Spellsource game servers
 */
public interface Cluster {
	Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

	static Atomix create(int port, Node... bootstrapNodes) {
		var hostIpAddress = Gateway.getHostIpAddress();
		var memberId = getMemberId(port, hostIpAddress);
		/*
		var path = "./.atomix/" + memberId;
		try {
			Files.createDirectories(Path.of(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}*/
		AtomixBuilder atomixBuilder = Atomix.builder()
				.withMemberId(memberId)
				.withHost("0.0.0.0")
				.withPort(port);
		/*
		var consensusProfileBuilder = ConsensusProfile.builder()
				.withDataPath("./.atomix/" + memberId);*/
		if (bootstrapNodes.length == 0) {
			atomixBuilder
					.withProfiles(Profile.dataGrid(1)/*, consensusProfileBuilder.withMembers(memberId).build()*/);
		} else {
			atomixBuilder
					.withManagementGroup(PrimaryBackupPartitionGroup.builder("spellsource-management-group")
							.withMemberGroupStrategy(MemberGroupStrategy.NODE_AWARE)
							.withNumPartitions(bootstrapNodes.length)
							.build())
					.withProfiles(Profile.dataGrid(bootstrapNodes.length))
					/*.withProfiles(Profile.dataGrid(bootstrapNodes.length), consensusProfileBuilder
							.withMembers(Streams.concat(Stream.of(memberId), Arrays
									.stream(bootstrapNodes)
									.map(Node::toString)).toArray(String[]::new))
							.build())*/
					.withMembershipProvider(BootstrapDiscoveryProvider
							.builder()
							.withNodes(bootstrapNodes).build());
		}
		return atomixBuilder
				.build();
	}

	static String getMemberId(int port, String hostIpAddress) {
		return String.format("spellsource-atomix-%s:%d", hostIpAddress, port);
	}

	static String getMemberId(String address) {
		return String.format("spellsource-atomix-%s:%d", address);
	}
}
