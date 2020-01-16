package com.hiddenswitch.spellsource.tests.hearthstone;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.aura.BuffAura;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.tests.util.TestBehaviour;
import net.demilich.metastone.tests.util.TestMinionCard;
import net.demilich.metastone.tests.util.TestSpellCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuraTests extends TestBase {

	@Test
	public void testAdjacentAura() {
		runGym((context, player, opponent) -> {
			TestMinionCard card = new TestMinionCard(1, 1);
			Minion testMinion1 = playMinionCard(context, player, card);

			Card direWolfCard = CardCatalogue.getCardById("minion_dire_wolf_alpha");
			Minion direWolf = playMinionCard(context, player, direWolfCard);

			card = new TestMinionCard(5, 5);
			Minion testMinion2 = playMinionCard(context, player, card);
			card = new TestMinionCard(5, 5);
			Minion testMinion3 = playMinionCard(context, player, card);

			assertEquals(direWolf.getAttack(), 2);
			assertEquals(testMinion1.getAttack(), 2);
			assertEquals(testMinion2.getAttack(), 6);
			assertEquals(testMinion3.getAttack(), 5);

			Card destroyCard = new TestSpellCard(DestroySpell.create());
			destroyCard.setTargetRequirement(TargetSelection.ANY);
			context.getLogic().receiveCard(player.getId(), destroyCard);
			GameAction destroyAction = destroyCard.play();
			destroyAction.setTarget(testMinion2);
			context.performAction(player.getId(), destroyAction);
			assertEquals(testMinion1.getAttack(), 2);
			assertEquals(direWolf.getAttack(), 2);
			assertEquals(testMinion3.getAttack(), 6);

			playCard(context, player, "spell_hellfire");
			assertEquals(direWolf.getAttack(), 2);
			assertEquals(testMinion3.getAttack(), 5);
		}, HeroClass.BLUE, HeroClass.RED);
	}

	@Test
	public void testAura() {
		runGym((context, player, opponent) -> {
			TestMinionCard card = new TestMinionCard(1, 1);
			card.getMinion().addEnchantment(new BuffAura(1, 1, EntityReference.OTHER_FRIENDLY_MINIONS, null));
			playCard(context, player, card);

			Actor minion1 = getSingleMinion(player.getMinions());
			assertEquals(minion1.getAttack(), 1);

			card = new TestMinionCard(1, 1);
			card.getMinion().addEnchantment(new BuffAura(1, 1, EntityReference.OTHER_FRIENDLY_MINIONS, null));
			Actor minion2 = playMinionCard(context, player, card);

			assertNotEquals(minion1, minion2);
			assertEquals(minion1.getAttack(), 2);
			assertEquals(minion2.getAttack(), 2);

			TestMinionCard minionCardOpponent = new TestMinionCard(3, 3);
			Actor enemyMinion = playMinionCard(context, opponent, minionCardOpponent);
			assertEquals(enemyMinion.getAttack(), 3);

			assertEquals(minion1.getAttack(), 2);
			assertEquals(minion2.getAttack(), 2);
			PhysicalAttackAction attackAction = new PhysicalAttackAction(enemyMinion.getReference());
			attackAction.setTarget(minion2);
			context.performAction(opponent.getId(), attackAction);
			assertEquals(minion1.getAttack(), 1);

			card = new TestMinionCard(1, 1);
			minion2 = playMinionCard(context, player, card);
			assertEquals(minion1.getAttack(), 1);
			assertEquals(minion2.getAttack(), 2);
		}, HeroClass.BLUE, HeroClass.RED);

	}

	@Test
	public void testAuraPlusFaceless() {
		runGym((context, player, opponent) -> {
			Minion murloc = playMinionCard(context, player, CardCatalogue.getCardById("minion_bluegill_warrior"));
			assertEquals(murloc.getAttack(), 2);
			assertEquals(murloc.getHp(), 1);

			Minion warleader = playMinionCard(context, player, CardCatalogue.getCardById("minion_murloc_warleader"));
			assertEquals(murloc.getAttack(), 4);
			assertEquals(murloc.getHp(), 1);
			assertEquals(warleader.getAttack(), 3);
			assertEquals(warleader.getHp(), 3);

			TestBehaviour behaviour = (TestBehaviour) context.getBehaviours().get(0);
			behaviour.setTargetPreference(warleader.getReference());

			Card facelessCard = CardCatalogue.getCardById("minion_faceless_manipulator");
			context.getLogic().receiveCard(player.getId(), facelessCard);
			GameAction action = facelessCard.play();
			action.setTarget(warleader);
			context.performAction(player.getId(), action);
			assertEquals(murloc.getAttack(), 6);
			assertEquals(murloc.getHp(), 1);
		}, HeroClass.WHITE, HeroClass.RED);
	}

	@Test
	public void testAuraPlusMindControl() {
		runGym((context, player, opponent) -> {
			context.getLogic().endTurn(player.getId());

			TestMinionCard card = new TestMinionCard(1, 1);
			card.getMinion().addEnchantment(new BuffAura(1, 1, EntityReference.FRIENDLY_MINIONS, null));
			Minion auraMinion = playMinionCard(context, opponent, card);
			Minion opponentMinion = playMinionCard(context, opponent, new TestMinionCard(1, 1));
			assertEquals(opponentMinion.getAttack(), 2);
			context.getLogic().endTurn(opponent.getId());

			card = new TestMinionCard(1, 1);
			Actor minion1 = playMinionCard(context, player, card);
			assertEquals(minion1.getAttack(), 1);

			Card mindControlCard = CardCatalogue.getCardById("spell_mind_control");
			context.getLogic().receiveCard(player.getId(), mindControlCard);
			GameAction mindControl = mindControlCard.play();
			mindControl.setTarget(auraMinion);
			context.performAction(player.getId(), mindControl);

			assertEquals(auraMinion.getOwner(), player.getId());
			assertEquals(minion1.getAttack(), 2);
			assertEquals(opponentMinion.getAttack(), 1);
		}, HeroClass.WHITE, HeroClass.RED);
	}

	@Test
	public void testOpponentAuraPlusFaceless() {
		runGym((context, player, opponent) -> {
			Minion wolf = playMinionCard(context, player, CardCatalogue.getCardById("minion_dire_wolf_alpha"));
			assertEquals(wolf.getAttack(), 2);
			assertEquals(wolf.getHp(), 2);

			Minion dummy = playMinionCard(context, player, CardCatalogue.getCardById("minion_target_dummy"));
			assertEquals(dummy.getAttack(), 1);
			assertEquals(dummy.getHp(), 2);
			assertEquals(dummy.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS), false);

			playMinionCard(context, player, CardCatalogue.getCardById("minion_wee_spellstopper"));
			assertEquals(dummy.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS), true);

			context.getLogic().endTurn(player.getId());

			TestBehaviour behaviour = (TestBehaviour) context.getBehaviours().get(1);
			behaviour.setTargetPreference(dummy.getReference());

			Card facelessCard = CardCatalogue.getCardById("minion_faceless_manipulator");
			context.getLogic().receiveCard(opponent.getId(), facelessCard);
			GameAction action = facelessCard.play();
			action.setTarget(dummy);
			context.performAction(opponent.getId(), action);

			Minion facelessCopy = getSummonedMinion(opponent.getMinions());
			assertEquals(facelessCopy.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS), false);
			assertEquals(facelessCopy.getAttack(), 0);
		}, HeroClass.WHITE, HeroClass.RED);
	}

}
