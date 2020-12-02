package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Accounts;

public class MigrationUtils {
	public static String getSpellsourceUserId() {
		var realm = Accounts.get().toCompletionStage().toCompletableFuture().join();
		// todo: is this an exact match? could be bad
		var ownerUserId = realm.users().search("Spellsource").stream().findFirst().get().getId();
		return ownerUserId;
	}
}
