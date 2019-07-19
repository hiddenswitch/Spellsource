package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.common.DeckListParsingException;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
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
	public void testDecklistParsing() {
		CardCatalogue.loadCardsFromPackage();
		String deckList1 = "### Witch Doctor: Beast Doctor\n" +
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

		String deckList2 = "### Witch Doctor: Aftermath Doctor\n" +
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
							"weapon_ritual_dagger",
							"spell_marked_for_sacrifice",
							"spell_evocation_witchdoctor"
					).forEach(cid -> {
						org.junit.Assert.assertEquals(validRequest.getCardIds().stream().filter(cid::equals).count(), 2L);
					});

					org.junit.Assert.assertEquals(validRequest.getHeroClass(), "ROSE");
					Assert.assertEquals(validRequest.getFormat(), "Spellsource");
				});

	}
}
