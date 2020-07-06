package com.hiddenswitch.spellsource.net;

import com.google.common.collect.Streams;
import io.atomix.cluster.Node;
import io.atomix.cluster.NodeId;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.cluster.protocol.SwimMembershipProtocol;
import io.atomix.core.Atomix;
import io.atomix.core.AtomixBuilder;
import io.atomix.core.profile.ConsensusProfile;
import io.atomix.core.profile.ConsensusProfileBuilder;
import io.atomix.core.profile.ConsensusProfileConfig;
import io.atomix.core.profile.Profile;
import io.atomix.primitive.partition.ManagedPartitionGroup;
import io.atomix.primitive.partition.MemberGroupStrategy;
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroup;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Manages the Atomix-based clustering and in-memory state management of Spellsource game servers
 */
public interface Cluster {
	Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

	static Atomix create(int port, Node... nodes) {
		var hostIpAddress = "localhost";
		var memberId = getMemberId(port, hostIpAddress);

		var path = "build/atomix/" + memberId.replace(":", "_");
		try {
			Files.createDirectories(Path.of(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		AtomixBuilder atomixBuilder = Atomix.builder()
				.withClusterId("spellsource")
				.withMemberId(memberId)
				.withHost(hostIpAddress)
				.withPort(port);

		var consensusProfileBuilder = ConsensusProfile.builder()
				.withDataPath(path);
		if (nodes.length == 0) {
			atomixBuilder
					.withProfiles(Profile.dataGrid(1)/*, consensusProfileBuilder.withMembers(memberId).build()*/);
		} else {
			var members = Arrays
					.stream(nodes)
					.map(Node::id)
					.map(NodeId::id).collect(toSet());
			LOGGER.info("create: members={}", members);
			atomixBuilder
					.withMembershipProtocol(SwimMembershipProtocol.builder()
							.withBroadcastDisputes(true)
							.withBroadcastUpdates(true)
							.withProbeInterval(Duration.ofMillis(100))
							.withNotifySuspect(true)
							.withFailureTimeout(Duration.ofSeconds(3))
							.build())
					.withMembershipProvider(new BootstrapDiscoveryProvider(nodes))
					.withManagementGroup(PrimaryBackupPartitionGroup.builder("system")
							.withNumPartitions(1)
							/*
							.withMembers(members)
							.withPartitionSize(nodes.length)
							.withDataDirectory(new File(path))*/
							.build())
					.withPartitionGroups(PrimaryBackupPartitionGroup.builder("spellsource-data")
							.withNumPartitions(3)
							/*
							.withPartitionSize(nodes.length)
							.withMembers(members)
							.withDataDirectory(new File(path + "/spellsource-data"))*/
							.build());
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
