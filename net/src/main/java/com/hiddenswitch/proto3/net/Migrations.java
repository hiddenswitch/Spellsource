package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.MigrateToRequest;
import com.hiddenswitch.proto3.net.models.MigrationRequest;
import com.hiddenswitch.proto3.net.models.MigrationResponse;
import com.hiddenswitch.proto3.net.models.MigrationToResponse;

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
	MigrationToResponse migrateTo(MigrateToRequest request) throws SuspendExecution, InterruptedException;

	Serializable forceUnlock(Serializable ignoredRequest) throws InterruptedException, SuspendExecution;
}
