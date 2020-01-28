package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.FixedCardsDeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.Zones;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OniQueenTests extends TestBase {

	@Test
	public void testInsurgencyCaptainKrika() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			playCard(context, player, "weapon_test_1_3");
			attack(context, player, player.getHero(), opponent.getHero());
			attack(context, player, player.getHero(), opponent.getHero());
			assertFalse(target.hasAttribute(Attribute.AURA_RUSH));
			playCard(context, player, "minion_insurgency_captain_krika");
			assertFalse(target.hasAttribute(Attribute.AURA_RUSH));
			attack(context, player, player.getHero(), opponent.getHero());
			assertFalse(target.hasAttribute(Attribute.AURA_RUSH));
			playCard(context, player, "minion_insurgency_captain_krika");
			assertTrue(target.hasAttribute(Attribute.AURA_RUSH));
		});
	}

	@Test
	public void testGatherInTheShadows() {
		runGym((context, player, opponent) -> {
			Card undertideTerror = receiveCard(context, player, "minion_storm_giant");
			receiveCard(context, player, "minion_storm_giant");
			for (int i = 0; i < 4; i++) {
				putOnTopOfDeck(context, player, "minion_storm_giant");
			}
			playCard(context, player, "spell_gather_in_the_shadows");
			assertEquals(2 + 3, player.getHand().size(), "start with 2 + 3 drawn");
			assertEquals(undertideTerror.getBaseManaCost() - 3, costOf(context, player, undertideTerror), "should have decreased by 3");
		});
	}

	@Test
	public void testThousandYearHatred() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion shouldDestroy1 = playMinionCard(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			Minion shouldNotDestroy1 = playMinionCard(context, opponent, "minion_neutral_test");
			Card shouldNotRemove1 = receiveCard(context, opponent, "minion_neutral_test");
			Card shouldRemove1 = receiveCard(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			Card shouldRemove2 = putOnTopOfDeck(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			context.endTurn();
			Minion shouldDestroy2 = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			Minion shouldNotDestroy2 = playMinionCard(context, player, "minion_neutral_test");
			Card shouldNotRemove2 = receiveCard(context, player, "minion_neutral_test");
			Card shouldRemove3 = receiveCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			Card shouldRemove4 = putOnTopOfDeck(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			playCard(context, player, "spell_thousand_year_hatred", shouldDestroy1);
			assertTrue(shouldDestroy1.isDestroyed());
			assertTrue(shouldDestroy2.isDestroyed());
			assertFalse(shouldNotDestroy1.isDestroyed());
			assertFalse(shouldNotDestroy2.isDestroyed());
			assertEquals(Zones.GRAVEYARD, shouldRemove1.getZone());
			assertEquals(Zones.GRAVEYARD, shouldRemove2.getZone());
			assertEquals(Zones.GRAVEYARD, shouldRemove3.getZone());
			assertEquals(Zones.GRAVEYARD, shouldRemove4.getZone());
			assertEquals(Zones.HAND, shouldNotRemove1.getZone());
			assertEquals(Zones.HAND, shouldNotRemove2.getZone());
		}, HeroClass.BLUEGREY, HeroClass.BLUEGREY);
	}

	@Test
	public void testRuffianShiro() {
		for (int i = 0; i < 10; i++) {
			runGym((context, player, opponent) -> {
				Minion boardDemon = playMinionCard(context, player, "minion_demon_test");
				Card handDemon = receiveCard(context, player, "minion_demon_test");

				receiveCard(context, player, "minion_neutral_test");
				receiveCard(context, player, "minion_neutral_test");
				receiveCard(context, player, "minion_neutral_test");
				receiveCard(context, player, "minion_neutral_test");

				playCard(context, player, "minion_ruffian_shiro", boardDemon);

				assertEquals(handDemon.getBonusAttack(), 2);
			});
		}
	}

	@Test
	public void testLumina() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("minion_lumina", "minion_demon_test"));
			playMinionCard(context, player, "minion_neutral_test");
			assertEquals(0, player.getHand().size());
			playCard(context, player, "minion_general_hun_zho");
			assertEquals("minion_lumina", player.getMinions().get(0).getSourceCard().getCardId());
			assertEquals(0, player.getHand().size());
		});
	}
}
