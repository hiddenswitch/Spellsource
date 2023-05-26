package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class CoreTests extends TestBase {

	@Test
	public void testBrawlerBushiBuffsBeforeAttack() {
		runGym((context, player, opponent) -> {
			Minion source = playMinionCard(context, player, 3, 4);
			context.endTurn();
			Minion target = playMinionCard(context, opponent, 0, 4);
			context.endTurn();
			playCard(context, player, "minion_brawler_bushi");
			attack(context, player, source, target);
			assertEquals(4, source.getAttack());
			assertEquals(3, source.getHp());
			assertTrue(target.isDestroyed());
		});
	}

	@Test
	public void testYokaiFire() {
		runGym((context, player, opponent) -> {
			Minion demon = playMinionCard(context, player, "minion_demon_test");
			int hp = opponent.getHero().getHp();
			playCard(context, player, "spell_yokai_fire", opponent.getHero());
			assertEquals(hp - 2, opponent.getHero().getHp());
		});

		runGym((context, player, opponent) -> {
			Minion demon = playMinionCard(context, player, "minion_demon_test");
			demon.setHp(3);
			playCard(context, player, "spell_yokai_fire", demon);
			assertEquals(1, demon.getHp());
		});

		runGym((context, player, opponent) -> {
			Minion demon = playMinionCard(context, player, "minion_demon_test");
			destroy(context, demon);
			int hp = opponent.getHero().getHp();
			playCard(context, player, "spell_yokai_fire", opponent.getHero());
			assertEquals(hp - 4, opponent.getHero().getHp());
		});

		runGym((context, player, opponent) -> {
			Minion notDemon = playMinionCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			destroy(context, notDemon);
			int hp = opponent.getHero().getHp();
			playCard(context, player, "spell_yokai_fire", opponent.getHero());
			assertEquals(hp - 2, opponent.getHero().getHp());
		});
	}
}
