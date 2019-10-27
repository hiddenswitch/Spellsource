package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import org.testng.annotations.Test;

import java.util.Objects;

import static org.testng.Assert.*;

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
}
