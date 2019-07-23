package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class StorytellerTests extends TestBase {
	@Test
	public void testBookOfLife() {
		runGym((context, player, opponent) -> {
			assertEquals(player.getHand().size(), 0);
			useHeroPower(context, player);
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getSourceCard().getCardId(), "spell_restorative_words");
		}, HeroClass.OLIVE, HeroClass.OLIVE);
	}

	@Test
	public void testEngagingStory() {
		runGym(((context, player, opponent) -> {
			player.getDeck().addCard("spell_lunstone");
			player.getDeck().addCard("spell_lunstone");
			player.getDeck().addCard("spell_lunstone");
			player.getDeck().addCard("spell_lunstone");
			player.getDeck().addCard("spell_lunstone");
			playCard(context, player, "spell_engaging_story");
			assertEquals(player.getHand().size(), 0);
			context.endTurn();
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getDeck().size(), 3);
			context.endTurn();
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 2);
			context.endTurn();
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 2);
		}));
	}

	@Test
	public void testMagicWordMishap() {
		runGym(((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			Minion minion = playMinionCard(context, player, "minion_test_3_2");
			yeti.setHp(3);
			playCard(context, player, "spell_magic_word_mishap", yeti);
			playCard(context, player, "spell_magic_word_mishap", minion);
			assertEquals(yeti.getHp(), yeti.getBaseHp());
			assertFalse(yeti.isDestroyed());
			assertTrue(minion.isDestroyed());
		}));
	}

	@Test
	public void testWritersBlock() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "spell_writers_block");
			context.endTurn();
			opponent.setMana(5);
			playCard(context, opponent, "spell_heal_all_minions");
			assertEquals(opponent.getMana(), 2);
			context.endTurn();
			context.endTurn();
			opponent.setMana(5);
			playCard(context, opponent, "spell_heal_all_minions");
			assertEquals(opponent.getMana(), 5);
		}));
	}

	@Test
	public void testDramaticPlaywright() {
		runGym(((context, player, opponent) -> {
			Minion minion = playMinionCard(context, opponent, "minion_test_3_2");
			Minion playwright = playMinionCard(context, player, "minion_dramatic_playwright");
			destroy(context, playwright);
			assertEquals(minion.getAttack(), 1);
			context.endTurn();
			assertEquals(minion.getAttack(), 1);
			context.endTurn();
			assertEquals(minion.getAttack(), minion.getBaseAttack());
		}));

		runGym(((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_test_3_2");
			Minion playwright = playMinionCard(context, player, "minion_dramatic_playwright");
			destroy(context, playwright);
			assertEquals(minion.getAttack(), minion.getBaseAttack());
		}));

		runGym(((context, player, opponent) -> {
			List<Minion> minions = new ArrayList<>();
			for (int i = 0; i < 7; i++) {
				minions.add(playMinionCard(context, opponent, "minion_test_3_2"));
			}
			Minion playwright = playMinionCard(context, player, "minion_dramatic_playwright");
			destroy(context, playwright);
			int count = 0;
			for (Minion minion : minions) {
				if (minion.getBaseAttack() != minion.getAttack()) {
					count++;
				}
			}
			assertEquals(count, 1);
		}
		));
	}

	@Test
	public void testTallTale() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "secret_tall_tale");
			assertEquals(player.getSecrets().size(), 1);
			playCard(context, player, "spell_frostfire", opponent.getHero());
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			playCard(context, opponent, "spell_5_to_enemy");
			assertEquals(player.getSecrets().size(), 1);
			playCard(context, opponent, "spell_frostfire", player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_skeptic");
		}));
	}

	public void testMiserableConclusion() {
		runGym((context, player, opponent) -> {
			List<Minion> minions = new ArrayList<>();
			for (int i = 0; i < 6; i++) {
				minions.add(playMinionCard(context, player, "minion_test_3_2"));
				minions.add(playMinionCard(context, opponent, "minion_test_3_2"));
			}
			minions.add(playMinionCard(context, player, "minion_test_3_2"));
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_miserable_conclusion");
			for (Minion minion : minions) {
				assertTrue(minion.isDestroyed());
			}
			assertFalse(yeti.isDestroyed());
		});
	}

	@Test
	public void testWhatBigFangs() {
		runGym((context, player, opponent) -> {
			Minion toBeDestroyed = playMinionCard(context, opponent, "minion_dramatic_playwright");
			Minion toAttack = playMinionCard(context, player, "minion_timeworn_archivist");
			playCard(context, player, "spell_what_big_fangs", toAttack);
			attack(context, player, toAttack, toBeDestroyed);
			assertTrue(toBeDestroyed.isDestroyed());
			assertTrue(toAttack.isDestroyed());
		});
	}

	@Test
	public void testManlyMountaineer() {
		runGym((context, player, opponent) -> {
			Minion mountaineer = playMinionCard(context, player, "minion_manly_mountaineer");
			mountaineer.setHp(4);
			attack(context, player, mountaineer, opponent.getHero());
			assertEquals(mountaineer.getHp(), 4);
			Minion minion = playMinionCard(context, opponent, "minion_test_3_2");
			attack(context, player, mountaineer, minion);
			assertEquals(mountaineer.getHp(), 5);
			Minion mountaineer2 = playMinionCard(context, opponent, "minion_manly_mountaineer");
			attack(context, player, mountaineer, mountaineer2);
			assertTrue(mountaineer.isDestroyed());
		});
	}

	@Test
	public void testWhimsicalGenerator() {
		runGym((context, player, opponent) -> {
			Minion minion1 = playMinionCard(context, player, "minion_test_3_2");
			Minion minion2 = playMinionCard(context, player, "minion_test_3_2");
			Minion minion3 = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_whimsical_generator", minion2);
			assertEquals(minion1.getAttack(), minion1.getBaseAttack() + 3);
			assertEquals(minion2.getAttack(), minion2.getBaseAttack() + 3);
			assertEquals(minion3.getAttack(), minion3.getBaseAttack() + 3);
			assertEquals(minion1.getHp(), minion1.getBaseHp() + 4);
			assertEquals(minion2.getHp(), minion2.getBaseHp() + 4);
			assertEquals(minion3.getHp(), minion3.getBaseHp() + 4);
			assertTrue(minion1.hasAttribute(Attribute.TAUNT));
			assertTrue(minion2.hasAttribute(Attribute.TAUNT));
			assertTrue(minion3.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testHouseOfCandy() {
		runGym(((context, player, opponent) -> {
			context.endTurn();
			Minion minion = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			playCard(context, player, "secret_house_of_candy");
			context.endTurn();
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			assertEquals(player.getSecrets().size(), 1);
			playMinionCard(context, opponent, "minion_test_3_2");
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(opponent.getMinions().size(), 2);
			assertTrue(opponent.getMinions().contains(yeti));
			assertTrue(opponent.getMinions().contains(minion));
		}));
	}

	@Test
	public void testTomeOfSecrets() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(5);
			int originalMana = player.getMaxMana();
			playCard(context, player, "spell_tome_of_secrets");
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getSecrets().get(0).getSourceCard().getHeroClass(), "OLIVE");
			assertEquals(player.getMaxMana(), originalMana + 1);
		});
	}
}
