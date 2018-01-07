package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Migrations;
import com.hiddenswitch.spellsource.models.MigrateToRequest;
import com.hiddenswitch.spellsource.models.MigrationRequest;
import com.hiddenswitch.spellsource.models.MigrationResponse;
import com.hiddenswitch.spellsource.models.MigrationToResponse;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.Registration;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.mongo.WriteOption;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.hiddenswitch.spellsource.util.QuickJson.json;

public class MigrationsImpl extends AbstractService<MigrationsImpl> implements Migrations {
	public static final String MIGRATIONS = "migrations";
	private List<MigrationRequest> migrations = new ArrayList<>();
	private static Logger logger = LoggerFactory.getLogger(Migrations.class);
	private Registration registration;

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
	public void start() throws SuspendExecution {
		super.start();

		// Automatically migrate if we have a version specified in an environment value
		if (System.getenv().containsKey("MIGRATE")) {
			try {
				int version = Integer.parseInt(System.getenv("MIGRATE"));
				migrateTo(new MigrateToRequest().withVersion(version));
			} catch (NumberFormatException e1) {
				logger.error("Invalid migration version specified: " + System.getenv("MIGRATE"));
			} catch (InterruptedException ignored) {
			} catch (Exception e2) {
				logger.fatal("Failed to migrate! Deployment will fail.");
				throw e2;
			}
		}


		registration = Rpc.register(this, Migrations.class, vertx.eventBus());
	}

	@Override
	public void stop() throws Exception {
		super.stop();

		if (registration != null) {
			Rpc.unregister(registration);
		}
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

		JsonObject control = getControl();
		int currentVersion = control.getInteger("version");
		if (!lock()) {
			logger.fatal("Not migrating, control is locked.");
			throw new RuntimeException();
		}

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
				throw new RuntimeException(e);
			}
		} else {
			try {
				for (int i = startIndex; i > endIndex; i--) {
					migrateDown(i);
					currentVersion = migrations.get(i - 1).getVersion();
				}
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

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
		return Mongo.mongo().updateCollection(MIGRATIONS, json("_id", "control", "locked", false), json("$set",
				json("locked", true, "lockedAt", Instant.now()))).getDocModified() == 1;
	}


	private JsonObject unlock(int version) throws SuspendExecution, InterruptedException {
		return setControl(json("locked", false, "version", version));
	}

	private void unlock() throws SuspendExecution, InterruptedException {
		Mongo.mongo().updateCollection(MIGRATIONS, json("_id", "control"), json("$set", json("locked", false)));
	}

	private JsonObject setControl(JsonObject controlDocument) throws SuspendExecution, InterruptedException {
		Mongo.mongo().updateCollectionWithOptions(MIGRATIONS, json("_id", "control"), json("$set", json("version", controlDocument.getInteger("version"), "locked", controlDocument.getBoolean("locked"))),
				new UpdateOptions()
						.setUpsert(true)
						.setWriteOption(WriteOption.MAJORITY));
		return controlDocument;
	}

	private JsonObject getControl() throws InterruptedException, SuspendExecution {
		JsonObject control = Mongo.mongo().findOne(MIGRATIONS, json("_id", "control"), json());
		return control == null ? setControl(json("version", 0, "locked", false)) : control;
	}
}
