package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VampireLordTests extends TestBase {
	@NotNull
	@Override
	public String getDefaultHeroClass() {
		return HeroClass.BLOOD;
	}

	@Test
	public void testYaganLifetaker() {
		runGym((context, player, opponent) -> {
			Minion otherTarget = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			int heroHp = player.getHero().getHp();
			Minion lifetaker = playMinionCard(context, player, "minion_yagan_lifetaker");
			assertEquals(player.getHero().getHp(), heroHp / 2);
			assertEquals(otherTarget.getHp() + lifetaker.getHp(), lifetaker.getBaseHp() + otherTarget.getBaseHp() + (heroHp - heroHp / 2));
		});
	}

	@Test
	public void testLividZealotBadSwarmInteraction() {
		runGym((context, player, opponent) -> {
			Minion batSwarm = playMinionCard(context, player, "minion_vampiric_savage");
			Minion shouldBeBuffed = playMinionCard(context, player, "minion_livid_zealot");
			context.endTurn();
			Minion target = playMinionCard(context, player, "minion_gorthal_the_ravager");
			context.endTurn();
			attack(context, player, batSwarm, target);
			assertTrue(batSwarm.isDestroyed());
			useHeroPower(context, player);
			assertEquals(shouldBeBuffed.getMaxHp(), shouldBeBuffed.getBaseHp() + 2);
		}, HeroClass.BLOOD, HeroClass.NAVY);
	}

	@Test
	public void testBatSwarm() {
		runGym((context, player, opponent) -> {
			// i.e. bat swarm
			Minion shouldBeBuffed = playMinionCard(context, player, "minion_vampiric_savage");
			useHeroPower(context, player);
			assertEquals(shouldBeBuffed.getMaxHp(), shouldBeBuffed.getBaseHp() + 2);
		});
	}

	@Test
	public void testRendingCurseEternalSteedInteraction() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			playMinionCard(context, player, "minion_eternal_steed");
			playCard(context, player, "spell_rending_curse", target);
			destroy(context, target);
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_eternal_steed");
			Minion other = playMinionCard(context, player, "minion_eternal_steed");
			playCard(context, player, "spell_rending_curse", target);
			playCard(context, player, "spell_rending_curse", other);
			destroy(context, target);
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_eternal_steed");
			Minion other = playMinionCard(context, player, "minion_eternal_steed");
			playMinionCard(context, player, "minion_eternal_steed");
			playMinionCard(context, player, "minion_eternal_steed");
			playCard(context, player, "spell_rending_curse", target);
			playCard(context, player, "spell_rending_curse", other);
			destroy(context, target);
		});
	}

	@Test
	public void testLadyUki() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_lady_uki");
			// Opener
			int hp = opponent.getHero().getHp();
			playCard(context, player, "weapon_test_opener_deal_1", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 2, "Doubled damage");

			// Aftermath
			hp = player.getHero().getHp();
			hp = player.getHero().getHp();
			playCard(context, player, "weapon_test_aftermath_deal_1_friendly_hero");
			playCard(context, player, "weapon_test_1_1");
			assertEquals(player.getHero().getHp(), hp - 2);
			// Aura should not "double"
			playCard(context, player, "weapon_test_aura");
			Minion test = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			assertEquals(test.getAttack(), test.getBaseAttack() + 1);
			// Trigger
			playCard(context, player, "weapon_test_trigger");
			hp = player.getHero().getHp();
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHero().getHp(), hp - 2);
		});
	}

	@Test
	public void testSuddenConversion() {
		runGym((context, player, opponent) -> {
			Minion target1 = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			Minion target2 = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			Minion target3 = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			playCard(context, player, "spell_sudden_conversion", target2);
			assertEquals(target2.getHp(), target1.getBaseHp() + target3.getBaseHp() + target2.getBaseHp());
			assertTrue(target1.isDestroyed());
			assertTrue(target3.isDestroyed());
		});
	}

	@Test
	public void testTiramashi() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_tiramashi");
			context.endTurn();
			Minion target = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			context.endTurn();
			playCard(context, player, "spell_test_drain", target);
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), CardCatalogue.getOneOneNeutralMinionCardId());
		});
	}

	@Test
	public void testFinalFeast() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				playCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			}
			context.endTurn();
			for (int i = 0; i < 2; i++) {
				playCard(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			}
			context.endTurn();
			// Minion arrangement
			//    X X     opposing
			// X X X X X  friendly
			playCard(context, player, "spell_final_feast");
			assertEquals(player.getMinions().get(0).getMaxHp(), 5, "drains from champion");
			assertEquals(player.getMinions().get(1).getMaxHp(), 5, "drains from 1 minion");
			assertEquals(player.getMinions().get(2).getMaxHp(), 9, "drains from 2 minions");
			assertEquals(player.getMinions().get(3).getMaxHp(), 5, "drains from 1 minion");
			assertEquals(player.getMinions().get(4).getMaxHp(), 5, "drains from champion");
		});
	}

	@Test
	public void testDoombringerVisha() {
		runGym((context, player, opponent) -> {
			Minion doombringer = playMinionCard(context, player, "minion_doombringer_visha");
			Minion target = playMinionCard(context, player, 10, 10);
			playCard(context, player, "spell_test_drain", target);
			assertEquals(doombringer.getMaxHp(), doombringer.getBaseHp(), "no buff because drain damage");
			playCard(context, player, "spell_test_deal_1", target);
			assertEquals(doombringer.getMaxHp(), doombringer.getBaseHp() + 2, "drain additional 2 from target");
		});
	}

	@Test
	public void testYakhaReiri() {
		runGym((context, player, opponent) -> {
			Card yakhaReiri = receiveCard(context, player, "minion_yakha_reiri");
			Minion target = playMinionCard(context, player, 0, 51);
			castDamageSpell(context, player, 49, target);
			player.setMana(10);
			assertFalse(context.getLogic().conditionMet(player.getId(), yakhaReiri));
			castDamageSpell(context, player, 1, target);
			assertTrue(context.getLogic().conditionMet(player.getId(), yakhaReiri));
		});
	}


	@Test
	public void testLifetaker() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				playMinionCard(context, player, "minion_neutral_test");
			}
			Minion lifetaker = playMinionCard(context, player, "minion_marrow_render");
			assertEquals(lifetaker.getMaxHp(), lifetaker.getBaseHp() + 3);
		});
	}

	@Test
	public void testBloodElfChampion() {
		runGym((context, player, opponent) -> {
			// No opposing minions, no swap
			Minion elf = playMinionCard(context, player, "minion_blood_elf_champion");
			assertEquals(elf.getHp(), 2);
		});

		runGym((context, player, opponent) -> {
			// One opposing minion, swap
			context.endTurn();
			Minion swapped = playMinionCard(context, opponent, "minion_neutral_test");
			swapped.setHp(10);
			context.endTurn();
			Minion elf = playMinionCard(context, player, "minion_blood_elf_champion");
			assertEquals(elf.getHp(), 10);
			assertEquals(swapped.getHp(), 2);
		});

		runGym((context, player, opponent) -> {
			// Two opposing minions, split
			context.endTurn();
			Minion swapped1 = playMinionCard(context, opponent, "minion_neutral_test");
			swapped1.setHp(10);
			Minion swapped2 = playMinionCard(context, opponent, "minion_neutral_test");
			swapped2.setHp(10);
			context.endTurn();
			Minion elf = playMinionCard(context, player, "minion_blood_elf_champion");
			assertEquals(elf.getHp(), 20);
			assertEquals(swapped1.getHp(), 1);
			assertEquals(swapped2.getHp(), 1);
		});

		runGym((context, player, opponent) -> {
			// Two opposing minions, handbuffed, split remainder to first minion
			context.endTurn();
			Minion swapped1 = playMinionCard(context, opponent, "minion_neutral_test");
			swapped1.setHp(10);
			Minion swapped2 = playMinionCard(context, opponent, "minion_neutral_test");
			swapped2.setHp(10);
			context.endTurn();
			Card elfCard = receiveCard(context, player, "minion_blood_elf_champion");
			elfCard.setAttribute(Attribute.HP_BONUS, 1);
			Minion elf = playMinionCard(context, player, elfCard);
			assertEquals(elf.getHp(), 20);
			assertEquals(swapped1.getHp(), 2);
			assertEquals(swapped2.getHp(), 1);
		});
	}


	@Test
	public void testSkullsplitterTroll() {
		runGym((context, player, opponent) -> {
			Minion troll = playMinionCard(context, player, "minion_skullsplitter_troll");
			for (int i = 0; i < 2; i++) {
				playMinionCard(context, player, "minion_neutral_test");
			}
			Minion lifetaker = playMinionCard(context, player, "minion_marrow_render");
			assertEquals(lifetaker.getMaxHp(), lifetaker.getBaseHp() + 3);
			assertEquals(troll.getAttack(), troll.getBaseAttack() + 1, "Draining only occurred once");
		});
	}

}


