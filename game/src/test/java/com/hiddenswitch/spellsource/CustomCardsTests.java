package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.events.GameStartEvent;
import net.demilich.metastone.game.events.PreGameStartEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.spells.ChangeHeroPowerSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.*;

public class CustomCardsTests extends TestBase {

	@Test
	public void testBloodPresence() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(27);
			Minion wolfrider = playMinionCard(context, player, "minion_wolfrider");
			SpellDesc spell = new SpellDesc(ChangeHeroPowerSpell.class);
			spell.put(SpellArg.CARD, "hero_power_blood_presence");
			context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, false);
			// Make sure aura actually gets recalculated
			context.getLogic().endOfSequence();
			assertEquals(player.getHero().getHeroPower().getCardId(), "hero_power_blood_presence");
			attack(context, player, wolfrider, opponent.getHero());
			assertEquals(player.getHero().getHp(), 30);
			spell.put(SpellArg.CARD, "hero_power_fireblast");
			context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, false);
			// Make sure aura actually gets recalculated
			context.getLogic().endOfSequence();
			assertFalse(wolfrider.hasAttribute(Attribute.AURA_LIFESTEAL));
			player.getHero().setHp(27);
			attack(context, player, wolfrider, opponent.getHero());
			assertEquals(player.getHero().getHp(), 27);
		});
	}

	@Test
	public void testSilverboneClaw() {
		runGym((context, player, opponent) -> {
			Card dragon = receiveCard(context, player, "token_44dragon");
			playCard(context, player, "weapon_silverbone_claw");
			assertEquals(dragon.getAttack(), dragon.getBaseAttack());
			assertEquals(dragon.getHp(), dragon.getBaseHp());
		});

		runGym((context, player, opponent) -> {
			Card dragon = receiveCard(context, player, "token_44dragon");
			Card dragon2 = receiveCard(context, player, "token_44dragon");
			playCard(context, player, "weapon_silverbone_claw");
			assertEquals(dragon.getBonusAttack(), 2);
			assertEquals(dragon.getBonusHp(), 0);
			assertEquals(dragon2.getBonusAttack(), 2);
			assertEquals(dragon2.getBonusHp(), 0);
		});
	}

	@Test
	public void testSentryJumper() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion source = playMinionCardWithBattlecry(context, player, "minion_sentry_jumper", target);
			assertTrue(target.isDestroyed());
			assertEquals(source.getHp(), source.getBaseHp() - target.getAttack());
		});
	}

	@Test
	public void testFortunaHunter() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_fortuna_hunter");
			Minion buffed = playMinionCard(context, player, "minion_wisp");
			assertEquals(buffed.getAttack(), buffed.getBaseAttack() + 1);
			assertEquals(buffed.getHp(), buffed.getBaseHp() + 1);
			Minion notBuffed = playMinionCard(context, player, "minion_bloodfen_raptor");
			assertEquals(notBuffed.getAttack(), notBuffed.getBaseAttack());
			assertEquals(notBuffed.getHp(), notBuffed.getBaseHp());
			player.setMana(5);
			playMinionCard(context, player, "minion_baby_gryphon");
			// Get the right card
			buffed = player.getMinions().get(player.getMinions().size() - 2);
			// Should have invoked
			assertEquals(buffed.getSourceCard().getCardId(), "minion_baby_gryphon");
			assertTrue(buffed.getSourceCard().hasAttribute(Attribute.INVOKED));
			assertEquals(buffed.getAttack(), buffed.getBaseAttack() + 1);
			assertEquals(buffed.getHp(), buffed.getBaseHp() + 1);
		});
	}

	@Test
	public void testRebelliousFlame() {
		runGym((context, player, opponent) -> {
			Card rebelliousFlame = receiveCard(context, player, "minion_rebellious_flame");
			playCard(context, player, rebelliousFlame);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_rebellious_flame");
		});

		runGym((context, player, opponent) -> {
			Card rebelliousFlame = receiveCard(context, player, "minion_rebellious_flame");
			destroy(context, playMinionCard(context, player, "minion_bloodfen_raptor"));
			Card spellRebelliousFlame = (Card) rebelliousFlame.transformResolved(context);
			assertEquals(spellRebelliousFlame.getCardId(), "spell_rebellious_flame");
			int opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, spellRebelliousFlame, opponent.getHero());
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getHero().getHp(), opponentHp - 3);
		});
	}

	@Test
	public void testGrandArtificerPipiAndWaxGolem() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_grand_artificer_pipi");
			playCard(context, player, "spell_mind_blast");
			Minion waxGolem = player.getMinions().get(1);
			assertEquals(waxGolem.getSourceCard().getCardId(), "token_wax_golem");
			assertEquals(opponent.getHero().getHp(), 30 - 5, "Should have been hit by Mind Blast #1");
			destroy(context, waxGolem);
			assertEquals(opponent.getHero().getHp(), 30 - 10, "Should have been hit by Mind Blast #2");
		});
	}

	@Test
	public void testOzumatOfTheDepths() {
		runGym((context, player, opponent) -> {
			destroy(context, playMinionCard(context, player, "token_ozumat_of_the_depths"));
			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getSecrets().get(0).getSourceCard().getCardId(), "token_ozumat's_nightmare");
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			playCardWithTarget(context, opponent, "spell_fireball", player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_nightmare_tentacle");
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			playCard(context, opponent, "minion_eater_of_secrets");
			assertEquals(player.getSecrets().size(), 0);
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
		});
	}

	@Test
	public void testStudy() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_study");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
		});
	}

	@Test
	public void testPanickedSummoning() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_bloodfen_raptor");
			receiveCard(context, player, "minion_eldritch_horror");
			playCard(context, player, "secret_panicked_summoning");
			context.endTurn();
			Minion wolfrider = playMinionCard(context, opponent, "minion_wolfrider");
			attack(context, opponent, wolfrider, player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "secret_panicked_summoning");
			context.endTurn();
			attack(context, opponent, wolfrider, player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_bloodfen_raptor");
		});
	}

	@Test
	public void testLanternCarrier() {
		runGym((context, player, opponent) -> {
			Minion lanternCarrier = playMinionCard(context, player, "minion_lantern_carrier");
			assertEquals(lanternCarrier.getAttack(), lanternCarrier.getBaseAttack());
			assertEquals(lanternCarrier.getHp(), lanternCarrier.getBaseHp());
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 1);
			assertEquals(bloodfen.getHp(), bloodfen.getBaseHp() + 1);

		});
	}

	@Test
	public void testSignsOfTheEnd() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_signs_of_the_end");
			assertEquals(player.getMinions().size(), 0);
			playCard(context, player, "spell_the_coin");
			assertEquals(player.getMinions().get(0).getSourceCard().getBaseManaCost(), 0);
		});
	}

	@Test
	public void testSouldrinkerDrake() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_souldrinker_drake");
			Card fireball = receiveCard(context, player, "spell_fireball");
			Card fireball2 = receiveCard(context, player, "spell_fireball");
			player.getHero().setHp(1);
			playCardWithTarget(context, player, fireball, opponent.getHero());
			assertEquals(player.getHero().getHp(), 1 + 6);
			playCardWithTarget(context, player, fireball2, opponent.getHero());
			assertEquals(player.getHero().getHp(), 1 + 6, "Lifesteal should not have been applied");
		});

		runGym((context, player, opponent) -> {
			// Souldrinker Drake should give Watchful Gaze, a secret, lifesteal
			playMinionCard(context, player, "minion_souldrinker_drake");
			playCard(context, player, "secret_watchful_gaze");
			context.endTurn();
			player.getHero().setHp(1);
			playCard(context, opponent, "minion_bloodfen_raptor");
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getHero().getHp(), 1 + 8);
		});
	}

	@Test
	public void testSkuggTheUnclean() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_skugg_the_unclean");
			player.setMana(3);
			playCard(context, player, "minion_hoardling_of_tolin");
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "token_skugg_rat");
		});
	}

	@Test
	public void testMagmaSpewer() {
		runGym((context, player, opponent) -> {
			// 2 mana total
			playCard(context, player, "minion_fire_fly");
			playCard(context, player, "minion_fire_fly");
			context.endTurn();
			context.endTurn();
			// 4 mana total
			playCard(context, player, "minion_water_elemental");
			context.endTurn();
			context.endTurn();
			// 8 mana total
			playMinionCard(context, player, "minion_thrakdos_the_hollow");
			int opponentHp = opponent.getHero().getHp();
			playMinionCardWithBattlecry(context, player, "minion_magma_spewer", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 4);
		});
	}

	@Test
	public void testMadProphetRosea() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_mad_prophet_rosea");
			player.setMana(9);
			Card theCoin = receiveCard(context, player, "spell_the_coin");
			assertEquals(costOf(context, player, theCoin), 0);
			playCard(context, player, theCoin);
			assertEquals(player.getMana(), 10);
			assertEquals(player.getMinions().size(), 1, "Only Mad Prophet Rosea");
			theCoin = receiveCard(context, player, "spell_the_coin");
			assertEquals(costOf(context, player, theCoin), 0, "Aura Invoke is gone!");
		});

		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_mad_prophet_rosea");
			player.setMana(10);
			Card theCoin = receiveCard(context, player, "spell_the_coin");
			assertEquals(costOf(context, player, theCoin), 10, "Aura Invoke applies");
			playCard(context, player, theCoin);
			assertEquals(player.getMana(), 1, "The coin gained 1 mana");
			assertEquals(player.getMinions().size(), 2, "Two: Mad Prophet Rosea & Yoth'al");
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_yoth'al_the_devourer");
		});
	}

	@Test
	public void testPathOfFrost() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_target_dummy");
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, player, "spell_path_of_frost", attacker);
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			attack(context, opponent, attacker, target);
			assertEquals(opponent.getHero().getHp(), opponentHp - attacker.getAttack());
			assertEquals(target.getHp(), target.getBaseHp());
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_target_dummy");
			context.endTurn();
			Minion attacker = playMinionCard(context, player, "minion_wolfrider");
			playCardWithTarget(context, player, "spell_path_of_frost", attacker);
			int opponentHp = opponent.getHero().getHp();
			attack(context, player, attacker, target);
			assertEquals(opponent.getHero().getHp(), opponentHp - attacker.getAttack());
			assertEquals(target.getHp(), target.getBaseHp());
		});
	}

	@Test
	public void testRafaamPhilanthropist() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "minion_bloodfen_raptor");
			playMinionCard(context, player, "minion_rafaam_philanthropist");
			assertEquals(opponent.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testParadoxNoggenfoggerAssassinateInteraction() {
		runGym((context, player, opponent) -> {
			Minion paradox = playMinionCard(context, player, "minion_paradox");
			Minion noggenfogger = playMinionCard(context, player, "minion_mayor_noggenfogger");
			playCardWithTarget(context, player, "spell_assassinate", paradox);
		});
	}

	@Test
	public void testMiniKnight() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				receiveCard(context, player, "minion_bloodfen_raptor");
			}
			Minion knight = playMinionCard(context, player, "minion_mini_knight");
			context.getLogic().endOfSequence();
			assertEquals(knight.getAttack(), knight.getBaseAttack());
			receiveCard(context, player, "minion_bloodfen_raptor");
			context.getLogic().endOfSequence();
			assertEquals(knight.getAttack(), knight.getBaseAttack() + 1);
			context.getLogic().removeCard(player.getHand().get(0));
			context.getLogic().endOfSequence();
			assertEquals(knight.getAttack(), knight.getBaseAttack() + 1);
		});
	}

	@Test
	public void testScissorsofDots() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scissors_of_dots");
			assertTrue(player.getHero().canAttackThisTurn(), "Should be able to attack now");
			attack(context, player, player.getHero(), opponent.getHero());
			assertTrue(player.getHero().canAttackThisTurn(), "Should be able to attack still (Windfury weapon)");
			attack(context, player, player.getHero(), opponent.getHero());
			assertFalse(player.getHero().canAttackThisTurn(), "Should NOT be able to attack still (Windfury weapon)");
		});
	}

	@Test
	public void testEnergeticMentee() {
		runGym((context, player, opponent) -> {
			player.setMana(2);
			playMinionCard(context, player, "minion_energetic_mentee");
			assertEquals(player.getMinions().size(), 1);
		});
		runGym((context, player, opponent) -> {
			player.setMana(3);
			playMinionCard(context, player, "minion_energetic_mentee");
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_deathwhelp");
		});
	}

	@Test
	public void testEvilCounterpart() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();

			// Does not trigger invoke
			player.setMana(7);
			Card card = receiveCard(context, player, "spell_evil_counterpart");
			assertEquals(costOf(context, player, card), 4);
			playCardWithTarget(context, player, card, target);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMana(), 7 - 4);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();

			// Does trigger invoke
			player.setMana(8);
			Card card = receiveCard(context, player, "spell_evil_counterpart");
			assertEquals(costOf(context, player, card), 8);
			playCardWithTarget(context, player, card, target);
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMana(), 0);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();

			// Does not trigger invoke
			player.setMana(5);
			playCard(context, player, "spell_preparation");
			Card card = receiveCard(context, player, "spell_evil_counterpart");
			assertEquals(costOf(context, player, card), 1, "Discounted by Preparation");
			playCardWithTarget(context, player, card, target);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMana(), 5 - 4 + 3);
		});
	}

	@Test
	public void testHaplessKnight() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion greaterAttack = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion lessAttack = playMinionCard(context, opponent, "minion_argent_squire");
			context.endTurn();
			Minion source = playMinionCard(context, player, "minion_hapless_knight");
			// Just opponent's hero and Argent Squire
			assertEquals(getPhysicalAttackActionStream(context).count(), 2L);
			assertTrue(getPhysicalAttackActionStream(context).noneMatch(ga -> ga.getTargetReference().equals(greaterAttack.getReference())));
			assertEquals(getPhysicalAttackActionStream(context).filter(ga -> ga.getTargetReference().equals(lessAttack.getReference())).count(), 1L);
		});
	}

	@NotNull
	public Stream<PhysicalAttackAction> getPhysicalAttackActionStream(GameContext context) {
		return context.getValidActions().stream().filter(ga -> ga instanceof PhysicalAttackAction)
				.map(ga -> (PhysicalAttackAction) ga);
	}

	@Test
	public void testVoidReaper() {
		runGym((context, player, opponent) -> {

			for (Player p : new Player[]{player, opponent}) {
				shuffleToDeck(context, p, "minion_bloodfen_raptor");
				shuffleToDeck(context, p, "minion_argent_squire");
				shuffleToDeck(context, p, "minion_argent_squire");
				receiveCard(context, p, "minion_bloodfen_raptor");
				receiveCard(context, p, "minion_argent_squire");
				receiveCard(context, p, "minion_argent_squire");
				playMinionCard(context, p, "minion_bloodfen_raptor");
				playMinionCard(context, p, "minion_argent_squire");
				playMinionCard(context, p, "minion_argent_squire");
			}

			// Removing bloodfen should leave two of everything on the board
			playMinionCardWithBattlecry(context, player, "minion_void_reaper",
					player.getMinions().get(0));

			assertEquals(player.getMinions().size(), 2 + 1, "2 argents + void reaper");
			for (Player p : new Player[]{player, opponent}) {
				assertEquals(p.getHand().size(), 2);
				assertEquals(p.getDeck().size(), 2);
			}
			assertEquals(opponent.getMinions().size(), 2);
		});
	}

	@Test
	public void testShieldOfNature() {
		runGym((context, player, opponent) -> {
			// Using life tap with shield of nature should not stack overflow
			playCard(context, player, "weapon_shield_of_nature");
			Weapon shield = player.getWeaponZone().get(0);
			player.setMana(2);
			context.getLogic().performGameAction(player.getId(), player.getHero().getHeroPower().play());
			// It should have run out of durability and been put to the graveyard
			assertEquals(shield.getZone(), Zones.GRAVEYARD);
		}, HeroClass.VIOLET, HeroClass.VIOLET);
	}

	@Test
	public void testForcesOfGilneas() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_forces_of_gilneas");
			for (int i = 0; i < 5; i++) {
				playCard(context, player, "spell_summon_for_opponent");
			}
			assertEquals(player.getHand().get(0).getCardId(), "minion_king_archibald");
		});
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_forces_of_gilneas");
			for (int i = 0; i < 5; i++) {
				playCard(context, player, "minion_bloodfen_raptor");
			}
			assertEquals(player.getQuests().size(), 1);
			assertEquals(player.getHand().size(), 0);
		});
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_forces_of_gilneas");
			context.endTurn();
			for (int i = 0; i < 5; i++) {
				playCard(context, opponent, "minion_bloodfen_raptor");
			}
			assertEquals(player.getQuests().size(), 1);
			assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_forces_of_gilneas");
			context.endTurn();
			for (int i = 0; i < 5; i++) {
				playCard(context, opponent, "spell_summon_for_opponent");
			}
			assertEquals(player.getQuests().size(), 1);
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testSuspiciousWanderer() {
		runGym((context, player, opponent) -> {
			Card card = receiveCard(context, player, "minion_suspicious_wanderer");
			Minion minion1 = playMinionCard(context, player, card);
			Minion minion2 = playMinionCard(context, player, player.getHand().get(0));
			context.endTurn();
			assertEquals(player.getHand().size(), 0);
			assertEquals(minion1.getZone(), Zones.BATTLEFIELD);
			assertEquals(minion2.getZone(), Zones.BATTLEFIELD);
		});
	}

	@Test
	public void testSlamhammerKnight() {
		// Test divine shield
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_slamhammer_knight");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_argent_squire");
			Minion bigMinion = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			attack(context, player, attacker, target);
			assertEquals(target.getHp(), 1);
			assertEquals(bigMinion.getHp(), bigMinion.getBaseHp() - attacker.getAttack());
			assertEquals(attacker.getHp(), attacker.getBaseHp() - target.getAttack());
		});

		// Confirm that minions go into negative HP
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_slamhammer_knight");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion bigMinion = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			attack(context, player, attacker, target);
			assertTrue(target.isDestroyed());
			assertEquals(bigMinion.getHp(), bigMinion.getBaseHp() - attacker.getAttack() + 2);
			assertEquals(attacker.getHp(), attacker.getBaseHp() - target.getAttack());
		});

		// If there's no excess damage, no damage
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_slamhammer_knight");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion bigMinion = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			attack(context, player, attacker, target);
			assertFalse(target.isDestroyed());
			assertEquals(bigMinion.getHp(), bigMinion.getBaseHp());
			assertEquals(attacker.getHp(), attacker.getBaseHp() - target.getAttack());
		});
	}

	@Test
	public void testMistyManaTea() {
		// Test basic
		runGym((context, player, opponent) -> {
			player.setMana(8);
			for (int i = 0; i < 8; i++) {
				playCardWithTarget(context, player, "spell_razorpetal", opponent.getHero());
			}
			assertEquals(player.getMana(), 0);
			playCard(context, player, "spell_misty_mana_tea");
			assertEquals(player.getMana(), 4);
		});

		// Test basic
		runGym((context, player, opponent) -> {
			player.setMana(7);
			for (int i = 0; i < 7; i++) {
				playCardWithTarget(context, player, "spell_razorpetal", opponent.getHero());
			}
			assertEquals(player.getMana(), 0);
			assertEquals(player.getAttributeValue(Attribute.MANA_SPENT_THIS_TURN), 7);
			playCard(context, player, "spell_misty_mana_tea");
			assertEquals(player.getMana(), 0);
		});


		// Test spend all your mana effects
		runGym((context, player, opponent) -> {
			player.setMana(8);
			playCard(context, player, "spell_spend_all_your_mana");
			assertEquals(player.getMana(), 0);
			playCard(context, player, "spell_misty_mana_tea");
			assertEquals(player.getMana(), 4);
		});

		// Test resets at end of turn
		runGym((context, player, opponent) -> {
			player.setMana(7);
			playCard(context, player, "spell_spend_all_your_mana");
			assertEquals(player.getMana(), 0);
			context.endTurn();
			context.endTurn();
			player.setMana(1);
			playCard(context, player, "spell_spend_all_your_mana");
			assertEquals(player.getMana(), 0);
			playCard(context, player, "spell_misty_mana_tea");
			assertEquals(player.getMana(), 0);
		});
	}

	@Test
	public void testTouchOfKarma() {
		// Target friendly
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_wolfrider");
			playCardWithTarget(context, player, "spell_touch_of_karma", attacker);
			int hp = opponent.getHero().getHp();
			attack(context, player, attacker, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - attacker.getAttack() - 2);
		});

		// Target opponent
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_wolfrider");
			context.endTurn();
			playCardWithTarget(context, player, "spell_touch_of_karma", attacker);
			context.endTurn();
			int hp = opponent.getHero().getHp();
			attack(context, opponent, attacker, player.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 2);
		});
	}

	@Test
	public void testKegSmash() {
		// Test regular
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion target2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion target3 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, player, "spell_keg_smash", target2);
			playCardWithTarget(context, player, "spell_razorpetal", target3);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
			playCardWithTarget(context, player, "spell_razorpetal", target1);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
		});

		// Test AoE
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion target2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion target3 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, player, "spell_keg_smash", target2);
			playCard(context, player, "spell_arcane_explosion");
			assertTrue(target1.isDestroyed());
			assertTrue(target2.isDestroyed());
			assertTrue(target3.isDestroyed());
			context.endTurn();
			Minion target4 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			assertFalse(target4.isDestroyed());
		});

		// Test hero attacking
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion target2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion target3 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, player, "spell_keg_smash", target2);
			playCard(context, player, "weapon_wicked_knife");

			attack(context, player, player.getHero(), target3);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
			playCardWithTarget(context, player, "spell_razorpetal", target1);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
		});

		// Test keg smash playing more than once
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion target2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion target3 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, player, "spell_keg_smash", target2);
			playCardWithTarget(context, player, "spell_razorpetal", target3);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
			playCardWithTarget(context, player, "spell_razorpetal", target1);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());

			// end some turns
			for (int i = 0; i < 3; i++) {
				context.endTurn();
				context.endTurn();
			}

			// keg smash again
			context.endTurn();
			target1 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			target2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			target3 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, player, "spell_keg_smash", target2);
			playCardWithTarget(context, player, "spell_razorpetal", target3);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
			playCardWithTarget(context, player, "spell_razorpetal", target1);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
		});
	}

	@Test
	public void testEnvelopingMists() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(1);
			Minion twoHp = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_enveloping_mists", twoHp);
			assertEquals(player.getHero().getHp(), 4);
		});
	}

	@Test
	public void testDampenHarm() {
		runGym((context, player, opponent) -> {
			// Your minions can only take 1 damage at a time until the start of your next turn.
			Minion target = playMinionCard(context, player, "minion_sleepy_dragon");
			playCard(context, player, "spell_dampen_harm");
			playCardWithTarget(context, player, "spell_fireball", target);
			assertEquals(target.getHp(), target.getBaseHp() - 1);
			playCardWithTarget(context, player, "spell_fireball", target);
			assertEquals(target.getHp(), target.getBaseHp() - 2);
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_wolfrider");
			attack(context, opponent, attacker, target);
			assertEquals(target.getHp(), target.getBaseHp() - 3);
		});
	}

	@Test
	public void testEchoOfGuldan() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "token_echo_of_guldan");
			int hp = player.getHero().getHp();
			playMinionCard(context, player, "minion_bloodfen_raptor");
			assertEquals(player.getHero().getHp(), hp - 2);
		});
	}

	@Test
	public void testEmeraldDreamEscapeFromDurnholdeDesolationOfKareshInSameGame() {
		runGym((context, player, opponent) -> {
			Minion emeraldDream = playMinionCard(context, player, "permanent_the_emerald_dream");
			Minion escapeFromDurnholde = playMinionCard(context, player, "permanent_escape_from_durnholde");
			Minion desolationOfKaresh = playMinionCard(context, player, "permanent_desolation_of_karesh");
			Minion twoTwoWisp = playMinionCard(context, player, "minion_wisp");
			Minion threeThreeWisp = playMinionCard(context, player, "minion_wisp");
			playMinionCardWithBattlecry(context, player, "minion_undercity_valiant", opponent.getHero());
			assertEquals(twoTwoWisp.getAttack(), twoTwoWisp.getBaseAttack() + 1);
			assertEquals(twoTwoWisp.getHp(), twoTwoWisp.getBaseHp() + 1);
			assertEquals(threeThreeWisp.getAttack(), threeThreeWisp.getBaseAttack() + 2);
			assertEquals(threeThreeWisp.getHp(), threeThreeWisp.getBaseHp() + 2);
			assertEquals(desolationOfKaresh.getAttributeValue(Attribute.RESERVED_INTEGER_1), 2);
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "spell_the_coin");
			}
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHand().size(), 3);
		});
	}

	@Test
	public void testEndOfTheLineSapInteraction() {
		// Ensure minion without taunt no longer has taunt after End of the Line 'd + Sap'ped
		runGym((context, player, opponent) -> {
			context.endTurn();
			shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			playCard(context, opponent, "spell_end_of_the_line");
			Minion bloodfen = playMinionCard(context, opponent, opponent.getHand().get(0));
			assertTrue(bloodfen.hasAttribute(Attribute.TAUNT));
			assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 5);
			context.endTurn();
			playCardWithTarget(context, player, "spell_sap", bloodfen);
			context.endTurn();
			bloodfen = playMinionCard(context, opponent, opponent.getHand().get(0));
			assertFalse(bloodfen.hasAttribute(Attribute.TAUNT));
			assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack());
		});
	}

	@Test
	public void testInstantEvolution() {
		runGym((context, player, opponent) -> {
			// Adds up to more than 12
			Minion target = playMinionCard(context, player, "minion_arcane_giant");
			playCardWithTarget(context, player, "spell_instant_evolution", target);
			assertEquals(target.transformResolved(context).getSourceCard().getBaseManaCost(), 12);
		});
	}

	@Test
	public void testShadowhornStag() {
		runGym((context, player, opponent) -> {
			Minion stag = playMinionCard(context, player, "minion_shadowhorn_stag");
			context.getLogic().setHpAndMaxHp(stag, 100);
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_wisp");
			Minion target2 = playMinionCard(context, opponent, "minion_wisp");
			context.endTurn();
			assertTrue(stag.canAttackThisTurn());
			assertTrue(context.getLogic().getValidActions(player.getId()).stream().anyMatch(ga -> ga.getSourceReference().equals(stag.getReference())));
			attack(context, player, stag, target1);
			assertTrue(context.getLogic().getValidActions(player.getId()).stream().anyMatch(ga -> ga.getSourceReference().equals(stag.getReference())));
			attack(context, player, stag, target2);
			assertTrue(context.getLogic().getValidActions(player.getId()).stream().anyMatch(ga -> ga.getSourceReference().equals(stag.getReference())));
			attack(context, player, stag, opponent.getHero());
			assertFalse(context.getLogic().getValidActions(player.getId()).stream().anyMatch(ga -> {
				EntityReference sourceReference = ga.getSourceReference();
				return sourceReference != null && sourceReference.equals(stag.getReference());
			}));
		});
	}

	@Test
	public void testScavengerThrun() {
		runGym((context, player, opponent) -> {
			Minion bloodfen1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion scavengerThrun = playMinionCard(context, player, "minion_scavenger_thrun");
			Minion bloodfen2 = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion killThis = playMinionCard(context, player, "minion_bloodfen_raptor");
			AtomicReference<String> adapted = new AtomicReference<>(null);
			overrideDiscover(context, player, discoverActions -> {
				adapted.set(discoverActions.get(0).getCard().getName());
				return discoverActions.get(0);
			});
			destroy(context, killThis);
			assertNotAdapted(adapted.get(), scavengerThrun);
			assertAdapted(adapted.get(), bloodfen1);
			assertAdapted(adapted.get(), bloodfen2);
		});
	}

	@Test
	@Ignore
	public void testANewChallenger() {
		runGym((context, player, opponent) -> {
			overrideRandomCard(context, "hero_nefarian");
			playCard(context, player, "spell_a_new_challenger");
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_nefarian");
			final String[] nefarianCards = (String[]) CardCatalogue.getCardById("hero_nefarian").getDesc()
					.getBattlecry().getSpell().subSpells(0).get(1).get(SpellArg.CARDS);
			final int drawnCards = (int) CardCatalogue.getCardById("hero_nefarian").getDesc()
					.getBattlecry().getSpell().subSpells(0).get(2).get(SpellArg.VALUE);
			// Draws a card
			assertEquals(player.getDeck().size(), nefarianCards.length - drawnCards);
		});
	}

	@Test
	public void testPrinceTenris() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_prince_tenris");
			assertEquals(player.getHero().getAttack(), 1);
			context.endTurn();
			assertEquals(player.getHero().getAttack(), 0);
			context.endTurn();
			assertEquals(player.getHero().getAttack(), 1);
		});
	}

	@Test
	public void testFelGiant() {
		runGym((context, player, opponent) -> {
			// Prevents fatigue damage
			putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			Card card = receiveCard(context, player, "minion_fel_giant");
			assertEquals(costOf(context, player, card), card.getBaseManaCost());
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play());
			assertEquals(costOf(context, player, card), card.getBaseManaCost() - 2);
		}, HeroClass.VIOLET, HeroClass.VIOLET);
	}

	@Test
	public void testDeepBorer() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			receiveCard(context, player, "minion_deep_borer");
			context.endTurn();
			assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			assertEquals(player.getDeck().get(0).getCardId(), "minion_deep_borer");
		});

		// Should not produce infinite loop
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_deep_borer");
			receiveCard(context, player, "minion_deep_borer");
			context.endTurn();
			assertEquals(player.getHand().get(0).getCardId(), "minion_deep_borer");
			assertEquals(player.getDeck().get(0).getCardId(), "minion_deep_borer");
		});
	}

	@Test
	public void testAnnoyingBeetle() {
		runGym((context, player, opponent) -> {
			Minion annoyingBeetle = playMinionCard(context, player, "minion_annoying_beetle");
			assertEquals(opponent.getHeroPowerZone().get(0).getCardId(), "hero_power_die_insect");
			context.endTurn();
			GameLogic spy = spy(context.getLogic());
			context.setLogic(spy);
			doAnswer(invocation -> player.getHero()).when(spy).getRandom(anyList());
			int hp = player.getHero().getHp();
			context.getLogic().performGameAction(opponent.getId(), opponent.getHeroPowerZone().get(0).play());
			assertEquals(player.getHero().getHp(), hp - 8);
			context.endTurn();

			destroy(context, annoyingBeetle);
			assertEquals(opponent.getHeroPowerZone().get(0).getCardId(), "hero_power_fireblast");
		}, HeroClass.BLUE, HeroClass.BLUE);
	}

	@Test
	public void testYouFromTheFutureKargath() {
		// You from the Future on Kargath Baldefist causes doubly triggered end of turn effects
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_kargath_bladefist");
			playCardWithTarget(context, player, "spell_you_from_the_future", target);
			Minion target2 = player.getMinions().get(1);
			Assert.assertTrue(target.isWounded());
			Assert.assertTrue(target2.isWounded());
			context.endTurn();
			assertEquals(target.getAttack(), target.getBaseAttack() + 4);
			assertEquals(target2.getAttack(), target2.getBaseAttack() + 4);
		});
	}

	@Test
	public void testTheEmeraldDream() {
		runGym((context, player, opponent) -> {
			Minion emeraldDream = playMinionCard(context, player, "permanent_the_emerald_dream");
			int count = 0;
			Minion snowflipper;
			for (int i = 0; i < 5; i++) {
				snowflipper = playMinionCard(context, player, "minion_snowflipper_penguin");
				count++;
				assertEquals(snowflipper.getAttack(), snowflipper.getBaseAttack() + count);
				assertEquals(snowflipper.getHp(), snowflipper.getBaseHp() + count);
			}
		});
	}

	@Test
	public void testFrenziedDiabolist() {
		runGym((context, player, opponent) -> {
			Card card1 = receiveCard(context, player, "minion_bloodfen_raptor");
			Card card2 = receiveCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_doomguard");
			Assert.assertTrue(card1.hasAttribute(Attribute.DISCARDED));
			Assert.assertTrue(card2.hasAttribute(Attribute.DISCARDED));
			CountDownLatch latch = new CountDownLatch(1);
			overrideDiscover(context, player, discoverActions -> {
				latch.countDown();
				assertEquals(discoverActions.size(), 1, "Should not show duplicate cards due to discover rules");
				assertEquals(discoverActions.get(0).getCard().getCardId(), "minion_bloodfen_raptor");
				return discoverActions.get(0);
			});
			playCard(context, player, "minion_frenzied_diabolist");
			assertEquals(latch.getCount(), 0);
		});
	}

	@Test
	public void testDreadCaptainBones() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_wicked_knife");
			final Weapon weapon = player.getWeaponZone().get(0);
			assertEquals(weapon.getDurability(), weapon.getBaseDurability());
			playCard(context, player, "minion_dread_captain_bones");
			assertEquals(weapon.getDurability(), weapon.getBaseDurability() + 1);
		});
	}

	@Test
	public void testFarseerNobundo() {
		// Test that battlecries from the hand are triggered.
		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Card startedInDeck = putOnTopOfDeck(context, player, "token_searing_totem");
			Card startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_king_mukla");
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			assertEquals(onBoardBefore.getAttack(), 1);
			assertEquals(onBoardBefore.getHp(), 1);
			assertEquals(opponent.getHand().size(), 2, "The opponent should have two bananas at the moment.");
			playCard(context, player, startedInHand);
			assertEquals(opponent.getHand().size(), 4, "The opponent should have four bananas.");
			context.endTurn();
			context.endTurn();
			assertEquals(startedInDeck.getZone(), Zones.HAND);
			playCard(context, player, startedInDeck);
			assertEquals(opponent.getHand().size(), 6);
		});

		// Test auras and triggers
		runGym((context, player, opponent) -> {
			int stormwinds = 0;
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Card startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_stormwind_champion");
			stormwinds++;
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			stormwinds++;
			assertEquals(onBoardBefore.getAttack(), onBoardBefore.getBaseAttack() + stormwinds - 1);
			assertEquals(onBoardBefore.getHp(), onBoardBefore.getBaseHp() + stormwinds - 1);
			playCard(context, player, startedInHand);
			stormwinds++;
			assertEquals(onBoardBefore.getAttack(), onBoardBefore.getBaseAttack() + stormwinds - 1);
			assertEquals(onBoardBefore.getHp(), onBoardBefore.getBaseHp() + stormwinds - 1);
		});

		runGym((context, player, opponent) -> {
			int clerics = 0;
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Card startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_northshire_cleric");
			clerics++;
			Minion damaged = playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			clerics++;
			playCard(context, player, startedInHand);
			clerics++;
			damaged.setHp(damaged.getHp() - 1);
			Assert.assertTrue(damaged.isWounded());
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			assertEquals(player.getHand().size(), 0);
			playCardWithTarget(context, player, "hero_power_heal", damaged);
			assertEquals(player.getHand().size(), clerics);
		});

		// Test deathrattle
		runGym((context, player, opponent) -> {
			int lootHoarders = 0;
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Card startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_loot_hoarder");
			lootHoarders++;
			Minion damaged = playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			lootHoarders++;
			playCard(context, player, startedInHand);
			lootHoarders++;
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_twisting_nether");
			assertEquals(player.getHand().size(), lootHoarders);
		});

		// Test copies text attribute of source card even when silenced
		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_argent_Squire");
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			Assert.assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
			playCardWithTarget(context, player, "spell_silence", copyCard);
			Assert.assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
		});

		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_argent_Squire");
			playCardWithTarget(context, player, "spell_silence", copyCard);
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			Assert.assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
		});

		// Test does not copy non-text attributes (buffs or whatever)
		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_argent_Squire");
			playCardWithTarget(context, player, "spell_windfury", copyCard);
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			Assert.assertFalse(onBoardBefore.hasAttribute(Attribute.WINDFURY));
		});
	}

	@Test
	public void testTheEndTime() {
		runGym((context, player, opponent) -> {
			Minion endTime = playMinionCard(context, player, "permanent_the_end_time");
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 20);
			context.endTurn();
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 19);
			context.endTurn();
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 19);
			context.endTurn();
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 17);
		});

		runGym((context, player, opponent) -> {
			Minion endTime = playMinionCard(context, player, "permanent_the_end_time");
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 20);
			endTime.setAttribute(Attribute.RESERVED_INTEGER_1, 1);
			context.endTurn();
			assertEquals(context.getStatus(), GameStatus.WON);
			assertEquals(context.getWinningPlayerId(), player.getId());
		});
	}

	@Test
	public void testSpaceMoorine() {
		runGym((context, player, opponent) -> {
			Minion spaceMoorine = playMinionCard(context, player, "minion_space_moorine");
			Assert.assertFalse(spaceMoorine.hasAttribute(Attribute.AURA_TAUNT));
			playCard(context, player, "spell_iron_hide");
			Assert.assertTrue(spaceMoorine.hasAttribute(Attribute.AURA_TAUNT));
			context.endTurn();
			Minion wolfrider = playMinionCard(context, opponent, "minion_wolfrider");
			Assert.assertTrue(context.getValidActions().stream().filter(va -> va.getActionType() == ActionType.PHYSICAL_ATTACK)
					.allMatch(t -> t.getTargetReference().equals(spaceMoorine.getReference())));
		});
	}

	@Test
	public void testArmageddonVanguard() {
		runGym((context, player, opponent) -> {
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion armageddon = playMinionCard(context, player, "minion_armageddon_vanguard");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, opponent, "spell_razorpetal", bloodfen);
			assertEquals(opponent.getHero().getHp(), opponentHp - 1);
		});

		runGym((context, player, opponent) -> {
			GameLogic spyLogic = spy(context.getLogic());
			context.setLogic(spyLogic);

			final Minion armageddon1 = playMinionCard(context, player, "minion_armageddon_vanguard");
			context.endTurn();
			final Minion armageddon2 = playMinionCard(context, opponent, "minion_armageddon_vanguard");
			doAnswer(invocation -> {
				List<Entity> randomTargets = invocation.getArgument(0);
				if (randomTargets.contains(armageddon1)) {
					return armageddon1;
				} else if (randomTargets.contains(armageddon2)) {
					return armageddon2;
				} else {
					throw new AssertionError("Unexpected random request");
				}
			}).when(spyLogic).getRandom(anyList());

			while (!armageddon1.isDestroyed()) {
				playCardWithTarget(context, opponent, "spell_razorpetal", armageddon1);
			}
			Assert.assertTrue(armageddon1.isDestroyed());
			Assert.assertTrue(armageddon2.isDestroyed());
		});

		runGym((context, player, opponent) -> {
			final Minion armageddon1 = playMinionCard(context, player, "minion_armageddon_vanguard");
			Minion target = playMinionCard(context, player, "minion_snowflipper_penguin");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, opponent, "spell_razorpetal", target);
			assertEquals(opponent.getHero().getHp(), opponentHp - 1);
		});
	}

	@Test
	public void testVindicatorMaraad() {
		runGym((context, player, opponent) -> {
			Card cost1Card = putOnTopOfDeck(context, player, "minion_argent_squire");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "spell_mirror_image");
			assertEquals(player.getHand().get(0), cost1Card);
		});

		runGym((context, player, opponent) -> {
			Card cost2Card = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "spell_mirror_image");
			assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Card cost1Card = putOnTopOfDeck(context, player, "minion_argent_squire");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "minion_bloodfen_raptor");
			assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Card cost2Card = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "minion_bloodfen_raptor");
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testEscapeFromDurnholde() {
		runGym((context, player, opponent) -> {
			Card shouldntDraw = putOnTopOfDeck(context, player, "spell_the_coin");
			Card shouldDraw = putOnTopOfDeck(context, player, "spell_the_coin");
			assertEquals(shouldntDraw.getZone(), Zones.DECK);
			assertEquals(shouldDraw.getZone(), Zones.DECK);
			playCard(context, player, "permanent_escape_from_durnholde");
			context.endTurn();
			assertEquals(shouldntDraw.getZone(), Zones.DECK);
			assertEquals(shouldDraw.getZone(), Zones.DECK);
			context.endTurn();
			assertEquals(shouldDraw.getZone(), Zones.HAND);
			assertEquals(shouldntDraw.getZone(), Zones.DECK);
		});

		runGym((context, player, opponent) -> {
			Card shouldDraw1 = putOnTopOfDeck(context, player, "spell_the_coin");
			Card shouldDraw2 = putOnTopOfDeck(context, player, "spell_the_coin");
			playCard(context, player, "permanent_escape_from_durnholde");
			playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			context.endTurn();
			assertEquals(shouldDraw1.getZone(), Zones.HAND);
			assertEquals(shouldDraw2.getZone(), Zones.HAND);
		});
	}

	@Test
	public void testHypnotist() {
		runGym((context, player, opponent) -> {
			Minion moltenGiant = playMinionCard(context, player, "minion_molten_giant");
			playMinionCardWithBattlecry(context, player, "minion_hypnotist", moltenGiant);
			assertEquals(moltenGiant.getAttack(), moltenGiant.getSourceCard().getBaseManaCost());
			assertEquals(moltenGiant.getHp(), moltenGiant.getSourceCard().getBaseManaCost());
		});

		runGym((context, player, opponent) -> {
			Card giantCard = receiveCard(context, player, "minion_molten_giant");
			// Reduce its effective cost
			playCardWithTarget(context, player, "spell_pyroblast", player.getHero());
			final int pyroblastDamage = 10;
			assertEquals(costOf(context, player, giantCard), giantCard.getBaseManaCost() - pyroblastDamage);
			Minion giant = playMinionCard(context, player, giantCard);
			playMinionCardWithBattlecry(context, player, "minion_hypnotist", giant);
			assertEquals(giant.getHp(), giant.getSourceCard().getBaseManaCost(), "Hypnotist should set hp to base cost.");
			assertEquals(giant.getAttack(), giant.getSourceCard().getBaseManaCost(), "Hypnotist should set attack to base cost.");
		});
	}

	@Test
	public void testDesolationOfKaresh() {
		// No combos played, should die
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			context.endTurn();
			Assert.assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, should die in 2 turns
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			Assert.assertFalse(desolation.isDestroyed());
			context.endTurn();
			context.endTurn();
			Assert.assertTrue(desolation.isDestroyed());
		});

		// Not combo card played, should die next turn
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			Assert.assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, then not activated combo card played. Should die in 2 turns.
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			Assert.assertFalse(desolation.isDestroyed());
			context.endTurn();
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			Assert.assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, then not activated combo card played, then activated combo card played. Should die in 3 turns.
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			Assert.assertFalse(desolation.isDestroyed());
			context.endTurn();
			playCard(context, player, "minion_defias_ringleader");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			Assert.assertFalse(desolation.isDestroyed());
			context.endTurn();
			context.endTurn();
			Assert.assertTrue(desolation.isDestroyed());
		});
	}

	@Test
	public void testShadowOfThePast() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "minion_wolfrider");
			playCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Minion shadow = playMinionCard(context, player, "minion_shadow_of_the_past");
			playCard(context, player, "minion_boulderfist_ogre");
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", shadow);
			assertEquals(player.getHand().get(0).getCardId(), "spell_fireball");
		});
	}

	@Test
	public void testOwnWorstEnemey() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_own_worst_enemy");
			Minion target = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			Minion source = playMinionCard(context, opponent, "minion_wolfrider");
			attack(context, opponent, source, target);
			Assert.assertTrue(source.isDestroyed());
			Assert.assertFalse(target.isDestroyed());
			Assert.assertTrue(player.getGraveyard().stream().anyMatch(c -> c.getEntityType() == EntityType.MINION
					&& c.getSourceCard().getCardId().equals("minion_wolfrider")));
		});
	}

	@Test
	public void testInfiniteTimereaver() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Card toDraw = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_infinite_timereaver");
			playCardWithTarget(context, player, "spell_fireball", target);
			assertEquals(player.getHand().get(0), toDraw);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Card toDraw = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_infinite_timereaver");
			playCard(context, player, "spell_flamestrike");
			assertEquals(player.getHand().get(0), toDraw);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_infinite_timereaver");
			playCardWithTarget(context, player, "spell_razorpetal", target);
			assertEquals(player.getHand().size(), 0);
			playCardWithTarget(context, player, "spell_razorpetal", target);
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testFreya() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_freya");
			Minion nordrassil = player.getMinions().get(1);
			assertEquals(nordrassil.getSourceCard().getCardId(), "permanent_seed_of_nordrassil");
			assertEquals(nordrassil.getAttributeValue(Attribute.RESERVED_INTEGER_1), 0, "Freya should not trigger Seed");
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			assertEquals(nordrassil.getAttributeValue(Attribute.RESERVED_INTEGER_1), bloodfen.getAttack() + bloodfen.getHp());
			for (int i = 0; i < 2; i++) {
				playCard(context, player, "minion_faceless_behemoth");
			}

			assertEquals(nordrassil.transformResolved(context).getSourceCard().getCardId(), "token_nordrassil", "Seed transformed into Nordrassil");
		});
	}

	@Test
	public void testTheBigGameHunt() {
		runGym((context, player, opponent) -> {
			Minion bigGameHunt = playMinionCard(context, player, "permanent_the_big_game_hunt");
			int elapsedLocalPlayerTurns = 0;
			Minion bloodfen1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion bloodfen2 = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion bloodfen3 = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			elapsedLocalPlayerTurns++;
			Minion bloodfen4 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion bloodfen5 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion bloodfen6 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			// one point for player
			attack(context, player, bloodfen1, bloodfen4);
			context.endTurn();
			elapsedLocalPlayerTurns++;
			// two points for opponent
			attack(context, opponent, bloodfen5, bloodfen2);
			attack(context, opponent, bloodfen6, bloodfen3);
			context.endTurn();
			for (int i = elapsedLocalPlayerTurns; i < 4; i++) {
				context.endTurn();
				context.endTurn();
			}
			Assert.assertTrue(bigGameHunt.isDestroyed());
			// Should be a total of -1
			assertEquals(bigGameHunt.getAttributeValue(Attribute.RESERVED_INTEGER_1), -1);
			Minion kingBangalash1 = player.getMinions().get(0);
			assertEquals(kingBangalash1.getSourceCard().getCardId(), "minion_king_bangalash");
			assertEquals(kingBangalash1.getAttack(), kingBangalash1.getBaseAttack() - 1);
			assertEquals(kingBangalash1.getHp(), kingBangalash1.getBaseHp() - 1);

			// Play a King Bangalash from the hand, observe it has the same buffs.
			Minion kingBangalash2 = playMinionCard(context, player, "minion_king_bangalash");
			assertEquals(kingBangalash2.getAttack(), kingBangalash2.getBaseAttack() - 1);
			assertEquals(kingBangalash2.getHp(), kingBangalash2.getBaseHp() - 1);
		});
	}

	@Test
	public void testLieInWait() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_lie_in_wait");
			context.endTurn();
			Minion wolfrider = playMinionCard(context, opponent, "minion_wolfrider");
			attack(context, opponent, wolfrider, player.getHero());

			assertEquals(player.getWeaponZone().get(0).getDurability(),
					CardCatalogue.getCardById("weapon_eaglehorn_bow").getBaseDurability() - 1,
					"Eaglehorn Bow loses durability because the secret triggered before it was in play.");
			Assert.assertTrue(wolfrider.isDestroyed());
		});
	}

	@Test
	public void testFifiFizzlewarp() {
		// Test that cards that have race-filtered battlecries work correctly after Fifi Fizzlewarp
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_boulderfist_ogre");

			for (int i = 0; i < 2; i++) {
				putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			}

			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_virmen_sensei");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();

			context.getLogic().discardCard(player, fifi);

			for (int i = 0; i < 3; i++) {
				context.getLogic().drawCard(player.getId(), player);
			}

			for (Card card : player.getHand().subList(0, 2)) {
				assertEquals(card.getCardId(), "minion_virmen_sensei");
				assertEquals(card.getRace(), Race.BEAST);
			}

			final Card boulderfist = player.getHand().get(2);
			assertEquals(boulderfist.getCardId(), "minion_virmen_sensei");
			assertEquals(boulderfist.getRace(), Race.NONE);

			final Card vermin1 = player.getHand().get(0);
			final Card vermin2 = player.getHand().get(1);

			Minion target = playMinionCard(context, player, vermin1);
			Minion notTarget = playMinionCard(context, player, boulderfist);

			CountDownLatch latch = new CountDownLatch(1);
			// Checks that a Virmen Sensei can target the Beast Virmen Sensei on the board and not the Race.NONE
			// Virmen Sensei that was created from the Boulderfist Ogre
			overrideBattlecry(context, player, battlecryActions -> {
				assertEquals(battlecryActions.size(), 1);
				assertEquals(battlecryActions.get(0).getTargetReference(), target.getReference());
				latch.countDown();
				return battlecryActions.get(0);
			});

			playCard(context, player, vermin2);
			assertEquals(latch.getCount(), 0, "Should have requested battlecries");
		});

		// Tol'Vir Warden should interact correctly with cards transformed by Fifi Fizzlewarp
		runGym((context, player, opponent) -> {
			// Cost 1 card
			Card shouldBeDrawn = putOnTopOfDeck(context, player, "minion_dire_mole");
			// Cost 2 card
			Card shouldNotBeDrawn = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			Card tolvirToPlay = putOnTopOfDeck(context, player, "minion_dire_mole");

			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_tolvir_warden");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();

			context.getLogic().discardCard(player, fifi);
			Card drawnCard = context.getLogic().drawCard(player.getId(), player);
			assertEquals(drawnCard, tolvirToPlay.transformResolved(context));
			tolvirToPlay = (Card) tolvirToPlay.transformResolved(context);
			shouldBeDrawn = (Card) shouldBeDrawn.transformResolved(context);
			shouldNotBeDrawn = (Card) shouldNotBeDrawn.transformResolved(context);

			playCard(context, player, tolvirToPlay);
			assertEquals(shouldBeDrawn.getZone(), Zones.HAND);
			assertEquals(shouldNotBeDrawn.getZone(), Zones.DECK);
		});

		// Getting Divine Shield minions from Fifi Fizzlewarp should work
		runGym((context, player, opponent) -> {
			Card shouldBeDrawn = putOnTopOfDeck(context, player, "minion_dire_mole");
			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_argent_squire");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();
			context.getLogic().discardCard(player, fifi);
			Card drawnCard = context.getLogic().drawCard(player.getId(), player);
			assertEquals(drawnCard, shouldBeDrawn.transformResolved(context));
			shouldBeDrawn = (Card) shouldBeDrawn.transformResolved(context);
			Minion argentSquire = playMinionCard(context, player, shouldBeDrawn);
			Assert.assertTrue(argentSquire.hasAttribute(Attribute.DIVINE_SHIELD));
		});

		// Test specifically Tirion's deathrattle
		runGym((context, player, opponent) -> {
			Card shouldBeDrawn = putOnTopOfDeck(context, player, "minion_dire_mole");
			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_tirion_fordring");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();
			context.getLogic().discardCard(player, fifi);
			Card drawnCard = context.getLogic().drawCard(player.getId(), player);
			assertEquals(drawnCard, shouldBeDrawn.transformResolved(context));
			shouldBeDrawn = (Card) shouldBeDrawn.transformResolved(context);
			Minion tirion = playMinionCard(context, player, shouldBeDrawn);
			playCardWithTarget(context, player, "spell_fireball", tirion);
			playCardWithTarget(context, player, "spell_fireball", tirion);
			Assert.assertTrue(tirion.isDestroyed());
			assertEquals(player.getHero().getWeapon().getSourceCard().getCardId(), "weapon_ashbringer");
		});

		// Test Leyline Manipulator doesn't reduce cost of fifi cards
		runGym((context, player, opponent) -> {
			Card shouldBeDrawn = putOnTopOfDeck(context, player, "minion_dire_mole");
			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_tirion_fordring");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();
			context.getLogic().discardCard(player, fifi);
			Card drawnCard = context.getLogic().drawCard(player.getId(), player);
			assertEquals(drawnCard, shouldBeDrawn.transformResolved(context));
			shouldBeDrawn = (Card) shouldBeDrawn.transformResolved(context);
			playCard(context, player, "minion_leyline_manipulator");
			assertEquals(costOf(context, player, shouldBeDrawn), CardCatalogue.getCardById("minion_dire_mole").getBaseManaCost());
		});
	}

	@Test
	public void testParadoxKingTogwaggleInteraction() {
		runGym((context, player, opponent) -> {
			Minion paradox = playMinionCard(context, player, "minion_paradox");
			playCard(context, player, "minion_king_togwaggle");
			assertEquals(player.getSetAsideZone().size(), 0);
			assertEquals(player.getHand().get(0).getCardId(), "minion_paradox");
		});
	}

	@Test
	public void testParadox() {
		runGym((context, player, opponent) -> {
			Minion paradox = playMinionCard(context, player, "minion_paradox");
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_paradox");
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_the_coin");
			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getHand().get(0).getCardId(), "minion_paradox");
		});
	}

	@Test
	public void testEchoOfMalfurion() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_bloodfen_raptor");
			Card boulderfist = receiveCard(context, player, "minion_boulderfist_ogre");
			Minion echo = playMinionCard(context, player, "token_echo_of_malfurion");
			assertEquals(echo.getAttack(), boulderfist.getAttack() + echo.getBaseAttack());
			assertEquals(echo.getHp(), boulderfist.getHp() + echo.getBaseHp());
		});

		runGym((context, player, opponent) -> {
			Minion echo = playMinionCard(context, player, "token_echo_of_malfurion");
			assertEquals(echo.getAttack(), echo.getBaseAttack());
			assertEquals(echo.getHp(), echo.getBaseHp());
		});
	}

	@Test
	public void testChromie() {
		// Test no excess cards
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 6; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
				receiveCard(context, player, "spell_mirror_image");
			}

			playCard(context, player, "minion_chromie");

			Assert.assertTrue(player.getHand().stream().allMatch(c -> c.getCardId().equals("minion_bloodfen_raptor")));
			Assert.assertTrue(player.getDeck().stream().allMatch(c -> c.getCardId().equals("spell_mirror_image")));
			assertEquals(player.getHand().size(), 6);
			assertEquals(player.getDeck().size(), 6);
		});

		// Test excess cards
		runGym((context, player, opponent) -> {
			player.getGraveyard().clear();

			for (int i = 0; i < 20; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}

			for (int i = 0; i < 6; i++) {
				receiveCard(context, player, "spell_mirror_image");
			}

			playCard(context, player, "minion_chromie");

			Assert.assertTrue(player.getHand().stream().allMatch(c -> c.getCardId().equals("minion_bloodfen_raptor")));
			Assert.assertTrue(player.getDeck().stream().allMatch(c -> c.getCardId().equals("spell_mirror_image")));
			assertEquals(player.getHand().size(), 10);
			assertEquals(player.getDeck().size(), 6);
		});
	}

	@Test
	public void testHighmountainPrimalist() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 29; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			overrideDiscover(context, player, "spell_mirror_image");
			playCard(context, player, "minion_highmountain_primalist");
			playCard(context, player, "minion_novice_engineer");
			assertEquals(player.getHand().get(0).getCardId(), "spell_mirror_image");
		});
	}

	@Test
	public void testDimensionalCourier() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_dimensional_courier");
			assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_snowflipper_penguin");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_dimensional_courier");
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testPermanentCallOfTheCrusade() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "permanent_call_of_the_crusade");
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			for (int i = 0; i < 3; i++) {
				assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 1);
				context.endTurn();
				context.endTurn();
			}
			assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack());
			assertEquals(player.getMinions().size(), 1);
		});
	}

	@Test
	public void testHandsOnHistorian() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			for (int i = 0; i < 3; i++) {
				receiveCard(context, player, "minion_bloodfen_raptor");
			}
			int size = player.getHand().size();
			playCard(context, player, "minion_hands_on_historian");
			assertEquals(player.getHand().size(), size);
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			for (int i = 0; i < 2; i++) {
				receiveCard(context, player, "minion_bloodfen_raptor");
			}
			int size = player.getHand().size();
			playCard(context, player, "minion_hands_on_historian");
			assertEquals(player.getHand().size(), size + 1);
		});
	}

	@Test
	public void testVampiricTouch() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion wolfrider = playMinionCard(context, opponent, "minion_wolfrider");
			context.endTurn();
			playCardWithTarget(context, player, "spell_vampiric_touch", wolfrider);
			Assert.assertTrue(wolfrider.isDestroyed());
			assertEquals(player.getMinions().get(0).getAttack(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_wolfrider");
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion mindControlTech = playMinionCard(context, opponent, "minion_mind_control_tech");
			context.endTurn();
			playCardWithTarget(context, player, "spell_vampiric_touch", mindControlTech);
			Assert.assertFalse(mindControlTech.isDestroyed());
			assertEquals(player.getMinions().size(), 0);
		});
	}

	@Test
	public void testDivineIntervention() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_divine_intervention");
			Minion lightwarden = playMinionCard(context, player, "minion_lightwarden");
			player.getHero().setHp(5);
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getHero().getHp(), 11, "Should have healed for 6");
			assertEquals(lightwarden.getAttack(), lightwarden.getBaseAttack() + 2, "Lightwarden should have buffed");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_divine_intervention");
			Minion lightwarden = playMinionCard(context, player, "minion_lightwarden");
			player.getHero().setHp(7);
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", player.getHero());
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getHero().getHp(), 1, "Should not have healed.");
			assertEquals(lightwarden.getAttack(), lightwarden.getBaseAttack(), "Lightwarden should not have buffed");
		});
	}

	@Test
	public void testYrel() {
		runGym((context, player, opponent) -> {
			Minion yrel = playMinionCard(context, player, "minion_yrel");
			player.setMaxMana(4);
			player.setMana(4);
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			assertEquals(player.getMana(), 0);
			player.setMana(4);
			playCardWithTarget(context, player, "spell_fireball", yrel);
			assertEquals(player.getMana(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion yrel = playMinionCard(context, player, "minion_yrel");
			player.setMaxMana(5);
			player.setMana(5);
			playCardWithTarget(context, player, "spell_power_word_tentacles", yrel);
			assertEquals(player.getMana(), 5);
		});
	}

	@Test
	public void testWorgenAmbusher() {
		runGym((context, player, opponent) -> {
			Minion worgen1 = playMinionCard(context, player, "minion_worgen_ambusher");
			assertEquals(worgen1.getAttack(), worgen1.getBaseAttack());
			Minion worgen2 = playMinionCard(context, player, "minion_worgen_ambusher");
			assertEquals(worgen2.getAttack(), worgen2.getBaseAttack() + 1);
		});
	}

	@Test
	public void testCriminologist() {
		final int MAGE = 0;
		final int HUNTER = 1;
		final int PALADIN = 2;
		final int ROGUE = 3;
		Stream.of(MAGE, HUNTER, PALADIN, ROGUE).forEach(heroClass -> {
			runGym((context, player, opponent) -> {
				playCard(context, player, "spell_the_coin");
				CardArrayList cards = new CardArrayList();
				overrideDiscover(context, player, discoveries -> {
					assertEquals(discoveries.size(), 4);
					discoveries.stream().map(DiscoverAction::getCard).forEach(cards::addCard);
					return discoveries.get(heroClass);
				});
				playCard(context, player, "minion_criminologist");
				Card card = player.getHand().get(0);
				Assert.assertTrue(card.isSecret());
				HeroClass secretClass = card.getHeroClass();
				switch (heroClass) {
					case MAGE:
						assertEquals(secretClass, HeroClass.BLUE);
						break;
					case HUNTER:
						assertEquals(secretClass, HeroClass.GREEN);
						break;
					case PALADIN:
						assertEquals(secretClass, HeroClass.GOLD);
						break;
					case ROGUE:
						assertEquals(secretClass, HeroClass.BLACK);
						break;
				}
			});
		});
	}

	@Test
	public void testBlackLotus() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_black_lotus");
			playCardWithTarget(context, player, "spell_razorpetal", opponent.getHero());
			Assert.assertFalse(opponent.getHero().isDestroyed());
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_black_lotus");
			context.endTurn();
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, player, "spell_razorpetal", bloodfen);
			Assert.assertTrue(bloodfen.isDestroyed());
		});
	}

	@Test
	public void testFrostBomb() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion other = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Minion friendly = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_frost_bomb", target);
			Assert.assertTrue(target.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			Assert.assertTrue(target.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			Assert.assertFalse(target.hasAttribute(Attribute.FROZEN));
			Assert.assertTrue(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion other = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Minion friendly = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_frost_bomb", target);
			Assert.assertTrue(target.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", target);
			Assert.assertTrue(target.isDestroyed());
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testJadeAmbush() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_jade_idol_1");
			Minion originalJade = player.getMinions().get(0);
			playCard(context, player, "secret_jade_ambush");
			context.endTurn();
			Minion hound = playMinionCard(context, opponent, "token_hound");
			attack(context, opponent, hound, originalJade);
			assertEquals(player.getSecrets().size(), 0, "Jade Ambush should have triggered.");
			assertEquals(player.getMinions().size(), 2, "The player should have two jade golems");
			Minion newJade = player.getMinions().get(1);
			assertEquals(newJade.getHp(), 1, "The second jade should have 1 HP left.");
			assertEquals(newJade.getAttributeValue(Attribute.LAST_HIT), 1, "The second jade should have taken 1 damage");
			Assert.assertTrue(hound.isDestroyed());
			Assert.assertFalse(originalJade.isDestroyed());
		});
	}

	@Test
	public void testFleetfootedScout() {
		runGym((context, player, opponent) -> {
			Card card1 = receiveCard(context, player, "spell_barrage");
			Minion fleetfooted = playMinionCard(context, player, "minion_fleetfooted_scout");
			Card card2 = receiveCard(context, player, "spell_load_and_lock");
			Card card3 = receiveCard(context, player, "spell_mirror_image");
			Stream.of(card1, card2).forEach(c -> assertEquals(costOf(context, player, c), c.getBaseManaCost() - 1));
			assertEquals(costOf(context, player, card3), card3.getBaseManaCost());
			playCardWithTarget(context, player, "spell_fireball", fleetfooted);
			Stream.of(card1, card2).forEach(c -> assertEquals(costOf(context, player, c), c.getBaseManaCost()));
			assertEquals(costOf(context, player, card3), card3.getBaseManaCost());
		});
	}

	@Test
	public void testSecretGarden() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			playCard(context, player, "secret_secret_garden");
			context.endTurn();
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			}
			playMinionCard(context, opponent, "minion_novice_engineer");
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getHand().size(), 0);
			playMinionCard(context, opponent, "minion_novice_engineer");
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getHand().size(), 3);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_secret_garden");
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			}
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			playMinionCard(context, opponent, "minion_novice_engineer");
			assertEquals(player.getSecrets().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_secret_garden");
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			}
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getHand().size(), 0);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			playMinionCard(context, opponent, "minion_novice_engineer");
			assertEquals(player.getSecrets().size(), 0);
		});
	}

	@Test
	public void testMasterSorcerer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_master_sorcerer");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_fireball", target);
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getDeck().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_master_sorcerer");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_fireball", target);
			assertEquals(player.getHand().size(), 0);
			assertEquals(player.getDeck().size(), 1);
		});
	}

	@Test
	public void testMetamagicTemporalFluxInteraction() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "spell_the_coin");
			}
			overrideDiscover(context, player, "spell_enhanced");
			playCard(context, player, "spell_metamagic");
			int opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, "spell_temporal_flux", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 3);
			assertEquals(player.getHand().size(), 3);
		});
	}

	@Test
	public void testMetamagic() {
		// Costs (2) less.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_quickened");
			playCard(context, player, "spell_metamagic");
			player.setMaxMana(10);
			player.setMana(10);
			Card explosion = receiveCard(context, player, "spell_arcane_explosion");
			assertEquals(costOf(context, player, explosion), explosion.getBaseManaCost() - 2);
			playCard(context, player, explosion);
			explosion = receiveCard(context, player, "spell_arcane_explosion");
			assertEquals(costOf(context, player, explosion), explosion.getBaseManaCost());
		});

		// Deals 1 damage to all enemy minions.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion villager = playMinionCard(context, opponent, "minion_possessed_villager");
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			overrideDiscover(context, player, "spell_unbounded");
			playCard(context, player, "spell_metamagic");
			Assert.assertFalse(villager.isDestroyed());
			assertEquals(bloodfen.getHp(), bloodfen.getBaseHp(), "Metamagic should not have triggered its own effect.");
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertTrue(villager.isDestroyed());
			Assert.assertTrue(bloodfen.isDestroyed(), "Two damage should have been dealt in this sequence.");
			assertEquals(opponent.getMinions().size(), 1, "There should just be a shadowbeast, because the additional spell effect does not happen in its own sequence.");
			context.endTurn();
			bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			assertEquals(opponent.getMinions().size(), 2, "There should be a shadowbeast and a bloodfen.");
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertFalse(bloodfen.isDestroyed(), "The next arcane explosion should not have destroyed the bloodfen since it only dealt 1 damage");
			assertEquals(opponent.getMinions().size(), 1, "But the Shadowbeast should have been destroyed.");
		});

		// Returns to your deck after you cast it.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_memorized");
			playCard(context, player, "spell_metamagic");
			playCard(context, player, "minion_bloodfen_raptor");
			assertEquals(player.getDeck().size(), 0, "We should not have shuffled a minion card into the deck.");
			context.endTurn();
			// We should still apply the effect to the next spell the player cast
			playCard(context, opponent, "spell_the_coin");
			assertEquals(player.getDeck().size(), 0, "The opponent's spell should not have been shuffled.");
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			assertEquals(player.getDeck().get(0).getCardId(), "spell_arcane_explosion");
			playCard(context, player, "spell_arcane_explosion");
			assertEquals(player.getDeck().size(), 1, "Only one copy of the card should have been shuffled.");
		});

		// Freezes two random enemies.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_chilled");
			playCard(context, player, "spell_metamagic");
			context.endTurn();
			Minion minion1 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion minion2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertTrue(minion1.hasAttribute(Attribute.FROZEN));
			Assert.assertTrue(minion2.hasAttribute(Attribute.FROZEN));
			assertEquals(minion1.getHp(), minion1.getBaseHp() - 1);
			assertEquals(minion2.getHp(), minion1.getBaseHp() - 1);
		});

		// The next spell you cast costs (2) more and has Spell Damage +2.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_enhanced");
			playCard(context, player, "spell_metamagic");
			Card fireball = receiveCard(context, player, "spell_fireball");
			assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost() + 2);
			assertEquals(player.getAttributeValue(Attribute.SPELL_DAMAGE), 2);
			int opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, fireball, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 8);
			fireball = receiveCard(context, player, "spell_fireball");
			assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost(), "The 2nd spell should not be more expensive");
			opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, fireball, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 6, "The 2nd spell should not have gotten spell damage +2.");
			opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, fireball, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 6, "The 3nd spell should not have gotten spell damage -2.");
		});

		// Deals 3 damage to a random enemy minion.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion chillwind = playMinionCard(context, opponent, "minion_chillwind_yeti");
			context.endTurn();
			overrideDiscover(context, player, "spell_empowered");
			playCard(context, player, "spell_metamagic");
			assertEquals(chillwind.getHp(), chillwind.getBaseHp(), "Metamagic should not have triggered its own effect.");
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			assertEquals(chillwind.getHp(), chillwind.getBaseHp() - 3);
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			assertEquals(chillwind.getHp(), chillwind.getBaseHp() - 3, "The empowered effect should have expired");
		});
	}

	@Test
	public void testNexusKingSalhadaar() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_nexus_king_salhadaar");
			assertEquals(player.getMinions().size(), 1);
			Assert.assertTrue(player.getHand().stream().allMatch(c -> costOf(context, player, c) == 1));
		});
	}

	@Test
	public void testSageOfFoursight() {
		runGym((context, player, opponent) -> {
			Minion sage = playMinionCard(context, player, "minion_sage_of_foursight");
			assertEquals(sage.getAttack(), sage.getBaseAttack(), "Sage should not buff itself.");
			assertEquals(sage.getHp(), sage.getBaseHp(), "Sage should not buff itself.");
			Card bloodfenCard = CardCatalogue.getCardById("minion_bloodfen_raptor");
			context.getLogic().receiveCard(player.getId(), bloodfenCard);
			assertEquals(costOf(context, player, bloodfenCard), bloodfenCard.getBaseManaCost() + 4, "Bloodfen should cost more because it's the next card the player will play.");

			// It should work with a one turn gap in the middle
			context.endTurn();
			context.endTurn();

			Minion bloodfen = playMinionCard(context, player, bloodfenCard);
			assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 4, "Bloodfen should be buffed.");
			assertEquals(bloodfen.getHp(), bloodfen.getBaseHp() + 4, "Bloodfen should be buffed.");
			Card bloodfenCard2 = CardCatalogue.getCardById("minion_bloodfen_raptor");
			context.getLogic().receiveCard(player.getId(), bloodfenCard2);
			assertEquals(costOf(context, player, bloodfenCard), bloodfenCard.getBaseManaCost(), "Bloodfen 2 should not cost more.");
			Minion bloodfen2 = playMinionCard(context, player, bloodfenCard2);
			assertEquals(bloodfen2.getAttack(), bloodfen2.getBaseAttack(), "The second bloodfen should not be buffed");
			assertEquals(bloodfen2.getHp(), bloodfen2.getBaseHp(), "The second bloodfen should not be buffed");
		});
	}

	@Test
	public void testScorpidStinger() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scorpid_stinger");
			context.endTurn();
			Minion flipper = playMinionCard(context, opponent, "minion_snowflipper_penguin");
			context.endTurn();
			attack(context, player, player.getHero(), flipper);
			Assert.assertTrue(player.getHand().containsCard("spell_inner_rage"));
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scorpid_stinger");
			context.endTurn();
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			attack(context, player, player.getHero(), bloodfen);
			Assert.assertFalse(player.getHand().containsCard("spell_inner_rage"));
		});
	}

	@Test
	public void testPulseBomb() {
		// Test excess on adjacents
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion boulderfist1 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion boulderfist2 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			playCardWithTarget(context, player, "spell_pulse_bomb", bloodfen);
			Assert.assertTrue(bloodfen.isDestroyed());
			// Up to 18 damage rule
			assertEquals(boulderfist1.getHp(), boulderfist1.getBaseHp() - 10 + bloodfen.getBaseHp());
			assertEquals(boulderfist2.getHp(), boulderfist2.getBaseHp() - 10 + bloodfen.getBaseHp());
		});

		// Test excess in event of divine shield using Explosive Runes rules
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion boulderfist1 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion boulderfist2 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			bloodfen.setAttribute(Attribute.DIVINE_SHIELD);
			context.endTurn();
			playCardWithTarget(context, player, "spell_pulse_bomb", bloodfen);
			Assert.assertFalse(bloodfen.isDestroyed());
			assertEquals(bloodfen.getHp(), bloodfen.getBaseHp());
			// Up to 18 damage rule
			assertEquals(boulderfist1.getHp(), boulderfist1.getBaseHp() - 10 + bloodfen.getBaseHp());
			assertEquals(boulderfist2.getHp(), boulderfist2.getBaseHp() - 10 + bloodfen.getBaseHp());
		});
	}

	@Test
	public void testArmaggedonVanguardBolfRamshieldInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_armageddon_vanguard");
			playCard(context, player, "minion_bolf_ramshield");
			context.endTurn();
			playCard(context, opponent, "minion_armageddon_vanguard");
			playCard(context, opponent, "minion_bolf_ramshield");
			context.endTurn();
			playCard(context, player, "spell_arcane_missiles");
		});
	}

	@Test
	public void testTerrorscaleStalkerBlinkDogInteraction() {
		runGym((context, player, opponent) -> {
			// Deathrattle: Give a random friendly Beast \"Deathrattle: Summon a Blink Dog\"
			Minion blinkDog = playMinionCard(context, player, "minion_blink_dog");
			playCard(context, player, "minion_terrorscale_stalker");
			// Now Blink Dog summons a blink dog and gives a randomly friendly beast an extra deathrattle
			playCardWithTarget(context, player, "spell_fireball", blinkDog);
			assertEquals(player.getMinions().stream().filter(m -> m.getSourceCard().getCardId().equals("minion_blink_dog")).count(), 1L);
		});
	}

	@Test
	public void testThinkFast() {
		runGym((context, player, opponent) -> {
			// TODO: This should still work if it's a different class
			playCard(context, player, "spell_mirror_image");
			int[] cost = new int[1];
			overrideDiscover(context, player, actions -> {
				cost[0] = actions.get(0).getCard().getBaseManaCost();
				return actions.get(0);
			});
			playCard(context, player, "spell_think_fast");
			assertEquals(costOf(context, player, player.getHand().get(0)), cost[0] - 1);
			context.endTurn();
			context.endTurn();
			assertEquals(costOf(context, player, player.getHand().get(0)), cost[0]);
		}, HeroClass.BLACK, HeroClass.BLACK);
	}

	@Test
	public void testDejaVu() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_deja_vu");
			assertEquals(player.getMinions().size(), 2);
			Assert.assertTrue(player.getHand().stream().allMatch(c -> costOf(context, player, c) == 1));
			playCard(context, player, player.getHand().get(1));
			playCard(context, player, player.getHand().get(0));
			for (int i = 2; i < 4; i++) {
				assertEquals(player.getMinions().get(i).getAttack(), 1);
				assertEquals(player.getMinions().get(i).getHp(), 1);
			}
		});
	}

	@Test
	public void testForeverAStudent() {
		runGym((context, player, opponent) -> {
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_forever_a_student", bloodfen);
			Minion bloodfen2 = playMinionCard(context, player, "minion_bloodfen_raptor");
			assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 1);
			assertEquals(bloodfen.getHp(), bloodfen.getBaseHp() + 1);
			assertEquals(bloodfen2.getAttack(), bloodfen2.getBaseAttack(), "The newly summoned minion should not be the benefit of the buff.");
			assertEquals(bloodfen2.getHp(), bloodfen2.getBaseHp());
			context.endTurn();
			playCard(context, opponent, "minion_bloodfen_raptor");
			assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 1, "Opponent summoning a minion should not affect the stats of the enchanted minion.");
			assertEquals(bloodfen.getHp(), bloodfen.getBaseHp() + 1);
		});
	}

	@Test
	public void testNickOfTime() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			shuffleToDeck(context, player, "minion_nick_of_time");
			context.endTurn();
			assertEquals(player.getMinions().stream().map(Minion::getSourceCard).map(Card::getCardId).filter(cid -> cid.equals("token_silver_hand_recruit")).count(), 2L);
		});
	}

	@Test
	public void testAwakenTheAncients() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_awaken_the_ancients");
			player.setMaxMana(10);
			player.setMana(10);
			playCard(context, player, "minion_bloodfen_raptor");
			assertEquals(player.getMana(), 10);
			playCard(context, player, "minion_bloodfen_raptor");
			assertEquals(player.getMana(), 8);
		});
	}

	@Test
	public void testAcceleratedGrowth() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			playCard(context, player, "spell_accelerated_growth");
			assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			assertEquals(opponent.getHand().get(0).getCardId(), "minion_bloodfen_raptor", "Testing the TargetPlayer.BOTH attribute on DrawCardSpell");
		});
	}

	@Test
	public void testMysticSkull() {
		runGym((context, player, opponent) -> {
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_mystic_skull", bloodfenRaptor);
			assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			Minion newBloodfenRaptor = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(newBloodfenRaptor.getAttack(), 5);
		});
	}

	@Test
	public void testGiantDisappointment() {
		runGym((context, player, opponent) -> {
			Card card = CardCatalogue.getCardById("minion_giant_disappointment");
			context.getLogic().receiveCard(player.getId(), card);
			assertEquals(costOf(context, player, card), 8);
		});
	}

	@Test
	public void testQuestGiver() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_bloodfen_raptor");
			playMinionCard(context, player, "minion_quest_giver");
			assertEquals(player.getDeck().get(0).getCardId(), "minion_bloodfen_raptor");
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_quest_giver");
			context.endTurn();
			context.endTurn();
			Minion newBloodfen = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(newBloodfen.getAttack(), 6);
			assertEquals(newBloodfen.getHp(), 5);
		});
	}

	@Test
	public void testPowerTrip() {
		// We reach turn 10 so we have 10 mana, we die
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
			for (int i = 0; i < 10; i++) {
				context.endTurn();
				context.endTurn();
			}
			Assert.assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
		});

		// Our opponent gives us 10 mana somehow, we die
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
			for (int i = 0; i < 2; i++) {
				context.endTurn();
				context.endTurn();
			}
			context.endTurn();
			assertEquals(player.getMaxMana(), 3);
			for (int i = 0; i < 7; i++) {
				playCard(context, opponent, "minion_arcane_golem");
				assertEquals(player.getMaxMana(), 3 + i + 1);
			}
			assertEquals(player.getMaxMana(), 10);
			Assert.assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
		});

		// Check that minions have +1/+1
		runGym((context, player, opponent) -> {
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_power_trip");
			assertEquals(bloodfenRaptor.getAttack(), bloodfenRaptor.getBaseAttack() + 1);
			assertEquals(bloodfenRaptor.getHp(), bloodfenRaptor.getBaseHp() + 1);
			context.endTurn();
			Minion opponentMinion = playMinionCard(context, player, "minion_chillwind_yeti");
			context.endTurn();
			playCardWithTarget(context, player, "spell_mind_control", opponentMinion);
			assertEquals(opponentMinion.getAttack(), opponentMinion.getBaseAttack() + 1);
			assertEquals(opponentMinion.getHp(), opponentMinion.getBaseHp() + 1);
		});

		// Check that Saronite Chain Gang has correct stats
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			playCard(context, player, "minion_saronite_chain_gang");
			player.getMinions().forEach(m -> assertEquals(m.getAttack(), 3));
			player.getMinions().forEach(m -> assertEquals(m.getHp(), 4));
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_saronite_chain_gang");
			playCard(context, player, "spell_power_trip");
			player.getMinions().forEach(m -> assertEquals(m.getAttack(), 3));
			player.getMinions().forEach(m -> assertEquals(m.getHp(), 4));
		});
	}

	@Test
	public void testDancemistress() {
		// When this minion is healed, check if Crazed Dancer is summoned
		runGym((context, player, opponent) -> {
			Minion dancemistress = playMinionCard(context, player, "minion_dancemistress");
			context.endTurn();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			context.endTurn();
			// Heals the dancemistress Minion
			playCardWithTarget(context, player, "spell_ancestral_healing", dancemistress);
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_crazed_dancer");
			// Check if the Crazed Dancer has attack and hp of 2
			assertEquals(player.getMinions().get(1).getBaseAttack(), 2);
			assertEquals(player.getMinions().get(1).getBaseHp(), 2);
		});

		// When a different minion is healed, Crazed Dancer is NOT summoned
		runGym((context, player, opponent) -> {
			Minion dancemistress = playMinionCard(context, player, "minion_dancemistress");
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			context.endTurn();
			// Heals the dancemistress Minion
			playCardWithTarget(context, player, "spell_ancestral_healing", bloodfenRaptor);
			Assert.assertFalse(player.getMinions().stream().anyMatch(m -> m.getSourceCard().getCardId().equals("minion_crazed_dancer")));
		});
	}

	@Test
	public void testSpikeToedBooterang() {
		// Attacks a opponent's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, opponent, "minion_river_crocolisk");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			assertEquals(opponent.getMinions().get(0).getHp(), 1);
		});

		// Attacks player's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, player, "minion_river_crocolisk");
			playCardWithTarget(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			assertEquals(player.getMinions().get(0).getHp(), 1);
		});

		// Defeats a Divine Shield
		runGym((context, player, opponent) -> {
			Minion silvermoonGuardian = playMinionCard(context, opponent, "minion_silvermoon_guardian");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", silvermoonGuardian);
			assertEquals(opponent.getMinions().get(0).getHp(), 2);
		});

		// If attacking Imp Gang Boss, summons two 1/1 Imps for opponent
		runGym((context, player, opponent) -> {
			Minion impGangBoss = playMinionCard(context, opponent, "minion_imp_gang_boss");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", impGangBoss);
			assertEquals(opponent.getMinions().get(1).getSourceCard().getCardId(), "token_imp");
			assertEquals(opponent.getMinions().get(2).getSourceCard().getCardId(), "token_imp");
		});
	}

	@Test
	public void testStablePortal() {
		// Correctly adds a Beast to player's hand with a mana cost 2 less
		runGym((context, player, opponent) -> {
			GameLogic spiedLogic = spy(context.getLogic());
			context.setLogic(spiedLogic);

			Mockito.doAnswer(invocation ->
					CardCatalogue.getCardById("minion_malorne"))
					.when(spiedLogic)
					.removeRandom(Mockito.anyList());

			playCard(context, player, "spell_stable_portal");
			Card card = player.getHand().get(0);
			assertEquals(card.getCardId(), "minion_malorne");
			int baseMana = card.getBaseManaCost();
			assertEquals(baseMana, 7);
			assertEquals(card.getRace(), Race.BEAST);
			assertEquals(costOf(context, player, card), baseMana - 2);
		});
	}

	@Test
	public void testCryWolf() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_cry_wolf");
			playCard(context, player, player.getHand().peekFirst());
			playCard(context, player, player.getHand().peekFirst());

			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_sheep");
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_wolf");
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "token_wolf");
		});

	}

	@Test
	public void testGnarlRoot() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 8; i++) {
				receiveCard(context, player, "minion_wisp");
			}
			playCard(context, player, "minion_gnarlroot");

			assertEquals(player.getHand().get(8).getCardId(), "token_treant");
			assertEquals(player.getHand().get(9).getCardId(), "token_treant");

			for (int i = 1; i < 4; i++) {
				assertEquals(player.getMinions().get(i).getSourceCard().getCardId(), "token_treant");
			}
		});
	}

	@Test
	public void testFlame() {
		runGym(((context, player, opponent) -> {
			receiveCard(context, player, "minion_rebellious_flame");
			playCard(context, player, "minion_wisp");
			assertEquals(player.getHand().get(0).getCardId(), "spell_rebellious_flame");
			playCard(context, player, "spell_arcane_explosion");
			assertEquals(player.getHand().get(0).getCardId(), "minion_rebellious_flame");
			playCard(context, player, player.getHand().get(0));
			assertEquals(player.getHand().size(), 0);
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_rebellious_flame");
			receiveCard(context, player, "spell_rebellious_flame");
			playCardWithTarget(context, player, player.getHand().get(0), opponent.getHero());
			assertEquals(player.getHand().size(), 0);
		}));
	}

	@Test
	public void testTriplicate() {
		runGym((context, player, opponent) -> {
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			playCardWithTarget(context, player, "spell_triplicate", wisp);
			assertTrue(player.getMinions().size() > 1);
			assertTrue(player.getHand().size() > 0);
			assertTrue(player.getDeck().size() > 0);
		});
	}

	@Test
	public void testPolyDragon() {
		runGym((context, player, opponent) -> {
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			playCardWithTarget(context, player, "spell_polymorph_dragon", wisp);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_whelp");
			playCardWithTarget(context, player, player.getHand().peekFirst(), player.getMinions().get(0));
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_1212dragon");
		});
	}


	@Test
	public void testDragonlingPet() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_dragonling_pet");
			shuffleToDeck(context, player, "minion_murloc_raider");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			assertTrue(player.getDeck().size() > 1);
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_dragonling_pet");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			assertEquals(player.getDeck().size(), 0);
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testWyrmrestSniper() {
		// Friendly Dragon survives damage so 3 damage is dealt to the opponent hero
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_sleepy_dragon");
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			assertEquals(opponent.getHero().getHp(), opponentHp - 3);
			Assert.assertFalse(wyrmrest.hasAttribute(Attribute.STEALTH));
		});

		// Friendly Dragon does not survive damage, no damage is dealt
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_sleepy_dragon");
			// Set hp to 1 so it dies
			minion.setHp(1);
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			Assert.assertTrue(minion.isDestroyed());
			assertEquals(opponent.getHero().getHp(), opponentHp, "Opponent's HP should not have changed.");
			Assert.assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
		});

		// Enemy Dragon survives damage, no damage is dealt to the opponent's hero
		runGym((context, player, opponent) -> {
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			Minion minion = playMinionCard(context, opponent, "minion_sleepy_dragon");

			// Damages minions by 1
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertFalse(minion.isDestroyed());
			assertEquals(minion.getHp(), minion.getBaseHp() - 1);
			assertEquals(opponent.getHero().getHp(), opponentHp, "Opponent's HP should not have changed.");
			Assert.assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
		});
	}

	@Test
	public void testAegwynn() {
		Map<String, Integer> spellMap = new HashMap<>();
		spellMap.put("spell_fireball", 0);
		spellMap.put("spell_arcane_explosion", 2);
		spellMap.put("spell_flamestrike", 2);
		spellMap.put("spell_frostbolt", 0);
		for (String spell : spellMap.keySet()) {
			runGym((context, player, opponent) -> {
				shuffleToDeck(context, player, spell);
				shuffleToDeck(context, player, "minion_aegwynn");
				context.fireGameEvent(new GameStartEvent(context, player.getId()));
				assertEquals(player.hasAttribute(Attribute.SPELL_DAMAGE) ? player.getAttribute(Attribute.SPELL_DAMAGE) : 0, spellMap.get(spell));
			});
		}

	}

	@Test
	public void testRelicRaider() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "weapon_vinecleaver");
			playCard(context, player, "minion_relic_raider");
			if (!player.getWeaponZone().isEmpty()) {
				Weapon weapon = player.getWeaponZone().get(0);
				assertEquals(weapon.getName(), "Vinecleaver");
				assertEquals(weapon.getAttack(), 1);
				assertEquals(weapon.getHp(), 2);
			}
		});
	}

	@Test
	public void testQuartz() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_quartz_spellstone");
			playCard(context, opponent, "minion_wisp");
			playCard(context, player, "spell_frost_nova");
			playCard(context, player, "spell_frost_nova");
			assertEquals(player.getHand().get(0).getCardId(), "spell_quartz_spellstone");
		});

		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_quartz_spellstone");
			playCard(context, opponent, "minion_wisp");
			playCard(context, opponent, "minion_wisp");
			playCard(context, player, "spell_frost_nova");
			playCard(context, player, "spell_frost_nova");
			assertEquals(player.getHand().get(0).getCardId(), "spell_greater_quartz_spellstone");
		});

		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_quartz_spellstone");
			playCard(context, opponent, "minion_wisp");
			playCard(context, opponent, "minion_wisp");
			playCard(context, player, "spell_frost_nova");
			assertEquals(player.getHand().get(0).getCardId(), "spell_quartz_spellstone");
		});
	}

	@Test
	public void testImmolate() {
		runGym(((context, player, opponent) -> {
			Minion watcher = playMinionCard(context, opponent, "minion_ancient_watcher");
			playCard(context, player, "minion_spellshifter");
			playCardWithTarget(context, player, "spell_immolate", watcher);

			for (int i = 1; i < 5; i++) {
				if (find(context, "minion_ancient_watcher") != null) {
					assertEquals(watcher.getHp(), 5 - i * 2);
					context.endTurn();
				}
			}
		}));
	}

	@Test
	public void testCommanderGarrosh() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_commander_garrosh");
			player.setMana(10);
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play());
			assertEquals(player.getHero().getArmor(), 13);
			assertEquals(player.getMana(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_commander_garrosh");
			playCard(context, player, "minion_raza_the_chained");
			player.setMana(10);
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play());
			assertEquals(player.getHero().getArmor(), 14);
			assertEquals(player.getMana(), 0);
		});
	}

	@Test
	public void testHagara() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_hagara_the_stormbinder");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			playCard(context, player, "minion_earth_elemental");
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 3);
			useHeroPower(context, player);
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 0);
			playCard(context, player, "hero_hagatha_the_witch");
			playCard(context, player, "minion_earth_elemental");
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 0);
			playCard(context, player, "spell_volcano");
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 2);
			playCard(context, player, "minion_earth_elemental");
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 0);
		}, HeroClass.SILVER, HeroClass.SILVER);
	}
	@Test
	public void testAFinalStrike() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_a_final_strike");
			final Quest quest = player.getQuests().get(0);
			opponent.getHero().setHp(100);
			for (int i = 0; i < 3; i++) {
				playCardWithTarget(context, player, "spell_pyroblast", opponent.getHero());
			}
			assertTrue(quest.isExpired());
			assertEquals(opponent.getHero().getHp(), 40);
		});
	}
	@Test
	public void testVereesaWindrunner2() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_cloaked_huntress");
			receiveCard(context, player, "secret_freezing_trap");
			assertEquals(player.getHand().get(0).getCardId(), "secret_freezing_trap");
			assertEquals(context.getLogic().getModifiedManaCost(player, player.getHand().get(0)), 0);
			Minion vereesa = playMinionCard(context, player, "minion_vereesa_windrunner");
			assertEquals(player.getHand().get(0).getCardId(), "spell_freezing_trap");
			assertEquals(context.getLogic().getModifiedManaCost(player, player.getHand().get(0)), 0);
			destroy(context, vereesa);
			assertEquals(player.getHand().get(0).getCardId(), "secret_freezing_trap");
		});
		runGym((context, player, opponent) -> {
			List<String> secrets = Arrays.asList("freezing_trap", "misdirection", "explosive_trap",
					"bear_trap", "rat_trap", "hidden_cache", "snipe", "snake_trap", "venomstrike_trap",
					"wandering_monster", "dart_trap");
			for (String secret : secrets) {
				Card card = shuffleToDeck(context, player, "secret_" + secret);
				context.getLogic().drawCard(player.getId(), player);
				playMinionCard(context, player, "minion_vereesa_windrunner");
				assertEquals(player.getHand().get(0).getCardId(), "spell_" + secret, player.getHand().get(0).getCardId());
				assertTrue(player.getHand().get(0).hasAttribute(Attribute.SECRET), secret);
				playCard(context, player, "spell_cataclysm");
			}
		});
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "secret_explosive_trap");
			receiveCard(context, player, "spell_lesser_emerald_spellstone");
			playCard(context, player, "weapon_eaglehorn_bow");
			playCard(context, player, "minion_vereesa_windrunner");
			playCard(context, player, player.getHand().get(0));
			assertEquals(player.getHand().get(0).getCardId(), "spell_emerald_spellstone");
			assertEquals(player.getWeaponZone().get(0).getDurability(), 3);
		});
	}
	@Test
	public void testLunasOtherPocketGalaxy() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_lunas_other_pocket_galaxy");
			for (int i = 0; i < 100; i++) {
				playCard(context, player, "spell_excess_mana");
				assertEquals(player.getDeck().size(), 60);
			}
			assertEquals(player.isDestroyed(), false);
		});
	}
	@Test
	public void testLittleHelper() {
		runGym((context, player, opponent) -> {
			CardList heroPowers = CardCatalogue.getAll().filtered(Card::isHeroPower);
			for (Card heroPower : heroPowers) {
				SpellDesc spell = new SpellDesc(ChangeHeroPowerSpell.class);
				spell.put(SpellArg.CARD, heroPower.getCardId());
				context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, false);

				playCard(context, player, "minion_little_helper");
				Card card = player.getHand().get(0);
				//System.out.println(heroPower.getCardId() + " " + card.getBaseManaCost() + " " + card.getDescription());
				playCard(context, player, "spell_cataclysm");
			}
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_uther_of_the_ebon_blade");
			for (int i = 0; i < 3; i++) {
				playCard(context, player, "minion_little_helper");
			}
			useHeroPower(context, player);
			for (int i = 0; i < 3; i++) {
				playCard(context, player, player.getHand().get(0));
			}
			assertTrue(opponent.isDestroyed());
		});

	}

	@Test
	public void testAlternateStartingHeroPowers() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "passive_dire_beast");
			context.fireGameEvent(new PreGameStartEvent(context, player.getId()));
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_dire_beast");
		});

		int yup = 0;

		for (int i = 0; i < 100; i++) {
			DebugContext debug = createContext(HeroClass.GREEN, HeroClass.GREEN, false);
			debug.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
			debug.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
				for (int j = 0; j < 10; j++) {
					deck.addCard("minion_faithful_lumi");
				}
			});
			debug.getPlayer1().getDeck().addCard(debug.getCardById("passive_dire_beast"));
			debug.getPlayer1().getDeck().addCard(debug.getCardById("minion_baku_the_mooneater"));
			debug.init();
			if (debug.getPlayer1().getHeroPowerZone().get(0).getCardId().equals("hero_power_dire_stable")) {
				yup++;
			}
		}
		assertEquals(yup, 100);
	}


}

