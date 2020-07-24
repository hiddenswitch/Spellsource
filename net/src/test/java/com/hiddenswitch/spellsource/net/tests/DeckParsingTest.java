package com.hiddenswitch.spellsource.net.tests;

import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.decks.DeckListParsingException;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DeckParsingTest {
	@Test
	public void testDecklistParsingNumberNameCards() {
		CardCatalogue.loadCardsFromPackage();
		var deckList1 = "Name: Test Name\nClass: WHITE\nFormat: All\n1x Has Number Name 1\n1x 2 Has Number Name";
		var createRequest = DeckCreateRequest.fromDeckList(deckList1);
		assertTrue(createRequest.getCardIds().contains("minion_has_number_name_1"));
		assertTrue(createRequest.getCardIds().contains("minion_has_number_name_2"));
	}

	@Test
	public void testDecklistTwoWordHero() {
		CardCatalogue.loadCardsFromPackage();
		var deckList1 = "Name: Test Name\nClass: WHITE\nFormat: All\nHero: Two Words\n1x Has Number Name 1\n1x 2 Has Number Name";
		var createRequest = DeckCreateRequest.fromDeckList(deckList1);
		assertTrue(createRequest.getCardIds().contains("minion_has_number_name_1"));
		assertTrue(createRequest.getCardIds().contains("minion_has_number_name_2"));
		assertEquals(createRequest.getHeroCardId(), "hero_two_word_name");
	}

	@Test
	public void testDecklistParsing() {
		CardCatalogue.loadCardsFromPackage();
		var deckList1 = "### Witch Doctor: Beast Doctor\n" +
				"# Class: ROSE\n" +
				"# Format: Spellsource\n" +
				"#\n" +
				"# 2x (1) Dinosoul\n" +
				"# 2x (1) Marked for Sacrifice\n" +
				"# 2x (2) Bird-glary\n" +
				"# 2x (2) Ritual Dagger\n" +
				"# 2x (2) Shedding Chameleon\n" +
				"# 2x (3) Corvid Call\n" +
				"# 2x (3) Nightmare Warden\n" +
				"# 2x (3) Prolific Tamer\n" +
				"# 2x (4) Mutamite Terror\n" +
				"# 2x (4) Prized Boar\n" +
				"# 2x (4) Shimmerscale\n" +
				"# 1x (5) Ptero Max\n" +
				"# 2x (6) Evocation\n" +
				"# 2x (6) Packmother\n" +
				"# 1x (7) Gaitha the Protector\n" +
				"# 2x (7) Jungle King\n" +
				"#";

		var deckList2 = "### Witch Doctor: Aftermath Doctor\n" +
				"# Class: ROSE\n" +
				"# Format: Spellsource\n" +
				"#\n" +
				"# 2x (1) Frightful Vision\n" +
				"# 2x (1) Marked for Sacrifice\n" +
				"# 2x (1) Nostalgia\n" +
				"# 2x (1) Jungle Soulfinder\n" +
				"# 2x (2) Forgotten Ancestor\n" +
				"# 2x (2) Morbid Mockery\n" +
				"# 2x (2) Ritual Dagger\n" +
				"# 1x (3) Berserk\n" +
				"# 2x (3) Grim Clairvoyant\n" +
				"# 2x (4) Dragon Pod\n" +
				"# 2x (5) Draining Ooze\n" +
				"# 1x (5) Pandora's Box\n" +
				"# 2x (6) Evocation\n" +
				"# 2x (6) Spiritual Diffusion\n" +
				"# 2x (7) Giant's Toad\n" +
				"# 1x (8) Doctor Rigo Morti\n" +
				"# 1x (8) Puppeteer Senzaku\n" +
				"#";

		Stream.of(deckList1, deckList2)
				.forEach(deckList -> {
					DeckCreateRequest validRequest;
					try {
						validRequest = DeckCreateRequest.fromDeckList(deckList)
								.withUserId("testUserId");
					} catch (DeckListParsingException e) {
						fail("Deck failed to parse due to error: " + e.getMessage());
						return;
					}

					assertTrue(validRequest.isValid());

					Stream.of(
							"weapon_ritual_dagger",
							"spell_marked_for_sacrifice",
							"spell_evocation_witchdoctor"
					).forEach(cid -> {
						assertEquals(validRequest.getCardIds().stream().filter(cid::equals).count(), 2L);
					});

					assertEquals(validRequest.getHeroClass(), "ROSE");
					assertEquals(validRequest.getFormat(), "Spellsource");
				});

	}
}
