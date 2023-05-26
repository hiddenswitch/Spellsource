package com.hiddenswitch.spellsource.tests.cards;

import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
public class TargetingTests extends TestBase {

	@Test
	public void testTaunt() {
		runGym((context, player, opponent) -> {
			Card tauntCard = context.getCardCatalogue().getCardById("minion_test_taunts");
			context.getLogic().receiveCard(opponent.getId(), tauntCard);

			Card attackerCard = receive(context, player, 1, 1, 0);

			context.performAction(opponent.getId(), tauntCard.play());
			context.performAction(player.getId(), attackerCard.play());

			Entity attacker = getSingleMinion(player.getMinions());
			Entity defender = getSingleMinion(opponent.getMinions());
			assertEquals(defender.hasAttribute(Attribute.TAUNT), true);

			List<Entity> validTargets;

			GameAction attackAction = new PhysicalAttackAction(attacker.getReference());
			validTargets = context.getLogic().getValidTargets(player.getId(), attackAction);
			assertEquals(validTargets.size(), 1);

			GameAction fireblast = player.getHeroPowerZone().get(0).play();
			validTargets = context.getLogic().getValidTargets(player.getId(), fireblast);
			assertEquals(validTargets.size(), 4);

			defender.getAttributes().remove(Attribute.TAUNT);

			validTargets = context.getLogic().getValidTargets(player.getId(), attackAction);
			assertEquals(validTargets.size(), 2);

			validTargets = context.getLogic().getValidTargets(player.getId(), fireblast);
			assertEquals(validTargets.size(), 4);

			// taunt should be ignored when the minion is stealthed
			defender.setAttribute(Attribute.TAUNT);
			defender.setAttribute(Attribute.STEALTH);
			validTargets = context.getLogic().getValidTargets(player.getId(), attackAction);
			assertEquals(validTargets.size(), 1);
			assertEquals(validTargets.get(0).getEntityType(), EntityType.HERO);
		}, "JADE", "OBSIDIAN");
	}
}
