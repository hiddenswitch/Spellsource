package com.blizzard.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestMinionCard;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GoblinsVersusGnomesTests extends TestBase {
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
		GameContext context = createContext(HeroClass.GOLD, HeroClass.BLACK);
		Player paladin = context.getPlayer1();

		MinionCard adjacentMinionCard1 = new TestMinionCard(1, 5, 0);
		playMinionCard(context, paladin, adjacentMinionCard1);

		MinionCard targetMinionCard = (MinionCard) CardCatalogue.getCardById("minion_burly_rockjaw_trogg");
		Minion targetMinion = playMinionCard(context, paladin, targetMinionCard);

		MinionCard adjacentMinionCard2 = new TestMinionCard(1, 5, 0);
		playMinionCard(context, paladin, adjacentMinionCard2);

		context.getLogic().endTurn(paladin.getId());

		Assert.assertEquals(paladin.getMinions().size(), 3);

		Player rogue = context.getPlayer2();

		Card betrayal = CardCatalogue.getCardById("spell_betrayal");

		context.getLogic().receiveCard(rogue.getId(), betrayal);
		GameAction action = betrayal.play();
		action.setTarget(targetMinion);
		context.getLogic().performGameAction(rogue.getId(), action);

		Assert.assertEquals(paladin.getMinions().size(), 1);
	}
}
