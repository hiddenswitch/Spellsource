package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.decks.DeckListParsingException;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Stream;

public class DeckParsingTest {
	@Test
	public void testDecklistParsingNumberNameCards() {
		CardCatalogue.loadCardsFromPackage();
		String deckList1 = "Name: Test Name\nClass: WHITE\nFormat: All\n1x Has Number Name 1\n1x 2 Has Number Name";
		final DeckCreateRequest createRequest = DeckCreateRequest.fromDeckList(deckList1);
		Assert.assertTrue(createRequest.getCardIds().contains("minion_has_number_name_1"));
		Assert.assertTrue(createRequest.getCardIds().contains("minion_has_number_name_2"));
	}

	@Test
	public void testDecklistTwoWordHero() {
		CardCatalogue.loadCardsFromPackage();
		String deckList1 = "Name: Test Name\nClass: WHITE\nFormat: All\nHero: Two Words\n1x Has Number Name 1\n1x 2 Has Number Name";
		final DeckCreateRequest createRequest = DeckCreateRequest.fromDeckList(deckList1);
		Assert.assertTrue(createRequest.getCardIds().contains("minion_has_number_name_1"));
		Assert.assertTrue(createRequest.getCardIds().contains("minion_has_number_name_2"));
		Assert.assertEquals(createRequest.getHeroCardId(), "hero_two_word_name");
	}

	@Test
	public void testBoomsdayDecklistParsing() {
		CardCatalogue.loadCardsFromPackage();
		Stream.of("AAECAZ8FBugBucECnOIC/eoC0PQCp4IDDIwBngHIBKcF8wX1Ba8Hm8sC48sC1uUCrfIC2P4CAA==",
				"AAECAa0GDAnFBO0F0wqWxALTxQLJxwLHywKJzQLwzwKQ0wLD6gIJ+wGhBNHBAtXBAujQAqniAsvmAp/rAqH+AgA=",
				"AAECAaoICJQDtAPtBcLOArrSAqfuAur6Apn7Agv5A4EE9QT+BbIG9QjHwQKNzgLD0gLz5wKf/QIA")
				.forEach(deckList -> {
					DeckCreateRequest deck = DeckCreateRequest.fromDeckList(deckList);
					Assert.assertEquals(deck.getCardIds().size(), 30);
				});
	}

	@Test
	public void testDecklistParsing() {
		CardCatalogue.loadCardsFromPackage();
		String deckList1 = "### Tempo Rogue\n" +
				"# Class: Black\n" +
				"# Format: Standard\n" +
				"# Year of the Mammoth\n" +
				"#\n" +
				"# 2x (0) Backstab\n" +
				"# 2x (0) Shadowstep\n" +
				"# 2x (1) Cold Blood\n" +
				"# 2x (1) Fire Fly\n" +
				"# 1x (1) Patches the Pirate\n" +
				"# 2x (1) Southsea Deckhand\n" +
				"# 2x (1) Swashburglar\n" +
				"# 1x (2) Prince Keleseth\n" +
				"# 1x (3) Coldlight Oracle\n" +
				"# 1x (3) Edwin VanCleef\n" +
				"# 2x (3) SI:7 Agent\n" +
				"# 1x (3) Southsea Captain\n" +
				"# 1x (3) Tar Creeper\n" +
				"# 2x (3) Vicious Fledgling\n" +
				"# 1x (4) Spellbreaker\n" +
				"# 2x (5) Cobalt Scalebane\n" +
				"# 1x (5) Leeroy Jenkins\n" +
				"# 2x (5) Vilespine Slayer\n" +
				"# 1x (7) Bonemare\n" +
				"# 1x (7) The Curator\n" +
				"#\n" +
				"AAECAaIHCpG8ApziAvgHsgKoBcrDAvIFrwSmzgK5sgIKtAHtAowC68IC1AWStgLdCJ/CAsrLAoHCAgA=\n" +
				"#\n" +
				"# To use this deck, copy it to your clipboard and create a new deck in Hearthstone";

		String deckList2 = "AAECAaIHCpG8ApziAvgHsgKoBcrDAvIFrwSmzgK5sgIKtAHtAowC68IC1AWStgLdCJ/CAsrLAoHCAgA=";

		Stream.of(deckList1, deckList2)
				.forEach(deckList -> {
					final DeckCreateRequest validRequest;
					try {
						validRequest = DeckCreateRequest.fromDeckList(deckList)
								.withUserId("testUserId");
					} catch (DeckListParsingException e) {
						org.junit.Assert.fail("Deck failed to parse due to error: " + e.getMessage());
						return;
					}

					org.junit.Assert.assertTrue(validRequest.isValid());

					Stream.of(
							"minion_patches_the_pirate",
							"minion_prince_keleseth",
							"minion_coldlight_oracle",
							"minion_edwin_vancleef",
							"minion_southsea_captain",
							"minion_tar_creeper",
							"minion_spellbreaker",
							"minion_leeroy_jenkins",
							"minion_bonemare",
							"minion_the_curator"
					).forEach(cid -> {
						org.junit.Assert.assertEquals(validRequest.getCardIds().stream().filter(cid::equals).count(), 1L);
					});

					Stream.of(
							"spell_backstab",
							"spell_shadowstep",
							"spell_cold_blood",
							"minion_fire_fly",
							"minion_southsea_deckhand",
							"minion_swashburglar",
							"minion_si7_agent",
							"minion_vicious_fledgling",
							"minion_cobalt_scalebane",
							"minion_vilespine_slayer"
					).forEach(cid -> {
						org.junit.Assert.assertEquals(validRequest.getCardIds().stream().filter(cid::equals).count(), 2L);
					});

					org.junit.Assert.assertEquals(validRequest.getHeroClass(), "BLACK");
					Assert.assertEquals(validRequest.getFormat(), "Standard");
				});

	}
}
