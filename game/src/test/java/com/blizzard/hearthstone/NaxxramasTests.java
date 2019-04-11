package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NaxxramasTests extends TestBase {

	@Test
	public void testAnubarAmbusher() {
		// Anub'ar Ambusher shouldn't return mortally wounded minions back to your hand
		runGym((context, player, opponent) -> {
			Minion ambusher = playMinionCard(context, player, "minion_anubar_ambusher");
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, opponent, "minion_kobold_geomancer");
			// Deals 5 damage
			playCard(context, opponent, "spell_flamestrike");
			Assert.assertTrue(ambusher.isDestroyed());
			Assert.assertTrue(bloodfen.isDestroyed());
			Assert.assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion ambusher = playMinionCard(context, player, "minion_anubar_ambusher");
			Minion boulderfist = playMinionCard(context, player, "minion_boulderfist_ogre");
			context.endTurn();
			playCard(context, opponent, "minion_kobold_geomancer");
			// Deals 5 damage
			playCard(context, opponent, "spell_flamestrike");
			Assert.assertTrue(ambusher.isDestroyed());
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(player.getHand().size(), 1);
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_boulderfist_ogre");
		});
	}

	@Test
	public void testLoatheb() {
		runGym((context, player, opponent) -> {
			Card friendlyFireball = receiveCard(context, player, "spell_fireball");
			playCard(context, player, "minion_loatheb");
			Assert.assertEquals(costOf(context, player, friendlyFireball), friendlyFireball.getBaseManaCost());
			context.endTurn();
			Card fireball = receiveCard(context, opponent, "spell_fireball");
			Assert.assertEquals(costOf(context, player, friendlyFireball), friendlyFireball.getBaseManaCost());
			Assert.assertEquals(costOf(context, opponent, fireball), fireball.getBaseManaCost() + 5);
			context.endTurn();
			context.endTurn();
			Assert.assertEquals(costOf(context, opponent, fireball), fireball.getBaseManaCost());
		});
	}

	/**
	 * Your opponent plays a Mad Scientist then you play Stampeding Kodo. First, the Kodo enters the board, then during
	 * its Battlecry Phase the Mad Scientist is marked pending destroy. After the Battlecry Phase ends, a Death Phase
	 * begins where the Mad Scientist's Deathrattle puts Mirror Entity into play. We now proceed to the After Play Phase,
	 * where Mirror Entity is Queued and resolved, creating a copy of the Kodo.
	 */
	@Test
	public void testMadScientist() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			shuffleToDeck(context, opponent, "secret_mirror_entity");
			playMinionCard(context, opponent, "minion_mad_scientist");
			context.endTurn();
			playMinionCard(context, player, "minion_stampeding_kodo");
			Assert.assertEquals(opponent.getMinions().size(), 1);
			Assert.assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), "minion_stampeding_kodo");
		});

		/*
		  Alternatively, if you play a minion and a Knife Juggler triggers, killing the enemy Mad Scientist and putting
		  Mirror Entity into play, the Secret does NOT trigger. This is because the After Play Phase has already
		  passed.
		 */

		runGym((context, player, opponent) -> {
			context.endTurn();
			shuffleToDeck(context, opponent, "secret_mirror_entity");
			Minion madScientist = playMinionCard(context, opponent, "minion_mad_scientist");
			madScientist.setHp(1);
			context.endTurn();
			Minion knifeJuggler = playMinionCard(context, player, "minion_knife_juggler");
			overrideMissilesTrigger(context, knifeJuggler, madScientist);
			playMinionCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testDarkCultistFlamestrikeBuffInteraction() {
		runGym((context, player, opponent) -> {
			player.setMana(10);
			opponent.setMana(10);

			Card darkCultist = CardCatalogue.getCardById("minion_dark_cultist");
			playCard(context, opponent, darkCultist);
			Card darkIronDwarf = CardCatalogue.getCardById("minion_dark_iron_dwarf");
			playCard(context, opponent, darkIronDwarf);

			Assert.assertEquals(opponent.getMinions().size(), 2);

			Card flamestrike = CardCatalogue.getCardById("spell_flamestrike");
			playCard(context, player, flamestrike);

			// there should be no minions left after the Flamestrike
			// the Dark Cultist Deathrattle shouldn't have any effect, as both minions are removed simultaneously
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testPoisonSeeds() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < GameLogic.MAX_MINIONS; i++) {
				playMinionCard(context, player, "minion_chillwind_yeti");
			}

			context.endTurn();
			for (int i = 0; i < 3; i++) {
				playMinionCard(context, opponent, "minion_nerubian_egg");
			}
			context.endTurn();

			Assert.assertEquals(player.getMinions().size(), GameLogic.MAX_MINIONS);
			Assert.assertEquals(opponent.getMinions().size(), 3);

			playCard(context, player, "spell_poison_seeds");

			Assert.assertEquals(player.getMinions().size(), GameLogic.MAX_MINIONS);
			Assert.assertEquals(opponent.getMinions().size(), 6);
			for (Minion minion : player.getMinions()) {
				final String cardId = minion.getSourceCard().getCardId();
				// TODO: Exclude these weird cards
				Assert.assertTrue(cardId.equals("token_treant") || cardId.equals("minion_deck_death"));
			}
		});
	}

	@Test
	public void testPoisonSeedsAuchenai() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, opponent, "minion_zombie_chow");
			playMinionCard(context, opponent, "minion_zombie_chow");
			playMinionCard(context, opponent, "minion_auchenai_soulpriest");
			playCard(context, player, "spell_pyroblast", player.getHero());
			Assert.assertEquals(player.getHero().getHp(), GameLogic.MAX_HERO_HP - 10);
			Card poisonSeeds = CardCatalogue.getCardById("spell_poison_seeds");
			playCard(context, player, poisonSeeds);
			Assert.assertEquals(player.getHero().getHp(), GameLogic.MAX_HERO_HP);
		}, HeroClass.BROWN, HeroClass.WHITE);
	}

	@Test
	public void testPoisonSeedsHauntedCreeper() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				playMinionCard(context, player, "minion_haunted_creeper");
			}
			Assert.assertEquals(player.getMinions().size(), 4);
			playCard(context, player, "spell_poison_seeds");

			Assert.assertEquals(player.getMinions().size(), GameLogic.MAX_MINIONS);

			for (Minion minion : player.getMinions()) {
				Assert.assertEquals(minion.getSourceCard().getCardId(), "token_spectral_spider");
			}
		}, HeroClass.BROWN, HeroClass.BLACK);
	}
}
