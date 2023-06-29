package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.Accounts;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;
import java.util.UUID;

public class V5__Create_package_cards_owner extends BaseJavaMigration {
	@Override
	public Integer getChecksum() {
		return 1;
	}

	@Override
	public void migrate(Context context) throws Exception {
		var userEntity = Accounts.createUser("noreply@hiddenswitch.com", "Spellsource", UUID.randomUUID().toString()).toCompletionStage().toCompletableFuture().join();
		var realm = Accounts.realm().toCompletionStage().toCompletableFuture().join();
		var userRepresentation = new UserRepresentation();
		userRepresentation.setEnabled(false);
		realm.users().get(userEntity.getId()).disableCredentialType(Collections.singletonList(CredentialRepresentation.PASSWORD));
		realm.users().get(userEntity.getId()).update(userRepresentation);
	}
}
