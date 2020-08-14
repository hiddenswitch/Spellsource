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
		var deckList1 = "### Big Baron\n" +
				"# Class: NAVY\n" +
				"# Format: Spellsource\n" +
				"#\n" +
				"# 2x (1) Enchanted Shield\n" +
				"# 2x (1) Gather Strength\n" +
				"# 2x (3) Bewitch\n" +
				"# 2x (3) Defenses Up\n" +
				"# 2x (3) Duplimancy\n" +
				"# 2x (4) Defender of Tomorrow\n" +
				"# 2x (4) Hidden Treasure\n" +
				"# 2x (4) Self-Appoint\n" +
				"# 2x (5) Bog Mutant\n" +
				"# 2x (5) Savage Werewolf\n" +
				"# 2x (7) Clash!\n" +
				"# 2x (7) Landsieged Drake\n" +
				"# 2x (7) Unstable Artifact\n" +
				"# 1x (8) Maskless Manhorse, Revengeance\n" +
				"# 1x (9) Gor'thal the Ravager\n" +
				"# 1x (10) Raid Boss Gnaxx\n" +
				"# 1x (10) Sorceress Eka\n" +
				"#\n";

		var deckList2 = "### First Flight\n" +
				"# Class: AZURE\n" +
				"# Format: Spellsource\n" +
				"#\n" +
				"# 2x (1) Enhancing Shaman\n" +
				"# 2x (1) Skywalker\n" +
				"# 2x (1) Solis Caster\n" +
				"# 2x (2) Blow Away\n" +
				"# 2x (2) Channeled Spirit\n" +
				"# 2x (2) Fluttering Songbird\n" +
				"# 2x (2) Lightning Strike\n" +
				"# 2x (2) Nimbus Strider\n" +
				"# 1x (3) Explore the Endless Skies\n" +
				"# 2x (3) Spirited Sprite\n" +
				"# 2x (3) Wind's Grace\n" +
				"# 2x (4) Evasive Cirrus\n" +
				"# 2x (4) Fae Harbinger\n" +
				"# 2x (6) Faerie Squadron\n" +
				"# 2x (6) Typhoon\n" +
				"# 1x (7) Stormbringer Kurt\n" +
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
				});

	}
}
