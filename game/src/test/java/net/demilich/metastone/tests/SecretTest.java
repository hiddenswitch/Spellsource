package net.demilich.metastone.tests;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestMinionCard;
import net.demilich.metastone.tests.util.TestSecretCard;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SecretTest extends TestBase {

	@Test
	public void testKillingStopsAttack() {
		DebugContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player mage = context.getPlayer1();
		mage.setMana(10);
		Player warrior = context.getPlayer2();
		warrior.setMana(10);

		final int SECRET_DAMAGE = 2;
		playCard(context, mage, new TestSecretCard(SECRET_DAMAGE));
		playCard(context, warrior, new TestMinionCard(2, 3));

		context.setActivePlayerId(warrior.getId());
		Actor minion = getSingleMinion(warrior.getMinions());
		attack(context, warrior, minion, mage.getHero());
		Assert.assertEquals(mage.getHero().getHp(), mage.getHero().getMaxHp() - minion.getAttack());
		Assert.assertEquals(minion.getHp(), minion.getMaxHp() - SECRET_DAMAGE);

		playCard(context, mage, new TestSecretCard(SECRET_DAMAGE));
		attack(context, warrior, minion, mage.getHero());
		Assert.assertTrue(minion.isDestroyed());
		Assert.assertEquals(mage.getHero().getHp(), mage.getHero().getMaxHp() - minion.getAttack());
	}

	@Test
	public void testNewSpellTarget() {
		DebugContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player mage = context.getPlayer1();
		mage.setMana(10);
		Player warrior = context.getPlayer2();
		warrior.setMana(10);

		int fullHp = 10;
		playCard(context, warrior, new TestMinionCard(2, fullHp));

		Actor minion = getSingleMinion(warrior.getMinions());
		context.getLogic().endTurn(mage.getId());

		for (int i = 0; i < 2; i++) {
			playCard(context, mage, "secret_spellbender");
			Assert.assertEquals(mage.getSecrets().size(), 1);

			Card testSpellCard = CardCatalogue.getCardById("spell_frostbolt");
			context.getLogic().receiveCard(warrior.getId(), testSpellCard);
			GameAction spellAttackAction = testSpellCard.play();
			spellAttackAction.setTarget(minion);

			context.setActivePlayerId(warrior.getId());
			context.getLogic().performGameAction(warrior.getId(), spellAttackAction);

			Assert.assertEquals(minion.getHp(), fullHp);
			Assert.assertEquals(warrior.getMinions().size(), 1);

			attack(context, warrior, minion, mage.getHero());
		}

	}

	@Test
	public void testPlayOnlyOnce() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player mage = context.getPlayer1();
		mage.setMana(10);
		Player warrior = context.getPlayer2();
		warrior.setMana(10);

		Card secretCard = new TestSecretCard();
		context.getLogic().receiveCard(mage.getId(), secretCard);
		Assert.assertTrue(context.getLogic().canPlaySecret(mage, secretCard));
		context.getLogic().performGameAction(mage.getId(), secretCard.play());

		Card secretCard2 = new TestSecretCard();
		context.getLogic().receiveCard(mage.getId(), secretCard2);
		Assert.assertFalse(context.getLogic().canPlaySecret(mage, secretCard2));

		Card otherSecret = (Card) CardCatalogue.getCardById("secret_explosive_trap");
		context.getLogic().receiveCard(mage.getId(), otherSecret);
		Assert.assertTrue(context.getLogic().canPlaySecret(mage, otherSecret));
	}

	@Test
	public void testDuplicate() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);

		playCard(context, player, "secret_duplicate");

		Minion novice = playMinionCard(context, player, CardCatalogue.getCardById("minion_novice_engineer"));
		while (player.getHand().getCount() < GameLogic.MAX_HAND_CARDS) {
			playCard(context, player, "minion_novice_engineer");
		}
		Assert.assertEquals(player.getHand().getCount(), GameLogic.MAX_HAND_CARDS);
		context.endTurn();
		playCard(context, opponent, "weapon_fiery_war_axe");

		attack(context, opponent, opponent.getHero(), novice);
		// player has full hand, therefor Duplicate should not have triggered
		Assert.assertEquals(player.getSecrets().size(), 1);
	}

	@Test
	public void testExplosivePlusFreezingTrap() {
		GameContext context = createContext(HeroClass.RED, HeroClass.GREEN);
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);
		clearHand(context, player);
		clearHand(context, opponent);
		clearZone(context, player.getDeck());
		clearZone(context, opponent.getDeck());

		Card card = CardCatalogue.getCardById("minion_wisp");
		Minion minion = playMinionCard(context, player, card);
		context.endTurn();

		Card explosiveTrap = CardCatalogue.getCardById("secret_explosive_trap");
		playCard(context, opponent, explosiveTrap);
		Card freezingTrap = CardCatalogue.getCardById("secret_freezing_trap");
		playCard(context, opponent, freezingTrap);
		context.endTurn();

		Assert.assertEquals(player.getMinions().size(), 1);
		Assert.assertEquals(opponent.getSecrets().size(), 2);

		attack(context, player, minion, opponent.getHero());
		Assert.assertEquals(player.getMinions().size(), 0);
		Assert.assertEquals(opponent.getSecrets().size(), 1);
	}

	@Test
	public void testFreezingPlusBearTrap() {
		GameContext context = createContext(HeroClass.RED, HeroClass.GREEN);
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);
		clearHand(context, player);
		clearHand(context, opponent);
		clearZone(context, player.getDeck());
		clearZone(context, opponent.getDeck());

		Card card = CardCatalogue.getCardById("minion_wisp");
		Minion minion = playMinionCard(context, player, card);
		context.endTurn();

		Card freezingTrap = CardCatalogue.getCardById("secret_freezing_trap");
		playCard(context, opponent, freezingTrap);
		Card explosiveTrap = CardCatalogue.getCardById("secret_bear_trap");
		playCard(context, opponent, explosiveTrap);

		context.endTurn();

		Assert.assertEquals(player.getMinions().size(), 1);
		Assert.assertEquals(opponent.getSecrets().size(), 2);

		attack(context, player, minion, opponent.getHero());
		Assert.assertEquals(player.getMinions().size(), 0);
		Assert.assertEquals(opponent.getSecrets().size(), 1);
	}

	@Test
	public void testIceBlockWithArmor() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		player.getHero().setHp(3);
		player.getHero().setAttribute(Attribute.ARMOR, 10);
		playCard(context, player, "secret_ice_block");
		context.endTurn();

		playCard(context, opponent, CardCatalogue.getCardById("spell_bash"), player.getHero());
		// Ice block should not have triggered, as the Mage had enough armor to
		// prevent fatal damage
		Assert.assertEquals(player.getSecrets().size(), 1);
		Assert.assertFalse(player.getHero().hasAttribute(Attribute.IMMUNE));
	}

	@Test
	public void testAvenge() {
		GameContext context = createContext(HeroClass.GOLD, HeroClass.RED);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		playCard(context, player, "secret_avenge");
		playCard(context, player, "minion_murloc_raider");
		Minion minion = playMinionCard(context, player, CardCatalogue.getCardById("minion_murloc_raider"));
		Assert.assertEquals(player.getSecrets().size(), 1);
		context.endTurn();
		playCard(context, opponent, CardCatalogue.getCardById("spell_bash"), minion);

		Assert.assertEquals(player.getSecrets().size(), 0);
	}

}
