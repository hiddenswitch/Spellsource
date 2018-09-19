package net.demilich.metastone.tests;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WeaponTests extends TestBase {

	@Test
	public void testWeapon() {
		runGym((context, player, opponent) -> {
			Card weaponCard = CardCatalogue.getCardById("weapon_test_3_2");

			Hero warrior = player.getHero();
			context.setActivePlayerId(player.getId());
			context.getLogic().startTurn(player.getId());
			Assert.assertEquals(warrior.getAttack(), 0);
			context.getLogic().receiveCard(player.getId(), weaponCard);
			context.getLogic().performGameAction(player.getId(), weaponCard.play());
			Assert.assertEquals(warrior.getAttack(), 3);
			Assert.assertEquals(warrior.getWeapon().getDurability(), 2);

			attack(context, player, warrior, context.getPlayer2().getHero());
			Assert.assertEquals(warrior.getWeapon().getDurability(), 1);
		}, HeroClass.RED, HeroClass.RED);
	}
}
