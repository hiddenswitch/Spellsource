package net.demilich.metastone.tests;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestMinionCard;
import net.demilich.metastone.tests.util.TestSpellCard;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.behaviour.AbstractBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.HasChooseOneActions;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.SetHpSpell;
import net.demilich.metastone.game.spells.SilenceSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

public class AdvancedMechanicTests extends TestBase {

	@Test
	public void testChooseOne() {
		GameContext context = createContext(HeroClass.BROWN, HeroClass.RED);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		context.endTurn();
		TestMinionCard minionCard = new TestMinionCard(1, 4);
		playCard(context, opponent, minionCard);
		context.endTurn();

		player.getHero().getHeroPower().markUsed();
		for (Card card : player.getHand().toList()) {
			context.getLogic().removeCard(card);
		}
		Card wrath = CardCatalogue.getCardById("spell_test_choose_one");
		HasChooseOneActions wrathChooseOne = (HasChooseOneActions) wrath;
		context.getLogic().receiveCard(player.getId(), wrath);
		player.setMana(wrath.getBaseManaCost() + 1);
		List<GameAction> validActions = context.getLogic().getValidActions(player.getId());
		Assert.assertEquals(player.getHand().getCount(), 1);
		// player should have 3 valid actions: two from 'Choose One' card and 1 'End Turn'
		Assert.assertEquals(validActions.size(), 3);

		GameAction playWrath = wrathChooseOne.playOptions()[0];
		playWrath.setTarget(getSingleMinion(opponent.getMinions()));
		context.getLogic().performGameAction(player.getId(), playWrath);

		validActions = context.getLogic().getValidActions(player.getId());
		// This time it should just be the 'End Turn'
		Assert.assertEquals(validActions.size(), 1);
		Assert.assertEquals(player.getHand().getCount(), 0);
	}

	@Test
	public void testCopyCards() {
		GameContext context = createContext(HeroClass.WHITE, HeroClass.RED);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();
		clearHand(context, player);

		int cardsInHand = player.getHand().getCount();
		int cardsInOpponentsDeck = opponent.getDeck().getCount();
		Card thoughtsteal = CardCatalogue.getCardById("spell_test_copy_cards");
		context.getLogic().receiveCard(player.getId(), thoughtsteal);
		context.getLogic().performGameAction(player.getId(), thoughtsteal.play());
		Assert.assertEquals(opponent.getDeck().getCount(), cardsInOpponentsDeck);
		Assert.assertEquals(player.getHand().getCount(), cardsInHand + 2);
	}

	@Test
	public void testDivineShield() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player mage = context.getPlayer1();
		mage.setMana(10);
		Player warrior = context.getPlayer2();
		warrior.setMana(10);

		MinionCard minionCard1 = new TestMinionCard(2, 2, Attribute.DIVINE_SHIELD);
		context.getLogic().receiveCard(mage.getId(), minionCard1);
		context.getLogic().performGameAction(mage.getId(), minionCard1.play());

		MinionCard minionCard2 = new TestMinionCard(5, 5);
		context.getLogic().receiveCard(warrior.getId(), minionCard2);
		context.getLogic().performGameAction(warrior.getId(), minionCard2.play());

		Actor attacker = getSingleMinion(mage.getMinions());
		Actor defender = getSingleMinion(warrior.getMinions());

		GameAction attackAction = new PhysicalAttackAction(attacker.getReference());
		attackAction.setTarget(defender);

		context.getLogic().performGameAction(mage.getId(), attackAction);
		Assert.assertEquals(attacker.getHp(), attacker.getMaxHp());
		Assert.assertEquals(defender.getHp(), defender.getMaxHp() - attacker.getAttack());
		Assert.assertEquals(attacker.isDestroyed(), false);

		context.getLogic().performGameAction(mage.getId(), attackAction);
		Assert.assertEquals(attacker.getHp(), attacker.getMaxHp() - defender.getAttack());
		Assert.assertEquals(defender.getHp(), defender.getMaxHp() - attacker.getAttack() * 2);
		Assert.assertEquals(attacker.isDestroyed(), true);
	}

	@Test
	public void testEnrage() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.WHITE);
		Player mage = context.getPlayer1();
		mage.setMana(10);
		Player priest = context.getPlayer2();
		priest.setMana(10);

		final int BASE_ATTACK = 2;
		final int ENRAGE_ATTACK_BONUS = 3;
		playCard(context, priest, CardCatalogue.getCardById("minion_test_enrage"));

		playCard(context, mage, new TestMinionCard(1, 10));

		Entity attacker = getSingleMinion(mage.getMinions());
		Actor defender = getSingleMinion(priest.getMinions());

		Assert.assertEquals(defender.getAttack(), BASE_ATTACK);
		Assert.assertEquals(defender.hasAttribute(Attribute.ENRAGED), false);

		// attack once, should apply the enrage attack bonus
		GameAction attackAction = new PhysicalAttackAction(attacker.getReference());
		attackAction.setTarget(defender);
		context.getLogic().performGameAction(mage.getId(), attackAction);
		Assert.assertEquals(defender.getAttack(), BASE_ATTACK + ENRAGE_ATTACK_BONUS);
		Assert.assertEquals(defender.hasAttribute(Attribute.ENRAGED), true);
		// attack second time, enrage bonus should not increase
		context.getLogic().performGameAction(mage.getId(), attackAction);
		Assert.assertEquals(defender.getAttack(), BASE_ATTACK + ENRAGE_ATTACK_BONUS);

		// heal - enrage attack bonus should be gone
		GameAction healAction = priest.getHero().getHeroPower().play();
		healAction.setTarget(defender);
		context.getLogic().performGameAction(priest.getId(), healAction);
		Assert.assertEquals(defender.getAttack(), BASE_ATTACK);
		Assert.assertEquals(defender.hasAttribute(Attribute.ENRAGED), false);

		// attack once more - should enrage again
		context.getLogic().performGameAction(mage.getId(), attackAction);
		Assert.assertEquals(defender.getAttack(), BASE_ATTACK + ENRAGE_ATTACK_BONUS);
		Assert.assertEquals(defender.hasAttribute(Attribute.ENRAGED), true);

		// attack should be set to 1
		playCardWithTarget(context, mage, CardCatalogue.getCardById("spell_humility"), defender);
		Assert.assertEquals(defender.getAttack(), 1);
		Assert.assertEquals(defender.hasAttribute(Attribute.ENRAGED), true);
	}

	@Test
	public void testOverload() {
		GameContext context = createContext(HeroClass.SILVER, HeroClass.RED);
		Player player = context.getPlayer1();
		int playerId = player.getId();

		context.getLogic().startTurn(playerId);
		Assert.assertEquals(player.getMana(), 1);
		context.getLogic().endTurn(playerId);
		context.getLogic().startTurn(playerId);
		Assert.assertEquals(player.getMana(), 2);

		Card overloadCard = new TestMinionCard(1, 1);
		overloadCard.setAttribute(Attribute.OVERLOAD, 2);
		context.getLogic().receiveCard(playerId, overloadCard);
		context.getLogic().performGameAction(playerId, overloadCard.play());
		context.getLogic().endTurn(playerId);
		context.getLogic().startTurn(playerId);
		Assert.assertEquals(player.getMana(), 1);

		context.getLogic().endTurn(playerId);
		context.getLogic().startTurn(playerId);
		Assert.assertEquals(player.getMana(), 4);
	}

	@Test
	public void testSetHpPlusSilence() {
		GameContext context = createContext(HeroClass.GREEN, HeroClass.RED);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		int baseHp = 5;
		// summon a minion and check the base hp
		playCard(context, opponent, new TestMinionCard(4, baseHp));
		Actor minion = getSingleMinion(opponent.getMinions());
		Assert.assertEquals(minion.getHp(), baseHp);

		int modifiedHp = 1;
		// cast a spell on the minion which modifies the hp
		SpellDesc setHpSpell = SetHpSpell.create(modifiedHp);
		SpellCard spellCard = new TestSpellCard(setHpSpell);
		spellCard.setTargetRequirement(TargetSelection.MINIONS);
		context.getLogic().receiveCard(player.getId(), spellCard);
		GameAction playSpellCard = spellCard.play();
		playSpellCard.setTarget(minion);
		context.getLogic().performGameAction(player.getId(), playSpellCard);
		Assert.assertEquals(minion.getHp(), modifiedHp);
		Assert.assertEquals(minion.getMaxHp(), modifiedHp);

		// silence the creature - hp should be back to original value
		SpellDesc silenceSpell = SilenceSpell.create();
		spellCard = new TestSpellCard(silenceSpell);
		spellCard.setTargetRequirement(TargetSelection.MINIONS);
		context.getLogic().receiveCard(player.getId(), spellCard);
		playSpellCard = spellCard.play();
		playSpellCard.setTarget(minion);
		context.getLogic().performGameAction(player.getId(), playSpellCard);
		Assert.assertEquals(minion.getHp(), baseHp);
	}

	@Test
	public void testShorttermBuffs() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player mage = context.getPlayer1();
		mage.setMana(10);
		Player warrior = context.getPlayer2();
		warrior.setMana(10);

		int baseAttack = 1;
		mage.setBehaviour(new AbstractBehaviour() {

			@Override
			public String getName() {
				return "Select-First";
			}

			@Override
			public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
				return new ArrayList<Card>();
			}

			@Override
			public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
				return validActions.get(0);
			}

		});

		playCard(context, mage, new TestMinionCard(baseAttack, 1));
		Actor testSubject = getSingleMinion(mage.getMinions());
		Assert.assertEquals(testSubject.getAttack(), baseAttack);

		playCard(context, mage, CardCatalogue.getCardById("minion_test_buffs"));
		Assert.assertEquals(testSubject.getAttack(), baseAttack + 2);
		context.getLogic().endTurn(mage.getId());
		Assert.assertEquals(testSubject.getAttack(), baseAttack);
	}

	@Test
	public void testSpellpower() {
		runGym((context, player, opponent) -> {
			player.setMana(10);
			opponent.setMana(10);

			Assert.assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp());
			Card damageSpell = CardCatalogue.getCardById("spell_test_spellpower");
			int expectedDamage = 5;
			context.getLogic().receiveCard(player.getId(), damageSpell);

			context.getLogic().performGameAction(player.getId(), damageSpell.play());
			Assert.assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - expectedDamage);

			MinionCard spellPowerMinionCard = (MinionCard) CardCatalogue.getCardById("minion_test_spellpower");
			context.getLogic().receiveCard(player.getId(), spellPowerMinionCard);
			context.getLogic().performGameAction(player.getId(), spellPowerMinionCard.play());
			damageSpell = damageSpell.getCopy();
			context.getLogic().receiveCard(player.getId(), damageSpell);
			context.getLogic().performGameAction(player.getId(), damageSpell.play());
			int spellPower = getSingleMinion(player.getMinions()).getAttributeValue(Attribute.SPELL_DAMAGE);
			Assert.assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 2 * expectedDamage - spellPower);

			int opponentHp = opponent.getHero().getHp();
			GameAction useHeroPower = player.getHero().getHeroPower().play();
			useHeroPower.setTarget(opponent.getHero());
			context.getLogic().performGameAction(player.getId(), useHeroPower);

			// hero power should not be affected by SPELL_DAMAGE, and thus deal 1 damage
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 1);
		});
	}
}
