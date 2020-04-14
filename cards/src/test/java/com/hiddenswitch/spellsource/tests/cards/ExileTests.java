package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class ExileTests extends TestBase {

	@Test
	public void testLineBreaker() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			context.endTurn();
			context.endTurn();
			attack(context, opponent, attacker, player.getHero());
			context.endTurn();
			Minion shouldHaveBlitz = playMinionCard(context, player, "minion_line_breaker");
			assertTrue(shouldHaveBlitz.hasAttribute(Attribute.CHARGE));
			assertTrue(shouldHaveBlitz.canAttackThisTurn());
			assertTrue(context.getValidActions().stream().anyMatch(ga -> Objects.equals(ga.getSourceReference(), shouldHaveBlitz.getReference()) && Objects.equals(ga.getTargetReference(), opponent.getHero().getReference())));
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			context.endTurn();
			context.endTurn();
			attack(context, opponent, attacker, player.getHero());
			context.endTurn();
			context.endTurn();
			context.endTurn();
			Minion shouldHaveBlitz = playMinionCard(context, player, "minion_line_breaker");
			assertFalse(shouldHaveBlitz.hasAttribute(Attribute.CHARGE));
			assertTrue(context.getValidActions().stream().noneMatch(ga -> Objects.equals(ga.getSourceReference(), shouldHaveBlitz.getReference()) && Objects.equals(ga.getTargetReference(), opponent.getHero().getReference())));
		});
	}

	@Test
	@Disabled("behaviour has changed")
	public void testObsigonBountySorcerer() {
		runGym((context, player, opponent) -> {
			Card obsigon = receiveCard(context, player, "minion_obsigon_bounty_sorcerer");
			assertEquals(obsigon.getDescription(context, player),
					"Dash. Opener: Deal 2 damage to a random enemy. Repeat 0 times. (Increases for each ally that died this turn)");
			Minion attacker = playMinionCard(context, player, "minion_charge_test");
			attack(context, player, attacker, opponent.getHero());
			assertEquals(obsigon.getDescription(context, player),
					"Dash. Opener: Deal 2 damage to a random enemy. Repeat 0 times. (Increases for each ally that died this turn)");
			context.endTurn();
			context.endTurn();
			assertEquals(obsigon.getDescription(context, player),
					"Dash. Opener: Deal 2 damage to a random enemy. Repeat 1 time. (Increases for each ally that died this turn)");
		});
		runGym((context, player, opponent) -> {
			Card obsigon = receiveCard(context, player, "minion_obsigon_bounty_sorcerer");
			assertEquals(obsigon.getDescription(context, player),
					"Dash. Opener: Deal 2 damage to a random enemy. Repeat 0 times. (Increases for each ally that died this turn)");
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, player, attacker, player.getHero());
			context.endTurn();
			assertEquals(obsigon.getDescription(context, player),
					"Dash. Opener: Deal 2 damage to a random enemy. Repeat 1 time. (Increases for each ally that died this turn)");
		});
	}
}
