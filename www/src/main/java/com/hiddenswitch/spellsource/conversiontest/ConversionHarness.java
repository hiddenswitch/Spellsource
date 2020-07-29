package com.hiddenswitch.spellsource.conversiontest;

import io.vertx.core.json.Json;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;

public class ConversionHarness {
	private static Object PROBE = new Object();

	public static boolean assertCardReplaysTheSame(long seed, String cardId, String replacementJson) {
		synchronized (PROBE) {
			var originalCard = CardCatalogue.getCards().get(cardId);
			var originalCardDesc = CardCatalogue.getRecords().get(cardId).getDesc();
			try {
				GameContext context = GameContext.fromTwoRandomDecks(seed);
				ensureCardIsInDeck(context, cardId);
				context.play();

				if (context.getTrace().getRawActions().stream().noneMatch(ga -> ga
						.getSourceReference() != null && ga.getSource(context).getSourceCard().getCardId().equals(cardId))) {
					throw new AssertionError("this seed will never use the card ID");
				}


				var desc = Json.decodeValue(replacementJson, CardDesc.class);
				CardCatalogue.getRecords().get(cardId).setDesc(desc);
				CardCatalogue.getCards().replace(cardId, desc.create());

				GameContext reproduction = GameContext.fromTwoRandomDecks(seed);
				ensureCardIsInDeck(reproduction, cardId);
				reproduction.play();

				return context.getTurn() == reproduction.getTurn();
			} finally {
				CardCatalogue.getCards().replace(cardId, originalCard);
				CardCatalogue.getRecords().get(cardId).setDesc(originalCardDesc);
			}
		}
	}

	static void ensureCardIsInDeck(GameContext context, String cardId) {
		for (var player : context.getPlayers()) {
			for (var i = 0; i < 5; i++) {
				player.getDeck().addCard(cardId);
			}
		}
	}
}
