package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.impl.MigratorImpl;
import com.hiddenswitch.spellsource.models.MigrateToRequest;
import com.hiddenswitch.spellsource.models.MigrationRequest;
import com.hiddenswitch.spellsource.models.MigrationResponse;
import com.hiddenswitch.spellsource.models.MigrationToResponse;
import com.hiddenswitch.spellsource.util.Migrator;
import com.hiddenswitch.spellsource.util.RpcOptions;
import io.vertx.core.Vertx;

import java.io.Serializable;

/**
 * The migrations service performs database migrations on Mongo, using a mongo document as a lock.
 */
public interface Migrations {
	/**
	 * Adds a given up and down function to a specific version.
	 *
	 * @param request A specification for a migration.
	 * @return An empty object indicating the migration was successfully registered.
	 */
	MigrationResponse add(MigrationRequest request);

	/**
	 * A migration request.
	 *
	 * @param request A version to migrate up or down to, or a request to migrate to the latest version.
	 * @return {@link MigrationToResponse#succeededMigration()} if the migration succeeded, otherwise {@link
	 * MigrationToResponse#failedMigration()} if it failed.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	@RpcOptions(sendTimeoutMS = 4 * 60 * 1000L)
	MigrationToResponse migrateTo(MigrateToRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Force the database to be unlocked. Very dangerous.
	 *
	 * @param ignoredRequest Ignored parameter.
	 * @return A void parameter.
	 * @throws InterruptedException
	 * @throws SuspendExecution
	 */
	Serializable forceUnlock(Serializable ignoredRequest) throws InterruptedException, SuspendExecution;

	/**
	 * Gets a {@link Migrator} that can be used to easily run a migration
	 *
	 * @param vertx
	 * @return
	 */
	static Migrator migrate(Vertx vertx) {
		return new MigratorImpl(vertx);
	}
}
