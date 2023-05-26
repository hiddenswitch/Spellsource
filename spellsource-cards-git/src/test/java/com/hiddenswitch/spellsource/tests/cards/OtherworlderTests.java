package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
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
			List<Attribute> attributes = new ArrayList<>();
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

	@Test
	public void testBioweaponize() {
		runGym((context, player, opponent) -> {
			Minion source = playMinionCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			playCard(context, player, "spell_bioweaponize", source);
			context.endTurn();
			Minion target = playMinionCard(context, opponent, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			int targetAttack = 10;
			target.setAttack(targetAttack);
			context.endTurn();
			attack(context, player, source, target);
			assertEquals(target.getAttack(), targetAttack - 1 /*wither*/);
		});

		runGym((context, player, opponent) -> {
			Minion parasite = playMinionCard(context, player, "minion_realm_parasite");
			playCard(context, player, "spell_bioweaponize", parasite);
			context.endTurn();
			Minion target = playMinionCard(context, opponent, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			context.endTurn();
			int targetAttack = 10;
			target.setAttack(targetAttack);
			attack(context, player, parasite, target);
			assertEquals(target.getAttack(), targetAttack - 2 /*wither*/);
		});
	}

	@Test
	public void testWitherdrake() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			int targetAttack = 10;
			target.setAttack(targetAttack);
			target.setHp(2);
			context.endTurn();
			playMinionCard(context, player, "minion_witherdrake", target);
			assertEquals(target.getAttack(), targetAttack - 1/*wither*/);
			context.endTurn();
			assertEquals(target.getAttack(), targetAttack - 1/*wither*/);
			context.endTurn();
			assertEquals(target.getAttack(), targetAttack);
		});
	}

	@Test
	public void testHopeEater() {
		runGym((context, player, opponent) -> {
			Minion fourFour = playMinionCard(context, opponent, "minion_saloon_barkeep");
			assertEquals(4, fourFour.getAttack());
			assertEquals(4, fourFour.getHp());
			Minion hopeEater = playMinionCard(context, player, "minion_hope_eater");
			assertEquals(2, fourFour.getAttack());
			destroy(context, hopeEater);
			assertEquals(4, fourFour.getAttack());
		});

		runGym((context, player, opponent) -> {
			Minion fourFour = playMinionCard(context, opponent, "minion_saloon_barkeep");
			assertEquals(4, fourFour.getAttack());
			assertEquals(4, fourFour.getHp());
			Minion hopeEater = playMinionCard(context, player, "minion_hope_eater");
			Minion hopeEater2 = playMinionCard(context, player, "minion_hope_eater");
			assertEquals(1, fourFour.getAttack());
			destroy(context, hopeEater);
			assertEquals(2, fourFour.getAttack());
			destroy(context, hopeEater2);
			assertEquals(4, fourFour.getAttack());
		});

		runGym((context, player, opponent) -> {
			Minion fourFour = playMinionCard(context, opponent, "minion_saloon_barkeep");
			assertEquals(4, fourFour.getAttack());
			assertEquals(4, fourFour.getHp());
			Minion hopeEater = playMinionCard(context, player, "minion_hope_eater");
			Minion hopeEater2 = playMinionCard(context, player, "minion_hope_eater");
			assertEquals(1, fourFour.getAttack());
			destroy(context, hopeEater2);
			assertEquals(2, fourFour.getAttack());
			destroy(context, hopeEater);
			assertEquals(4, fourFour.getAttack());
		});
	}

}
