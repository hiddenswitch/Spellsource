package net.demilich.metastone.tests;

import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestSpellCard;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class HeroPowerTest extends TestBase {

	@Test
	public void testArmorUp() {
		runGym((context, player, opponent) -> {
			Hero hero = player.getHero();

			Assert.assertEquals(hero.getHp(), GameLogic.MAX_HERO_HP);

			GameAction armorUp = hero.getHeroPower().play();
			context.getLogic().performGameAction(context.getPlayer1().getId(), armorUp);

			int armorUpBonus = 2;
			Assert.assertEquals(hero.getHp(), GameLogic.MAX_HERO_HP);
			Assert.assertEquals(hero.getArmor(), armorUpBonus);

			SpellDesc damage = DamageSpell.create(EntityReference.FRIENDLY_HERO, 2 * armorUpBonus);
			playCard(context, context.getPlayer1(), new TestSpellCard(damage));
			Assert.assertEquals(hero.getHp(), GameLogic.MAX_HERO_HP - armorUpBonus);
			Assert.assertEquals(hero.getArmor(), 0);

			// there was a bug where armor actually increased the hp of the hero
			// when
			// the damage dealt was less than the total armor. Following test
			// covers that scenario
			context.getLogic().performGameAction(context.getPlayer1().getId(), armorUp);
			damage = DamageSpell.create(EntityReference.FRIENDLY_HERO, armorUpBonus / 2);
			playCard(context, context.getPlayer1(), new TestSpellCard(damage));

			Assert.assertEquals(hero.getHp(), GameLogic.MAX_HERO_HP - armorUpBonus);
			Assert.assertEquals(hero.getArmor(), armorUpBonus / 2);
		}, HeroClass.RED, HeroClass.RED);

	}

	@Test
	public void testFireblast() {
		runGym((context, player, opponent) -> {
			Hero mage = player.getHero();
			Hero victim = opponent.getHero();
			int victimStartHp = victim.getHp();

			GameAction fireblast = mage.getHeroPower().play();
			fireblast.setTarget(victim);
			final int fireblastDamage = 1;
			context.getLogic().performGameAction(context.getPlayer1().getId(), fireblast);
			Assert.assertEquals(victim.getHp(), victimStartHp - fireblastDamage);
		}, HeroClass.BLUE, HeroClass.RED);
	}

	@Test
	public void testLesserHeal() {
		runGym((context, player, opponent) -> {
			Hero priest = player.getHero();

			int lesserHealing = 2;
			priest.setHp(GameLogic.MAX_HERO_HP - lesserHealing);
			Assert.assertEquals(priest.getHp(), GameLogic.MAX_HERO_HP - lesserHealing);

			GameAction lesserHeal = priest.getHeroPower().play();
			lesserHeal.setTarget(priest);
			context.getLogic().performGameAction(context.getPlayer1().getId(), lesserHeal);
			Assert.assertEquals(priest.getHp(), GameLogic.MAX_HERO_HP);
			context.getLogic().performGameAction(context.getPlayer1().getId(), lesserHeal);
			Assert.assertEquals(priest.getHp(), GameLogic.MAX_HERO_HP);
		}, HeroClass.WHITE, HeroClass.RED);

	}

	@Test
	public void testLifeTap() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			Hero warlock = player.getHero();

			Assert.assertEquals(warlock.getHp(), GameLogic.MAX_HERO_HP);

			int cardCount = player.getHand().getCount();
			GameAction lifetap = warlock.getHeroPower().play();
			context.getLogic().performGameAction(player.getId(), lifetap);

			final int lifeTapDamage = 2;
			Assert.assertEquals(warlock.getHp(), GameLogic.MAX_HERO_HP - lifeTapDamage);
			Assert.assertEquals(player.getHand().getCount(), cardCount + 1);
		}, HeroClass.VIOLET, HeroClass.RED);

	}
}
