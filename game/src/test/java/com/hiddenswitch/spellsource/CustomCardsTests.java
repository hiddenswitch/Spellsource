package com.hiddenswitch.spellsource;

import com.google.common.collect.Sets;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.FixedCardsDeckFormat;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.events.GameStartEvent;
import net.demilich.metastone.game.events.TurnEndEvent;
import net.demilich.metastone.game.events.TurnStartEvent;
import net.demilich.metastone.game.events.WillEndSequenceEvent;
import net.demilich.metastone.game.events.PreGameStartEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.spells.ChangeHeroPowerSpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.OverrideDiscoverBehaviour;
import net.demilich.metastone.tests.util.TestBase;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.*;

public class CustomCardsTests extends TestBase {

	@Test
	public void testDaringDuelist() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion source = playMinionCard(context, player, "minion_daring_duelist");
			// Ensure Daring Duelist dies
			source.setHp(2);
			context.endTurn();
			context.endTurn();
			Card shouldBeInDeck = shuffleToDeck(context, player, "spell_the_coin");
			attack(context, player, source, target);
			assertTrue(source.isDestroyed());
			assertEquals(shouldBeInDeck.getZone(), Zones.DECK);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			target.setAttack(0);
			context.endTurn();
			Minion source = playMinionCard(context, player, "minion_daring_duelist");
			context.endTurn();
			context.endTurn();
			Card shouldBeInHand = shuffleToDeck(context, player, "spell_the_coin");
			attack(context, player, source, target);
			assertFalse(source.isDestroyed());
			assertEquals(shouldBeInHand.getZone(), Zones.HAND);
		});
	}

	@Test
	public void testDoomerDiver() {
		runGym((context, player, opponent) -> {
			Card shouldDraw = shuffleToDeck(context, player, "spell_the_coin");
			Minion pirate = playMinionCard(context, player, "minion_charge_pirate");
			attack(context, player, pirate, opponent.getHero());
			playMinionCard(context, player, "minion_doomed_diver");
			assertEquals(shouldDraw.getZone(), Zones.HAND);
		});

		runGym((context, player, opponent) -> {
			Card shouldDraw = shuffleToDeck(context, player, "spell_the_coin");
			Minion pirate = playMinionCard(context, player, "minion_charge_pirate");
			playMinionCard(context, player, "minion_doomed_diver");
			assertEquals(shouldDraw.getZone(), Zones.DECK);
		});
	}

	@Test
	public void testShapeseeper() {
		runGym((context, player, opponent) -> {
			Card shapeseeperCard = receiveCard(context, player, "minion_shapeseeper");
			playCard(context, player, "minion_neutral_test");
			assertEquals(costOf(context, player, shapeseeperCard), shapeseeperCard.getBaseManaCost() + 1);
			Minion shapeseeper = playMinionCard(context, player, shapeseeperCard);
			assertEquals(shapeseeper.getAttack(), shapeseeper.getBaseAttack() + 1);
			assertEquals(shapeseeper.getHp(), shapeseeper.getBaseHp() + 1);
		});
	}

	@Test
	public void testDiscoInfero() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			Minion disco = playMinionCardWithBattlecry(context, player, "minion_disco_inferno", target);
			assertEquals(disco.getAttack(), target.getBaseAttack());
			assertEquals(disco.getHp(), target.getBaseHp());
			assertEquals(target.getAttack(), disco.getBaseAttack());
			assertEquals(target.getHp(), disco.getBaseHp());
		});
	}

	@Test
	public void testRecurringTorrent() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_the_coin");
			playCard(context, player, "minion_recurring_torrent");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "spell_the_coin");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_the_coin");
			context.endTurn();
			context.endTurn();
			playCard(context, player, "minion_recurring_torrent");
			assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "spell_the_coin");
			context.endTurn();
			playCard(context, player, "minion_recurring_torrent");
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testMindswapper() {
		runGym((context, player, opponent) -> {
			Card formerlyOpponentCard = receiveCard(context, opponent, "spell_test_overload");
			Card formerlyPlayerCard = receiveCard(context, player, "spell_test_spellpower");
			Card shouldNotSwapOpponent = receiveCard(context, opponent, "spell_the_coin");
			Card shouldNotSwapPlayer = receiveCard(context, player, "spell_the_coin");
			playCard(context, player, "minion_mindswapper");
			assertEquals(formerlyOpponentCard.getOwner(), player.getId());
			assertEquals(formerlyPlayerCard.getOwner(), opponent.getId());
			assertEquals(shouldNotSwapOpponent.getOwner(), opponent.getId());
			assertEquals(shouldNotSwapPlayer.getOwner(), player.getId());
		});
	}

	@Test
	public void testFassnuAvenger() {
		runGym((context, player, opponent) -> {
			Minion toDestroy = playMinionCard(context, player, "minion_test_deathrattle");
			Minion fassnu = playMinionCard(context, player, "minion_fassnu_avenger");
			destroy(context, toDestroy);
			Card shouldBeDrawn = shuffleToDeck(context, player, "spell_the_coin");
			destroy(context, fassnu);
			assertEquals(shouldBeDrawn.getZone(), Zones.HAND);
		});
	}

	@Test
	public void testCursingChimp() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playMinionCardWithBattlecry(context, player, "minion_cursing_chimp", target);
			assertEquals(target.transformResolved(context).getSourceCard().getCardId(), "minion_cursing_chimp");
		});
	}

	@Test
	public void testChromaticVohlok() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "minion_chromatic_vohlok");
			assertEquals(target.transformResolved(context).getSourceCard().getCardId(), "minion_chromatic_vohlok");
		});
	}

	@Test
	public void testBulletBull() {
		runGym((context, player, opponent) -> {
			Minion bullet = playMinionCard(context, player, "minion_bullet_bull");
			assertEquals(bullet.getAttack(), bullet.getBaseAttack() * 2);
			context.endTurn();
			assertEquals(bullet.getAttack(), bullet.getBaseAttack());
			context.endTurn();
			assertEquals(bullet.getAttack(), bullet.getBaseAttack() * 2);
		});
	}

	@Test
	public void testHoldoverLich() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_holdover_lich");
			Minion target1 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_test_deal_6", target1);
			assertEquals(target1.getHp(), 1);
			assertEquals(target1.getZone(), Zones.BATTLEFIELD);
			assertTrue(target1.hasAttribute(Attribute.CANNOT_REDUCE_HP_BELOW_1));
			context.endTurn();
			assertTrue(target1.isDestroyed());
			assertEquals(target1.getZone(), Zones.GRAVEYARD);
		});
	}

	@Test
	public void testMutamiteTerror() {
		runGym((context, player, opponent) -> {
			Card leftCard = receiveCard(context, player, "minion_neutral_test");
			Card rightCard = receiveCard(context, player, "minion_neutral_test");
			playCard(context, player, "minion_mutamite_terror");
			assertFalse(leftCard.hasAttribute(Attribute.DISCARDED));
			assertTrue(rightCard.hasAttribute(Attribute.DISCARDED));
		});
	}

	@Test
	public void testAberaSwarmEvolver() {
		runGym((context, player, opponent) -> {
			AtomicReference<String> name = new AtomicReference<>();
			AtomicInteger counter = new AtomicInteger(0);
			overrideDiscover(context, player, discoverActions -> {
				name.set(discoverActions.get(0).getCard().getName());
				counter.incrementAndGet();
				return discoverActions.get(0);
			});
			Minion larva = playMinionCard(context, player, "token_spiderling");
			playMinionCard(context, player, "token_abera_swarm_evolver");

			assertAdapted(name.get(), larva);
			assertEquals(player.getMinions().size(), 2);
			assertEquals(counter.get(), 1);
			playCard(context, player, "spell_spider_swarm");
			assertEquals(counter.get(), 1);
			assertEquals(player.getMinions().size(), 4);
			assertAdapted(name.get(), player.getMinions().get(2));
			assertAdapted(name.get(), player.getMinions().get(3));
		});
	}

	@Test
	public void testTheMaelstrom() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "permanent_the_maelstrom");
			int playerHp = player.getHero().getHp();
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, "spell_fireball", opponent.getHero());
			assertEquals(player.getHero().getHp() + opponent.getHero().getHp(), playerHp + opponentHp - 12);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "permanent_the_maelstrom");
			context.endTurn();
			int playerHp = player.getHero().getHp();
			int opponentHp = opponent.getHero().getHp();
			playCard(context, opponent, "spell_fireball", player.getHero());
			assertEquals(player.getHero().getHp() + opponent.getHero().getHp(), playerHp + opponentHp - 12);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "permanent_the_maelstrom");
			playCard(context, player, "spell_mirror_image");
			assertEquals(player.getMinions().size(), 5, "Maelstrom + 4 Mirror Image tokens");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "permanent_the_maelstrom");
			context.endTurn();
			playCard(context, opponent, "spell_mirror_image");
			assertEquals(opponent.getMinions().size(), 4, "4 Mirror Image tokens");
		});
	}

	@Test
	public void testPastryCook() {
		// Check condition isn't met when nothing is roasted
		runGym((context, player, opponent) -> {
			Card pastryCook = receiveCard(context, player, "minion_pastry_cook");
			Card shouldNotBeRoasted = shuffleToDeck(context, player, "spell_the_coin");
			player.setMana(pastryCook.getBaseManaCost());
			assertFalse(context.getLogic().conditionMet(player.getId(), pastryCook));
		});

		// Roasted using a spell
		runGym((context, player, opponent) -> {
			// Make sure the number stuck into the ROASTED attribute as the current turn isn't idiosyncratically zero
			context.endTurn();
			context.endTurn();
			Card pastryCook = receiveCard(context, player, "minion_pastry_cook");
			Card shouldBeRoasted = shuffleToDeck(context, player, "spell_the_coin");
			playCard(context, player, "minion_food_critic");
			assertEquals(shouldBeRoasted.getAttributeValue(Attribute.ROASTED), context.getTurn());
			player.setMana(pastryCook.getBaseManaCost());
			assertTrue(context.getLogic().conditionMet(player.getId(), pastryCook));
		});

		// Roasted by having a full hand
		runGym((context, player, opponent) -> {
			// Make sure the number stuck into the ROASTED attribute as the current turn isn't idiosyncratically zero
			context.endTurn();
			context.endTurn();
			Card pastryCook = receiveCard(context, player, "minion_pastry_cook");
			for (int i = 0; i < 9; i++) {
				receiveCard(context, player, "spell_the_coin");
			}
			Card shouldBeRoasted = shuffleToDeck(context, player, "spell_the_coin");
			playCard(context, player, "spell_arcane_intellect");
			assertEquals(shouldBeRoasted.getAttributeValue(Attribute.ROASTED), context.getTurn());
			player.setMana(pastryCook.getBaseManaCost());
			assertTrue(context.getLogic().conditionMet(player.getId(), pastryCook));
		});
	}

	@Test
	public void testBossHarambo() {
		runGym((context, player, opponent) -> {
			int BANANAS_EXPECTED_IN_HAND = 7;
			int BANANAS_EXPECTED_IN_DECK = 10 - BANANAS_EXPECTED_IN_HAND;
			for (int i = 0; i < 10 - BANANAS_EXPECTED_IN_HAND; i++) {
				receiveCard(context, player, "spell_the_coin");
			}
			playCard(context, player, "minion_boss_harambo");
			assertEquals(player.getHand().filtered(c -> c.getCardId().equals("spell_bananas")).size(), BANANAS_EXPECTED_IN_HAND);
			assertEquals(player.getDeck().size(), BANANAS_EXPECTED_IN_DECK);
			assertEquals(Stream.concat(player.getHand().stream(),
					player.getDeck().stream()).filter(c -> c.getCardId().equals("spell_bananas")).count(), 10);
		});
	}

	@Test
	public void testFantasticFeast() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(1);
			playCard(context, player, "spell_fantastic_feast");
			assertEquals(player.getDeck().size(), 2);
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp());
		});
	}

	@Test
	public void testFleshMonstrosity() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_divine_shield_test");
			target.setAttack(10);
			target.setHp(15);
			Minion fleshMonstrosity = playMinionCardWithBattlecry(context, player, "minion_flesh_monstrosity", target);
			assertEquals(fleshMonstrosity.getAttack(), fleshMonstrosity.getBaseAttack() + target.getAttack());
			assertEquals(fleshMonstrosity.getMaxHp(), fleshMonstrosity.getBaseHp() + target.getHp());
			assertTrue(fleshMonstrosity.hasAttribute(Attribute.DIVINE_SHIELD));
			assertTrue(fleshMonstrosity.getDescription().contains("Divine Shield"));
		});
	}

	@Test
	public void testBlackOxBrew() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_black_ox_brew");
			int hp = player.getHero().getHp();
			playCard(context, player, "spell_fireball", player.getHero());
			assertEquals(player.getHero().getHp(), hp - 1);
			context.endTurn();
			hp = player.getHero().getHp();
			playCard(context, opponent, "spell_fireball", player.getHero());
			assertEquals(player.getHero().getHp(), hp - 1);
			context.endTurn();
			hp = player.getHero().getHp();
			playCard(context, player, "spell_fireball", player.getHero());
			assertEquals(player.getHero().getHp(), hp - 6);
		});
	}

	@Test
	public void testAnzuTheRavenGod() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_anzu_the_raven_god");
			playCard(context, player, "spell_doom");
			assertEquals(player.getMinions().size(), 0);
		});
	}

	@Test
	public void testSeaWitchShufflesCard() {
		runGym((context, player, opponent) -> {
			useHeroPower(context, player, player.getHero().getReference());
			assertEquals(player.getDeck().get(0).getCardId(), "spell_ocean_depths");
		}, HeroClass.TEAL, HeroClass.TEAL);
	}

	@Test
	public void testLoyalLandowner() {
		runGym((context, player, opponent) -> {
			Minion loyal = playMinionCard(context, player, "minion_loyal_landowner");
			context.endTurn();
			context.endTurn();
			assertTrue(context.getValidActions().stream().anyMatch(ga -> ga.getActionType() == ActionType.PHYSICAL_ATTACK));
			attack(context, player, loyal, opponent.getHero());
			assertEquals(player.getMinions().size(), 2);
			assertFalse(context.getValidActions().stream().anyMatch(ga -> ga.getActionType() == ActionType.PHYSICAL_ATTACK));
		});
	}

	@Test
	public void testBrothersInBlood() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_brothers_in_blood", target);
			Minion buffed = playMinionCard(context, player, "minion_neutral_test");
			assertEquals(buffed.getAttack(), buffed.getBaseAttack() * 2);
			assertEquals(buffed.getMaxHp(), buffed.getBaseHp() * 2);
			Minion notBuffed = playMinionCard(context, player, "minion_black_test");
			assertEquals(notBuffed.getAttack(), notBuffed.getBaseAttack());
			assertEquals(notBuffed.getMaxHp(), notBuffed.getBaseHp());
		});
	}

	@Test
	public void testUnearthedHorror() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_unearthed_horror");
			for (int i = 1; i < 4; i++) {
				playCard(context, player, "spell_underwater_horrors", target);
				context.getLogic().drawCard(player.getId(), player);
				assertEquals(player.getHand().get(0).getCardId(), "minion_unearthed_horror");
				target = playMinionCard(context, player, player.getHand().get(0));
				assertEquals(target.getAttack(), target.getBaseAttack() + i * 7);
				assertEquals(target.getMaxHp(), target.getBaseHp() + i * 7);
			}
		});
	}

	@Test
	public void testSunslayer() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				putOnTopOfDeck(context, player, "spell_the_coin");
			}
			playCard(context, player, "weapon_sunslayer");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getWeaponZone().get(0).getDescription(context, player), "After your champion attacks, draw 0 cards. (Increases for every spell you've cast this turn)");
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_the_coin");
			assertEquals(player.getWeaponZone().get(0).getDescription(context, player), "After your champion attacks, draw 1 card. (Increases for every spell you've cast this turn)");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testArcaneSigil() {
		runGym((context, player, opponent) -> {
			// Shouldn't fire itself infinitely
			playCard(context, player, "secret_arcane_sigil");
			playCard(context, player, "secret_counterspell");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			playCard(context, opponent, "spell_mirror_image");
			assertEquals(opponent.getHero().getHp(), opponentHp - 2, "Should have triggered Arcane Sigil");
			assertEquals(player.getSecrets().size(), 0);
		});
	}

	@Test
	public void testCursedMirror() {
		runGym((context, player, opponent) -> {
			Minion test = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_cursed_mirror", test);
			assertEquals(test.getHp(), 30);
			assertEquals(player.getHero().getHp(), CardCatalogue.getCardById("minion_neutral_test").getBaseHp() + 10);
		});
	}

	@Test
	public void testGhuunTheFalseGod() {
		runGym((context, player, opponent) -> {
			// Cost 1
			playMinionCard(context, player, "minion_wisp");
			// Cost 2
			Minion destroyed = playMinionCard(context, player, "minion_bloodfen_raptor");
			// Cost 3
			playMinionCard(context, player, "minion_mind_control_tech");
			destroy(context, destroyed);
			Card card1 = receiveCard(context, player, "minion_cost_three_test");
			Card card2 = receiveCard(context, player, "minion_cost_three_test");
			Card card3 = receiveCard(context, player, "minion_neutral_test");
			playCard(context, player, "minion_ghuun_the_false_god");
			assertEquals(card1.getZone(), Zones.GRAVEYARD);
			assertEquals(card2.getZone(), Zones.GRAVEYARD);
			assertEquals(card3.getZone(), Zones.HAND);
			assertEquals(player.getMinions().size(), 5);
		});
	}

	@Test
	public void testBloodseeker() {
		GymFactory factory = getGymFactory((context, player, opponent) -> {
			player.setAttribute(Attribute.DISABLE_FATIGUE);
			opponent.setAttribute(Attribute.DISABLE_FATIGUE);
		});
		factory.run((context, player, opponent) -> {

			Minion bloodseeker = playMinionCard(context, player, "minion_bloodseeker");
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
			playCard(context, player, "spell_razorpetal", player.getHero());
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack() + 1);
			context.endTurn();
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
		});

		factory.run((context, player, opponent) -> {
			Minion bloodseeker = playMinionCard(context, player, "minion_bloodseeker");
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
			context.endTurn();
			playMinionCard(context, opponent, "minion_kobold_librarian");
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
			playCard(context, opponent, "spell_fireball", player.getHero());
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack() + 6);
			playCard(context, opponent, "spell_fireball", player.getHero());
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack() + 12);
			context.endTurn();
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
		});

		factory.run((context, player, opponent) -> {
			Minion bloodseeker1 = playMinionCard(context, player, "minion_bloodseeker");
			playCard(context, player, "spell_fireball", player.getHero());
			assertEquals(bloodseeker1.getAttack(), bloodseeker1.getBaseAttack() + 6);
			playCard(context, player, "minion_herald_volazj");
			Minion bloodseeker2 = player.getMinions().get(2);
			assertEquals(bloodseeker2.getSourceCard().getCardId(), "minion_bloodseeker");
			assertNotEquals(bloodseeker1, bloodseeker2);
			assertEquals(bloodseeker2.getAttack(), 7, "it's a 1/1 + 6");
			playCard(context, player, "spell_fireball", player.getHero());
			assertEquals(bloodseeker1.getAttack(), bloodseeker1.getBaseAttack() + 12);
			assertEquals(bloodseeker2.getAttack(), 1 + 12, "it's a 1/1 + 12");
			Minion bloodseeker3 = playMinionCard(context, player, "minion_bloodseeker");
			assertEquals(bloodseeker3.getAttack(), bloodseeker3.getBaseAttack() + 12);
			Minion bloodseeker4 = playMinionCardWithBattlecry(context, player, "minion_faceless_manipulator", bloodseeker3);
			assertEquals(bloodseeker4.getSourceCard().getCardId(), "minion_bloodseeker");
			assertNotEquals(bloodseeker1, bloodseeker4);
			assertNotEquals(bloodseeker2, bloodseeker4);
			assertNotEquals(bloodseeker3, bloodseeker4);
			assertEquals(bloodseeker4.getAttack(), bloodseeker4.getBaseAttack() + 12);
			context.endTurn();
			for (Minion bloodseeker : new Minion[]{bloodseeker1, bloodseeker3, bloodseeker4}) {
				assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
			}
			assertEquals(bloodseeker2.getAttack(), 1);
		});

		factory.run((context, player, opponent) -> {
			Minion bloodseeker = playMinionCard(context, player, "minion_bloodseeker");
			playCard(context, player, "spell_fireball", player.getHero());
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack() + 6);
			Minion faceless = playMinionCardWithBattlecry(context, player, "minion_faceless_manipulator", bloodseeker);
			assertEquals(faceless.getAttack(), faceless.getBaseAttack() + 6);
			context.endTurn();
			playCard(context, opponent, "spell_fireball", player.getHero());
			playCard(context, opponent, "spell_razorpetal", opponent.getHero());
			assertEquals(faceless.getAttack(), faceless.getBaseAttack() + 6);
			playCard(context, opponent, "spell_mind_control", faceless);
			assertEquals(faceless.getOwner(), opponent.getId());
			assertEquals(faceless.getAttack(), faceless.getBaseAttack() + 1, "+1 from razorpetal damage this turn");
			context.endTurn();
			assertEquals(faceless.getAttack(), faceless.getBaseAttack());
		});
	}

	@Test
	public void testOnyxPawn() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 15; i++) {
				putOnTopOfDeck(context, player, "spell_the_coin");
			}
			Minion transformed = playMinionCard(context, player, "minion_onyx_pawn");
			assertEquals(transformed.getSourceCard().getCardId(), "token_onyx_queen");
		});

		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				putOnTopOfDeck(context, player, "spell_the_coin");
			}
			Minion transformed = playMinionCard(context, player, "minion_onyx_pawn");
			assertEquals(transformed.getSourceCard().getCardId(), "token_onyx_queen");
		});
	}

	@Test
	public void testHeadlessHorseman() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_headless_horseman");
			Minion headlessHorseman = player.getMinions().stream().filter(m -> m.getSourceCard().getCardId().equals("minion_headless_horseman")).findFirst().orElseThrow(AssertionError::new);
			assertEquals(player.getMinions().size(), 7);
			destroy(context, headlessHorseman);
			assertTrue(player.getMinions().stream().noneMatch(m -> m.hasAttribute(Attribute.PERMANENT)));
		});
	}

	@Test
	public void testChaugnarTheCorruptor() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_neutral_test");
			playCard(context, player, "minion_chaugnar_the_corruptor");
			context.endTurn();
			context.endTurn();
			Minion test = playMinionCard(context, player, player.getHand().get(0));
			test.setAttribute(Attribute.CHARGE);
			attack(context, player, test, opponent.getHero());
			assertTrue(test.isDestroyed());
		});
	}

	@Test
	public void testReaderEaterGhahnbTheJudicatorInteraction() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 20; i++) {
				shuffleToDeck(context, player, "spell_the_coin");
			}
			playCard(context, player, "minion_ghahnb_the_judicator");
			Minion buffed = playMinionCard(context, player, "minion_reader_eater");
			useHeroPower(context, player, player.getHero().getReference());
			assertEquals(buffed.getAttack(), buffed.getBaseAttack() + 15);
			assertEquals(buffed.getMaxHp(), buffed.getBaseHp() + 15);
			assertEquals(player.getDeck().size(), 20 - 15, "Drew 15 cards, never added 15");
		});
	}

	@Test
	public void testUnderwaterHorrors() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "spell_underwater_horrors", target);
			putOnTopOfDeck(context, opponent, "spell_the_coin");
			context.endTurn();
			assertEquals(opponent.getHand().size(), 1);
			assertEquals(opponent.getHand().get(0).getCardId(), "spell_the_coin");
			context.endTurn();
			assertEquals(opponent.getHand().size(), 2);
			assertEquals(opponent.getHand().get(1).getCardId(), "minion_neutral_test");
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_underwater_horrors", target);
			putOnTopOfDeck(context, player, "spell_the_coin");
			context.endTurn();
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_neutral_test");
		});
	}

	@Test
	public void testForgottenScience() {
		runGym((context, player, opponent) -> {
			Minion intendedTarget = playMinionCard(context, player, "minion_neutral_test");
			Minion target1 = playMinionCard(context, player, "minion_neutral_test");
			Minion target2 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_forgotten_science");
			playCard(context, player, "spell_fireball", intendedTarget);
			assertFalse(intendedTarget.isDestroyed());
			assertTrue(target1.isDestroyed());
			assertTrue(target2.isDestroyed());
			playCard(context, player, "spell_fireball", intendedTarget);
			assertTrue(intendedTarget.isDestroyed());
		});
	}

	@Test
	public void testKahlOfTheDeep() {
		runGym((context, player, opponent) -> {
			Minion kahl = playMinionCard(context, player, "minion_kahl_of_the_deep");
			destroy(context, kahl);
			assertEquals(opponent.getDeck().size(), 1);
			for (int i = 0; i < 9; i++) {
				// Inserts to the bottom of the deck
				context.getLogic().insertIntoDeck(opponent, CardCatalogue.getCardById("spell_the_coin"), 0);
			}
			assertEquals(opponent.getDeck().size(), 10);
			context.endTurn();
			assertEquals(opponent.getHand().size(), 9, "Drew 8 cards + Kahl");
			assertEquals(opponent.getDeck().size(), 1, "1 card left in the deck");
			assertEquals(opponent.getHand().get(0).getCardId(), "minion_kahl_of_the_deep");
		});
	}

	@Test
	public void testVanalAmalgamInteraction() {
		runGym((context, player, opponent) -> {
			Minion amalgam = playMinionCard(context, player, "minion_nightmare_amalgam");
			Minion vanal = playMinionCardWithBattlecry(context, player, "minion_vanal_petkiper", amalgam);
			assertEquals(vanal.getAttack(), vanal.getBaseAttack() + 1);
			assertEquals(amalgam.getAttack(), amalgam.getBaseAttack() + 1);
		});
	}

	@Test
	public void testTaintedRavenSilenceInteraction() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 6; i++) {
				receiveCard(context, opponent, "spell_the_coin");
			}
			Minion taintedRaven = playMinionCard(context, player, "minion_tainted_raven");
			Card fireball = receiveCard(context, player, "spell_fireball");
			assertEquals(context.getLogic().applySpellpower(player, fireball, 6), 8);
			playCard(context, player, "spell_silence", taintedRaven);
			assertEquals(context.getLogic().applySpellpower(player, fireball, 6), 6);
		});
	}

	@Test
	public void testPrimordialSupremacy() {
		runGym((context, player, opponent) -> {
			Minion titan = playMinionCard(context, player, "minion_degenerator");
			Minion xenodrone = playMinionCard(context, player, "token_xenodrone_01");
			Minion buffed = playMinionCard(context, player, "minion_neutral_test");
			buffed.setAttribute(Attribute.WITHER, 1);
			Minion notBuffed = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_primordial_supremacy");
			for (Minion shouldBeBuffed : new Minion[]{titan, xenodrone}) {
				assertEquals(shouldBeBuffed.getAttack(), shouldBeBuffed.getBaseAttack() + 1);
				assertEquals(shouldBeBuffed.getMaxHp(), shouldBeBuffed.getBaseHp() + 2);
			}
			assertEquals(notBuffed.getAttack(), notBuffed.getBaseAttack());
			assertEquals(notBuffed.getMaxHp(), notBuffed.getBaseHp());
		});
	}

	@Test
	public void testHeartpiercer() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion hit = playMinionCard(context, player, "minion_neutral_test");
			hit.setHp(CardCatalogue.getCardById("weapon_heartpiercer").getDamage() + 2);
			Minion notHit = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "weapon_heartpiercer");
			player.getHero().getWeapon().setHp(1);
			attack(context, player, player.getHero(), hit);
			assertTrue(hit.isDestroyed());
			assertEquals(player.getHero().getAttributeValue(Attribute.DRAINED_THIS_TURN), 2);
		});
	}

	@Test
	public void testTheBloodEngine() {
		runGym((context, player, opponent) -> {
			// Drain 2 total
			playCard(context, player, "spell_test_drain", opponent.getHero());
			playCard(context, player, "spell_test_drain", opponent.getHero());
			context.endTurn();
			context.endTurn();
			// Drain three this turn
			playCard(context, player, "spell_test_drain", opponent.getHero());
			playCard(context, player, "spell_test_drain", opponent.getHero());
			playCard(context, player, "spell_test_drain", opponent.getHero());
			int opponentHealth = opponent.getHero().getHp();
			playMinionCard(context, player, "minion_the_blood_engine");
			assertEquals(opponent.getHero().getHp(), opponentHealth - 2);
		});
	}

	@Test
	public void testBloodToIron() {
		runGym((context, player, opponent) -> {
			Minion mech = playMinionCard(context, player, "minion_mech_test");
			Card bloodToIronCard = receiveCard(context, player, "spell_blood_to_iron");
			assertTrue(bloodToIronCard.getDescription(context, player).contains("Take 2"));
			int hp = player.getHero().getHp();
			playCard(context, player, bloodToIronCard);
			assertEquals(player.getHero().getHp(), hp - 2);
			assertEquals(player.getMinions().size(), 2);
		});

		runGym((context, player, opponent) -> {
			Minion mech = playMinionCard(context, player, "minion_mech_test");
			mech.setAttribute(Attribute.SPELL_DAMAGE, 1);
			Card bloodToIronCard = receiveCard(context, player, "spell_blood_to_iron");
			assertTrue(bloodToIronCard.getDescription(context, player).contains("Take *3*"));
			int hp = player.getHero().getHp();
			playCard(context, player, bloodToIronCard);
			assertEquals(player.getHero().getHp(), hp - 3);
			assertEquals(player.getMinions().size(), 2);
		});
	}

	@Test
	public void testDegenerator() {
		runGym((context, player, opponent) -> {
			Minion degenerator = playMinionCard(context, player, "minion_degenerator");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			int wither = degenerator.getAttributeValue(Attribute.WITHER);
			int maxHp = degenerator.getAttack() + wither + 1;
			context.getLogic().setHpAndMaxHp(target, maxHp);
			assertEquals(target.getHp(), maxHp);
			context.endTurn();
			attack(context, player, degenerator, target);
			assertEquals(target.getHp(), maxHp - wither - degenerator.getAttack());
			assertEquals(target.getAttack(), Math.max(0, target.getBaseAttack() - wither));
			context.endTurn();
			assertEquals(target.getHp(), maxHp - wither - degenerator.getAttack());
			assertEquals(target.getAttack(), Math.max(0, target.getBaseAttack() - wither));
			context.endTurn();
			assertEquals(target.getHp(), maxHp - degenerator.getAttack());
			assertEquals(target.getAttack(), target.getBaseAttack());
		});

		// Check that wither kills minions
		runGym((context, player, opponent) -> {
			Minion degenerator = playMinionCard(context, player, "minion_degenerator");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			int wither = degenerator.getAttributeValue(Attribute.WITHER);
			int maxHp = degenerator.getAttack() + wither - 1;
			context.getLogic().setHpAndMaxHp(target, maxHp);
			assertEquals(target.getHp(), maxHp);
			context.endTurn();
			attack(context, player, degenerator, target);
			assertTrue(target.isDestroyed());
		});
	}

	@Test
	public void testTaintedRaven() {
		runGym((context, player, opponent) -> {
			Minion taintedRaven = playMinionCard(context, player, "minion_tainted_raven");
			for (int i = 0; i < 5; i++) {
				receiveCard(context, opponent, "spell_the_coin");
			}
			context.getLogic().endOfSequence();
			int hp = opponent.getHero().getHp();
			playCard(context, player, "spell_fireball", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 6, "No spell damage yet");
			receiveCard(context, opponent, "spell_the_coin");
			context.getLogic().endOfSequence();
			hp = opponent.getHero().getHp();
			playCard(context, player, "spell_fireball", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 6 - 2, "+2 spell damage");
			context.getLogic().discardCard(opponent, opponent.getHand().get(0));
			context.getLogic().discardCard(opponent, opponent.getHand().get(0));
			hp = opponent.getHero().getHp();
			context.getLogic().endOfSequence();
			playCard(context, player, "spell_fireball", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 6, "No spell damage");
		});
	}

	@Test
	public void testBloodMoonRising() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_blood_moon_rising");
			Minion charger = playMinionCard(context, player, "minion_charge_test");
			attack(context, player, charger, opponent.getHero());
			assertEquals(player.getHero().getMaxHp(), player.getHero().getBaseHp() + charger.getAttack());
		});
	}

	@Test
	public void testVampiricSavage() {
		runGym((context, player, opponent) -> {
			Minion savage = playMinionCard(context, player, "minion_vampiric_savage");
			playCard(context, player, "spell_hemoshield");
			assertEquals(savage.getMaxHp(), savage.getBaseHp() + 5);
		});
	}

	@Test
	public void testSkullsplitterTroll() {
		runGym((context, player, opponent) -> {
			Minion troll = playMinionCard(context, player, "minion_skullsplitter_troll");
			for (int i = 0; i < 2; i++) {
				playMinionCard(context, player, "minion_neutral_test");
			}
			Minion lifetaker = playMinionCard(context, player, "minion_lifetaker");
			assertEquals(lifetaker.getMaxHp(), lifetaker.getBaseHp() + 3);
			assertEquals(troll.getAttack(), troll.getBaseAttack() + 1, "Draining only occurred once");
		});
	}

	@Test
	public void testLifetaker() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				playMinionCard(context, player, "minion_neutral_test");
			}
			Minion lifetaker = playMinionCard(context, player, "minion_lifetaker");
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
	public void testDeathsCaress() {
		runGym((context, player, opponent) -> {
			Minion spellDamage1 = playMinionCard(context, player, "minion_neutral_test");
			spellDamage1.setAttribute(Attribute.SPELL_DAMAGE, 1);
			Minion testTarget = playMinionCard(context, player, "minion_neutral_test");
			testTarget.setHp(10);
			playCard(context, player, "spell_deaths_caress", testTarget);
			context.endTurn();
			assertEquals(testTarget.getHp(), 6, "Should have been dealt 3 + 1 spell damage");
		});
	}

	@Test
	public void testStitches() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			Minion stitches = playMinionCardWithBattlecry(context, player, "minion_stitches", target);
			assertTrue(target.isDestroyed());
			assertEquals(stitches.getAttack(), target.getBaseAttack() + stitches.getBaseAttack());
			assertEquals(stitches.getMaxHp(), target.getBaseHp() + stitches.getBaseHp());
		});
	}

	@Test
	public void testRoll() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_blessing_of_might", target);
			playCard(context, player, "spell_roll", target);
			Minion newTarget = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(newTarget.getAttack(), target.getBaseAttack() + 3);
		});
	}

	@Test
	public void testScatterstorm() {
		runGym((context, player, opponent) -> {
			Minion friendly = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_wisp");
			context.endTurn();
			Card newFriendly = shuffleToDeck(context, player, "minion_bloodfen_raptor");
			Card newEnemy = shuffleToDeck(context, opponent, "minion_river_crocolisk");
			playCard(context, player, "spell_scatterstorm");
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getDeck().get(0).getCardId(), friendly.getSourceCard().getCardId());
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), newFriendly.getSourceCard().getCardId());
			assertEquals(opponent.getDeck().size(), 1);
			assertEquals(opponent.getDeck().get(0).getCardId(), enemy.getSourceCard().getCardId());
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), newEnemy.getSourceCard().getCardId());
		});
	}

	@Test
	public void testSweetStrategy() {
		runGym((context, player, opponent) -> {
			Card shouldNotShuffle = receiveCard(context, player, "spell_the_coin");
			Card shouldShuffle = receiveCard(context, player, "spell_fireball");
			playCard(context, player, "spell_sweet_strategy");
			assertEquals(player.getDeck().size(), 2);
			assertTrue(player.getDeck().stream().allMatch(c -> c.getCardId().equals(shouldShuffle.getCardId())));
		});
	}

	@Test
	public void testCastleGiant() {
		runGym((context, player, opponent) -> {
			Card castleGiant = receiveCard(context, player, "minion_castle_giant");
			assertEquals(costOf(context, player, castleGiant), castleGiant.getBaseManaCost());
			useHeroPower(context, player);
			context = context.clone();
			player = context.getPlayer1();
			assertEquals(costOf(context, player, castleGiant), castleGiant.getBaseManaCost() - 1);
		}, HeroClass.GOLD, HeroClass.GOLD);

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_justicar_trueheart");
			Card castleGiant = receiveCard(context, player, "minion_castle_giant");
			assertEquals(costOf(context, player, castleGiant), castleGiant.getBaseManaCost());
			useHeroPower(context, player);
			assertEquals(costOf(context, player, castleGiant), castleGiant.getBaseManaCost() - 2);
		}, HeroClass.GOLD, HeroClass.GOLD);

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_justicar_trueheart");
			useHeroPower(context, player);
			Card castleGiant = receiveCard(context, player, "minion_castle_giant");
			assertEquals(costOf(context, player, castleGiant), castleGiant.getBaseManaCost() - 2);
		}, HeroClass.GOLD, HeroClass.GOLD);
	}

	@Test
	public void testOneOnOne() {
		// Test 2 enemies, CAN choose enemy because there is another enemy to attack it.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion enemy1 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion enemy2 = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			Card checkTargets = receiveCard(context, player, "spell_one_on_one");
			player.setMana(3);
			assertTrue(context.getValidActions().stream().anyMatch(ga -> ga.getSourceReference().equals(checkTargets.getReference()) && ga.getTargetReference().equals(enemy1.getReference())));
			assertTrue(context.getValidActions().stream().anyMatch(ga -> ga.getSourceReference().equals(checkTargets.getReference()) && ga.getTargetReference().equals(enemy2.getReference())));
		});

		// Test 1 enemy, two friendlies, cannot choose enemy because there won't be another enemy to attack it
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			Minion friendly1 = playMinionCard(context, player, "minion_neutral_test");
			Minion friendly2 = playMinionCard(context, player, "minion_neutral_test");
			Card checkTargets = receiveCard(context, player, "spell_one_on_one");
			player.setMana(3);
			assertFalse(context.getValidActions().stream().anyMatch(ga -> ga.getSourceReference().equals(checkTargets.getReference()) && ga.getTargetReference().equals(enemy.getReference())));
		});

		// Test 2 enemies, choose one, check that the other initiates the attack
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion enemy1 = playMinionCard(context, opponent, "minion_neutral_test");
			// Choosing values for unambiguous differences
			enemy1.setAttack(1);
			enemy1.setHp(3);
			Minion enemy2 = playMinionCard(context, opponent, "minion_neutral_test");
			enemy2.setAttack(2);
			enemy2.setHp(5);
			context.endTurn();
			Card checkTargets = receiveCard(context, player, "spell_one_on_one");
			player.setMana(3);
			playCard(context, player, checkTargets, enemy1);
			assertEquals(enemy1.getHp(), 3 - 2);
			assertEquals(enemy2.getHp(), 5 - 1);
		});
	}

	@Test
	public void testDoubleDown() {
		// On your side: A marin, another marin, a treasure chest, a seven shot gunner, and another seven shot gunner. Note we destroyed the enemy treasure chest
		runGym((context, player, opponent) -> {
			Minion shouldNotHave = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, shouldNotHave);
			context.endTurn();
			context.endTurn();
			playMinionCard(context, player, "minion_marin_the_fox");
			Minion treasure = opponent.getMinions().get(0);
			playMinionCard(context, player, "minion_seven_shot_gunner");
			assertTrue(treasure.isDestroyed());
			playCard(context, player, "spell_double_down");
			assertEquals(player.getMinions().size(), 5);
			Map<String, Integer> counts = new HashMap<>();
			counts.put("minion_marin_the_fox", 2);
			counts.put("token_treasure_chest", 1);
			counts.put("minion_seven_shot_gunner", 2);
			counts.put("minion_neutral_test", 0);
			for (Map.Entry<String, Integer> count : counts.entrySet()) {
				assertEquals(player.getMinions().stream().filter(e -> e.getSourceCard().getCardId().equals(count.getKey())).count(), (long) count.getValue());
			}
		});
	}

	@Test
	public void testSevenShotGunner() {
		// Test Marin the Fox interaction
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_marin_the_fox");
			Minion treasure = opponent.getMinions().get(0);
			playMinionCard(context, player, "minion_seven_shot_gunner");
			assertTrue(treasure.isDestroyed());
			assertEquals(player.getHand().size(), 1);
			assertTrue(Sets.newHashSet("token_golden_kobold",
					"spell_tolins_goblet",
					"spell_wondrous_wand",
					"spell_zarogs_crown").contains(player.getHand().get(0).getCardId()));
		});
	}

	@Test
	public void testFairyFixpicker() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_murloc_fixpicker");
			context.getLogic().drawCard(player.getId(), player);
			assertEquals(player.getHand().size(), 2);
			assertTrue(player.getHand().get(0).getRace().hasRace(Race.FAE));
			assertTrue(player.getHand().get(1).getRace().hasRace(Race.FAE));
			assertEquals(player.getDeck().size(), 0);
		});
	}

	@Test
	public void testKitesailRavager() {
		// Should pick off minions one by one
		runGym((context, player, opponent) -> {
			Card kitesailCard = receiveCard(context, player, "minion_kitesail_ravager");
			int quantity = kitesailCard.getBaseHp() - 1;
			List<Minion> targets = new ArrayList<>();
			int friendlyCount = quantity / 2 + (quantity % 2);
			for (int i = 0; i < friendlyCount; i++) {
				targets.add(playMinionCard(context, player, "minion_wisp"));
			}
			int enemyCount = quantity / 2;
			context.endTurn();
			for (int i = 0; i < enemyCount; i++) {
				targets.add(playMinionCard(context, opponent, "minion_wisp"));
			}
			context.endTurn();
			Minion kitesail = playMinionCard(context, player, kitesailCard);
			assertTrue(targets.stream().allMatch(Actor::isDestroyed));
			assertEquals(kitesail.getHp(), 1);
		});
	}

	@Test
	public void testDoomgunners() {
		// Attack 3 times, required 4 to kill
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_doomgunners");
			context.endTurn();
			Minion defender = playMinionCard(context, player, "minion_neutral_test");
			defender.setAttack(0);
			defender.setHp(attacker.getAttack() * 3 + 1);
			context.endTurn();
			attack(context, player, attacker, defender);
			assertFalse(attacker.isDestroyed());
			assertFalse(defender.isDestroyed());
			assertEquals(defender.getHp(), 1, "Should still be alive, after 3 attacks");
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 1 - 3);
		});

		// Just needs 2 attacks to finish
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_doomgunners");
			context.endTurn();
			Minion defender = playMinionCard(context, player, "minion_neutral_test");
			defender.setAttack(0);
			defender.setHp(attacker.getAttack() * 2);
			context.endTurn();
			attack(context, player, attacker, defender);
			assertFalse(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 1 - 2);
		});

		// Defender kills attacker during the second attack, should not trigger a third attack. Third attack would kill the defender
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_doomgunners");
			context.endTurn();
			Minion defender = playMinionCard(context, player, "minion_neutral_test");
			defender.setAttack(attacker.getHp() / 2);
			defender.setHp(attacker.getAttack() * 2 + 1);
			context.endTurn();
			attack(context, player, attacker, defender);
			assertTrue(attacker.isDestroyed());
			assertFalse(defender.isDestroyed());
			assertEquals(defender.getHp(), 1, "Should still be alive, after 2 attacks");
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 1 - 2, "Attacker should have only get 2 attacks");
		});
	}

	@Test
	public void testCattaTheMerciless() {

		runGym((context, player, opponent) -> {
			Minion catta = playMinionCard(context, player, "minion_catta_the_merciless");

			Minion attacker = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();

			attack(context, player, attacker, defender);

			assertTrue(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 0);
		});

		// Test interaction that requires 2 attacks for defender to die
		runGym((context, player, opponent) -> {
			Minion catta = playMinionCard(context, player, "minion_catta_the_merciless");

			Minion attacker = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_neutral_test");
			defender.setAttack(0);
			attacker.setAttack(1);
			context.endTurn();

			int expectedAttacks = defender.getHp() / attacker.getAttack();
			attack(context, player, attacker, defender);

			assertFalse(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 1 - expectedAttacks);
		});

		// Test interaction with immune
		runGym((context, player, opponent) -> {
			Minion catta = playMinionCard(context, player, "minion_catta_the_merciless");

			Minion attacker = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_neutral_test");
			defender.setAttack(96);
			defender.setHp(96);
			attacker.setAttribute(Attribute.IMMUNE);
			attacker.setAttack(1);
			context.endTurn();

			int expectedAttacks = defender.getHp() / attacker.getAttack();
			attack(context, player, attacker, defender);

			assertFalse(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 1 - expectedAttacks);
		});

		// Test infinite recursion mitigation
		runGym((context, player, opponent) -> {
			Minion catta = playMinionCard(context, player, "minion_catta_the_merciless");

			Minion attacker = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_neutral_test");
			defender.setAttack(0);
			defender.setHp(97);
			attacker.setAttack(1);
			context.endTurn();

			int expectedAttacks = defender.getHp() / attacker.getAttack();
			attack(context, player, attacker, defender);

			assertFalse(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), -95, "Hit limit on attacks");
		});
	}

	@Test
	public void testAysaCloudsinger() {
		GymFactory factory = getGymFactory((context, player, opponent) -> {
			int heroHp = 15;
			player.getHero().setHp(heroHp);
			playCard(context, player, "minion_aysa_cloudsinger");
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_aysa_cloudsinger");
			assertEquals(player.getMinions().size(), 1);
			Minion chenToken = player.getMinions().get(0);
			assertEquals(chenToken.getSourceCard().getCardId(), "token_chen_stormstout");
			assertEquals(chenToken.getHp(), heroHp);
			assertEquals(chenToken.getMaxHp(), 30);
		});

		// Test 1: Fatal damage, chen destroyed, player loses
		factory.run((context, player, opponent) -> {
			Minion chenToken = player.getMinions().get(0);
			destroy(context, chenToken);
			playCard(context, player, "spell_pyroblast", player.getHero());
			assertTrue(context.updateAndGetGameOver());
			assertEquals(context.getWinner(), opponent);
		});

		// Test 2: fatal damage, chen on board, player restores chen with health
		factory.run((context, player, opponent) -> {
			Minion chenToken = player.getMinions().get(0);
			int hpValue = 17;
			chenToken.setHp(hpValue);
			playCard(context, player, "spell_pyroblast", player.getHero());
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_chen_stormstout");
			assertEquals(player.getHero().getHp(), hpValue);
			assertEquals(player.getMinions().size(), 0);
		});

		// Test 3: fatal damage, chen in hand, player restores chen with full health
		factory.run((context, player, opponent) -> {
			Minion chenToken = player.getMinions().get(0);
			context.endTurn();
			playCard(context, opponent, "spell_sap", chenToken);
			context.endTurn();
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getMinions().size(), 0);
			playCard(context, player, "spell_pyroblast", player.getHero());
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_chen_stormstout");
			assertEquals(player.getHero().getHp(), 30);
			assertEquals(player.getHand().size(), 0);
		});

		// Test 4: fatal damage, chen in deck, player restores chen with full health.
		factory.run((context, player, opponent) -> {
			Minion chenToken = player.getMinions().get(0);
			context.endTurn();
			playCard(context, opponent, "spell_recycle", chenToken);
			// Give the player something to draw
			putOnTopOfDeck(context, player, "spell_the_coin");
			context.endTurn();
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getMinions().size(), 0);
			playCard(context, player, "spell_pyroblast", player.getHero());
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_chen_stormstout");
			assertEquals(player.getHero().getHp(), 30);
			assertEquals(player.getDeck().size(), 0);
		});
	}

	@Test
	public void testFissureLordXahdorahInteraction() {
		runGym((context, player, opponent) -> {
			Minion xahDorah = playMinionCard(context, player, "minion_lord_xah_dorah");
			context.endTurn();
			Minion warGolem = playMinionCard(context, opponent, "minion_war_golem");
			context.endTurn();
			useHeroPower(context, player, xahDorah.getReference());
			assertEquals(xahDorah.getAttack(), xahDorah.getBaseAttack() + 1);
			playCard(context, player, "spell_fissure");
			assertTrue(warGolem.isDestroyed());
		}, HeroClass.RUST, HeroClass.RUST);
	}

	@Test
	public void testIdiotSandwich() {
		runGym((context, player, opponent) -> {
			Minion notBuffed1 = playMinionCard(context, player, "minion_neutral_test");
			Minion left = playMinionCard(context, player, "minion_neutral_test");
			Minion idiot = playMinionCard(context, player, "minion_idiot_sandwich");
			Minion right = playMinionCard(context, player, "minion_neutral_test");
			Minion notBuffed2 = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, idiot);
			assertEquals(left.getAttack(), left.getBaseAttack() + 2);
			assertEquals(left.getMaxHp(), left.getBaseHp() + 2);
			assertEquals(right.getAttack(), right.getBaseAttack() + 2);
			assertEquals(right.getMaxHp(), right.getBaseHp() + 2);
			for (Minion notBuffed : new Minion[]{notBuffed1, notBuffed2}) {
				assertEquals(notBuffed.getAttack(), notBuffed.getBaseAttack());
				assertEquals(notBuffed.getMaxHp(), notBuffed.getBaseHp());
			}
		});
	}

	@Test
	public void testAncestralPlaneGrumbleTheWorldshakerInteraction() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_grumble_worldshaker");
			context.endTurn();
			playCard(context, opponent, "spell_psychic_scream");
			assertEquals(player.getDeck().get(0).getCardId(), "minion_grumble_worldshaker");
			putOnTopOfDeck(context, player, "spell_the_coin");
			context.endTurn();
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "spell_the_coin", "Should draw the Coin");
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_grumble_worldshaker");
			AtomicBoolean didDiscover = new AtomicBoolean();
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(discoverActions.size(), 1);
				assertEquals(discoverActions.get(0).getCard().getCardId(), "minion_grumble_worldshaker");
				assertTrue(didDiscover.compareAndSet(false, true), "should discover once");
				return discoverActions.get(0);
			});
			playCard(context, player, "spell_ancestral_plane");
			assertTrue(didDiscover.get());
			assertEquals(player.getHand().get(1).getCardId(), "minion_grumble_worldshaker");
		});
	}

	@Test
	public void testLuminaLightOfTheforest() {
		// Test that Lumina doesn't trigger off herself
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> {
				fail("should not discover");
				return discoverActions.get(0);
			});
			playMinionCard(context, player, "minion_lumina");
		});

		// Test that Lumina only discovers minions and not spells
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(discoverActions.size(), 1);
				assertEquals(discoverActions.get(0).getCard().getCardType(), CardType.MINION);
				return discoverActions.get(0);
			});
			context.setDeckFormat(new FixedCardsDeckFormat("minion_wisp", "spell_mirror_image"));
			playMinionCard(context, player, "minion_lumina");
			playMinionCard(context, player, "minion_wisp");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_wisp");
		});

		// Test that Lumina discovers minions of the same tribe
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(discoverActions.size(), 1);
				assertEquals(discoverActions.get(0).getCard().getCardType(), CardType.MINION);
				return discoverActions.get(0);
			});
			context.setDeckFormat(new FixedCardsDeckFormat("minion_bloodfen_raptor", "spell_mirror_image"));
			playMinionCard(context, player, "minion_lumina");
			playMinionCard(context, player, "minion_bloodfen_raptor");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
		}, HeroClass.BLUE, HeroClass.BLUE);
	}

	@Test
	public void testAncestralLegacy() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_murloc_tidehunter");
			assertEquals(player.getMinions().size(), 2);
			playCard(context, player, "spell_doom");
			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getGraveyard().size(), 4, "Should contain Doom, Murloc Tidehunter the card, Murloc Tidehunter the minion and Murloc Scout the minion.");
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(discoverActions.size(), 1);
				assertEquals(discoverActions.get(0).getCard().getCardId(), "minion_murloc_tidehunter");
				return discoverActions.get(0);
			});
			playCard(context, player, "spell_ancestral_legacy");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_murloc_tidehunter");
		});
	}

	@Test
	public void testTikrakazzLordJaraxxusInteraction() {
		runGym((context, player, opponent) -> {
			// Adapting Lord Jaraxxus in this phase shouldn't cause an exception, because changing heroes the way the Lord
			// does seems to put the minion into the removed from play zone.
			playCard(context, player, "minion_tikrakazz");
			playCard(context, player, "minion_lord_jaraxxus");
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_jaraxxus");
			playCard(context, player, "minion_lord_jaraxxus");
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_jaraxxus");
		});
	}

	@Test
	public void testFendOff() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 2; i++) {
				playMinionCard(context, player, "minion_dire_mole");
			}
			context.endTurn();
			for (int i = 0; i < 3; i++) {
				Minion enemy = playMinionCard(context, opponent, "minion_dire_mole");
				context.getLogic().setHpAndMaxHp(enemy, 10);
			}
			context.endTurn();
			playCard(context, player, "spell_fend_off");
			for (Minion source : player.getMinions()) {
				assertEquals(source.getHp(), source.getMaxHp() - 1, "Should take 1 damage from attacking a random target");
			}
			assertEquals(opponent.getMinions().stream().mapToInt(m -> m.getMaxHp() - m.getHp()).sum(), player.getMinions().stream().mapToInt(Minion::getAttack).sum(), "Each source minion should have attacked");
		});
	}

	@Test
	public void testRhunokTheBear() {
		runGym((context, player, opponent) -> {
			Minion bear = playMinionCard(context, player, "minion_rhunok_the_bear");
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "spell_bananas", bear);
			assertEquals(player.getMinions().size(), 2);
			playCard(context, player, "spell_bananas", wisp);
			assertEquals(player.getMinions().size(), 3);
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "minion_wisp", "Wisp copied");
			Minion wispCopy = player.getMinions().get(2);
			assertEquals(wispCopy.getAttack(), wispCopy.getBaseAttack() + 1, "Keeps buffs since it was copied AFTER the spell was cast");
			assertEquals(wispCopy.getMaxHp(), wispCopy.getBaseHp() + 1, "Keeps buffs since it was copied AFTER the spell was cast");
			assertTrue(wispCopy.hasAttribute(Attribute.TAUNT), "Gained taunt");
		});
	}


	@Test
	public void testRafaamSupremeThief() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_the_coin");
			shuffleToDeck(context, opponent, "minion_wisp");
			playCard(context, player, "hero_rafaam_supreme_thief");
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_wisp");
			assertEquals(opponent.getDeck().size(), 1);
			assertEquals(opponent.getDeck().get(0).getCardId(), "minion_wisp");
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "hero_rafaam_supreme_thief");
			useHeroPower(context, player, target.getReference());
			destroy(context, target);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_neutral_test");
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "hero_rafaam_supreme_thief");
			playCard(context, player, "spell_mind_control", target);
			useHeroPower(context, player, target.getReference());
			destroy(context, target);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_neutral_test");
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "hero_rafaam_supreme_thief");
			useHeroPower(context, player, target.getReference());
			context.endTurn();
			playCard(context, opponent, "spell_mind_control", target);
			destroy(context, target);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_neutral_test");
		});
	}

	@Test
	public void testAutomedicAndrone() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_automedic_androne");
			player.getHero().setHp(10);
			playCard(context, player, "spell_healing_touch", player.getHero());
			assertEquals(player.getHero().getHp(), 10);
			assertEquals(player.getHero().getArmor(), 8);
			context.endTurn();
			opponent.getHero().setHp(10);
			playCard(context, opponent, "spell_healing_touch", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), 10);
			assertEquals(opponent.getHero().getArmor(), 8);
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_tyrantus");
			playMinionCard(context, player, "minion_automedic_androne");
			target.setHp(3);
			playCard(context, player, "spell_healing_touch", target);
			assertEquals(target.getHp(), 3);
			assertEquals(player.getHero().getArmor(), 8);
			context.endTurn();
			target = playMinionCard(context, opponent, "minion_tyrantus");
			target.setHp(3);
			playCard(context, opponent, "spell_healing_touch", target);
			assertEquals(target.getHp(), 3);
			assertEquals(opponent.getHero().getArmor(), 8);
		});
	}

	@Test
	public void testSunlance() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_wisp");
			for (int i = 0; i < 4; i++) {
				shuffleToDeck(context, player, "spell_the_coin");
			}
			playCard(context, player, "spell_sunlance", target);
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 1);
		});
	}

	@Test
	public void testThunderfury() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_thunderfury");
			player.getHero().getWeapon().setMaxHp(4);
			player.getHero().getWeapon().setHp(4);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHero().getWeapon().getDurability(), player.getHero().getWeapon().getMaxDurability() - 1);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHero().getWeapon().getDurability(), player.getHero().getWeapon().getMaxDurability() - 2);
			playCard(context, player, "spell_test_overload");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHero().getWeapon().getDurability(), player.getHero().getWeapon().getMaxDurability() - 2);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 0);
			assertFalse(player.getHero().getWeapon().hasAttribute(Attribute.AURA_IMMUNE));
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHero().getWeapon().getDurability(), player.getHero().getWeapon().getMaxDurability() - 3);
		});
	}

	@Test
	public void testElorthaNoShadra() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_ice_rager");
			playCard(context, player, "minion_elortha_no_shadra");
			context.getLogic().drawCard(player.getId(), player);
			Minion iceRager = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(iceRager.getDeathrattles().size(), 1);
			destroy(context, iceRager);
			assertEquals(player.getMinions().size(), 2);
			Minion revived = player.getMinions().get(1);
			destroy(context, revived);
			assertTrue(revived.isDestroyed());
			assertEquals(player.getMinions().size(), 1);
		});
	}

	@Test
	public void testPurrfectTrackerSeaforiumBombInteraction() {
		for (int i = 0; i < 100; i++) {
			runGym((context, player, opponent) -> {
				putOnTopOfDeck(context, player, "spell_the_coin");
				putOnTopOfDeck(context, player, "spell_seaforium_bomb");
				playCard(context, player, "minion_purrfect_tracker");
			});
		}
	}

	@Test
	public void testBrightEyedScoutInteractions() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "weapon_unidentified_maul");
			playCard(context, player, "minion_bright_eyed_scout");
			assertEquals(costOf(context, player, player.getHand().get(0)), 5);
		});

		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "spell_the_coin");
			putOnTopOfDeck(context, player, "spell_seaforium_bomb");
			playCard(context, player, "minion_bright_eyed_scout");
			// The coin should be in your hand, and its cost should not have been changed
			assertEquals(costOf(context, player, player.getHand().get(0)), 0);
		});

		runGym((context, player, opponent) -> {
			receiveCard(context, opponent, "spell_the_coin");
			shuffleToDeck(context, player, "minion_chameleos");
			playCard(context, player, "minion_bright_eyed_scout");
			context.endTurn();
			context.endTurn();
			// It's the coin now
			assertEquals(costOf(context, player, player.getHand().get(0)), 0);
			context.endTurn();
			context.endTurn();
			assertEquals(costOf(context, player, player.getHand().get(0)), 0);
		});
	}

	@Test
	public void testElaborateSchemeGloatInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_elaborate_scheme");
			Card gloat = putOnTopOfDeck(context, player, "secret_gloat");
			putOnTopOfDeck(context, player, "spell_the_coin");
			putOnTopOfDeck(context, player, "spell_the_coin");
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getSecrets().get(0).getSourceCard().getCardId(), "secret_gloat");
		});
	}

	@Test
	public void testCracklingArrows() {
		runGym((context, player, opponent) -> {
			List<Minion> minions = new ArrayList<>();
			context.endTurn();
			for (int i = 0; i < 6; i++) {
				minions.add(playMinionCard(context, opponent, "minion_wisp"));
			}
			context.endTurn();
			int opponentHealth = opponent.getHero().getHp();
			playCard(context, player, "secret_avenge");
			playCard(context, player, "secret_counterspell");
			assertEquals(player.getSecrets().size(), 2);
			playCard(context, player, "spell_crackling_arrows");
			assertEquals(player.getSecrets().size(), 2);
			// 2 secrets + 1 minimum call = spell cast 3 times, all 6 minions should be dead
			int count = 0;
			for (Minion minion : minions) {
				if (minion.isDestroyed()) {
					count++;
				}
			}
			assertEquals(6, count + opponentHealth - opponent.getHero().getHp());
		});
	}

	@Test
	public void testGiantBarbecue() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion target2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion target3 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "spell_giant_barbecue", target2);
			assertTrue(target1.isDestroyed());
			assertTrue(target2.isDestroyed());
			assertTrue(target3.isDestroyed());
		});
	}

	@Test
	public void testSidelineCoach() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_sideline_coach");
			Minion attacker = playMinionCard(context, player, "minion_wisp");
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_target_dummy");
			context.endTurn();
			attack(context, player, attacker, defender);
			assertEquals(attacker.getAttack(), attacker.getBaseAttack() + 1);
			assertEquals(attacker.getMaxHp(), attacker.getBaseHp() + 1);
			assertEquals(defender.getAttack(), defender.getBaseAttack());
			assertEquals(defender.getMaxHp(), defender.getBaseHp());
		});
	}

	@Test
	public void testDoctorHatchett() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playMinionCard(context, player, "minion_doctor_hatchett");
			// Destroy the egg
			destroy(context, player.getMinions().get(1));
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_bloodfen_raptor");
			assertEquals(player.getDeck().size(), 0);
		});
	}

	@Test
	public void testBwonsamdi() {
		// Test that a deathrattle minion played from the hand doesn't get its own deathrattle copied onto it
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_bwonsamdi");
			Card leper = receiveCard(context, player, "minion_leper_gnome");
			playCard(context, player, leper);
			// Destroy the Leper Gnome
			destroy(context, player.getMinions().get(1));
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 2, "Leper Gnome should not have gotten its deathrattle doubled from Bwonsamdi.");
		});

		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_bwonsamdi");
			Card leper = receiveCard(context, player, "minion_leper_gnome");
			Card hoarder = receiveCard(context, player, "minion_loot_hoarder");
			shuffleToDeck(context, player, "spell_the_coin");
			playCard(context, player, leper);
			// Destroy the Leper Gnome
			destroy(context, player.getMinions().get(1));
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 2, "Leper Gnome should not have gotten its deathrattle doubled from Bwonsamdi.");
			assertEquals(player.getHand().peek().getCardId(), "spell_the_coin", "Should have drawn The Coin from a Loot Hoarder deathrattle.");
		});
	}

	@Test
	public void testVindication() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "spell_vindication", target);
			assertTrue(player.getHero().hasAttribute(Attribute.DIVINE_SHIELD));
			playCard(context, player, "spell_razorpetal", player.getHero());
			assertFalse(player.getHero().hasAttribute(Attribute.DIVINE_SHIELD));
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp());
		});
	}

	@Test
	public void testTestYourMight() {
		runGym((context, player, opponent) -> {
			Minion loser = playMinionCard(context, player, "token_treant");
			Minion notSelected1 = playMinionCard(context, player, "minion_wisp");
			context.endTurn();
			// It just has to be a 3/3
			Minion winner = playMinionCard(context, opponent, "minion_mind_control_tech");
			Minion notSelected2 = playMinionCard(context, opponent, "minion_wisp");
			context.endTurn();
			playCard(context, player, "spell_test_your_might");
			assertTrue(loser.isDestroyed());
			assertFalse(notSelected1.isDestroyed());
			assertFalse(notSelected2.isDestroyed());
			assertFalse(winner.isDestroyed());
			assertEquals(winner.getAttack(), winner.getBaseAttack() + 2);
			assertEquals(winner.getMaxHp(), winner.getBaseHp() + 2);
			assertEquals(winner.getHp(), winner.getBaseHp() + 2 - loser.getAttack());
		});

		// Flip player controlling winning minion
		runGym((context, player, opponent) -> {
			Minion winner = playMinionCard(context, player, "minion_mind_control_tech");
			Minion notSelected1 = playMinionCard(context, player, "minion_wisp");
			context.endTurn();
			// It just has to be a 3/3
			Minion loser = playMinionCard(context, opponent, "token_treant");
			Minion notSelected2 = playMinionCard(context, opponent, "minion_wisp");
			context.endTurn();
			playCard(context, player, "spell_test_your_might");
			assertTrue(loser.isDestroyed());
			assertFalse(notSelected1.isDestroyed());
			assertFalse(notSelected2.isDestroyed());
			assertFalse(winner.isDestroyed());
			assertEquals(winner.getAttack(), winner.getBaseAttack() + 2);
			assertEquals(winner.getMaxHp(), winner.getBaseHp() + 2);
			assertEquals(winner.getHp(), winner.getBaseHp() + 2 - loser.getAttack());
		});
	}

	@Test
	public void testLordStormsong() {
		runGym((context, player, opponent) -> {
			Minion diedWhileNotAlive = playMinionCard(context, player, "minion_wisp");
			Minion diedWhileAlive1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			Minion diedWhileAlive2 = playMinionCard(context, opponent, "token_treant");
			context.endTurn();
			destroy(context, diedWhileNotAlive);
			Minion stormsong = playMinionCard(context, player, "minion_lord_stormsong");
			destroy(context, diedWhileAlive1);
			destroy(context, diedWhileAlive2);
			destroy(context, stormsong);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), diedWhileAlive1.getSourceCard().getCardId());
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), diedWhileAlive2.getSourceCard().getCardId());
			assertEquals(player.getMinions().size(), 1, "Should contain resurrected Bloodfen");
			assertEquals(opponent.getMinions().size(), 1, "Should contain Treant");
		});

		// Test with transformation
		runGym((context, player, opponent) -> {
			Minion diedWhileNotAlive = playMinionCard(context, player, "minion_wisp");
			Minion transformedWhileAlive = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			Minion diedWhileAlive2 = playMinionCard(context, opponent, "token_treant");
			context.endTurn();
			destroy(context, diedWhileNotAlive);
			Minion stormsong = playMinionCard(context, player, "minion_lord_stormsong");
			playCard(context, player, "spell_polymorph", transformedWhileAlive);
			transformedWhileAlive = (Minion) transformedWhileAlive.transformResolved(context);
			destroy(context, transformedWhileAlive);
			destroy(context, diedWhileAlive2);
			destroy(context, stormsong);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), transformedWhileAlive.getSourceCard().getCardId());
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), diedWhileAlive2.getSourceCard().getCardId());
			assertEquals(player.getMinions().size(), 1, "Should contain resurrected Bloodfen");
			assertEquals(opponent.getMinions().size(), 1, "Should contain Treant");
		});
	}

	@Test
	public void testKthirCorruptor() {
		runGym((context, player, opponent) -> {
			Minion kthir = playMinionCard(context, player, "minion_kthir_corruptor");
			playCard(context, player, "spell_fireball", opponent.getHero());
			assertEquals(kthir.getAttack(), kthir.getBaseAttack() + 2);
			assertEquals(kthir.getMaxHp(), kthir.getBaseHp() + 2);
			playCard(context, player, "spell_mirror_image");
			assertEquals(kthir.getAttack(), kthir.getBaseAttack() + 2);
			assertEquals(kthir.getMaxHp(), kthir.getBaseHp() + 2);
			playCard(context, player, "secret_dart_trap");
			assertEquals(kthir.getAttack(), kthir.getBaseAttack() + 4);
			assertEquals(kthir.getMaxHp(), kthir.getBaseHp() + 4);
		});
	}

	@Test
	public void testAnobii() {
		runGym((context, player, opponent) -> {
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playMinionCardWithBattlecry(context, player, "minion_anobii", bloodfenRaptor);
			Minion anobii = player.getMinions().get(1);
			bloodfenRaptor = (Minion) bloodfenRaptor.transformResolved(context);
			assertEquals(bloodfenRaptor.getSourceCard().getCardId(), "permanent_cocoon");
			destroy(context, anobii);
			bloodfenRaptor = (Minion) bloodfenRaptor.transformResolved(context);
			assertEquals(bloodfenRaptor.getSourceCard().getCardId(), "minion_bloodfen_raptor");
		});
	}

	@Test
	public void testCryptladyZara() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_boulderfist_ogre");
			playCard(context, player, "hero_cryptlady_zara");
			playCard(context, player, "spell_fireball", target);
			assertEquals(target.getHp(), target.getMaxHp() - 1);
			context.endTurn();
			playCard(context, opponent, "spell_spirit_bomb" /*4damage*/, target);
			assertEquals(target.getHp(), target.getMaxHp() - 1 - 4);
		});
	}

	@Test
	public void testColosseumBehemoth() {
		runGym((context, player, opponent) -> {
			Minion behemoth = playMinionCard(context, player, "minion_colosseum_behemoth");
			context.endTurn();
			Minion bloodfenRaptor = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			assertTrue(context.getValidActions().stream().filter(pa -> pa.getActionType() == ActionType.PHYSICAL_ATTACK).allMatch(pa -> pa.getTargetReference().equals(opponent.getHero().getReference())));
		});
	}

	@Test
	public void testEchoingPotion() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_echoing_potion");
			playCard(context, player, "minion_wisp");
			assertEquals(player.getMinions().size(), 2);
			Minion copy = player.getMinions().get(1);
			assertEquals(copy.getSourceCard().getCardId(), "minion_wisp");
			assertEquals(copy.getAttack(), 3);
			assertEquals(copy.getMaxHp(), 3);
		});
	}

	@Test
	public void testMushrooms() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion big = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			playCard(context, player, "spell_clarity_mushroom");
			player.setMana(10);
			assertTrue(context.getValidActions().stream().anyMatch(hp -> hp.getActionType() == ActionType.HERO_POWER && hp.getTargetReference().equals(big.getReference())));
			useHeroPower(context, player, big.getReference());
			assertEquals(big.getHp(), big.getMaxHp() - 4);
		}, HeroClass.TOAST, HeroClass.TOAST);

		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_hallucinogenic_mushroom");
			player.setMana(10);
			int hp = opponent.getHero().getHp();
			useHeroPower(context, player);
			assertEquals(opponent.getHero().getHp(), hp - 4);
			assertEquals(player.getHand().size(), 1);
			assertTrue(Arrays.asList("spell_clarity_mushroom", "spell_healing_mushroom", "spell_toxic_mushroom", "spell_hallucinogenic_mushroom").contains(player.getHand().get(0).getCardId()));
		}, HeroClass.TOAST, HeroClass.TOAST);

		runGym((context, player, opponent) -> {
			player.getHero().setHp(10);
			playCard(context, player, "spell_healing_mushroom");
			player.setMana(10);
			int hp = opponent.getHero().getHp();
			useHeroPower(context, player);
			assertEquals(opponent.getHero().getHp(), hp - 4);
			assertEquals(player.getHero().getHp(), 10 + 4);
		}, HeroClass.TOAST, HeroClass.TOAST);

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion big = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			playCard(context, player, "spell_toxic_mushroom");
			player.setMana(10);
			// Temporarily override the target of the cook hero power
			CardDesc clone = player.getHeroPowerZone().get(0).getDesc().clone();
			clone.setSpell(clone.getSpell().clone());
			SpellDesc damageSpell = (SpellDesc) clone.getSpell().getSpell().get(SpellArg.SPELL2);
			damageSpell.put(SpellArg.RANDOM_TARGET, false);
			damageSpell.put(SpellArg.TARGET, big.getReference());
			player.getHeroPowerZone().get(0).setDesc(clone);
			useHeroPower(context, player);
			assertTrue(big.isDestroyed());
		}, HeroClass.TOAST, HeroClass.TOAST);
	}

	@Test
	public void testNazmiriStalker() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_nazmiri_stalker");
			Minion target1 = playMinionCard(context, player, "minion_wisp");
			Minion target2 = playMinionCard(context, player, "minion_wisp");
			Minion target3 = playMinionCard(context, player, "minion_wisp");
			// Cast a +1/+2 on a target
			playCard(context, player, "spell_sound_the_bells", target2);
			assertEquals(target1.getAttack(), target1.getBaseAttack() + 1);
			assertEquals(target1.getMaxHp(), target1.getBaseHp() + 2);
			assertEquals(target2.getAttack(), target1.getBaseAttack());
			assertEquals(target2.getMaxHp(), target1.getBaseHp());
			assertEquals(target3.getAttack(), target1.getBaseAttack() + 1);
			assertEquals(target3.getMaxHp(), target1.getBaseHp() + 2);
		});
	}

	@Test
	public void testSolarPower() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_the_coin");
			playCard(context, player, "spell_solar_power");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "spell_the_coin");
			context.endTurn();
			assertEquals(player.getHand().size(), 1);
			context.endTurn();
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getHand().get(1).getCardId(), "spell_the_coin");
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHand().size(), 2);
		});
	}

	@Test
	public void testSilvermoonOperative() {
		runGym((context, player, opponent) -> {
			Card silvermoonCard = receiveCard(context, player, "minion_silvermoon_operative");
			assertEquals(silvermoonCard.getAttributeValue(Attribute.RECEIVED_ON_TURN), context.getTurn());
			Minion silvermoon = playMinionCard(context, player, silvermoonCard);
			assertEquals(silvermoon.getAttack(), silvermoon.getBaseAttack() + 2, "Did buff");
		});
	}

	@Test
	public void testSorrowstone() {
		runGym((context, player, opponent) -> {
			Minion target1 = playMinionCard(context, player, "minion_wisp");
			Minion target3 = playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "secret_sorrowstone");
			context.endTurn();
			Minion target2 = playMinionCard(context, opponent, "minion_wisp");
			destroy(context, target1);
			assertEquals(player.getSecrets().size(), 1);
			destroy(context, target2);
			assertEquals(player.getSecrets().size(), 1);
			destroy(context, target3);
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getMinions().size(), 3);
		});
	}

	@Test
	public void testCatacombCandlefin() {
		runGym((context, player, opponent) -> {
			Minion shouldNotBeSummoned1 = playMinionCard(context, player, "minion_murloc_tinyfin");
			Minion shouldBeSummoned = playMinionCard(context, player, "minion_murloc_warleader");
			Minion shouldNotBeSummoned2 = playMinionCard(context, player, "minion_bloodfen_raptor");
			destroy(context, shouldNotBeSummoned1);
			destroy(context, shouldBeSummoned);
			destroy(context, shouldNotBeSummoned2);
			playCard(context, player, "minion_catacomb_candlefin");
			assertEquals(player.getHand().get(0).getCardId(), shouldBeSummoned.getSourceCard().getCardId());
		});
	}

	@Test
	public void testCrypticRuins() {
		for (int i = 0; i < 8; i++) {
			final int j = i;
			runGym((context, player, opponent) -> {
				Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
				bloodfenRaptor.setAttribute(Attribute.SPELL_DAMAGE, j);
				AtomicInteger didDiscover = new AtomicInteger(0);
				Card spellCard = receiveCard(context, player, "spell_the_coin");
				int spellpower = context.getLogic().applySpellpower(player, spellCard, 3);
				overrideDiscover(context, player, discoverActions -> {
					assertTrue(discoverActions.size() > 0);
					assertTrue(spellpower >= j);
					int whichDiscover = didDiscover.getAndIncrement();
					for (DiscoverAction action : discoverActions) {
						switch (whichDiscover) {
							case 0:
								assertEquals(action.getCard().getBaseManaCost(), spellpower);
								break;
							case 1:
								assertEquals(action.getCard().getAttack(), spellpower);
								break;
							case 2:
								assertEquals(action.getCard().getBaseHp(), spellpower);
								break;
						}
					}
					return discoverActions.get(0);
				});

				playCard(context, player, "spell_cryptic_ruins");
				assertEquals(didDiscover.get(), 3);
			});
		}
	}

	@Test
	public void testBreathOfFire() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion damaged = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			playMinionCard(context, opponent, "minion_immune_test");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, "spell_breath_of_fire");
			assertEquals(opponent.getHero().getHp(), opponentHp - 2, "Now includes immune minions");
			assertFalse(damaged.isDestroyed());
			assertEquals(damaged.getHp(), damaged.getMaxHp() - 1);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion damaged = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			playMinionCard(context, opponent, "minion_immune_test");
			context.endTurn();
			player.setAttribute(Attribute.SPELL_DAMAGE, 1);
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, "spell_breath_of_fire");
			assertEquals(opponent.getHero().getHp(), opponentHp - 3, "Now uses spell damage");
			assertTrue(damaged.isDestroyed());
		});
	}

	@Test
	public void testCromwell() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "spell_the_coin");
			putOnTopOfDeck(context, player, "minion_deathwing");
			assertEquals(context.resolveSingleTarget(player, player, EntityReference.FRIENDLY_TOP_CARD).getSourceCard().getCardId(), "minion_deathwing");
			playCard(context, player, "minion_cromwell");
			assertEquals(context.resolveSingleTarget(player, player, EntityReference.FRIENDLY_TOP_CARD).getSourceCard().getCardId(), "spell_the_coin");
		});
	}

	@Test
	public void testLavaSoup() {
		runGym((context, player, opponent) -> {
			Card shouldNotBeRoasted1 = putOnTopOfDeck(context, player, "spell_the_coin");
			Card shouldBeRoasted1 = putOnTopOfDeck(context, player, "spell_the_coin");
			Card shouldBeRoasted2 = putOnTopOfDeck(context, player, "spell_the_coin");
			assertEquals(player.getDeck().size(), 3);
			Card cost2Card = receiveCard(context, player, "spell_cost_2_card");
			assertEquals(costOf(context, player, cost2Card), 2);
			player.setMaxMana(2);
			player.setMana(2);
			assertTrue(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			player.setMana(1);
			assertFalse(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			playCard(context, player, "spell_lava_soup");
			assertTrue(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			playCard(context, player, cost2Card);
			assertEquals(player.getDeck().size(), 1);
			assertTrue(shouldBeRoasted1.hasAttribute(Attribute.ROASTED));
			assertTrue(shouldBeRoasted2.hasAttribute(Attribute.ROASTED));
			assertFalse(shouldNotBeRoasted1.hasAttribute(Attribute.ROASTED));
		});

		runGym((context, player, opponent) -> {
			Card shouldNotBeRoasted1 = putOnTopOfDeck(context, player, "spell_the_coin");
			assertEquals(player.getDeck().size(), 1);
			Card cost2Card = receiveCard(context, player, "spell_cost_2_card");
			assertEquals(costOf(context, player, cost2Card), 2);
			player.setMaxMana(2);
			player.setMana(2);
			assertTrue(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			player.setMana(1);
			assertFalse(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			playCard(context, player, "spell_lava_soup");
			assertFalse(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			assertEquals(player.getDeck().size(), 1);
			assertFalse(shouldNotBeRoasted1.hasAttribute(Attribute.ROASTED));
		});
	}

	@Test
	public void testDeathwingsDinner() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_wisp");
			playMinionCard(context, player, "minion_wisp");
			Minion target = playMinionCard(context, player, "minion_boulderfist_ogre");
			Card shouldNotBeRoasted1 = putOnTopOfDeck(context, player, "spell_the_coin");
			Card shouldBeRoasted1 = putOnTopOfDeck(context, player, "spell_the_coin");
			Card shouldBeRoasted2 = putOnTopOfDeck(context, player, "spell_the_coin");
			playCard(context, player, "spell_deathwing_s_dinner");
			assertEquals(player.getDeck().size(), 1);
			assertTrue(shouldBeRoasted1.hasAttribute(Attribute.ROASTED));
			assertTrue(shouldBeRoasted2.hasAttribute(Attribute.ROASTED));
			assertFalse(shouldNotBeRoasted1.hasAttribute(Attribute.ROASTED));
			playCard(context, player, "spell_fireball", target);
			assertTrue(target.isDestroyed());
			assertTrue(shouldBeRoasted1.hasAttribute(Attribute.ROASTED));
			assertTrue(shouldBeRoasted2.hasAttribute(Attribute.ROASTED));
			assertFalse(shouldNotBeRoasted1.hasAttribute(Attribute.ROASTED));
		});
	}

	@Test
	public void testChiliDragonbreath() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_chili_dragonbreath");
			Minion minion = playMinionCard(context, player, "minion_blackwing_technician");
			assertEquals(minion.getAttack(), minion.getBaseAttack() + 1);
			assertEquals(minion.getMaxHp(), minion.getBaseHp() + 1);
		});
	}

	@Test
	public void testButcher() {
		// Destroy friendly, should get butcher in the same place. Should work with full board
		runGym((context, player, opponent) -> {
			Minion wisp0 = playMinionCard(context, player, "minion_wisp");
			Minion wisp1 = playMinionCard(context, player, "minion_wisp");
			Minion wisp2 = playMinionCard(context, player, "minion_wisp");
			playMinionCard(context, player, "minion_wisp");
			playMinionCard(context, player, "minion_wisp");
			playMinionCard(context, player, "minion_wisp");
			playMinionCard(context, player, "minion_wisp");
			assertEquals(wisp1.getEntityLocation().getIndex(), 1);
			playCard(context, player, "spell_butcher", wisp1);
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_pile_of_meat");
		});

		// Destroy enemy
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion wisp0 = playMinionCard(context, opponent, "minion_wisp");
			Minion wisp1 = playMinionCard(context, opponent, "minion_wisp");
			Minion wisp2 = playMinionCard(context, opponent, "minion_wisp");
			playMinionCard(context, opponent, "minion_wisp");
			playMinionCard(context, opponent, "minion_wisp");
			playMinionCard(context, opponent, "minion_wisp");
			playMinionCard(context, opponent, "minion_wisp");
			context.endTurn();
			assertEquals(wisp1.getEntityLocation().getIndex(), 1);
			playCard(context, player, "spell_butcher", wisp1);
			assertEquals(opponent.getMinions().get(1).getSourceCard().getCardId(), "token_pile_of_meat");
		});
	}

	@Test
	public void testFogburner() {
		runGym((context, player, opponent) -> {
			Minion fogburner = playMinionCard(context, player, "minion_fogburner");
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "spell_volcanic_potion");
			assertEquals(fogburner.getAttack(), fogburner.getBaseAttack() + 2, "+2 from two indirect damages");
			assertEquals(fogburner.getMaxHp(), fogburner.getBaseHp() + 2, "+2 from two indirect damages");
		});

		runGym((context, player, opponent) -> {
			Minion fogburner = playMinionCard(context, player, "minion_fogburner");
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "spell_fireball", wisp);
			assertEquals(fogburner.getAttack(), fogburner.getBaseAttack());
			assertEquals(fogburner.getMaxHp(), fogburner.getBaseHp());
		});
	}

	@Test
	public void testBananamancer() {
		runGym((context, player, opponent) -> {
			// Giving a hero bonus armor with a spell played from hand
			playMinionCard(context, player, "minion_bananamancer");
			playCard(context, player, "spell_gnash");
			assertEquals(player.getHero().getAttack(), 4, "3 + 1 spell damage");
			assertEquals(player.getHero().getArmor(), 4, "3 + 1 spell damage");
		});

		runGym((context, player, opponent) -> {
			// Giving a minion a buff from a spell should buff it, from a subsequent battlecry should not
			playMinionCard(context, player, "minion_bananamancer");
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "spell_mark_of_the_lotus");
			assertEquals(wisp.getAttack(), wisp.getBaseAttack() + 2, "1 + 1 spell damage");
			assertEquals(wisp.getHp(), wisp.getBaseHp() + 2, "1 + 1 spell damage");
			playMinionCardWithBattlecry(context, player, "minion_fallen_sun_cleric", wisp);
			assertEquals(wisp.getAttack(), wisp.getBaseAttack() + 3, "1 + 1 spell damage + 1 Sun Cleric buff");
			assertEquals(wisp.getHp(), wisp.getBaseHp() + 3, "1 + 1 spell damage + 1 Sun Cleric buff");
		});

		runGym((context, player, opponent) -> {
			// Give your hero 2x the mana spent in armor + 1 spell damage = 21 armor
			playMinionCard(context, player, "minion_bananamancer");
			player.setMaxMana(10);
			player.setMana(10);
			playCard(context, player, "spell_forbidden_armor");
			assertEquals(player.getHero().getArmor(), 21, "2x the mana spent in armor + 1 spell damage = 21 armor");
		});
	}

	@Test
	public void testFlamewarper() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_flamewarper");
			int hp = opponent.getHero().getHp();
			playCard(context, player, "spell_fireball", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 12);
			playCard(context, player, "spell_fireball", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 18);
		});
	}

	@Test
	public void testWyrmrestAspirant() {
		runGym((context, player, opponent) -> {
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_aspirant");
			int TEMPORARY_ATTACK_BONUS = 2;
			playMinionCardWithBattlecry(context, player, "minion_abusive_sergeant", wyrmrest);
			assertEquals(wyrmrest.getAttack(), wyrmrest.getBaseAttack() + 2 * TEMPORARY_ATTACK_BONUS);
			int ATTACK_BONUS = 4;
			playCard(context, player, "spell_blessing_of_kings", wyrmrest);
			assertEquals(wyrmrest.getAttack(), wyrmrest.getBaseAttack() + 2 * (TEMPORARY_ATTACK_BONUS + ATTACK_BONUS));
			playCard(context, player, "spell_blessed_champion", wyrmrest);
			// current attack: 4 base + 2 temporary + 4 attack bonus
			// doubling from blessed champion: 4 base + 2 temporary + 4 attack bonuses + 2 temporary + 4 attack bonuses + 4 base
			// to bonuses: 4 base + 16 bonuses
			// doubling bonuses: 4 base + 32 bonuses
			assertEquals(wyrmrest.getAttack(), 36);
			context.endTurn();
			// doubling from blessed champion: 4 base + 4 attack bonuses + 4 attack bonuses + 4 base
			// to bonuses: 4 base + 12 bonuses
			// doubling of bonuses: 4 base + 24 bonuses
			assertEquals(wyrmrest.getAttack(), 28);
		});
	}

	@Test
	public void testBlackflameRitual() {
		for (int i = 0; i < 7; i++) {
			final int count = i;
			runGym((context, player, opponent) -> {
				List<Minion> minions = new ArrayList<>();
				for (int j = 0; j < count; j++) {
					minions.add(playMinionCard(context, player, "minion_wisp"));
				}
				for (Minion minion : minions) {
					destroy(context, minion);
				}
				player.setMana(10);
				playCard(context, player, "spell_blackflame_ritual");
				assertEquals(player.getMana(), 10 - count);
				if (count == 0) {
					assertEquals(player.getMinions().size(), 0);
				} else {
					assertEquals(player.getMinions().size(), 2);
					for (int k = 0; k < 2; k++) {
						assertEquals(player.getMinions().get(k).getAttack(), count);
						assertEquals(player.getMinions().get(k).getHp(), count);
					}
				}
			});
		}
	}

	@Test
	public void testBlackflameRitualMadProphecyInteraction() {
		// Interaction with Mad Prophet Rosea should cast a 2x 10/10 minions
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_mad_prophet_rosea");
			player.setMaxMana(10);
			player.setMana(10);
			Card blackflameRitual = receiveCard(context, player, "spell_blackflame_ritual");
			playCard(context, player, blackflameRitual);
			for (int i = 2; i < 4; i++) {
				Minion token = player.getMinions().get(i);
				assertEquals(token.getAttack(), 10);
				assertEquals(token.getHp(), 10);
			}
		});
	}

	@Test
	public void testArcaneTyrantInvokeInteraction() {
		runGym((context, player, opponent) -> {
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			player.setMana(10);
			player.setMaxMana(10);
			// Petrifying Gaze is a cost 3 with an invoke of 9
			playCard(context, player, "spell_petrifying_gaze", bloodfen);
			Card arcaneTyrant = receiveCard(context, player, "minion_arcane_tyrant");
			assertEquals(costOf(context, player, arcaneTyrant), 0, "Petrifying Gaze should have been played as a Cost-9 card.");
		});
	}

	@Test
	public void testElaborateScheme() {
		runGym((context, player, opponent) -> {
			String[] cardIds = {"secret_cat_trick", "secret_dart_trap", "secret_explosive_runes"};
			Stream.of(cardIds).forEach(cardId -> shuffleToDeck(context, player, cardId));
			playCard(context, player, "secret_elaborate_scheme");
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			assertEquals(player.getHand().size(), 2, "Should draw a card at start of the turn and due to Elaborate Scheme");
			assertEquals(player.getSecrets().size(), 1, "Should have triggered Elaborate Scheme and put a secret into play.");
			Set<String> secretsInHand = player.getHand().stream().map(Card::getCardId).collect(Collectors.toSet());
			assertFalse(secretsInHand.contains(player.getSecrets().get(0).getSourceCard().getCardId()));
			Set<String> remainingSecret = new HashSet<>(Arrays.asList(cardIds));
			remainingSecret.removeAll(secretsInHand);
			assertTrue(remainingSecret.contains(player.getSecrets().get(0).getSourceCard().getCardId()));
			assertEquals(remainingSecret.size(), 1);
		});
	}

	@Test
	public void testHeartstopAura() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_wisp");
			Minion target2 = playMinionCard(context, opponent, "minion_wisp");
			context.endTurn();
			Minion attacker = playMinionCard(context, player, "minion_charge_test");
			Minion defender = playMinionCard(context, player, "minion_wisp");
			Minion doubleDefender = playMinionCard(context, player, "minion_boulderfist_ogre");
			playCard(context, player, "spell_heartstop_aura");
			attack(context, player, attacker, target1);
			assertFalse(attacker.isDestroyed());
			assertTrue(target1.isDestroyed());
			context.endTurn();
			Minion opponentAttacker = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, opponent, opponentAttacker, defender);
			assertTrue(opponentAttacker.isDestroyed());
			assertFalse(defender.isDestroyed());
			// Deal 6 damage to 7 hp boulderfist
			playCard(context, opponent, "spell_fireball", doubleDefender);
			assertEquals(doubleDefender.getHp(), 1);
			context.endTurn();
			assertTrue(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertFalse(doubleDefender.isDestroyed(), "Boulderfist Ogre should not have taken enough damage to be killed.");
			attacker = playMinionCard(context, player, "minion_charge_test");
			attack(context, player, attacker, target2);
			assertTrue(attacker.isDestroyed(), "Hearstopped enchantment should have expired.");
		});

		runGym((context, player, opponent) -> {
			// TODO: Test playing Heartstopped two turns in a row
		});
	}

	@Test
	public void testFissure() {
		runGym((context, player, opponent) -> {
			Minion threeTwo = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion oneOneBuffed = playMinionCard(context, opponent, "minion_snowflipper_penguin");
			playCard(context, player, "spell_nightmare", oneOneBuffed);
			playCard(context, player, "spell_fissure");
			assertFalse(oneOneBuffed.isDestroyed());
			assertTrue(threeTwo.isDestroyed());
		});
	}

	@Test
	public void testHeavyDutyDragoons() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_heavy_duty_dragoon");
			playMinionCard(context, player, "minion_heavy_duty_dragoon");
			context.performAction(player.getId(), player.getHeroPowerZone().get(0).play().withTargetReference(player.getHero().getReference()));
			context.getLogic().endOfSequence();
			assertEquals(player.getHero().getAttack(), 3);
		}, HeroClass.RUST, HeroClass.RUST);
	}

	@Test
	public void testHeavyDutyDragoonChenStormstoutInteraction() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_heavy_duty_dragoon");
			useHeroPower(context, player, target.getReference());
			assertEquals(player.getHero().getAttack(), 2);
		}, HeroClass.JADE, HeroClass.JADE);
	}

	@Test
	public void testLadyDeathwhisper() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			for (int i = 0; i < 5; i++) {
				Minion penguin = playMinionCard(context, opponent, "minion_snowflipper_penguin");
				for (int j = 0; j < i; j++) {
					playCard(context, opponent, "spell_bananas", penguin);
				}
				assertEquals(penguin.getHp(), 1 + i);
			}
			context.endTurn();
			playMinionCard(context, player, "minion_lady_deathwhisper");
			assertTrue(opponent.getMinions().stream().allMatch(m -> m.getHp() == 1));
		});
	}

	@Test
	public void testGrimestreetVigilante() {
		runGym((context, player, opponent) -> {
			Minion vigilante = playMinionCard(context, player, "minion_grimestreet_vigilante");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_snowflipper_penguin");
			context.endTurn();
			Card bloodfen = receiveCard(context, player, "minion_bloodfen_raptor");
			attack(context, player, vigilante, target);
			assertEquals(bloodfen.getAttributeValue(Attribute.ATTACK_BONUS), 4);
			Minion bloodfenMinion = playMinionCard(context, player, bloodfen);
			assertEquals(bloodfenMinion.getAttack(), bloodfenMinion.getBaseAttack() + 4);
		});
	}

	@Test
	public void testColdsteelBlade() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_coldsteel");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_snowflipper_penguin");
			context.endTurn();
			attack(context, player, player.getHero(), target);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_44dragon");
		});
	}

	@Test
	public void testMenacingDragotron() {
		runGym((context, player, opponent) -> {
			Minion toDestroy = playMinionCard(context, player, "minion_menacing_dragotron");
			Minion shouldBeDestroyed = playMinionCard(context, player, "minion_snowflipper_penguin");
			Minion shouldNotBeDestroyed = playMinionCard(context, player, "minion_bloodfen_raptor");
			destroy(context, toDestroy);
			assertTrue(shouldBeDestroyed.isDestroyed());
			assertFalse(shouldNotBeDestroyed.isDestroyed());
		});
	}

	@Test
	public void testBloodPresence() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(27);
			Minion charger = playMinionCard(context, player, "minion_charge_test");
			SpellDesc spell = new SpellDesc(ChangeHeroPowerSpell.class);
			spell.put(SpellArg.CARD, "hero_power_blood_presence");
			context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, false);
			// Make sure aura actually gets recalculated
			context.getLogic().endOfSequence();
			assertEquals(player.getHero().getHeroPower().getCardId(), "hero_power_blood_presence");
			attack(context, player, charger, opponent.getHero());
			assertEquals(player.getHero().getHp(), 30);
			spell.put(SpellArg.CARD, "hero_power_fireblast");
			context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, false);
			// Make sure aura actually gets recalculated
			context.getLogic().endOfSequence();
			assertFalse(charger.hasAttribute(Attribute.AURA_LIFESTEAL));
			player.getHero().setHp(27);
			attack(context, player, charger, opponent.getHero());
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
			player.setMana(6);
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
			playCard(context, player, spellRebelliousFlame, opponent.getHero());
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
			playCard(context, opponent, "spell_fireball", player.getHero());
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_nightmare_tentacle");
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			playCard(context, opponent, "minion_eater_of_secrets");
			assertEquals(player.getSecrets().size(), 1);
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
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, opponent, charger, player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "secret_panicked_summoning");
			context.endTurn();
			attack(context, opponent, charger, player.getHero());
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
			context.setDeckFormat(new DeckFormat().withCardSets(CardSet.BASIC, CardSet.CLASSIC));
			playCard(context, player, "spell_signs_of_the_end");
			assertEquals(player.getMinions().size(), 0);
			playCard(context, player, "spell_the_coin");
			assertEquals(player.getMinions().get(0).getSourceCard().getBaseManaCost(), 0);
		});

		runGym((context, player, opponent) -> {
			context.setDeckFormat(new DeckFormat().withCardSets(CardSet.BASIC, CardSet.CLASSIC));
			playCard(context, player, "spell_signs_of_the_end");
			player.setMana(7);
			playCard(context, player, "spell_earthquake");
			assertTrue(player.getMinions().stream().anyMatch(m -> m.getSourceCard().getBaseManaCost() == 7));
		});
	}

	@Test
	public void testSouldrinkerDrake() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_souldrinker_drake");
			Card fireball = receiveCard(context, player, "spell_fireball");
			Card fireball2 = receiveCard(context, player, "spell_fireball");
			player.getHero().setHp(1);
			playCard(context, player, fireball, opponent.getHero());
			assertEquals(player.getHero().getHp(), 1 + 6);
			playCard(context, player, fireball2, opponent.getHero());
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
			playCard(context, player, "spell_path_of_frost", attacker);
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
			Minion attacker = playMinionCard(context, player, "minion_charge_test");
			playCard(context, player, "spell_path_of_frost", attacker);
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
			Minion noggenfogger = playMinionCard(context, player, "minion_mayor_noggenfogger");
			Minion paradox = playMinionCard(context, player, "minion_paradox");
			assertTrue(paradox.isInPlay());
			playCard(context, player, "spell_assassinate", paradox);
			assertTrue(paradox.isDestroyed());
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
			receiveCard(context, opponent, "minion_bloodfen_raptor");
			context.getLogic().endOfSequence();
			assertEquals(knight.getAttack(), knight.getBaseAttack() + 1, "Opponent card should not buff mini knight");
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
			assertEquals(player.getMinions().size(), 3);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_deathwhelp");
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "token_deathwhelp");
			assertFalse(player.getMinions().get(0).hasAttribute(Attribute.CHARGE));
			assertFalse(player.getMinions().get(2).hasAttribute(Attribute.CHARGE));
		});

		runGym((context, player, opponent) -> {
			player.setMana(3);
			playMinionCard(context, player, "minion_energetic_mentee");
			assertEquals(player.getMinions().size(), 3);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_deathwhelp");
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "token_deathwhelp");
			assertTrue(player.getMinions().get(0).hasAttribute(Attribute.CHARGE));
			assertTrue(player.getMinions().get(2).hasAttribute(Attribute.CHARGE));
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
			playCard(context, player, card, target);
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
			playCard(context, player, card, target);
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
			playCard(context, player, card, target);
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
			context.performAction(player.getId(), player.getHero().getHeroPower().play());
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
				playCard(context, player, "spell_razorpetal", opponent.getHero());
			}
			assertEquals(player.getMana(), 0);
			playCard(context, player, "spell_misty_mana_tea");
			assertEquals(player.getMana(), 4);
		});

		// Test basic
		runGym((context, player, opponent) -> {
			player.setMana(7);
			for (int i = 0; i < 7; i++) {
				playCard(context, player, "spell_razorpetal", opponent.getHero());
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
			Minion attacker = playMinionCard(context, player, "minion_charge_test");
			playCard(context, player, "spell_touch_of_karma", attacker);
			int hp = opponent.getHero().getHp();
			attack(context, player, attacker, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - attacker.getAttack() - 2);
		});

		// Target opponent
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_charge_test");
			context.endTurn();
			playCard(context, player, "spell_touch_of_karma", attacker);
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
			playCard(context, player, "spell_keg_smash", target2);
			playCard(context, player, "spell_razorpetal", target3);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
			playCard(context, player, "spell_razorpetal", target1);
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
			playCard(context, player, "spell_keg_smash", target2);
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
			playCard(context, player, "spell_keg_smash", target2);
			playCard(context, player, "weapon_wicked_knife");

			attack(context, player, player.getHero(), target3);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
			playCard(context, player, "spell_razorpetal", target1);
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
			playCard(context, player, "spell_keg_smash", target2);
			playCard(context, player, "spell_razorpetal", target3);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
			playCard(context, player, "spell_razorpetal", target1);
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
			playCard(context, player, "spell_keg_smash", target2);
			playCard(context, player, "spell_razorpetal", target3);
			assertTrue(target3.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
			playCard(context, player, "spell_razorpetal", target1);
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
			playCard(context, player, "spell_enveloping_mists", twoHp);
			assertEquals(player.getHero().getHp(), 4);
		});
	}

	@Test
	public void testDampenHarm() {
		runGym((context, player, opponent) -> {
			// Your minions can only take 1 damage at a time until the start of your next turn.
			Minion target = playMinionCard(context, player, "minion_sleepy_dragon");
			playCard(context, player, "spell_dampen_harm");
			playCard(context, player, "spell_fireball", target);
			assertEquals(target.getHp(), target.getBaseHp() - 1);
			playCard(context, player, "spell_fireball", target);
			assertEquals(target.getHp(), target.getBaseHp() - 2);
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_charge_test");
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
			assertEquals(desolationOfKaresh.getAttributeValue(Attribute.RESERVED_INTEGER_1), 4);
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
			playCard(context, player, "spell_sap", bloodfen);
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
			playCard(context, player, "spell_instant_evolution", target);
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
	@Ignore("too many changes to test")
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
			context.performAction(player.getId(), player.getHeroPowerZone().get(0).play());
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
			context.performAction(opponent.getId(), opponent.getHeroPowerZone().get(0).play());
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
			playCard(context, player, "spell_you_from_the_future", target);
			Minion target2 = player.getMinions().get(1);
			assertTrue(target.isWounded());
			assertTrue(target2.isWounded());
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
			assertTrue(card1.hasAttribute(Attribute.DISCARDED));
			assertTrue(card2.hasAttribute(Attribute.DISCARDED));
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
			assertTrue(damaged.isWounded());
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "hero_power_heal", damaged);
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
			assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
			playCard(context, player, "spell_silence", copyCard);
			assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
		});

		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_argent_Squire");
			playCard(context, player, "spell_silence", copyCard);
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
		});

		// Test does not copy non-text attributes (buffs or whatever)
		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_argent_Squire");
			playCard(context, player, "spell_windfury", copyCard);
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			Assert.assertFalse(onBoardBefore.hasAttribute(Attribute.WINDFURY));
		});
	}

	@Test
	public void testTheEndTime() {
		runGym((context, player, opponent) -> {
			Minion endTime = playMinionCard(context, player, "permanent_the_end_time");
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 40);
			context.endTurn();
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 39);
			context.endTurn();
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 38);
			context.endTurn();
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 35, "The infinite wardens in play reduce this by 2 each.");
		});

		runGym((context, player, opponent) -> {
			Minion endTime = playMinionCard(context, player, "permanent_the_end_time");
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 40);
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
			assertTrue(spaceMoorine.hasAttribute(Attribute.AURA_TAUNT));
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			assertTrue(context.getValidActions().stream().filter(va -> va.getActionType() == ActionType.PHYSICAL_ATTACK)
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
			playCard(context, opponent, "spell_razorpetal", bloodfen);
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
				playCard(context, opponent, "spell_razorpetal", armageddon1);
			}
			assertTrue(armageddon1.isDestroyed());
			assertTrue(armageddon2.isDestroyed());
		});

		runGym((context, player, opponent) -> {
			final Minion armageddon1 = playMinionCard(context, player, "minion_armageddon_vanguard");
			Minion target = playMinionCard(context, player, "minion_snowflipper_penguin");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			playCard(context, opponent, "spell_razorpetal", target);
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
			playCard(context, player, "spell_pyroblast", player.getHero());
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
		// No combos played, should die player's next turn
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			context.endTurn();
			assertFalse(desolation.isDestroyed());
			context.endTurn();
			assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, should die in 4 turns
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			context.endTurn();
			context.endTurn();
			Assert.assertFalse(desolation.isDestroyed());
			context.endTurn();
			assertTrue(desolation.isDestroyed());
		});

		// Not combo card played, should die player's next turn
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			assertFalse(desolation.isDestroyed());
			context.endTurn();
			assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, then not activated combo card played. Should die in 4 turns.
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			Assert.assertFalse(desolation.isDestroyed());
			context.endTurn();
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			context.endTurn();
			assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, then not activated combo card played, then activated combo card played. Should die in 6 turns.
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
			context.endTurn();
			assertTrue(desolation.isDestroyed());
		});
	}

	@Test
	public void testShadowOfThePast() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "minion_charge_test");
			playCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Minion shadow = playMinionCard(context, player, "minion_shadow_of_the_past");
			playCard(context, player, "minion_boulderfist_ogre");
			context.endTurn();
			playCard(context, opponent, "spell_fireball", shadow);
			assertEquals(player.getHand().get(0).getCardId(), "spell_fireball");
		});
	}

	@Test
	public void testOwnWorstEnemey() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_own_worst_enemy");
			Minion target = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			Minion source = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, opponent, source, target);
			assertTrue(source.isDestroyed());
			Assert.assertFalse(target.isDestroyed());
			assertTrue(player.getGraveyard().stream().anyMatch(c -> c.getEntityType() == EntityType.MINION
					&& c.getSourceCard().getCardId().equals("minion_charge_test")));
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
			playCard(context, player, "spell_fireball", target);
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
			playCard(context, player, "spell_razorpetal", target);
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_razorpetal", target);
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
			assertTrue(bigGameHunt.isDestroyed());
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
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, opponent, charger, player.getHero());

			assertEquals(player.getWeaponZone().get(0).getDurability(),
					CardCatalogue.getCardById("weapon_eaglehorn_bow").getBaseDurability() - 1,
					"Eaglehorn Bow loses durability because the secret triggered before it was in play.");
			assertTrue(charger.isDestroyed());
		});
	}

	@Test
	public void testFifiFizzlewarpPermanentsInteraction() {
		// Should not choose a permanent to write on a card
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("permanent_test"));
			putOnTopOfDeck(context, player, "minion_boulderfist_ogre");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			context.getLogic().drawCard(player.getId(), player);
			context.getLogic().discardCard(player, fifi);

			Card shouldNotBePermanent = player.getHand().get(0);
			assertFalse(shouldNotBePermanent.hasAttribute(Attribute.PERMANENT));
		});
	}

	@Test
	public void testFifiFizzlewarpShouldTriggerWithSpells() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("minion_loot_hoarder"));
			putOnTopOfDeck(context, player, "spell_mirror_image");
			putOnTopOfDeck(context, player, "minion_boulderfist_ogre");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			context.getLogic().discardCard(player, fifi);
			context.getLogic().drawCard(player.getId(), player);
			Card shouldHaveLootHoarder = player.getHand().get(0);
			assertEquals(shouldHaveLootHoarder.getCardId(), "minion_loot_hoarder");
		});
	}

	@Test
	public void testFifiFizzlewarpUnlicensedApothecaryInteraction() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_boulderfist_ogre");
			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_unlicensed_apothecary");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();
			context.getLogic().drawCard(player.getId(), player);
			context.getLogic().discardCard(player, fifi);
			Card unlicensedApothecaryText = player.getHand().get(0);
			assertEquals(unlicensedApothecaryText.getCardId(), "minion_unlicensed_apothecary");
			Card boulderfist = CardCatalogue.getCardById("minion_boulderfist_ogre");
			assertEquals(unlicensedApothecaryText.getAttack(), boulderfist.getBaseAttack());
			assertEquals(unlicensedApothecaryText.getHp(), boulderfist.getBaseHp());
			int triggersBefore = context.getTriggerManager().getTriggers().size();
			playCard(context, player, unlicensedApothecaryText);
			assertEquals(context.getTriggerManager().getTriggers().size(), triggersBefore + 1, "Should have put Unlicensed Apothecary text into play");
			int playerHp = player.getHero().getHp();
			playCard(context, player, "minion_wisp");
			assertEquals(player.getHero().getHp(), playerHp - 5, "Should have triggered Unlicensed Apothecary text");
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
			assertTrue(argentSquire.hasAttribute(Attribute.DIVINE_SHIELD));
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
			playCard(context, player, "spell_fireball", tirion);
			playCard(context, player, "spell_fireball", tirion);
			assertTrue(tirion.isDestroyed());
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

			assertTrue(player.getHand().stream().allMatch(c -> c.getCardId().equals("minion_bloodfen_raptor")));
			assertTrue(player.getDeck().stream().allMatch(c -> c.getCardId().equals("spell_mirror_image")));
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

			assertTrue(player.getHand().stream().allMatch(c -> c.getCardId().equals("minion_bloodfen_raptor")));
			assertTrue(player.getDeck().stream().allMatch(c -> c.getCardId().equals("spell_mirror_image")));
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
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			context.endTurn();
			playCard(context, player, "spell_vampiric_touch", charger);
			assertTrue(charger.isDestroyed());
			assertEquals(player.getMinions().get(0).getAttack(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_charge_test");
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion mindControlTech = playMinionCard(context, opponent, "minion_mind_control_tech");
			context.endTurn();
			playCard(context, player, "spell_vampiric_touch", mindControlTech);
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
			playCard(context, opponent, "spell_fireball", player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getHero().getHp(), 11, "Should have healed for 6");
			assertEquals(lightwarden.getAttack(), lightwarden.getBaseAttack() + 2, "Lightwarden should have buffed");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_divine_intervention");
			Minion lightwarden = playMinionCard(context, player, "minion_lightwarden");
			player.getHero().setHp(7);
			context.endTurn();
			playCard(context, opponent, "spell_fireball", player.getHero());
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
			playCard(context, player, "spell_fireball", opponent.getHero());
			assertEquals(player.getMana(), 0);
			player.setMana(4);
			playCard(context, player, "spell_fireball", yrel);
			assertEquals(player.getMana(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion yrel = playMinionCard(context, player, "minion_yrel");
			player.setMaxMana(5);
			player.setMana(5);
			playCard(context, player, "spell_power_word_tentacles", yrel);
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
				assertTrue(card.isSecret());
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
			playCard(context, player, "spell_razorpetal", opponent.getHero());
			Assert.assertFalse(opponent.getHero().isDestroyed());
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_black_lotus");
			context.endTurn();
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "spell_razorpetal", bloodfen);
			assertTrue(bloodfen.isDestroyed());
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
			playCard(context, player, "spell_frost_bomb", target);
			assertTrue(target.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			assertTrue(target.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			Assert.assertFalse(target.hasAttribute(Attribute.FROZEN));
			assertTrue(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion other = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Minion friendly = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_frost_bomb", target);
			assertTrue(target.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			playCard(context, opponent, "spell_fireball", target);
			assertTrue(target.isDestroyed());
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
			assertTrue(hound.isDestroyed());
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
			playCard(context, player, "spell_fireball", fleetfooted);
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
			playCard(context, player, "spell_fireball", target);
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getDeck().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_master_sorcerer");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_fireball", target);
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
			playCard(context, player, "spell_temporal_flux", opponent.getHero());
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
			assertTrue(villager.isDestroyed());
			assertTrue(bloodfen.isDestroyed(), "Two damage should have been dealt in this sequence.");
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
			assertTrue(minion1.hasAttribute(Attribute.FROZEN));
			assertTrue(minion2.hasAttribute(Attribute.FROZEN));
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
			playCard(context, player, fireball, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 8);
			fireball = receiveCard(context, player, "spell_fireball");
			assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost(), "The 2nd spell should not be more expensive");
			opponentHp = opponent.getHero().getHp();
			playCard(context, player, fireball, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 6, "The 2nd spell should not have gotten spell damage +2.");
			opponentHp = opponent.getHero().getHp();
			playCard(context, player, fireball, opponent.getHero());
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
			playCard(context, player, "spell_fireball", opponent.getHero());
			assertEquals(chillwind.getHp(), chillwind.getBaseHp() - 3);
			playCard(context, player, "spell_fireball", opponent.getHero());
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
			assertTrue(player.getHand().stream().allMatch(c -> costOf(context, player, c) == 1));
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
			assertTrue(player.getHand().containsCard("spell_inner_rage"));
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
			playCard(context, player, "spell_pulse_bomb", bloodfen);
			assertTrue(bloodfen.isDestroyed());
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
			playCard(context, player, "spell_pulse_bomb", bloodfen);
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
			playCard(context, player, "spell_fireball", blinkDog);
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
			assertTrue(player.getHand().stream().allMatch(c -> costOf(context, player, c) == 1));
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
			playCard(context, player, "spell_forever_a_student", bloodfen);
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
			playCard(context, player, "spell_mystic_skull", bloodfenRaptor);
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
			assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
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
			assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
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
			playCard(context, player, "spell_mind_control", opponentMinion);
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
			playCard(context, player, "spell_ancestral_healing", dancemistress);
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
			playCard(context, player, "spell_ancestral_healing", bloodfenRaptor);
			Assert.assertFalse(player.getMinions().stream().anyMatch(m -> m.getSourceCard().getCardId().equals("minion_crazed_dancer")));
		});
	}

	@Test
	public void testSpikeToedBooterang() {
		// Attacks a opponent's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, opponent, "minion_river_crocolisk");
			context.endTurn();
			playCard(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			assertEquals(opponent.getMinions().get(0).getHp(), 1);
		});

		// Attacks player's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, player, "minion_river_crocolisk");
			playCard(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			assertEquals(player.getMinions().get(0).getHp(), 1);
		});

		// Defeats a Divine Shield
		runGym((context, player, opponent) -> {
			Minion silvermoonGuardian = playMinionCard(context, opponent, "minion_silvermoon_guardian");
			context.endTurn();
			playCard(context, player, "spell_spike_toed_booterang", silvermoonGuardian);
			assertEquals(opponent.getMinions().get(0).getHp(), 2);
		});

		// If attacking Imp Gang Boss, summons two 1/1 Imps for opponent
		runGym((context, player, opponent) -> {
			Minion impGangBoss = playMinionCard(context, opponent, "minion_imp_gang_boss");
			context.endTurn();
			playCard(context, player, "spell_spike_toed_booterang", impGangBoss);
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
			playCard(context, player, player.getHand().get(0), opponent.getHero());
			assertEquals(player.getHand().size(), 0);
		}));
	}

	@Test
	public void testTriplicate() {
		runGym((context, player, opponent) -> {
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "spell_triplicate", wisp);
			assertTrue(player.getMinions().size() > 1);
			assertTrue(player.getHand().size() > 0);
			assertTrue(player.getDeck().size() > 0);
		});
	}

	@Test
	public void testPolyDragon() {
		runGym((context, player, opponent) -> {
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "spell_polymorph_dragon", wisp);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_whelp");
			playCard(context, player, player.getHand().peekFirst(), player.getMinions().get(0));
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
			assertTrue(minion.isDestroyed());
			assertEquals(opponent.getHero().getHp(), opponentHp, "Opponent's HP should not have changed.");
			assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
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
			assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
		});
	}

	@Test
	public void testAegwynn() {
		Map<String, Integer> spellMap = new HashMap<>();
		spellMap.put("spell_fireball", 0);
		spellMap.put("spell_arcane_explosion", 1);
		spellMap.put("spell_flamestrike", 1);
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
			playCard(context, player, "spell_immolate", watcher);

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
			context.performAction(player.getId(), player.getHeroPowerZone().get(0).play());
			assertEquals(player.getHero().getArmor(), 13);
			assertEquals(player.getMana(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_commander_garrosh");
			playCard(context, player, "minion_raza_the_chained");
			player.setMana(10);
			context.performAction(player.getId(), player.getHeroPowerZone().get(0).play());
			assertEquals(player.getHero().getArmor(), 14);
			assertEquals(player.getMana(), 0);
		});
	}

	@Test
	public void testHagaraTheStormbinder() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_hagara_the_stormbinder");
			shuffleToDeck(context, player, "minion_neutral_test");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			assertEquals(context.getTriggersAssociatedWith(player.getReference()).size(), 0, "Should not have activated");
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_hagara_the_stormbinder");
			shuffleToDeck(context, player, "minion_silver_test");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			assertEquals(context.getTriggersAssociatedWith(player.getReference()).size(), 1, "Should have activated");
		});

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
				playCard(context, player, "spell_pyroblast", opponent.getHero());
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

			assertFalse(player.isDestroyed());
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
	public void testScaleOfTheEarthWarder() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scale_of_the_earth_warder");
			assertEquals(player.getHero().getWeapon().getDurability(), 12);
			playCard(context, opponent, "spell_fireball", player.getHero());
			assertEquals(player.getHero().getHp(), 30);
			assertEquals(player.getHero().getWeapon().getDurability(), 6);
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			playCard(context, opponent, "spell_pyroblast", player.getHero());
			assertEquals(player.getHero().getHp(), 30);
			assertNull(player.getHero().getWeapon());
			assertTrue(wisp.isDestroyed());
		});
	}

	@Test
	public void testXalatath() {
		runGym((context, player, opponent) -> {
			assertEquals(costOf(context, player, player.getHeroPowerZone().get(0)), 2);
			Minion maiden = playMinionCard(context, player, "minion_maiden_of_the_lake");
			assertEquals(costOf(context, player, player.getHeroPowerZone().get(0)), 1);
			playCard(context, player, "weapon_xalatath");
			assertEquals(costOf(context, player, player.getHeroPowerZone().get(0)), 0);
			destroy(context, maiden);
			assertEquals(costOf(context, player, player.getHeroPowerZone().get(0)), 1);
		});
	}

	@Test
	public void testTheSilverHand() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_onyxia");
			playCard(context, player, "weapon_the_silver_hand");
			playCard(context, player, "spell_level_up");
			player.getMinions().forEach(minion -> assertEquals(minion.getAttack(), minion.getBaseAttack() + 2));
		});
	}

	@Test
	public void testWarSwords() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "weapon_warswords_of_the_valarjar");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			assertEquals(player.getHero().getHp(), 20);
			assertEquals(player.getWeaponZone().get(0).getName(), "Warswords of the Valarjar");
		});
	}

	@Test
	public void testUlthalesh() {
		runGym((context, player, opponent) -> {
			player.setMana(10);
			Card darkPact = receiveCard(context, player, "spell_dark_pact");
			playCard(context, player, "minion_wisp");
			playCard(context, player, "minion_wisp");
			playCard(context, opponent, "minion_wisp");
			int actionsBefore = (int) context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(darkPact.getReference()))
					.count();
			assertEquals(actionsBefore, 2);
			playCard(context, player, "weapon_ulthalesh");
			int actionsAfter = (int) context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(darkPact.getReference()))
					.count();

			assertEquals(actionsAfter, 3);
			playCard(context, player, darkPact, opponent.getMinions().get(0));

			assertTrue(!player.getHero().isDestroyed());
		});

		runGym((context, player, opponent) -> {
			player.setMana(10);
			Card boi = receiveCard(context, player, "minion_sanguine_reveler");
			playCard(context, player, "minion_wisp");
			Minion enemyWisp = playMinionCard(context, opponent, "minion_wisp");
			int i = 0;
			try {
				playMinionCardWithBattlecry(context, player, boi, enemyWisp);
			} catch (AssertionError e) {
				i++;
			}
			assertEquals(i, 1);
			playCard(context, player, "weapon_ulthalesh");

			playMinionCardWithBattlecry(context, player, boi, enemyWisp);

			assertTrue(!player.getHero().isDestroyed());
		});

		runGym((context, player, opponent) -> {
			Card brew = receiveCard(context, player, "minion_youthful_brewmaster");
			Minion wisp = playMinionCard(context, opponent, "minion_wisp");
			playCard(context, player, "weapon_ulthalesh");
			playMinionCardWithBattlecry(context, player, brew, wisp);
			assertEquals(player.getHand().size(), 1);
			assertEquals(opponent.getHand().size(), 0);
		});

		for (int i = 0; i < 10; i++) {
			runGym((context, player, opponent) -> {
				Card sac = receiveCard(context, player, "spell_unwilling_sacrifice");
				Minion wisp = playMinionCard(context, opponent, "minion_wisp");
				Minion wisp2 = playMinionCard(context, opponent, "minion_wisp");
				playCard(context, player, "weapon_ulthalesh");
				playCard(context, player, sac, wisp);
				assertTrue(wisp.isDestroyed());
				assertTrue(wisp2.isDestroyed());
			});
		}
	}

	@Test
	public void testScepterOfSargerasDiluteSoulInteraction() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_the_coin");
			receiveCard(context, player, "minion_neutral_test");
			receiveCard(context, player, "minion_red_test");
			playCard(context, player, "weapon_scepter_of_sargeras");
			AtomicInteger discovers = new AtomicInteger();
			overrideDiscover(context, player, discoverActions -> {
				discovers.incrementAndGet();
				assertEquals(discoverActions.size(), 3);
				return discoverActions.stream().filter(c -> c.getCard().getCardId().equals("spell_the_coin")).findFirst().orElseThrow(AssertionError::new);
			});
			playCard(context, player, "spell_dilute_soul");
			assertEquals(discovers.get(), 1);
			assertEquals(player.getHand().size(), 2);
			context.endTurn();
			assertEquals(player.getHand().size(), 4);
			assertEquals(player.getHand().stream().filter(c -> c.getCardId().equals("spell_the_coin")).count(), 2L);
		});
	}

	@Test
	public void testScepterOfSargeras() {
		for (int i = 0; i < 10; i++) {
			runGym((context, player, opponent) -> {
				receiveCard(context, player, "minion_target_dummy");
				receiveCard(context, player, "minion_snowflipper_penguin");
				receiveCard(context, player, "minion_snowflipper_penguin");

				playCard(context, player, "weapon_scepter_of_sargeras");
				overrideDiscover(context, player, "minion_target_dummy");
				playCard(context, player, "spell_soulfire", opponent.getHero());
				assertEquals(player.getHand().size(), 2);
				assertEquals(player.getHand().get(0).getCardId(), "minion_snowflipper_penguin");
				assertEquals(player.getHand().get(1).getCardId(), "minion_snowflipper_penguin");
			});
		}
	}

	@Test
	public void testFandralStaghelmPlagueLordInteraction() {
		runGym((context, player, opponent) -> {
			player.setMana(2);
			SpellUtils.castChildSpell(context, player, ChangeHeroPowerSpell.create("hero_power_plague_lord"), player, player);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream().count(), 3);
			playCard(context, player, "minion_fandral_staghelm");
			player.setMana(2);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream().count(), 2);
			context.performAction(player.getId(), context.getLogic().getValidActions(player.getId())
					.stream().filter(gameAction -> gameAction.getActionType().equals(ActionType.HERO_POWER)).findFirst().get());
			context.getLogic().canPlayCard(player.getId(), player.getHeroPowerZone().get(0).getReference());
			assertEquals(player.getHero().getAttack(), 3);
			assertEquals(player.getHero().getArmor(), 3);
		});
	}

	@Test
	public void testScytheOfElune() {
		runGym((context, player, opponent) -> {
			player.setMana(10);
			Card roots = receiveCard(context, player, "spell_living_roots");
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.NONE))
					.count(), 1);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.ANY))
					.count(), 2);

			playCard(context, player, "weapon_scythe_of_elune");
			assertEquals(player.getWeaponZone().get(0).getDescription(), "Your Choose One effects have both options combined. Swaps each turn.");
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.NONE))
					.count(), 0);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.ANY))
					.count(), 2);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getWeaponZone().get(0).getDescription(), "Your Choose One effects have only their first option. Swaps each turn.");
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.NONE))
					.count(), 0);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.ANY))
					.count(), 2);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getWeaponZone().get(0).getDescription(), "Your Choose One effects have only their second option. Swaps each turn.");
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.NONE))
					.count(), 1);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.ANY))
					.count(), 0);
			playCard(context, player, "weapon_rusty_hook");
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.NONE))
					.count(), 1);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.ANY))
					.count(), 2);
		});
	}

	@Test
	public void testEbonChill() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_ebonchill");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, player, "spell_frost_nova");
			for (Minion minion : opponent.getMinions()) {
				assertEquals(minion.getHp(), minion.getMaxHp() - 3);
			}
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_ebonchill");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, player, "spell_frost_nova");
			for (int i = 0; i < 6; i++) {
				assertEquals(opponent.getMinions().get(i).getHp(), 6);
			}
		});
	}

	@Test
	public void testFelomelorn() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_felomelorn");
			receiveCard(context, player, "spell_flamestrike");
			shuffleToDeck(context, player, "spell_flamestrike");
			playCard(context, player, "minion_kobold_geomancer");
			Minion dummy = playMinionCard(context, opponent, "minion_unpowered_steambot");
			context.fireGameEvent(new TurnEndEvent(context, player.getId()));
			assertEquals(dummy.getHp(), 4);
			assertEquals(player.getHand().size() + player.getDeck().size(), 1);
		});
	}

	@Test
	public void testTuure() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_tuure");
			for (int i = 0; i < 10; i++) {
				context.fireGameEvent(new TurnStartEvent(context, player.getId()));
			}
		});
	}

	@Test
	public void testFangsOfAshmane() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_fangs_of_ashmane_artifact");
			playCard(context, player, "spell_bite");
			assertEquals(player.getHero().getAttack(), 8);
			destroy(context, player.getWeaponZone().get(0));
			assertEquals(player.getHero().getAttack(), 4);
		});
	}

	@Test
	public void testTheDreadblade() {
		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			assertFalse(dreadBlade.hasAttribute(Attribute.LIFESTEAL));
			playCard(context, player, "weapon_spectral_cutlass");
			assertTrue(dreadBlade.hasAttribute(Attribute.LIFESTEAL));
			playCard(context, player, dreadBlade);
			assertTrue(player.getWeaponZone().get(0).hasAttribute(Attribute.LIFESTEAL));
		}, HeroClass.BLACK, HeroClass.BLACK);

		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			assertFalse(dreadBlade.hasAttribute(Attribute.BATTLECRY));
			playCard(context, player, "weapon_jade_claws");
			assertTrue(dreadBlade.hasAttribute(Attribute.BATTLECRY));
			playCard(context, player, dreadBlade);
			assertEquals(player.getMinions().size(), 2);
		}, HeroClass.BLACK, HeroClass.BLACK);

		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			assertFalse(dreadBlade.hasAttribute(Attribute.DEATHRATTLES));
			playCard(context, player, "weapon_hammer_of_twilight");
			assertTrue(dreadBlade.hasAttribute(Attribute.DEATHRATTLES));
			playCard(context, player, dreadBlade);
			destroy(context, player.getWeaponZone().get(0));
			assertEquals(player.getMinions().size(), 2);
		}, HeroClass.BLACK, HeroClass.BLACK);

		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			Card shard = receiveCard(context, player, "weapon_obsidian_shard");
			assertEquals(costOf(context, player, dreadBlade), 7);
			assertEquals(costOf(context, player, shard), 4);
			playCard(context, player, "spell_arcane_missiles");
			playCard(context, player, "spell_arcane_missiles");
			assertEquals(costOf(context, player, dreadBlade), 7);
			assertEquals(costOf(context, player, shard), 2);
			playCard(context, player, shard);
			assertEquals(costOf(context, player, dreadBlade), 5);
			playCard(context, player, "weapon_obsidian_shard");
			assertEquals(costOf(context, player, dreadBlade), 3);
			playCard(context, player, "spell_arcane_missiles");
			assertEquals(costOf(context, player, dreadBlade), 1);
		}, HeroClass.BLACK, HeroClass.BLACK);

		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			receiveCard(context, player, "minion_voidlord");
			receiveCard(context, player, "minion_voidlord");
			receiveCard(context, player, "minion_voidlord");
			receiveCard(context, player, "minion_voidlord");
			playCard(context, player, "weapon_skull_of_the_manari");
			context.fireGameEvent(new TurnStartEvent(context, player.getId()));
			assertEquals(player.getMinions().size(), 1);
			playCard(context, player, dreadBlade);
			context.fireGameEvent(new TurnStartEvent(context, player.getId()));
			assertEquals(player.getMinions().size(), 2);
		});

		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			playCard(context, player, "weapon_candleshot");
			assertTrue(player.getHero().hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING));
			destroy(context, player.getWeaponZone().get(0));
			assertFalse(player.getHero().hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING));
			playCard(context, player, dreadBlade);
			assertTrue(player.getHero().hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING));
			destroy(context, player.getWeaponZone().get(0));
			assertFalse(player.getHero().hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING));
		});
	}

	@Test
	public void testTombStoneTerror() {
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, opponent, "minion_kobold_monk");
			assertTrue(opponent.getHero().hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS));
			destroy(context, minion);
			playCard(context, player, "minion_tombstone_terror");
			assertTrue(player.getHero().hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS));
		});
	}

	@Test
	public void testTeronGorefiend() {
		runGym((context, player, opponent) -> {
			Minion gorefiend = playMinionCard(context, player, "minion_teron_gorefiend");
			receiveCard(context, player, "minion_harvest_golem");
			shuffleToDeck(context, player, "minion_harvest_golem");
			context.fireGameEvent(new WillEndSequenceEvent(context));
			assertTrue(gorefiend.hasAttribute(Attribute.DEATHRATTLES));
			playCard(context, player, "spell_twisting_nether");
			assertEquals(player.getMinions().size(), 2);
		});
	}

	@Test
	public void testMidnightRide() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_wisp");
			playCard(context, player, "minion_wisp");
			playCard(context, opponent, "minion_wisp");
			playCard(context, player, "spell_midnight_ride");
			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getHand().size(), 0);
			assertEquals(player.getDeck().size(), 2);

			context.getLogic().drawCard(player.getId(), null);
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getHand().size(), 0);
			assertEquals(player.getMinions().size(), 1);

			context.getLogic().drawCard(player.getId(), null);
			assertEquals(player.getDeck().size(), 0);
			assertEquals(player.getHand().size(), 0);
			assertEquals(player.getMinions().size(), 2);


			assertEquals(opponent.getDeck().size(), 1);
			assertEquals(opponent.getHand().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
			context.getLogic().drawCard(opponent.getId(), null);
			assertEquals(opponent.getDeck().size(), 0);
			assertEquals(opponent.getHand().size(), 0);
			assertEquals(opponent.getMinions().size(), 1);
		});
	}

	@Test
	public void testIceCap() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_snowflipper_penguin");
			}

			playCard(context, player, "spell_icecap");
			playCard(context, player, "spell_arcane_intellect");
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 27);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 27);
			playCard(context, player, "spell_arcane_intellect");
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 27);
		});
	}

	@Test
	public void testMealcatcher() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_argent_squire");
			shuffleToDeck(context, opponent, "minion_molten_giant");
			Minion mealcatcher = playMinionCard(context, player, "minion_mealcatcher");
			assertEquals(mealcatcher.getAttack(), mealcatcher.getBaseAttack() + 1);
			assertEquals(mealcatcher.getHp(), mealcatcher.getBaseHp() + 1);
			assertTrue(mealcatcher.hasAttribute(Attribute.DIVINE_SHIELD));
			assertEquals(player.getDeck().size(), 0);
		});
	}

	@Test
	public void testPuzzleBox() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				shuffleToDeck(context, player, "minion_wisp");
			}
			playCard(context, player, "permanent_puzzle_box");
			assertEquals(player.getDeck().size(), 0);
			for (int i = 0; i < 4; i++) {
				assertEquals(player.getHand().size(), 0);
				context.endTurn();
			}
			assertEquals(player.getHand().size(), 4);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHand().size(), 4);
		});
	}

	@Test
	public void testDerangedShifter() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_swamp_leech");
			Minion shifter = playMinionCard(context, player, "minion_deranged_shifter");
			assertTrue(shifter.hasAttribute(Attribute.LIFESTEAL));
		});
	}

	@Test
	public void testContinuity() {
		runGym((context, player, opponent) -> {
			Card flame1 = receiveCard(context, player, "token_flame_elemental");
			Card flame2 = shuffleToDeck(context, player, "token_flame_elemental");
			Minion flame3 = playMinionCard(context, player, "token_flame_elemental");
			Minion flame4 = playMinionCard(context, player, "token_flame_elemental");
			playCard(context, player, "spell_continuity", flame3);
			assertEquals(flame4.getAttack(), 3);
			assertEquals(flame3.getAttack(), 3);
			assertEquals(flame2.getBaseAttack() + flame2.getBonusAttack(), 3);
			assertEquals(flame1.getBaseAttack() + flame1.getBonusAttack(), 3);
		});
	}


	/*
	@Test
	public void testEchoLocate() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "minion_target_dummy");
			shuffleToDeck(context, player, "minion_snowflipper_penguin");
			playCardWithTarget(context, player, "spell_forgotten_torch", opponent.getHero());
			playCardWithTarget(context, player, "spell_echolocate", opponent.getHero());

		});
	}
	*/

	@Test
	public void testIntoTheMines() {
		runGym((context, player, opponent) -> {
			Card intoTheMines = receiveCard(context, player, "quest_into_the_mines");
			assertTrue(intoTheMines.isQuest());
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_into_the_mines");
			for (int i = 0; i < 9; i++) {
				playCard(context, player, "spell_freezing_potion", opponent.getHero());
			}
			assertEquals(player.getHand().size(), 1);

		});
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_into_the_mines");
			playCard(context, player, "spell_pyroblast", player.getHero());
			playCard(context, player, "spell_healing_rain");
			assertEquals(player.getHand().size(), 1);

		});
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_into_the_mines");
			playCard(context, opponent, "minion_wisp");
			playCard(context, opponent, "minion_wisp");
			playCard(context, opponent, "minion_wisp");
			playCard(context, player, "spell_mass_dispel");
			playCard(context, player, "spell_mass_dispel");
			playCard(context, player, "spell_mass_dispel");
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testAkarador() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_wisp");
			playCard(context, player, "minion_murloc_tinyfin");
			playCard(context, player, "minion_target_dummy");
			playCard(context, player, "spell_twisting_nether");
			playCard(context, player, "minion_snowflipper_penguin");
			playCard(context, player, "spell_twisting_nether");
			playCard(context, player, "minion_akarador");
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_snowflipper_penguin");
		});
	}

	@Test
	public void testBonefetcher() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_argent_squire");
			Minion bonefetcher = playMinionCard(context, player, "minion_bonefetcher");
			assertTrue(bonefetcher.hasAttribute(Attribute.DIVINE_SHIELD));
		});
	}

	@Test
	public void testSlainParty() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_fiendish_circle");
			playCard(context, opponent, "spell_call_in_the_finishers");
			playCard(context, player, "spell_twisting_nether");
			playCard(context, player, "spell_slain_party");
			for (Minion minion : player.getMinions()) {
				assertTrue(minion.getRace().hasRace(Race.MURLOC));
			}
		});
	}

	@Test
	public void testSummonAttackCards() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				playCard(context, player, "minion_murloc_tinyfin");
			}
			Minion dummy = playMinionCard(context, opponent, "minion_target_dummy");
			playMinionCardWithBattlecry(context, player, "minion_boneyard_brute", dummy);
			assertEquals(dummy.getHp(), 1);
		});

		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				playCard(context, player, "minion_murloc_tinyfin");
			}
			Minion dummy = playMinionCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, player, "spell_bat_swarm", dummy);
			assertEquals(dummy.getHp(), 5);
		});
	}

	@Test
	public void testArthasMenethil() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_arthas_menethil");
			player.setMana(10);
			playCard(context, player, "spell_summon_for_opponent");
			assertEquals(opponent.getMinions().size(), 1);
			destroy(context, opponent.getMinions().get(0));
			assertEquals(opponent.getMinions().size(), 0);
			assertTrue(context.getValidActions().stream().anyMatch(ga -> ga.getActionType() == ActionType.HERO_POWER));
			useHeroPower(context, player);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_wisp");
		});

		runGym((context, player, opponent) -> {
			playCard(context, opponent, "spell_fiendish_circle");
			playCard(context, player, "spell_twisting_nether");
			context.endTurn();
			context.endTurn();
			playCard(context, player, "hero_arthas_menethil");
			player.setMana(10);
			assertTrue(!context.getLogic().canPlayCard(player.getId(), player.getHeroPowerZone().get(0).getReference()));
			playCard(context, opponent, "spell_call_in_the_finishers");
			playCard(context, player, "spell_twisting_nether");
			assertTrue(context.getLogic().canPlayCard(player.getId(), player.getHeroPowerZone().get(0).getReference()));
			useHeroPower(context, player);
			assertTrue(player.getMinions().get(0).getRace().hasRace(Race.MURLOC));
		});
	}

	@Test
	public void testLadyDeathwhisper2() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_lady_deathwhisper2");
			Minion wurm = playMinionCard(context, opponent, "minion_violet_wurm");
			playCard(context, player, "spell_pyroblast", wurm);
			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testPayRespects() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_pay_respects");
			playCard(context, player, "spell_fiendish_circle");
			playCard(context, player, "spell_dark_pact", player.getMinions().get(0));
			playCard(context, player, "spell_pay_respects");
			for (Minion minion : player.getMinions()) {
				assertEquals(minion.getHp(), 2);
			}
		});
	}

	@Test
	public void testRiseOfTheAncientOnesChange() {
		runGym((context, player, opponent) -> {
			Card rise = receiveCard(context, player, "spell_rise_of_the_ancient_ones");
			assertEquals(context.getLogic().getModifiedManaCost(player, rise), 30);
			playCard(context, player, "minion_onyxia");
			playCard(context, opponent, "minion_onyxia");
			playCard(context, player, "spell_twisting_nether");
			assertEquals(context.getLogic().getModifiedManaCost(player, rise), 16);
		});
	}

	@Test
	public void testObsidianSpellstone() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_obsidian_spellstone");
			playCard(context, player, "spell_overtap");
		});
	}

	@Test
	public void testAlternateBaku() {
		DebugContext context = createContext(HeroClass.SILVER, HeroClass.SILVER, false, DeckFormat.CUSTOM);
		context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
			Stream.generate(() -> "minion_faithful_lumi")
					.map(CardCatalogue::getCardById)
					.limit(29)
					.forEach(deck::addCard);
			deck.addCard(CardCatalogue.getCardById("minion_alternate_baku_the_mooneater"));
		});
		context.init();
		assertEquals(context.getPlayer1().getHeroPowerZone().get(0).getCardId(), "hero_power_alternate_totemic_slam");

		DebugContext context2 = createContext(HeroClass.WHITE, HeroClass.WHITE, false, DeckFormat.CUSTOM);
		context2.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context2.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
			Stream.generate(() -> "minion_faithful_lumi")
					.map(CardCatalogue::getCardById)
					.limit(29)
					.forEach(deck::addCard);
			deck.addCard(CardCatalogue.getCardById("minion_alternate_baku_the_mooneater"));
		});
		context2.init();
		assertEquals(context2.getPlayer1().getHeroPowerZone().get(0).getCardId(), "hero_power_heal");
	}

	@Test
	public void testAlternateGenn() {
		DebugContext context = createContext(HeroClass.WHITE, HeroClass.WHITE, false, DeckFormat.CUSTOM);
		context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
			Stream.generate(() -> "minion_bloodfen_raptor")
					.map(CardCatalogue::getCardById)
					.limit(29)
					.forEach(deck::addCard);
			deck.addCard(CardCatalogue.getCardById("minion_alternate_genn_greymane"));
		});

		context.init();
		assertTrue(context.getEntities().anyMatch(c -> c.getSourceCard().getCardId().equals("spell_the_coin")));
		playCard(context, context.getPlayer1(), "hero_shadowreaper_anduin");
		// Both player's hero powers should cost one
		assertEquals(context.getEntities().filter(c -> c.getEntityType() == EntityType.CARD)
				.map(c -> (Card) c)
				.filter(c -> c.getCardType() == CardType.HERO_POWER)
				.filter(c -> costOf(context, context.getPlayer(c.getOwner()), c) == 1)
				.count(), 2L);
	}

	@Test
	public void testAlternateStartingHeroPowers() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "passive_dire_beast");
			context.fireGameEvent(new PreGameStartEvent(context, player.getId()));
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_dire_beast");
		});

		int direStables = 0;

		for (int i = 0; i < 100; i++) {
			DebugContext debug = createContext(HeroClass.GREEN, HeroClass.GREEN, false, DeckFormat.ALL);
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
				direStables++;
			}
		}
		assertEquals(direStables, 100);
	}

	@Test
	public void testDragonfly() {
		runGym((context, player, opponent) -> {
			Minion dragonfly = playMinionCard(context, player, "minion_dragonfly");
			playCard(context, player, "spell_dragonfire_potion");
			assertFalse(dragonfly.isDestroyed(), "Still alive because dragonfly also counts as a dragon");
			playCard(context, player, "minion_timber_wolf");
			assertEquals(dragonfly.getAttack(), dragonfly.getBaseAttack() + 1);
		});

	}

	@Test
	public void testWhizbangThePlunderful() {
		runGym((context, player, opponent) -> {
			Card card = receiveCard(context, player, "minion_king_togwaggle");
			playCard(context, player, "minion_whizbang_the_plunderful");
			assertEquals(card.getHp(), 8);
			assertEquals(costOf(context, player, card), 5);

		});
	}

	@Test
	public void testBronzeTimekeeper() {
		runGym((context, player, opponent) -> {
			Minion jelly = playMinionCard(context, player, "minion_green_jelly");
			playMinionCardWithBattlecry(context, player, "minion_bronze_timekeeper", jelly);
			assertEquals(player.getMinions().size(), 3);
		});

		runGym((context, player, opponent) -> {
			Minion rag = playMinionCard(context, player, "minion_ragnaros_the_firelord");
			playCard(context, player, "minion_drakkari_enchanter");
			playCard(context, player, "minion_brann_bronzebeard");
			playMinionCardWithBattlecry(context, player, "minion_bronze_timekeeper", rag);
			assertEquals(opponent.getHero().getHp(), -2);
		});
	}

	@Test
	public void testTrophyHuntress() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion murloc = playMinionCard(context, opponent, "minion_murloc_tinyfin");
			Minion beast = playMinionCard(context, opponent, "minion_snowflipper_penguin");
			Minion dragon = playMinionCard(context, opponent, "token_whelp");
			for (Minion minion : opponent.getMinions()) {
				context.getLogic().setHpAndMaxHp(minion, 4);
			}
			context.endTurn();
			playMinionCardWithBattlecry(context, player, "minion_trophy_huntress", murloc);
			assertEquals(murloc.getHp(), murloc.getMaxHp() - 1);
			playMinionCardWithBattlecry(context, player, "minion_trophy_huntress", beast);
			assertEquals(beast.getHp(), beast.getMaxHp() - 2);
			playMinionCardWithBattlecry(context, player, "minion_trophy_huntress", dragon);
			assertEquals(dragon.getHp(), dragon.getMaxHp() - 3);
		});
	}

	/*
	@Test
	public void testEnchantments() {
		runGym((context, player, opponent) -> {
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "spell_blessing_of_wisdom", wisp);
			assertEquals(wisp.getEnchantmentsFromContext(context).size(), 1);

		});
	}
	*/

	@Test
	public void testZilchGodOfNothing() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_zilch_god_of_nothing");
			shuffleToDeck(context, player, "minion_wax_elemental");
			shuffleToDeck(context, player, "minion_public_defender");


			shuffleToDeck(context, opponent, "minion_zilch_god_of_nothing");
			shuffleToDeck(context, opponent, "minion_wisp");

			context.fireGameEvent(new GameStartEvent(context, player.getId()));

			for (Card card : player.getDeck()) {
				assertEquals(card.getBonusAttack(), 1);
				assertEquals(card.getBonusHp(), -1);
			}

			for (Card card : opponent.getDeck()) {
				assertEquals(card.getBonusAttack(), 0);
				assertEquals(card.getBonusHp(), 0);
			}
		});
	}

	@Test
	public void testThrowGlaive() {
		runGym((context, player, opponent) -> {
			Minion wisp1 = playMinionCard(context, opponent, "minion_wisp");
			Minion wisp2 = playMinionCard(context, opponent, "minion_wisp");
			Minion wisp3 = playMinionCard(context, opponent, "minion_wisp");

			playCard(context, player, "weapon_warglaives_of_azzinoth");

			playCard(context, player, "spell_leeching_poison");
			playCard(context, player, "minion_high_priest_thekal"); //health to 1

			playCard(context, player, "spell_throw_glaive", wisp2);

			assertEquals(opponent.getMinions().size(), 0);
			assertEquals(player.getHero().getHp(), 13);
		});
	}

	@Test
	public void testEyeBeam() {
		runGym((context, player, opponent) -> {
			Minion dino1 = playMinionCard(context, opponent, "minion_ultrasaur");
			Minion dino2 = playMinionCard(context, opponent, "minion_ultrasaur");
			Minion dino3 = playMinionCard(context, opponent, "minion_ultrasaur");

			playCard(context, player, "spell_eye_beam", dino2);

			assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion dino1 = playMinionCard(context, opponent, "minion_stegodon");
			Minion dino2 = playMinionCard(context, opponent, "minion_ultrasaur");
			Minion dino3 = playMinionCard(context, opponent, "minion_tyrantus");

			playCard(context, player, "spell_eye_beam", dino2);
			assertEquals(dino1.getHp(), 0);
			assertEquals(dino2.getHp(), 8);
			assertEquals(dino3.getHp(), 6);
		});

		runGym((context, player, opponent) -> {
			Minion harvest = playMinionCard(context, opponent, "minion_harvest_golem");
			playCard(context, player, "spell_eye_beam", harvest);
			assertEquals(opponent.getMinions().size(), 1);
		});
	}

	@Test
	public void testNemesis() {
		runGym((context, player, opponent) -> {
			Minion demon1 = playMinionCard(context, opponent, "minion_fearsome_doomguard");
			Minion demon2 = playMinionCard(context, opponent, "minion_voidwalker");
			Minion ogre = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			playCard(context, opponent, "minion_lord_jaraxxus");

			playCard(context, player, "spell_nemesis", ogre);
			assertEquals(ogre.getHp(), 1);
			assertEquals(demon1.getHp(), 8);
			assertEquals(demon2.getHp(), 3);
			assertEquals(opponent.getHero().getHp(), 15);

			playCard(context, player, "spell_nemesis", demon1);
			assertEquals(demon1.getHp(), 1);
			assertEquals(demon2.getHp(), 1);
			assertEquals(opponent.getHero().getHp(), 1);
		});

	}

	@Test
	public void testFelblade() {
		runGym((context, player, opponent) -> {
			Card felblade = receiveCard(context, player, "weapon_felblade");
			Minion wisp = playMinionCard(context, opponent, "minion_wisp");
			playCard(context, player, "weapon_illidari_warglaives");
			assertEquals(costOf(context, player, felblade), 3);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(costOf(context, player, felblade), 2);
			useHeroPower(context, player, wisp.getReference());
			assertTrue(wisp.isDestroyed());
			assertEquals(costOf(context, player, felblade), 1);
		}, HeroClass.PURPLE, HeroClass.PURPLE);
	}

	@Test
	public void testVengefulRetreat() {
		runGym((context, player, opponent) -> {
			Minion badWisp = playMinionCard(context, opponent, "minion_wisp");
			Minion goodWisp = playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "spell_vengeful_retreat", goodWisp);
			playCard(context, player, player.getHand().get(0));
			assertTrue(player.getMinions().get(0).canAttackThisTurn());

		});
	}

	@Test
	public void testMetamorphosis() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_wisp");
			}

			playCard(context, player, "spell_metamorphosis");
			assertEquals(player.getHand().size(), 0);

			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_demonic_form");

			Minion thek = playMinionCard(context, player, "minion_high_priest_thekal");
			assertTrue(thek.hasAttribute(Attribute.LIFESTEAL));
			assertEquals(player.getHand().size(), 1);

			playCard(context, player, "weapon_warglaives_of_azzinoth");
			assertTrue(player.getWeaponZone().get(0).hasAttribute(Attribute.LIFESTEAL));
			assertEquals(player.getHand().size(), 2);

			context.endTurn();
			context.endTurn();

			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_demonic_form");

			playCard(context, player, "spell_chaos_nova");
			assertEquals(player.getHero().getHp(), 3);

			context.endTurn();
			context.endTurn();

			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_demonic_form");

			context.endTurn();
			context.endTurn();

			assertNotEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_demonic_form");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_metamorphosis");

			Card demonForm = player.getHeroPowerZone().get(0);


			assertEquals(demonForm.getCardId(), "hero_power_demonic_form");
			String[] strings = demonForm.evaluateDescriptions(context, player);

			assertEquals(strings[0], "2");
			assertEquals(strings[1], "s");

			context.endTurn();
			context.endTurn();


			strings = demonForm.evaluateDescriptions(context, player);
			assertEquals(strings[0], "1");
			assertEquals(strings[1], "");
		});
	}

	@Test
	public void testDemonSpikes() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_demon_spikes");
			assertEquals(player.getHero().getAttack(), 1);
			context.endTurn();
			assertEquals(player.getHero().getAttack(), 1);
			context.endTurn();

			assertEquals(player.getHero().getAttack(), 0);
		});
	}

	@Test
	public void testFelRush() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_wisp");
			}
			Minion wisp = playMinionCard(context, opponent, "minion_wisp");
			playCard(context, player, "weapon_illidari_warglaives");
			playCard(context, player, "spell_fel_rush");
			assertEquals(player.getHand().size(), 1);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHand().size(), 2);
			useHeroPower(context, player, wisp.getReference());
			assertEquals(player.getHand().size(), 3);
		}, HeroClass.PURPLE, HeroClass.PURPLE);
	}

	@Test
	public void testGlaivesOfTheFallen() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_glaives_of_the_fallen");
			Minion wisp = playMinionCard(context, opponent, "minion_wisp");
			attack(context, player, player.getHero(), wisp);
			assertEquals(player.getHand().size(), 1);

			Minion croc = playMinionCard(context, opponent, "minion_river_crocolisk");
			attack(context, player, player.getHero(), croc);
			assertEquals(player.getHand().size(), 1);
			assertTrue(croc.hasAttribute(Attribute.DEATHRATTLES));

			playCard(context, player, "weapon_glaives_of_the_fallen");
			attack(context, player, player.getHero(), croc);

			assertEquals(player.getHand().size(), 2);
		});
	}

	@Test
	public void testChaosBlades() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_chaos_blades");
			Minion adept = playMinionCard(context, player, "minion_illidari_adept");
			Minion wisp = playMinionCard(context, opponent, "minion_wisp");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(adept.getAttack(), 3);
			assertTrue(wisp.isDestroyed());
			assertEquals(player.getWeaponZone().get(0).getDurability(), 4);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_chaos_blades");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getWeaponZone().get(0).getDurability(), 5);

			Minion wisp = playMinionCard(context, opponent, "minion_wisp");
			attack(context, player, player.getHero(), opponent.getHero());
			assertTrue(wisp.isDestroyed());

			assertEquals(player.getWeaponZone().get(0).getDurability(), 3);
		});
	}

	@Test
	public void testIllidariEnforcer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_fireball", opponent.getHero());
			context.endTurn();
			context.getTurn();
			Minion enforcer = playMinionCard(context, opponent, "minion_illidari_enforcer");
			assertTrue(enforcer.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testImmolationAura() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_immolation_aura");
			Minion wisp = playMinionCard(context, opponent, "minion_wisp");
			context.endTurn();
			attack(context, opponent, wisp, player.getHero());
			assertTrue(wisp.isDestroyed());
		});
	}

	@Test
	public void testMomentum() {
		runGym((context, player, opponent) -> {
			Minion azzinoth = playMinionCard(context, player, "minion_azzinoth");
			shuffleToDeck(context, player, "minion_wisp");
			playCard(context, player, "spell_momentum");
			context.getLogic().drawCard(player.getId(), null);
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getHand().size(), 1);
			destroy(context, azzinoth);
			assertEquals(player.getDeck().size(), 2);
			assertEquals(player.getHand().size(), 2);
		});
	}

	@Test
	public void testFelBarrage() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_fel_barrage");
			assertEquals(opponent.getHero().getHp(), 20);
			playCard(context, player, "minion_bloodmage_thalnos");
			playCard(context, player, "spell_fel_barrage");
			assertEquals(opponent.getHero().getHp(), 8);
		});
	}
}

