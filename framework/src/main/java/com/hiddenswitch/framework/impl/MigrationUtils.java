package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Accounts;

public class MigrationUtils {
	public static String getSpellsourceUserId() {
		var realm = Accounts.get().toCompletionStage().toCompletableFuture().join();
		var ownerUserId = realm.users().search("Spellsource", true).stream().findFirst().get().getId();
		return ownerUserId;
	}
}
