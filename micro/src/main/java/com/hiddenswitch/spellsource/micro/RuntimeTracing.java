package com.hiddenswitch.spellsource.micro;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;

import java.util.stream.IntStream;

public class RuntimeTracing {
	public static void main(String[] args) {
		CardCatalogue.loadCardsFromPackage();
		IntStream.range(0, Integer.parseInt(System.getenv().getOrDefault("SPELLSOURCE_RUNTIME_TRACING_GAMES", "10000"))).parallel()
				.forEach(i -> {
					GameContext context = GameContext.fromTwoRandomDecks(DeckFormat.spellsource());
					context.play();
				});
	}
}
