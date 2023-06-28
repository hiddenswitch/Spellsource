package com.hiddenswitch.framework.impl;

import com.google.common.hash.Hashing;
import com.hiddenswitch.framework.Accounts;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;

import java.nio.charset.Charset;
import java.util.Comparator;

public class MigrationUtils {
	public static String getSpellsourceUserId() {
		var realm = Accounts.get().toCompletionStage().toCompletableFuture().join();
		// todo: is this an exact match? could be bad
		var ownerUserId = realm.users().search("Spellsource").stream().findFirst().get().getId();
		return ownerUserId;
	}

	public static int cardsChecksum() {
		var checksum = Hashing.crc32().newHasher();
        ClasspathCardCatalogue.INSTANCE.getRecords().values().stream()
				.sorted(Comparator.comparing(CardCatalogueRecord::getId))
				.map(CardCatalogueRecord::getDesc)
				.map(Json::encode)
				.forEach(str -> checksum.putString(str, Charset.defaultCharset()));
		return checksum.hash().asInt();
	}

}
