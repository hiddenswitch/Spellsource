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

					org.junit.Assert.assertEquals(validRequest.getHeroClass(), HeroClass.BLACK);
					Assert.assertEquals(validRequest.getFormat(), "Standard");
				});

	}
}
