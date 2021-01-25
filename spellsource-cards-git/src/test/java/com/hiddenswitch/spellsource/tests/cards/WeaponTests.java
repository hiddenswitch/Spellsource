package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.Hero;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class WeaponTests extends TestBase {

	@Test
	public void testWeapon() {
		runGym((context, player, opponent) -> {
			Card weaponCard = CardCatalogue.getCardById("weapon_test_3_2");

			context.setActivePlayerId(player.getId());
			context.getLogic().startTurn(player.getId());
			assertEquals(player.getHero().getAttack(), 0);
			context.getLogic().receiveCard(player.getId(), weaponCard);
			context.performAction(player.getId(), weaponCard.play());
			assertEquals(player.getHero().getAttack() +  player.getWeapon().getAttack(), 3);
			assertEquals(player.getWeaponZone().get(0).getDurability(), 2);

			attack(context, player, player.getHero(), context.getPlayer2().getHero());
			assertEquals(player.getWeaponZone().get(0).getDurability(), 1);
			context.endTurn();
			context.endTurn();
			attack(context, player, player.getHero(), context.getPlayer2().getHero());
			assertTrue(player.getWeaponZone().isEmpty());
		}, "RED", "RED");
	}
}
