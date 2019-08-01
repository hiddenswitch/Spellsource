package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestMinionCard;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TargetingTests extends TestBase {

	@Test
	public void testTaunt() {
		GameContext context = createContext("JADE", "OBSIDIAN");
		Player monk = context.getPlayer1();
		Player victim = context.getPlayer2();

		Card tauntCard = CardCatalogue.getCardById("minion_test_taunts");
		context.getLogic().receiveCard(victim.getId(), tauntCard);

		Card attackerCard = new TestMinionCard(1, 1, 0);
		context.getLogic().receiveCard(monk.getId(), attackerCard);

		context.performAction(victim.getId(), tauntCard.play());
		context.performAction(monk.getId(), attackerCard.play());

		Entity attacker = getSingleMinion(monk.getMinions());
		Entity defender = getSingleMinion(victim.getMinions());
		Assert.assertEquals(defender.hasAttribute(Attribute.TAUNT), true);

		List<Entity> validTargets;

		GameAction attackAction = new PhysicalAttackAction(attacker.getReference());
		validTargets = context.getLogic().getValidTargets(monk.getId(), attackAction);
		Assert.assertEquals(validTargets.size(), 1);

		GameAction fireblast = monk.getHero().getHeroPower().play();
		validTargets = context.getLogic().getValidTargets(monk.getId(), fireblast);
		Assert.assertEquals(validTargets.size(), 4);

		defender.getAttributes().remove(Attribute.TAUNT);

		validTargets = context.getLogic().getValidTargets(monk.getId(), attackAction);
		Assert.assertEquals(validTargets.size(), 2);

		validTargets = context.getLogic().getValidTargets(monk.getId(), fireblast);
		Assert.assertEquals(validTargets.size(), 4);

		// taunt should be ignored when the minion is stealthed
		defender.setAttribute(Attribute.TAUNT);
		defender.setAttribute(Attribute.STEALTH);
		validTargets = context.getLogic().getValidTargets(monk.getId(), attackAction);
		Assert.assertEquals(validTargets.size(), 1);
		Assert.assertEquals(validTargets.get(0).getEntityType(), EntityType.HERO);

	}

}
