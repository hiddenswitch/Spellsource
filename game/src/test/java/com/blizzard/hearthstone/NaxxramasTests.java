package com.blizzard.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NaxxramasTests extends TestBase {
	/**
	 * Your opponent plays a Mad Scientist then you play Stampeding Kodo. First, the Kodo enters the board, then during
	 * its Battlecry Phase the Mad Scientist is marked pending destroy. After the Battlecry Phase ends, a Death Phase
	 * begins where the Mad Scientist's Deathrattle puts Mirror Entity into play. We now proceed to the After Play
	 * Phase, where Mirror Entity is Queued and resolved, creating a copy of the Kodo.
	 */
	@Test
	public void testMadScientist() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			shuffleToDeck(context,opponent,"secret_mirror_entity");
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
			shuffleToDeck(context,opponent,"secret_mirror_entity");
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
		GameContext context = createContext(HeroClass.BROWN, HeroClass.BLACK);
		Player druid = context.getPlayer1();
		Player rogue = context.getPlayer2();
		MinionCard chillwindYeti = (MinionCard) CardCatalogue.getCardById("minion_chillwind_yeti");

		for (int i = 0; i < GameLogic.MAX_MINIONS; i++) {
			playMinionCard(context, druid, chillwindYeti);
		}

		MinionCard nerubianEgg = (MinionCard) CardCatalogue.getCardById("minion_nerubian_egg");
		for (int i = 0; i < 3; i++) {
			playMinionCard(context, rogue, nerubianEgg);
		}

		Assert.assertEquals(druid.getMinions().size(), GameLogic.MAX_MINIONS);
		Assert.assertEquals(rogue.getMinions().size(), 3);

		SpellCard poisonSeeds = (SpellCard) CardCatalogue.getCardById("spell_poison_seeds");
		playCard(context, druid, poisonSeeds);

		Assert.assertEquals(druid.getMinions().size(), GameLogic.MAX_MINIONS);
		Assert.assertEquals(rogue.getMinions().size(), 6);
		for (Minion minion : druid.getMinions()) {
			final String cardId = minion.getSourceCard().getCardId();
			// TODO: Exclude these weird cards
			Assert.assertTrue(cardId.equals("token_treant") || cardId.equals("minion_deck_death"));
		}
	}

	@Test
	public void testPoisonSeedsAuchenai() {
		GameContext context = createContext(HeroClass.BROWN, HeroClass.WHITE);
		Player druid = context.getPlayer1();
		Player priest = context.getPlayer2();

		MinionCard zombieChow = (MinionCard) CardCatalogue.getCardById("minion_zombie_chow");
		playMinionCard(context, priest, zombieChow);
		playMinionCard(context, priest, zombieChow);

		MinionCard auchenaiSoulpriest = (MinionCard) CardCatalogue.getCardById("minion_auchenai_soulpriest");
		playMinionCard(context, priest, auchenaiSoulpriest);

		Card pyroblast = CardCatalogue.getCardById("spell_pyroblast");
		context.getLogic().receiveCard(druid.getId(), pyroblast);
		GameAction gameAction = pyroblast.play();
		gameAction.setTarget(druid.getHero());
		context.getLogic().performGameAction(druid.getId(), gameAction);

		Assert.assertEquals(druid.getHero().getHp(), GameLogic.MAX_HERO_HP - 10);

		SpellCard poisonSeeds = (SpellCard) CardCatalogue.getCardById("spell_poison_seeds");
		playCard(context, druid, poisonSeeds);

		Assert.assertEquals(druid.getHero().getHp(), GameLogic.MAX_HERO_HP);
	}

	@Test
	public void testPoisonSeedsHauntedCreeper() {
		GameContext context = createContext(HeroClass.BROWN, HeroClass.BLACK);
		Player druid = context.getPlayer1();
		MinionCard hauntedCreeper = (MinionCard) CardCatalogue.getCardById("minion_haunted_creeper");

		for (int i = 0; i < 4; i++) {
			playMinionCard(context, druid, hauntedCreeper);
		}
		Assert.assertEquals(druid.getMinions().size(), 4);

		SpellCard poisonSeeds = (SpellCard) CardCatalogue.getCardById("spell_poison_seeds");
		playCard(context, druid, poisonSeeds);

		Assert.assertEquals(druid.getMinions().size(), GameLogic.MAX_MINIONS);

		for (Minion minion : druid.getMinions()) {
			Assert.assertEquals(minion.getSourceCard().getCardId(), "token_spectral_spider");
		}
	}

}
