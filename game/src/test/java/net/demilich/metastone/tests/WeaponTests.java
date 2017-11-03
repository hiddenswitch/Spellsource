package net.demilich.metastone.tests;

import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.WeaponCard;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;

public class WeaponTests extends TestBase {
	
	@Test
	public void testWeapon() {
		DebugContext context = createContext(HeroClass.RED, HeroClass.RED);
		Player player = context.getPlayer1();
		Hero warrior = player.getHero();

		WeaponCard weaponCard = (WeaponCard) CardCatalogue.getCardById("weapon_test_3_2");

		context.setActivePlayerId(player.getId());
		context.getLogic().startTurn(player.getId());
		Assert.assertEquals(warrior.getAttack(), 0);
		context.getLogic().receiveCard(player.getId(), weaponCard);
		context.getLogic().performGameAction(player.getId(), weaponCard.play());
		Assert.assertEquals(warrior.getAttack(), 3);
		Assert.assertEquals(warrior.getWeapon().getDurability(), 2);

		attack(context, player, warrior, context.getPlayer2().getHero());
		Assert.assertEquals(warrior.getWeapon().getDurability(), 1);
	}
}
