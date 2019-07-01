package net.demilich.metastone.tests;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.aura.BuffAura;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestBehaviour;
import net.demilich.metastone.tests.util.TestMinionCard;
import net.demilich.metastone.tests.util.TestSpellCard;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AuraTests extends TestBase {

	@Test
	public void testAdjacentAura() {
		GameContext context = createContext("BLUE", "RED");
		Player player = context.getPlayer1();

		TestMinionCard card = new TestMinionCard(1, 1);
		Minion testMinion1 = playMinionCard(context, player, card);

		Card direWolfCard = CardCatalogue.getCardById("minion_dire_wolf_alpha");
		Minion direWolf = playMinionCard(context, player, direWolfCard);

		card = new TestMinionCard(5, 5);
		Minion testMinion2 = playMinionCard(context, player, card);
		card = new TestMinionCard(5, 5);
		Minion testMinion3 = playMinionCard(context, player, card);

		Assert.assertEquals(direWolf.getAttack(), 2);
		Assert.assertEquals(testMinion1.getAttack(), 2);
		Assert.assertEquals(testMinion2.getAttack(), 6);
		Assert.assertEquals(testMinion3.getAttack(), 5);

		Card destroyCard = new TestSpellCard(DestroySpell.create());
		destroyCard.setTargetRequirement(TargetSelection.ANY);
		context.getLogic().receiveCard(player.getId(), destroyCard);
		GameAction destroyAction = destroyCard.play();
		destroyAction.setTarget(testMinion2);
		context.performAction(player.getId(), destroyAction);
		Assert.assertEquals(testMinion1.getAttack(), 2);
		Assert.assertEquals(direWolf.getAttack(), 2);
		Assert.assertEquals(testMinion3.getAttack(), 6);

		playCard(context, player, "spell_hellfire");
		Assert.assertEquals(direWolf.getAttack(), 2);
		Assert.assertEquals(testMinion3.getAttack(), 5);
	}

	@Test
	public void testAura() {
		GameContext context = createContext("BLUE", "RED");
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		TestMinionCard card = new TestMinionCard(1, 1);
		card.getMinion().addEnchantment(new BuffAura(1, 1, EntityReference.OTHER_FRIENDLY_MINIONS, null));
		playCard(context, player, card);

		Actor minion1 = getSingleMinion(player.getMinions());
		Assert.assertEquals(minion1.getAttack(), 1);

		card = new TestMinionCard(1, 1);
		card.getMinion().addEnchantment(new BuffAura(1, 1, EntityReference.OTHER_FRIENDLY_MINIONS, null));
		Actor minion2 = playMinionCard(context, player, card);

		Assert.assertNotEquals(minion1, minion2);
		Assert.assertEquals(minion1.getAttack(), 2);
		Assert.assertEquals(minion2.getAttack(), 2);

		TestMinionCard minionCardOpponent = new TestMinionCard(3, 3);
		Actor enemyMinion = playMinionCard(context, opponent, minionCardOpponent);
		Assert.assertEquals(enemyMinion.getAttack(), 3);

		Assert.assertEquals(minion1.getAttack(), 2);
		Assert.assertEquals(minion2.getAttack(), 2);
		PhysicalAttackAction attackAction = new PhysicalAttackAction(enemyMinion.getReference());
		attackAction.setTarget(minion2);
		context.performAction(opponent.getId(), attackAction);
		Assert.assertEquals(minion1.getAttack(), 1);

		card = new TestMinionCard(1, 1);
		minion2 = playMinionCard(context, player, card);
		Assert.assertEquals(minion1.getAttack(), 1);
		Assert.assertEquals(minion2.getAttack(), 2);
	}

	@Test
	public void testAuraPlusFaceless() {
		GameContext context = createContext("WHITE", "RED");
		Player player = context.getPlayer1();

		Minion murloc = playMinionCard(context, player, CardCatalogue.getCardById("minion_bluegill_warrior"));
		Assert.assertEquals(murloc.getAttack(), 2);
		Assert.assertEquals(murloc.getHp(), 1);

		Minion warleader = playMinionCard(context, player, CardCatalogue.getCardById("minion_murloc_warleader"));
		Assert.assertEquals(murloc.getAttack(), 4);
		Assert.assertEquals(murloc.getHp(), 1);
		Assert.assertEquals(warleader.getAttack(), 3);
		Assert.assertEquals(warleader.getHp(), 3);

		TestBehaviour behaviour = (TestBehaviour) context.getBehaviours().get(0);
		behaviour.setTargetPreference(warleader.getReference());

		Card facelessCard = CardCatalogue.getCardById("minion_faceless_manipulator");
		context.getLogic().receiveCard(player.getId(), facelessCard);
		GameAction action = facelessCard.play();
		action.setTarget(warleader);
		context.performAction(player.getId(), action);
		Assert.assertEquals(murloc.getAttack(), 6);
		Assert.assertEquals(murloc.getHp(), 1);
	}

	@Test
	public void testAuraPlusMindControl() {
		GameContext context = createContext("WHITE", "RED");
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		context.getLogic().endTurn(player.getId());

		TestMinionCard card = new TestMinionCard(1, 1);
		card.getMinion().addEnchantment(new BuffAura(1, 1, EntityReference.FRIENDLY_MINIONS, null));
		Minion auraMinion = playMinionCard(context, opponent, card);
		Minion opponentMinion = playMinionCard(context, opponent, new TestMinionCard(1, 1));
		Assert.assertEquals(opponentMinion.getAttack(), 2);
		context.getLogic().endTurn(opponent.getId());

		card = new TestMinionCard(1, 1);
		Actor minion1 = playMinionCard(context, player, card);
		Assert.assertEquals(minion1.getAttack(), 1);

		Card mindControlCard = CardCatalogue.getCardById("spell_mind_control");
		context.getLogic().receiveCard(player.getId(), mindControlCard);
		GameAction mindControl = mindControlCard.play();
		mindControl.setTarget(auraMinion);
		context.performAction(player.getId(), mindControl);

		Assert.assertEquals(auraMinion.getOwner(), player.getId());
		Assert.assertEquals(minion1.getAttack(), 2);
		Assert.assertEquals(opponentMinion.getAttack(), 1);
	}

	@Test
	public void testOpponentAuraPlusFaceless() {
		GameContext context = createContext("WHITE", "RED");
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		Minion wolf = playMinionCard(context, player, CardCatalogue.getCardById("minion_dire_wolf_alpha"));
		Assert.assertEquals(wolf.getAttack(), 2);
		Assert.assertEquals(wolf.getHp(), 2);

		Minion dummy = playMinionCard(context, player, CardCatalogue.getCardById("minion_target_dummy"));
		Assert.assertEquals(dummy.getAttack(), 1);
		Assert.assertEquals(dummy.getHp(), 2);
		Assert.assertEquals(dummy.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS), false);

		playMinionCard(context, player, CardCatalogue.getCardById("minion_wee_spellstopper"));
		Assert.assertEquals(dummy.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS), true);

		context.getLogic().endTurn(player.getId());

		TestBehaviour behaviour = (TestBehaviour) context.getBehaviours().get(1);
		behaviour.setTargetPreference(dummy.getReference());

		Card facelessCard = CardCatalogue.getCardById("minion_faceless_manipulator");
		context.getLogic().receiveCard(opponent.getId(), facelessCard);
		GameAction action = facelessCard.play();
		action.setTarget(dummy);
		context.performAction(opponent.getId(), action);

		Minion facelessCopy = getSummonedMinion(opponent.getMinions());
		Assert.assertEquals(facelessCopy.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS), false);
		Assert.assertEquals(facelessCopy.getAttack(), 0);
	}

}
