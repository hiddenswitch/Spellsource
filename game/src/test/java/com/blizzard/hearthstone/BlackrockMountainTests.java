package com.blizzard.hearthstone;

import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestMinionCard;
import net.demilich.metastone.tests.util.TestSpellCard;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class BlackrockMountainTests extends TestBase {

	@Test
	public void testForceCastDoesntTriggerFlamewaker() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_flamewaker");
			playCard(context, player, "minion_force_cast_test");
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp(), "Should not have triggered Flamewaker");
			playCard(context, player, "spell_the_coin");
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 2, "Should have triggered Flamewaker");
		});
	}

	@Test
	public void testEmperorThaurissen() {
		runGym((context, player, opponent) -> {
			Card deckCard = shuffleToDeck(context, player, "minion_bloodfen_raptor");
			Card handCard = receiveCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_emperor_thaurissan");
			assertEquals(costOf(context, player, deckCard), 2);
			assertEquals(costOf(context, player, handCard), 2);
			context.endTurn();
			context.endTurn();
			assertEquals(costOf(context, player, deckCard), 2);
			assertEquals(costOf(context, player, handCard), 1);
			context.endTurn();
			context.endTurn();
			// Deck card is now in the hand at the end of the turn
			assertEquals(costOf(context, player, deckCard), 1);
			assertEquals(costOf(context, player, handCard), 0);
		});
	}

	@Test
	public void testHungryDragon() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_hungry_dragon");
			assertEquals(opponent.getMinions().size(), 1);
		});
	}

	/**
	 * You play a Grim Patron. Your opponent has a Knife Juggler and plays an Imp Gang Boss (the knife hits face). On your
	 * turn, your Grim Patron attacks their Imp Gang Boss. The simultaneous damage triggers are queued in the order [Imp
	 * Gang Boss, Grim Patron] because the defender queues first. An Imp is summoned, triggering the allied Knife Juggler
	 * to throw a knife and mortally wound your Grim Patron. Now your Grim Patron would trigger, but it is mortally
	 * wounded, so the trigger condition fails and you do not get a new Grim Patron.
	 */
	@Test
	public void testGrimPatron() {
		runGym((context, player, opponent) -> {
			Minion grimPatron = playMinionCard(context, player, "minion_grim_patron");
			context.endTurn();
			Minion knifeJuggler = playMinionCard(context, opponent, "minion_knife_juggler");
			overrideMissilesTrigger(context, knifeJuggler, player.getHero());
			int startingHp = player.getHero().getHp();
			Minion impGangBoss = playMinionCard(context, opponent, "minion_imp_gang_boss");
			assertEquals(player.getHero().getHp(), startingHp - 1);
			context.endTurn();
			PhysicalAttackAction attack = new PhysicalAttackAction(grimPatron.getReference());
			attack.setTargetReference(impGangBoss.getReference());
			overrideMissilesTrigger(context, knifeJuggler, grimPatron);
			context.getLogic().performGameAction(player.getId(), attack);
			assertEquals(player.getMinions().size(), 0);
		});
	}

	@Test()
	public void testAxeFlinger() {
		runGym((context, player, opponent) -> {
			int playerStartingHp = player.getHero().getHp();
			int opponentStartingHp = context.getPlayer2().getHero().getHp();
			playMinionCard(context, player, "minion_axe_flinger");
			playMinionCard(context, player, "minion_axe_flinger");

			context.getLogic().endTurn(player.getId());

			Card damageCard = new TestSpellCard(DamageSpell.create(EntityReference.ENEMY_CHARACTERS, 1));
			playCard(context, opponent, damageCard);
			assertEquals(player.getHero().getHp(), playerStartingHp - 1);
			assertEquals(opponent.getHero().getHp(), opponentStartingHp - 4);
		});
	}

	@Test
	public void testBlackwingCorruptor() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_blackwing_corruptor");
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp());

			receiveCard(context, player, "minion_azure_drake");
			playMinionCardWithBattlecry(context, player, "minion_blackwing_corruptor", player.getHero());
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp() - 3);
		});
	}

	@Test
	public void testBlackwingTechnician() {
		runGym((context, player, opponent) -> {
			Minion blackwingTechnician = playMinionCard(context, player, "minion_blackwing_technician");
			assertEquals(blackwingTechnician.getHp(), blackwingTechnician.getBaseHp());
			assertEquals(blackwingTechnician.getAttack(), blackwingTechnician.getBaseAttack());

			receiveCard(context, player, "minion_azure_drake");
			blackwingTechnician = playMinionCard(context, player, "minion_blackwing_technician");
			assertEquals(blackwingTechnician.getHp(), blackwingTechnician.getBaseHp() + 1);
			assertEquals(blackwingTechnician.getAttack(), blackwingTechnician.getBaseAttack() + 1);
		});
	}

	@Test
	public void testChromaggus() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_The_coin");
			shuffleToDeck(context, player, "spell_The_coin");
			shuffleToDeck(context, player, "spell_The_coin");
			context.setDeckFormat(new DeckFormat().withCardSets(CardSet.BASIC));
			assertEquals(player.getHand().getCount(), 0);

			playMinionCard(context, player, "minion_chromaggus");
			context.getLogic().drawCard(player.getId(), player.getHero());
			assertEquals(player.getHand().getCount(), 2);

			clearHand(context, player);

			assertEquals(player.getHand().getCount(), 0);

			playMinionCard(context, player, "minion_chromaggus");
			context.getLogic().drawCard(player.getId(), player.getHero());
			assertEquals(player.getHand().getCount(), 3);
		});
	}

	@Test
	public void testCoreRager() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_the_coin");
			Minion coreRager = playMinionCard(context, player, "minion_core_rager");
			assertEquals(coreRager.getAttack(), coreRager.getBaseAttack() + 3);
			assertEquals(coreRager.getHp(), coreRager.getBaseHp() + 3);

			context.getLogic().drawCard(player.getId(), player.getHero());

			coreRager = playMinionCard(context, player, "minion_core_rager");
			assertEquals(coreRager.getAttack(), coreRager.getBaseAttack());
			assertEquals(coreRager.getHp(), coreRager.getBaseHp());
		});
	}

	@Test
	public void testDarkIronSkulker() {
		runGym((context, player, opponent) -> {
			Minion testMinion1 = playMinionCard(context, player, new TestMinionCard(3, 3, 0));
			Minion injuredBlademaster = playMinionCard(context, player, "minion_injured_blademaster");
			Minion testMinion2 = playMinionCard(context, player, new TestMinionCard(3, 3, 0));
			assertEquals(testMinion1.getHp(), testMinion1.getMaxHp());
			assertEquals(injuredBlademaster.getHp(), injuredBlademaster.getMaxHp() - 4);
			assertEquals(testMinion2.getHp(), testMinion2.getMaxHp());

			context.getLogic().endTurn(player.getId());

			Minion testMinionOpponent = playMinionCard(context, opponent, new TestMinionCard(3, 3, 0));
			Minion injuredBlademasterOpponent = playMinionCard(context, opponent, "minion_injured_blademaster");
			assertEquals(testMinionOpponent.getHp(), testMinionOpponent.getMaxHp());
			assertEquals(injuredBlademasterOpponent.getHp(), injuredBlademasterOpponent.getMaxHp() - 4);

			Minion darkIronSkulker = playMinionCard(context, opponent, "minion_dark_iron_skulker");
			assertEquals(darkIronSkulker.getHp(), darkIronSkulker.getMaxHp());

			assertEquals(testMinionOpponent.getHp(), testMinionOpponent.getMaxHp());
			assertEquals(injuredBlademasterOpponent.getHp(), injuredBlademasterOpponent.getMaxHp() - 4);

			assertEquals(testMinion1.getHp(), testMinion1.getMaxHp() - 2);
			assertEquals(injuredBlademaster.getHp(), injuredBlademaster.getMaxHp() - 4);
			assertEquals(testMinion2.getHp(), testMinion2.getMaxHp() - 2);
		});
	}

	@Test
	public void testDragonConsort() {
		runGym((context, player, opponent) -> {
			final int MANA_REDUCTION = 2;
			Card dragonConsort = receiveCard(context, player, "minion_dragon_consort");
			assertEquals(costOf(context, player, dragonConsort), dragonConsort.getBaseManaCost());
			playMinionCard(context, player, "minion_dragon_consort");
			assertEquals(costOf(context, player, dragonConsort), dragonConsort.getBaseManaCost() - MANA_REDUCTION);
		});


	}

	@Test
	public void testDragonEgg() {
		runGym((context, player, opponent) -> {
			final String TOKEN = "token_black_whelp";

			Minion dragonEgg = playMinionCard(context, player, "minion_dragon_egg");
			assertEquals(getSummonedMinion(player.getMinions()), dragonEgg);

			playCard(context, player, "spell_fireball", dragonEgg);
			assertEquals(getSummonedMinion(player.getMinions()).getSourceCard().getCardId(), TOKEN);
		});
	}

	@Test
	public void testDragonkinSorceror() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new DeckFormat().withCardSets(CardSet.BASIC, CardSet.CLASSIC, CardSet.BLACKROCK_MOUNTAIN));
			final int ATTACK_BONUS = 1;
			final int HP_BONUS = 1;

			Minion dragonkin1 = playMinionCard(context, player, "minion_dragonkin_sorcerer");
			Minion dragonkin2 = playMinionCard(context, player, "minion_dragonkin_sorcerer");
			assertEquals(dragonkin1.getAttack(), dragonkin2.getAttack());
			assertEquals(dragonkin1.getHp(), dragonkin2.getHp());

			playCard(context, player, "spell_gang_up", dragonkin1);
			assertEquals(dragonkin1.getAttack(), dragonkin2.getAttack() + ATTACK_BONUS);
			assertEquals(dragonkin1.getHp(), dragonkin2.getHp() + HP_BONUS);
		});
	}

	@Test
	public void testDrakonidCrusher() {
		runGym((context, player, opponent) -> {
			final int ATTACK_BONUS = 3;
			final int HP_BONUS = 3;

			Minion drakonid = playMinionCard(context, player, "minion_drakonid_crusher");
			assertEquals(drakonid.getAttack(), drakonid.getBaseAttack());
			assertEquals(drakonid.getHp(), drakonid.getBaseHp());

			opponent.getHero().setHp(15);

			drakonid = playMinionCard(context, player, "minion_drakonid_crusher");
			assertEquals(drakonid.getAttack(), drakonid.getBaseAttack() + ATTACK_BONUS);
			assertEquals(drakonid.getHp(), drakonid.getBaseHp() + HP_BONUS);
		});
	}

	@Test
	public void testImpGangBossConeOfCold() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion firstYeti = playMinionCard(context, opponent, "minion_chillwind_yeti");
			Minion impGangBoss = playMinionCard(context, opponent, "minion_imp_gang_boss");
			Minion secondYeti = playMinionCard(context, opponent, "minion_chillwind_yeti");
			assertEquals(opponent.getMinions().size(), 3);
			context.endTurn();

			playCard(context, player, "spell_cone_of_cold", impGangBoss);
			assertEquals(opponent.getMinions().size(), 4);
			assertTrue(firstYeti.hasAttribute(Attribute.FROZEN));
			assertTrue(impGangBoss.hasAttribute(Attribute.FROZEN));
			assertFalse(secondYeti.hasAttribute(Attribute.FROZEN));
		});
	}


	@Test
	public void testEmperorThaurissanEmptyHand() {
		runGym((context, player, opponent) -> {
			Minion emperorThaurissan = playMinionCard(context, player, "minion_emperor_thaurissan");

			assertTrue(player.getHand().isEmpty());
			context.endTurn();

			playCard(context, opponent, "spell_assassinate", emperorThaurissan);
			receiveCard(context, player, "minion_chillwind_yeti");
			context.endTurn();

			Card card = player.getHand().peekFirst();
			int modifiedCost = costOf(context, player, card);
			assertEquals(card.getBaseManaCost(), modifiedCost);
		});
	}
}
