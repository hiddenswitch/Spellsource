package com.hiddenswitch.spellsource.conversiontest;

import io.vertx.core.json.Json;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;

import java.util.stream.LongStream;

public class ConversionHarness {
	private static Object PROBE = new Object();

	protected static class Tuple {
		GameContext context;
		long seed;

		public Tuple(GameContext context, long seed) {
			this.context = context;
			this.seed = seed;
		}
	}

	public static boolean assertCardReplaysTheSame(long[] seeds, String cardId, String replacementJson) {
		synchronized (PROBE) {
			var originalCard = CardCatalogue.getCards().get(cardId);
			var originalCardDesc = CardCatalogue.getRecords().get(cardId).getDesc();
			try {
				return LongStream.of(seeds)
						.mapToObj(seed -> {
							GameContext context = GameContext.fromTwoRandomDecks(seed);
							ensureCardIsInDeck(context, cardId);
							context.play();
							return new Tuple(context, seed);
						})
						.filter(tuple -> !(tuple.context.getTrace().getRawActions().stream().noneMatch(ga -> ga
								.getSourceReference() != null && ga.getSource(tuple.context).getSourceCard().getCardId().equals(cardId))))
						.allMatch(tuple -> {
							var desc = Json.decodeValue(replacementJson, CardDesc.class);
							CardCatalogue.getRecords().get(cardId).setDesc(desc);
							CardCatalogue.getCards().replace(cardId, desc.create());

							GameContext reproduction = GameContext.fromTwoRandomDecks(tuple.seed);
							ensureCardIsInDeck(reproduction, cardId);
							reproduction.play();

							return tuple.context.getTurn() == reproduction.getTurn();
						});
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
