package com.hiddenswitch.containers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;

public class MongoDBContainer extends GenericContainer<MongoDBContainer> {
	private static final Logger log = LoggerFactory.getLogger(MongoDBContainer.class);
	private static final int CONTAINER_EXIT_CODE_OK = 0;
	private static final int MONGODB_INTERNAL_PORT = 27017;
	private static final int AWAIT_INIT_REPLICA_SET_ATTEMPTS = 60;
	private static final String MONGODB_VERSION_DEFAULT = "3.6";
	private static final String MONGODB_DATABASE_NAME_DEFAULT = "metastone";
	private String databaseName = MONGODB_DATABASE_NAME_DEFAULT;
	private boolean replSet = false;

	public MongoDBContainer() {
		this("mongo:" + MONGODB_VERSION_DEFAULT);
	}

	public MongoDBContainer(@NotNull String dockerImageName) {
		super(dockerImageName);
		this.withExposedPorts(27017);
		waitingFor(Wait.forLogMessage(".*waiting for connections on port.*", 1));
	}

	public String getReplicaSetUrl() {
		if (!isRunning()) {
			throw new IllegalStateException("MongoDBContainer should be started first");
		}
		return String.format(
				"mongodb://%s:%d/%s",
				getContainerIpAddress(),
				getMappedPort(MONGODB_INTERNAL_PORT),
				getDatabaseName()
		);
	}

	protected void containerIsStarted(InspectContainerResponse containerInfo) {
		if (isReplSet()) {
			this.initReplicaSet();
		}
	}

	private String[] buildMongoEvalCommand(String command) {
		return new String[]{"mongo", "--eval", command};
	}

	private void checkMongoNodeExitCode(ExecResult execResult) {
		if (execResult.getExitCode() != CONTAINER_EXIT_CODE_OK) {
			String errorMessage = String.format("An error occurred: %s", execResult.getStdout());
			log.error(errorMessage);
			throw new ReplicaSetInitializationException(errorMessage);
		}
	}

	private String buildMongoWaitCommand() {
		return String.format("var attempt = 0; while(%s) { if (attempt > %d) {quit(1);} print('%s ' + attempt); sleep(1000);  attempt++;  }", "db.runCommand( { isMaster: 1 } ).ismaster==false", AWAIT_INIT_REPLICA_SET_ATTEMPTS, "An attempt to await for a single node replica set initialization:");
	}

	private void checkMongoNodeExitCodeAfterWaiting(ExecResult execResultWaitForMaster) {
		if (execResultWaitForMaster.getExitCode() != CONTAINER_EXIT_CODE_OK) {
			String errorMessage = String.format("A single node replica set was not initialized in a set timeout: %d attempts", AWAIT_INIT_REPLICA_SET_ATTEMPTS);
			log.error(errorMessage);
			throw new ReplicaSetInitializationException(errorMessage);
		}
	}


	private void initReplicaSet() {
		log.debug("Initializing a single node node replica set...");
		final ExecResult execResultInitRs;
		try {
			execResultInitRs = execInContainer(
					buildMongoEvalCommand("rs.initiate();")
			);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		log.debug(execResultInitRs.getStdout());
		checkMongoNodeExitCode(execResultInitRs);

		log.debug(
				"Awaiting for a single node replica set initialization up to {} attempts",
				AWAIT_INIT_REPLICA_SET_ATTEMPTS
		);
		final ExecResult execResultWaitForMaster;
		try {
			execResultWaitForMaster = execInContainer(
					buildMongoEvalCommand(buildMongoWaitCommand())
			);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		log.debug(execResultWaitForMaster.getStdout());

		checkMongoNodeExitCodeAfterWaiting(execResultWaitForMaster);
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public static class ReplicaSetInitializationException extends RuntimeException {
		ReplicaSetInitializationException(String errorMessage) {
			super(errorMessage);
		}
	}

	public boolean isReplSet() {
		return replSet;
	}

	public void setReplSet(boolean replSet) {
		this.replSet = replSet;
		if (replSet) {
			this.withCommand("--replSet", "docker-rs");
		} else {
			this.withCommand();
		}
	}
}
