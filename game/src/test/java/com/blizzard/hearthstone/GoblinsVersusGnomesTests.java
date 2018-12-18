package com.blizzard.hearthstone;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestMinionCard;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

public class GoblinsVersusGnomesTests extends TestBase {

	@Test
	public void testBlingtron3000() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_blingtron_3000");
			Assert.assertEquals(player.getWeaponZone().size(), 1);
			Assert.assertEquals(opponent.getWeaponZone().size(), 1);
		});
	}

	@Test
	public void testMalganis() {
		runGym((context, player, opponent) -> {
			Minion malganis = playMinionCard(context, player, "minion_malganis");
			int playerHp = player.getHero().getHp();
			playCard(context, player, "spell_fireball", player.getHero());
			assertEquals(player.getHero().getHp(), playerHp);
			destroy(context, malganis);
			playCard(context, player, "spell_fireball", player.getHero());
			assertEquals(player.getHero().getHp(), playerHp - 6);
		});
	}

	@Test
	public void testIllidanKnifeJugglerSheepDeathwingInteraction() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_the_coin");
			receiveCard(context, opponent, "spell_the_coin");
			Minion knifeJuggler = playMinionCard(context, player, "minion_knife_juggler");
			Minion illidan = playMinionCard(context, player, "minion_illidan_stormrage");
			// Knife Juggler dealt 1 dmg to opponent's hero
			context.endTurn();
			Minion sylvanas = playMinionCard(context, opponent, "minion_sylvanas_windrunner");
			for (int i = 0; i < 4; i++) {
				playMinionCard(context, opponent, "minion_explosive_sheep");
			}
			Minion explosiveSheep = playMinionCard(context, opponent, "minion_explosive_sheep");
			context.endTurn();
			overrideMissilesTrigger(context, knifeJuggler, explosiveSheep);
			playCard(context, player, "minion_deathwing");
			assertTrue(sylvanas.isDestroyed());
			assertTrue(illidan.isDestroyed());
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getMinions().size(), 1);
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), "minion_deathwing");
			assertEquals(player.getHand().size(), 0, "Player discards");
			assertEquals(opponent.getHand().size(), 1, "Opponent does not discard");
		});
	}

	@Test
	public void testIllidanKnifeJugglerAnubarAmbusherSheepDeathwingInteraction() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_the_coin");
			receiveCard(context, opponent, "spell_the_coin");
			Minion knifeJuggler = playMinionCard(context, player, "minion_knife_juggler");
			Minion illidan = playMinionCard(context, player, "minion_illidan_stormrage");
			// Knife Juggler dealt 1 dmg to opponent's hero
			context.endTurn();
			Minion sylvanas = playMinionCard(context, opponent, "minion_sylvanas_windrunner");
			Minion ambusher = playMinionCard(context, opponent, "minion_anubar_ambusher");
			for (int i = 0; i < 4; i++) {
				playMinionCard(context, opponent, "minion_explosive_sheep");
			}
			Minion explosiveSheep = playMinionCard(context, opponent, "minion_explosive_sheep");
			context.endTurn();
			overrideMissilesTrigger(context, knifeJuggler, explosiveSheep);
			playCard(context, player, "minion_deathwing");
			assertTrue(sylvanas.isDestroyed());
			assertTrue(illidan.isDestroyed());
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getMinions().size(), 1);
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), "minion_deathwing");
			assertEquals(player.getHand().size(), 0, "Player discards");
			assertEquals(opponent.getHand().size(), 1, "Opponent does not discard");
		});
	}

	@Test
	public void testMalganisVoidcallerVoidwalkerSheepInteraction() {
		/*
		From https://www.youtube.com/watch?v=lm1t1FU-ftc
		If you kill a minion with an aura and a minion with a Deathrattle simultaneously, non-Health effects of the aura
		are not active during the following Death Phase because the minion is removed from play. However, Health effects
		of the aura will be active, because Hearthstone does not recalculate Health changes after the minions are
		killed, unless a new minion is summoned.[261][262][263] However, a health-granting aura such as Mal'Ganis can
		save a minion from dying, even if it enters play the same Phase the other minion was mortally wounded, because
		the aura recalculation is done before the Death Creation Step.
		 */

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion voidwalker = playMinionCard(context, opponent, "minion_voidwalker");
			Minion voidcaller = playMinionCard(context, opponent, "minion_voidcaller");
			voidcaller.setHp(2);
			playCard(context, opponent, "minion_explosive_sheep");
			receiveCard(context, opponent, "minion_malganis");
			context.endTurn();
			playCard(context, player, "spell_holy_nova");
			Assert.assertEquals(voidwalker.getAttack(), 3);
			Assert.assertEquals(voidwalker.getHp(), 1);
			Minion malganis = opponent.getMinions().get(1);
			Assert.assertEquals(malganis.getAttack(), 9);
			Assert.assertEquals(malganis.getHp(), 5);
			Assert.assertEquals(opponent.getMinions().size(), 2);
		});
	}

	@Test
	public void testGahzrillaEnchantmentInteractions() {
		runGym((context, player, opponent) -> {
			Minion gahzrilla = playMinionCard(context, player, "minion_gahzrilla");
			playCard(context, player, "minion_lance_carrier");
			Assert.assertEquals(gahzrilla.getAttack(), 8);
			Assert.assertEquals(gahzrilla.getHp(), 9);
			playCard(context, player, "minion_cruel_taskmaster");
			Assert.assertEquals(gahzrilla.getAttack(), 20);
			Assert.assertEquals(gahzrilla.getHp(), 8);
			playCard(context, player, "minion_abusive_sergeant");
			Assert.assertEquals(gahzrilla.getAttack(), 22);
			context.endTurn();
			Assert.assertEquals(gahzrilla.getAttack(), 20);
		});
	}

	@Test
	public void testUnstablePortal() {
		runGym((context, player, opponent) -> {
			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_chillwind_yeti");
			playCard(context, player, "spell_unstable_portal");
			Card yeti = handle.get();
			Assert.assertEquals(costOf(context, player, yeti), yeti.getBaseManaCost() - 3);
		});
	}

	@Test
	public void testCallPet() {
		runGym((context, player, opponent) -> {
			Card dred = shuffleToDeck(context, player, "minion_swamp_king_dred");
			playCard(context, player, "spell_call_pet");
			Assert.assertEquals(costOf(context, player, dred), dred.getBaseManaCost() - 4);
		});

		runGym((context, player, opponent) -> {
			Card yeti = shuffleToDeck(context, player, "minion_chillwind_yeti");
			playCard(context, player, "spell_call_pet");
			Assert.assertEquals(costOf(context, player, yeti), yeti.getBaseManaCost());
		});
	}

	@Test
	public void testBetrayalOnBurlyRockjawTroggDeals5Damage() {
		runGym((context, player, opponent) -> {
			Card adjacentCard1 = new TestMinionCard(1, 5, 0);
			playMinionCard(context, player, adjacentCard1);

			Card targetCard = CardCatalogue.getCardById("minion_burly_rockjaw_trogg");
			Minion targetMinion = playMinionCard(context, player, targetCard);

			Card adjacentCard2 = new TestMinionCard(1, 5, 0);
			playMinionCard(context, player, adjacentCard2);

			context.getLogic().endTurn(player.getId());

			Assert.assertEquals(player.getMinions().size(), 3);
			Card betrayal = CardCatalogue.getCardById("spell_betrayal");

			context.getLogic().receiveCard(opponent.getId(), betrayal);
			GameAction action = betrayal.play();
			action.setTarget(targetMinion);
			context.getLogic().performGameAction(opponent.getId(), action);

			Assert.assertEquals(player.getMinions().size(), 1);
		}, HeroClass.GOLD, HeroClass.BLACK);
	}

	@Test
	public void testSteamwheedleSniper() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_wisp");
			player.setMana(10);
			int actionsBefore = (int) context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(player.getHeroPowerZone().get(0).getReference()))
					.count();
			playCard(context, player, "minion_steamwheedle_sniper");
			int actionsAfter = (int) context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(player.getHeroPowerZone().get(0).getReference()))
					.count();
			assertEquals(actionsBefore, 1);
			assertEquals(actionsAfter, 4);
		}, HeroClass.GREEN, HeroClass.GREEN);
	}
}
