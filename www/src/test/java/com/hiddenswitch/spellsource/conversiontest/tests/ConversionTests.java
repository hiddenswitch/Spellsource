package com.hiddenswitch.spellsource.conversiontest.tests;

import com.hiddenswitch.spellsource.client.models.CardType;
import com.hiddenswitch.spellsource.conversiontest.ConversionHarness;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.logic.GameLogic;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class ConversionTests {

	private static final Set<String> omitted = Set.of("spell_death_strike",
			"minion_emerald_dreamer",
			"spell_unidentified_mushroom",
			"spell_dancing_rune_weapon2",
			"spell_winter_is_coming",
			"spell_heartstrike",
			"spell_abominations_might",
			"minion_vohkrovanis",
			"spell_icy_talon",
			"spell_the_cyntanami",
			"spell_marble_spellstone",
			"minion_fifi_fizzlewarp",
			"spell_zagroz__inferno_bomb");

	public static Stream<String> getCardIds() {
		CardCatalogue.loadCardsFromPackage();
		return CardCatalogue.getCards().keySet().stream().filter(cardId -> {
			var card = CardCatalogue.getCards().get(cardId);
			var cardType = card.getCardType();
			return DeckFormat.spellsource().isInFormat(card)
					&& !omitted.contains(cardId)
					&& cardType != CardType.HERO_POWER
					&& (GameLogic.isCardType(cardType, CardType.SPELL)
					|| GameLogic.isCardType(cardType, CardType.MINION)
					|| GameLogic.isCardType(cardType, CardType.WEAPON));
		});
	}

	@ParameterizedTest()
	@MethodSource("getCardIds")
	public void testAllCardsReproduce(String cardId) {
		assertTrue(ConversionHarness.assertCardReplaysTheSame(new long[]{1L, 2L}, cardId, Json.encodePrettily(CardCatalogue.getCards().get(cardId).getDesc())));
	}
}
