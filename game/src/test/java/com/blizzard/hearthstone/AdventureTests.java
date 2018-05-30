package com.blizzard.hearthstone;

import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class AdventureTests extends TestBase {

	@Test
	public void testCurator() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "hero_the_curator");
			Assert.assertTrue(opponent.getHero().hasAttribute(Attribute.TAUNT));
			Minion notTargetable = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			Minion wolfrider = playMinionCard(context, player, "minion_wolfrider");
			Assert.assertTrue(context.getValidActions().stream().noneMatch(ga -> ga.getActionType() == ActionType.PHYSICAL_ATTACK
					&& ga.getTargetReference().equals(notTargetable.getReference())));
		});
	}

	@Test
	public void testMagmatron() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
				shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			}
			Minion magmatron = playMinionCard(context, player, "minion_magmatron");
			context.endTurn();
			Assert.assertEquals(opponent.getHero().getHp(), 28);
		});
	}

	@Test
	@Ignore
	public void testNefarian() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_nefarian");
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play());
			Assert.assertTrue(player.getHand().get(player.getHand().size() - 1).hasHeroClass(opponent.getHero().getHeroClass()));
		}, HeroClass.VIOLET, HeroClass.GOLD);
	}

	@Test
	public void testChieftainScarvash() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_chieftain_scarvash");
			Card friendly = receiveCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(costOf(context, player, friendly), friendly.getBaseManaCost());
			context.endTurn();
			Card enemy = receiveCard(context, opponent, "minion_bloodfen_raptor");
			Assert.assertEquals(costOf(context, player, friendly), friendly.getBaseManaCost());
			Assert.assertEquals(costOf(context, opponent, enemy), enemy.getBaseManaCost() + 1);
		});
	}

}
