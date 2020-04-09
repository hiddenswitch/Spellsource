package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.tests.util.TestMinionCard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
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
		assertEquals(defender.hasAttribute(Attribute.TAUNT), true);

		List<Entity> validTargets;

		GameAction attackAction = new PhysicalAttackAction(attacker.getReference());
		validTargets = context.getLogic().getValidTargets(monk.getId(), attackAction);
		assertEquals(validTargets.size(), 1);

		GameAction fireblast = monk.getHero().getHeroPower().play();
		validTargets = context.getLogic().getValidTargets(monk.getId(), fireblast);
		assertEquals(validTargets.size(), 4);

		defender.getAttributes().remove(Attribute.TAUNT);

		validTargets = context.getLogic().getValidTargets(monk.getId(), attackAction);
		assertEquals(validTargets.size(), 2);

		validTargets = context.getLogic().getValidTargets(monk.getId(), fireblast);
		assertEquals(validTargets.size(), 4);

		// taunt should be ignored when the minion is stealthed
		defender.setAttribute(Attribute.TAUNT);
		defender.setAttribute(Attribute.STEALTH);
		validTargets = context.getLogic().getValidTargets(monk.getId(), attackAction);
		assertEquals(validTargets.size(), 1);
		assertEquals(validTargets.get(0).getEntityType(), EntityType.HERO);

	}

}
