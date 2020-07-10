package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.amazonaws.util.Throwables;
import com.hiddenswitch.spellsource.net.Migrations;
import com.hiddenswitch.spellsource.net.models.MigrateToRequest;
import com.hiddenswitch.spellsource.net.models.MigrationRequest;
import com.hiddenswitch.spellsource.net.models.MigrationResponse;
import com.hiddenswitch.spellsource.net.models.MigrationToResponse;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.mongo.WriteOption;
import io.vertx.ext.sync.SyncVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;

public class MigrationsImpl extends SyncVerticle implements Migrations {
	public static final String MIGRATIONS = "migrations";
	private List<MigrationRequest> migrations = new ArrayList<>();
	private static Logger logger = LoggerFactory.getLogger(Migrations.class);

	public MigrationsImpl() {
		super();

		// There is always a version zero
		migrations.add(new MigrationRequest()
				.withVersion(0)
				.withUp(thisVertx -> {
				})
				.withDown(thisVertx -> {
				}));
	}

	@Override
	@Suspendable
	protected void syncStart() throws SuspendExecution {
		// Automatically migrate if we have a version specified in an environment value
		if (System.getenv().containsKey("MIGRATE")) {
			try {
				int version = Integer.parseInt(System.getenv("MIGRATE"));
				migrateTo(new MigrateToRequest().withVersion(version));
			} catch (NumberFormatException e1) {
				logger.error("Invalid migration version specified: " + System.getenv("MIGRATE"));
			} catch (InterruptedException ignored) {
			} catch (Exception e2) {
				logger.error("Failed to migrate! Deployment will fail.");
				throw e2;
			}
		}
	}

	@Override
	protected void syncStop() throws SuspendExecution {
	}

	@Override
	public MigrationResponse add(MigrationRequest request) {
		migrations.add(request);
		Collections.sort(migrations, (m1, m2) -> m1.getVersion() - m2.getVersion());
		return new MigrationResponse();
	}

	@Override
	@Suspendable
	public MigrationToResponse migrateTo(MigrateToRequest request) throws SuspendExecution, RuntimeException, InterruptedException {
		int version = 0;
		if (null != request.getLatest()
				&& request.getLatest()) {
			MigrationRequest highest = migrations.stream().max((m1, m2) -> m1.getVersion() - m2.getVersion()).orElseThrow(RuntimeException::new);
			version = highest.getVersion();
		} else if (null != request.getVersion()) {
			version = request.getVersion();
		}


		if (!lock()) {
			logger.warn("Not migrating, control is locked (there may be another migration in progress).");
			return MigrationToResponse.succeededMigration();
		}

		var controlDoc = mongo().findOne(MIGRATIONS, json("_id", "control"), json("_id", 1, "version", 1, "locked", 1));
		var currentVersion = controlDoc.getInteger("version");

		if (null != request.getRerun()
				&& request.getRerun()) {
			logger.info("Rerunning version " + Integer.toString(version));
			migrateUp(findIndexByVersion(version));
			logger.info("Finished migrating.");
			unlock();
			return MigrationToResponse.succeededMigration();
		}

		if (currentVersion == version) {
			logger.info("Not migrating, already at version " + Integer.toString(version));
			unlock();
			return MigrationToResponse.succeededMigration();
		}

		int startIndex = findIndexByVersion(currentVersion);
		int endIndex = findIndexByVersion(version);

		logger.info(String.format("Migrating from %d to %d...", startIndex, endIndex));

		if (endIndex > startIndex) {
			try {
				for (int i = startIndex; i < endIndex; i++) {
					migrateUp(i + 1);
					currentVersion = migrations.get(i + 1).getVersion();
				}
			} catch (Throwable e) {
				return MigrationToResponse.failedMigration(e);
			}
		} else {
			try {
				for (int i = startIndex; i > endIndex; i--) {
					migrateDown(i);
					currentVersion = migrations.get(i - 1).getVersion();
				}
			} catch (Throwable e) {
				return MigrationToResponse.failedMigration(e);
			}
		}

		// currentVersion was updated
		unlock(currentVersion);
		return MigrationToResponse.succeededMigration();
	}

	/**
	 * Forces the migration to be unlocked.
	 *
	 * @param ignoredRequest An ignored request parameter.
	 * @return An empty response when the unlock is successful.
	 */
	public Serializable forceUnlock(Serializable ignoredRequest) throws InterruptedException, SuspendExecution {
		unlock();
		return null;
	}

	private void migrateUp(int indexByVersion) throws SuspendExecution, InterruptedException {
		migrations.get(indexByVersion).getUp().call(vertx);
	}

	private void migrateDown(int indexByVersion) throws SuspendExecution, InterruptedException {
		migrations.get(indexByVersion).getDown().call(vertx);
	}

	private int findIndexByVersion(int version) {
		return IntStream.range(0, migrations.size())
				.filter(i -> migrations.get(i).getVersion() == version)
				.findFirst().orElseThrow(RuntimeException::new);
	}

	private boolean lock() throws SuspendExecution, InterruptedException {
		try {
			MongoClientUpdateResult res = awaitResult(h -> mongo().client().updateCollectionWithOptions(MIGRATIONS, json("_id", "control", "locked", false), json(
					"$set", json("locked", true, "lockedAt", Instant.now()),
					"$setOnInsert", json("version", 0)),
					new UpdateOptions()
							.setUpsert(true)
							.setWriteOption(WriteOption.FSYNCED), h));
			return res.getDocModified() == 1 || !res.getDocUpsertedId().isEmpty();
		} catch (Throwable ex) {
			var cause = Throwables.getRootCause(ex);
			if (ex instanceof MongoWriteException) {
				if (((MongoWriteException) ex).getCode() == 11000) {
					// we tried to insert because we could not find a doc that was unlocked, id already exists, so its locked
					return false;
				}
			}

			throw ex;
		}
	}


	private void unlock(int version) throws SuspendExecution, InterruptedException {
		mongo().updateCollectionWithOptions(MIGRATIONS, json("_id", "control", "locked", true, "version", version),
				json("$set", json("locked", false)),
				new UpdateOptions()
						.setWriteOption(WriteOption.FSYNCED));
	}

	private void unlock() throws SuspendExecution, InterruptedException {
		mongo().updateCollectionWithOptions(MIGRATIONS, json("_id", "control", "locked", true),
				json("$set", json("locked", false)),
				new UpdateOptions()
						.setWriteOption(WriteOption.FSYNCED));
	}
}
