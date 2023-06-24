package com.hiddenswitch.spellsource.conversiontest;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.tests.util.TestBase;

import java.util.Objects;
import java.util.stream.LongStream;

public class ConversionHarness {
	static {
	}

	private static final Object PROBE = new Object();

	protected static class Tuple {
		GameContext context;
		long seed;

		public Tuple(GameContext context, long seed) {
			this.context = context;
			this.seed = seed;
		}
	}

	public static boolean assertCardReplaysTheSame(int seed1, int seed2, String cardId, String replacementJson) {
		return assertCardReplaysTheSame(new long[]{seed1, seed2}, cardId, replacementJson);
	}

	public static boolean assertCardReplaysTheSame(long[] seeds, String cardId, String replacementJson) {
		synchronized (PROBE) {
			var cardCatalogue = ClasspathCardCatalogue.INSTANCE;
			var originalCard = cardCatalogue.getCards().get(cardId);
			var originalCardDesc = cardCatalogue.getRecords().get(cardId).getDesc();
			try {
				return LongStream.of(seeds)
						.mapToObj(seed -> {
							GameContext context = TestBase.fromTwoRandomDecks(seed);
							ensureCardIsInDeck(context, cardId);
							context.play();
							return new Tuple(context, seed);
						})
						.filter(tuple -> tuple.context.getTrace().getRawActions().stream().anyMatch(ga -> {
							if (ga.getSourceReference() == null) {
								return false;
							}
							var source = ga.getSource(tuple.context);
							if (source == null) {
								return false;
							}
							var sourceCard = source.getSourceCard();
							if (sourceCard == null) {
								return false;
							}
							return Objects.equals(sourceCard.getCardId(), cardId);
						}))
						.allMatch(tuple -> {
							var desc = Json.decodeValue(replacementJson, CardDesc.class);
							desc.setId(cardId);
							cardCatalogue.getRecords().put(cardId, new CardCatalogueRecord(cardId, desc));
							cardCatalogue.getCards().replace(cardId, desc.create());

							GameContext reproduction = TestBase.fromTwoRandomDecks(tuple.seed);
							ensureCardIsInDeck(reproduction, cardId);
							reproduction.play();

							return tuple.context.getTurn() == reproduction.getTurn();
						});
			} finally {
				cardCatalogue.getCards().replace(cardId, originalCard);
				cardCatalogue.getRecords().put(cardId, new CardCatalogueRecord(cardId, originalCardDesc));;
			}
		}
	}

	static void ensureCardIsInDeck(GameContext context, String cardId) {
		var cardType = context.getCardById(cardId).getCardType();
		if (cardType == CardType.CLASS || cardType == CardType.ENCHANTMENT || cardType == CardType.HERO_POWER ||
				(cardType == CardType.HERO && context.getCardById(cardId).hasAttribute(Attribute.HP))) {
			return;
		}
		for (var player : context.getPlayers()) {
			for (var i = 0; i < 5; i++) {
				player.getDeck().addCard(context.getCardCatalogue(), cardId);
			}
		}
	}
}
