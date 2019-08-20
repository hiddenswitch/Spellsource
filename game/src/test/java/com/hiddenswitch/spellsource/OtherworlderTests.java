package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.minions.Minion;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

public class OtherworlderTests extends TestBase {
	@Test
	public void testFormlessAgony() {
		runGym((context, player, opponent) -> {
			Minion permanent = playMinionCard(context, player, "permanent_formless_agony");
			context.endTurn();
			assertTrue(permanent.isDestroyed());
		});

		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_test_3_2");
			Minion permanent = playMinionCard(context, player, "permanent_formless_agony");
			context.endTurn();
			assertEquals(permanent.isDestroyed(), minion.getHp() == minion.getMaxHp());
		});
	}

	@Test
	public void testIncandescentMutation() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_test_3_2");
			putOnTopOfDeck(context, player, "minion_test_3_2");
			playCard(context, player, "spell_incandescent_mutation");
			assertEquals(player.getHand().size(), 2);

			Minion enemyMinion = playMinionCard(context, opponent, "minion_test_3_2");
			playMinionCard(context, player, player.getHand().get(0));
			assertEquals(enemyMinion.getHp(), enemyMinion.getMaxHp() - 1);

			playMinionCard(context, player, player.getHand().get(0));
			assertEquals(enemyMinion.getHp(), enemyMinion.getMaxHp() - 2);

			assertTrue(enemyMinion.isDestroyed());
		});

		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_test_3_2");
			putOnTopOfDeck(context, player, "minion_test_3_2");
			playCard(context, player, "spell_incandescent_mutation");
			assertEquals(player.getHand().size(), 2);

			Minion enemyMinion1 = playMinionCard(context, opponent, "minion_test_3_2");
			Minion enemyMinion2 = playMinionCard(context, opponent, "minion_test_3_2");
			playMinionCard(context, player, player.getHand().get(0));
			assertNotEquals(enemyMinion1.getHp(), enemyMinion2.getHp());
		});
	}

	@Test
	public void testGekraTheMachine() {
		runGym((context, player, opponent) -> {
			List<Attribute> attributes = new ArrayList();
			attributes.add(Attribute.DIVINE_SHIELD);
			attributes.add(Attribute.TAUNT);
			attributes.add(Attribute.STEALTH);
			attributes.add(Attribute.WINDFURY);

			Minion gekra = playMinionCard(context, player, "minion_gekra_the_machine");
			destroy(context, gekra);
			Card gekraReturned = player.getDeck().get(0);
			assertTrue(gekraReturned.hasAttribute(Attribute.DIVINE_SHIELD)
					|| gekraReturned.hasAttribute(Attribute.STEALTH)
					|| gekraReturned.hasAttribute(Attribute.TAUNT)
					|| gekraReturned.hasAttribute(Attribute.WINDFURY));

			playCard(context, player, "minion_test_draw");
			gekra = playMinionCard(context, player, player.getHand().get(0));
			destroy(context, gekra);
			gekraReturned = player.getDeck().get(0);
			int count = 0;
			for (Attribute attribute : attributes) {
				if (gekraReturned.hasAttribute(attribute)) {
					count++;
				}
			}
			assertEquals(count, 2);

			playCard(context, player, "minion_test_draw");
			gekra = playMinionCard(context, player, player.getHand().get(0));
			destroy(context, gekra);
			gekraReturned = player.getDeck().get(0);
			count = 0;
			for (Attribute attribute : attributes) {
				if (gekraReturned.hasAttribute(attribute)) {
					count++;
				}
			}
			assertEquals(count, 3);

			playCard(context, player, "minion_test_draw");
			gekra = playMinionCard(context, player, player.getHand().get(0));
			destroy(context, gekra);
			gekraReturned = player.getDeck().get(0);
			count = 0;
			for (Attribute attribute : attributes) {
				if (gekraReturned.hasAttribute(attribute)) {
					count++;
				}
			}
			assertEquals(count, 4);

			// At this point, gekra should have all possible attributes and not gain any more when returned to the deck.
			playCard(context, player, "minion_test_draw");
			gekra = playMinionCard(context, player, player.getHand().get(0));
			destroy(context, gekra);
			gekraReturned = player.getDeck().get(0);
			count = 0;
			for (Attribute attribute : attributes) {
				if (gekraReturned.hasAttribute(attribute)) {
					count++;
				}
			}
			assertEquals(count, 4);
		});
	}

	@Test
	public void testTerrainDevourer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_terrain_devourer");
			for (int i = 0; i < 3; i++) {
				playCard(context, player, "minion_test_3_2");
			}
			playCard(context, player, "minion_test_3_2");
			assertEquals(player.getHand().stream().filter(card -> card.getSourceCard().getCardId().equals("minion_test_3_2")).collect(Collectors.toList()).size(), 1);

			Minion minion2 = playMinionCard(context, opponent, "minion_test_3_2");
			assertFalse(minion2.isDestroyed());
			assertTrue(minion2.isInPlay());
		});
	}

	@Test
	public void testCeaselessGhast() {
		runGym((context, player, opponent) -> {
			Minion ghast = playMinionCard(context, player, "minion_ceaseless_ghast");

			destroy(context, ghast);
			ghast = player.getMinions().get(0);
			assertEquals(ghast.getAttack(), 3);

			destroy(context, ghast);
			ghast = player.getMinions().get(0);
			assertEquals(ghast.getAttack(), 2);

			destroy(context, ghast);
			ghast = player.getMinions().get(0);
			assertEquals(ghast.getAttack(), 1);

			destroy(context, ghast);
			assertEquals(player.getMinions().size(), 0);
		});
	}
}
