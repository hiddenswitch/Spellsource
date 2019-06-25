package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.util.Logging;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.FixedCardsDeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.GameStartEvent;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.tests.util.TestBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GameStateValueBehaviourTest extends TestBase implements Serializable {

	@Test
	public void testDesertMaiden() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_the_coin");
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_black_test");
			playMinionCard(context, player, "minion_desert_maiden");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			context.setBehaviour(player.getId(), behaviour);
			context.endTurn();
			while (context.takeActionInTurn()) {
			}
			context.endTurn();
			while (context.takeActionInTurn()) {
			}
		});
	}

	@Test
	public void testAIMakesOpponentMillCards() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion felReaver = playMinionCard(context, opponent, "minion_fel_reaver");
			context.endTurn();
			for (int i = 0; i < 60; i++) {
				shuffleToDeck(context, opponent, "spell_the_coin");
			}
			for (int i = 0; i < 10; i++) {
				receiveCard(context, player, "spell_a_f_kupcake");
			}
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			context.setBehaviour(player.getId(), behaviour);

			while (context.takeActionInTurn()) {
			}

			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testAIFindsPhysicalAttackLethal() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 7; i++) {
				playMinionCard(context, player, "minion_stonetusk_boar");
			}
			opponent.getHero().setHp(7);
			Minion highValueTarget = playMinionCard(context, opponent, "minion_neutral_test");
			highValueTarget.setAttack(1000);
			highValueTarget.setAttribute(Attribute.SPELL_DAMAGE, 1000);
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			behaviour.setExpandDepthForLethal(true);
			behaviour.setParallel(true);
			behaviour.setPruneContextStack(true);
			context.setBehaviour(player.getId(), behaviour);

			while (context.takeActionInTurn()) {
			}

			assertTrue(context.updateAndGetGameOver());
		});
	}

	@Test
	public void testShadowVisionsInPlan() {
		Logging.setLoggingLevel(Level.ERROR);
		String comboPriest = "Name: Combo Priest\n" +
				"Class: Priest\n" +
				"Format: Standard\n" +
				"2x Circle of Healing\n" +
				"2x Silence\n" +
				"2x Inner Fire\n" +
				"2x Northshire Cleric\n" +
				"2x Power Word: Shield\n" +
				"2x Divine Spirit\n" +
				"2x Radiant Elemental\n" +
				"2x Shadow Ascendant\n" +
				"2x Shadow Visions\n" +
				"2x Upgradeable Framebot\n" +
				"2x Wild Pyromancer\n" +
				"2x Acolyte of Pain\n" +
				"2x Bronze Gatekeeper\n" +
				"1x Mass Dispel\n" +
				"2x Unpowered Steambot\n" +
				"1x Lyra the Sunshard";
		String apmPriest = "Name: APM Priest\n" +
				"Class: Priest\n" +
				"Format: Standard\n" +
				"2x Topsy Turvy\n" +
				"2x Binding Heal\n" +
				"2x Power Word: Shield\n" +
				"1x Stonetusk Boar\n" +
				"2x Test Subject\n" +
				"1x Bloodmage Thalnos\n" +
				"2x Dead Ringer\n" +
				"1x Divine Spirit\n" +
				"1x Doomsayer\n" +
				"2x Loot Hoarder\n" +
				"2x Radiant Elemental\n" +
				"2x Shadow Visions\n" +
				"2x Spirit Lash\n" +
				"2x Twilight's Call\n" +
				"2x Vivid Nightmare\n" +
				"2x Witchwood Piper\n" +
				"2x Psychic Scream";
		String mecathunPriest = "Name: Mecha'thun Priest\n" +
				"Class: Priest\n" +
				"Format: Standard\n" +
				"2x Circle of Healing\n" +
				"2x Northshire Cleric\n" +
				"2x Power Word: Shield\n" +
				"1x Bloodmage Thalnos\n" +
				"2x Dead Ringer\n" +
				"2x Loot Hoarder\n" +
				"1x Plated Beetle\n" +
				"2x Radiant Elemental\n" +
				"2x Shadow Visions\n" +
				"2x Spirit Lash\n" +
				"2x Wild Pyromancer\n" +
				"2x Twilight's Call\n" +
				"2x Ticking Abomination\n" +
				"1x Reckless Experimenter\n" +
				"1x Coffin Crasher\n" +
				"1x Hemet, Jungle Hunter\n" +
				"2x Psychic Scream\n" +
				"1x Mecha'thun";
		String resurrectPriest = "Name: Resurrect Priest\n" +
				"Class: Priest\n" +
				"Format: Standard\n" +
				"2x Holy Smite\n" +
				"1x Bloodmage Thalnos\n" +
				"2x Mind Blast\n" +
				"2x Radiant Elemental\n" +
				"2x Shadow Visions\n" +
				"1x Shadow Word: Pain\n" +
				"2x Spirit Lash\n" +
				"2x Gilded Gargoyle\n" +
				"1x Shadow Word: Death\n" +
				"2x Eternal Servitude\n" +
				"1x Lyra the Sunshard\n" +
				"1x Zilliax\n" +
				"2x Shadow Essence\n" +
				"2x Lesser Diamond Spellstone\n" +
				"1x Prophet Velen\n" +
				"2x Psychic Scream\n" +
				"1x The Lich King\n" +
				"1x Malygos\n" +
				"1x Obsidian Statue\n" +
				"1x Zerek's Cloning Gallery";
		for (String deck : new String[]{comboPriest, apmPriest, resurrectPriest, mecathunPriest}) {
			runGym((context, player, opponent) -> {
				GameStateValueBehaviour behaviour1 = new InvalidPlanTest();
				GameStateValueBehaviour behaviour2 = new InvalidPlanTest();
				context.setBehaviour(0, behaviour1);
				context.setBehaviour(1, behaviour2);
				context.resume();
			}, DeckCreateRequest.fromDeckList(deck).toGameDeck(), DeckCreateRequest.fromDeckList(deck).toGameDeck());
		}
	}

	private static class InvalidPlanTest extends GameStateValueBehaviour {

		InvalidPlanTest() {
			super();
			setThrowOnInvalidPlan(true);
		}

		@Nullable
		@Override
		public GameAction requestAction(@NotNull GameContext context, @NotNull Player player, @NotNull List<GameAction> validActions) {
			try {
				return super.requestAction(context, player, validActions);
			} catch (IllegalStateException invalidPlan) {
				Assert.fail("Plan was invalid");
				throw invalidPlan;
			}
		}
	}

	@Test
	public void testDiscoverActionsWithLowDepth() {
		runGym((context, player, opponent) -> {
			GameStateValueBehaviour checkDepth = new GameStateValueBehaviour();
			checkDepth.setMaxDepth(2);
			for (int i = 0; i < 9; i++) {
				receiveCard(context, player, "minion_ivory_knight");
			}
			shuffleToDeck(context, player, "passive_zero_cost");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			assertEquals(costOf(context, player, player.getHand().get(0)), 0);
			context.setBehaviour(player.getId(), checkDepth);

			while (context.takeActionInTurn()) {
			}
		});
	}

	@Test
	public void testPlaysWildGrowth() {
		runGym((context, player, opponent) -> {
			// Should play wild growth
			receiveCard(context, player, "spell_wild_growth");
			receiveCard(context, player, "minion_pompous_thespian");
			player.setMana(3);
			context.setBehaviour(player.getId(), new GameStateValueBehaviour());

			while (context.takeActionInTurn()) {
			}

			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getMaxMana(), 2);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			playMinionCard(context, opponent, "minion_bloodfen_raptor");
			// should not play wild growth
			context.endTurn();
			// Set hp to yellow threat
			player.getHero().setHp(17);
			receiveCard(context, player, "spell_wild_growth");
			receiveCard(context, player, "minion_pompous_thespian");
			context.setBehaviour(player.getId(), new GameStateValueBehaviour());

			while (context.takeActionInTurn()) {
			}

			// Note: this is turn 2 for the player
			assertEquals(player.getMaxMana(), 2);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_pompous_thespian");
		});
	}

	@Test
	public void testBrannSpiteful() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("minion_faceless_behemoth"));
			Card brann = receiveCard(context, player, "minion_brann_bronzebeard");
			Card spiteful = receiveCard(context, player, "minion_spiteful_summoner");
			putOnTopOfDeck(context, player, "spell_ultimate_infestation");
			player.setMana(10);
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			List<GameAction> actions = new ArrayList<>();
			for (int i = 0; i < 2; i++) {
				GameAction chosen = behaviour.requestAction(context, player, context.getValidActions());
				actions.add(chosen);
				context.performAction(player.getId(), chosen);
			}
			Assert.assertEquals(actions.get(0).getSourceReference(), brann.getReference());
			Assert.assertEquals(actions.get(1).getSourceReference(), spiteful.getReference());
		});
	}

	@Test
	public void testAIExploresDiscoversForWinning() {
		runGym((context, player, opponent) -> {

			// There is a branch of discover actions that wins the bot the game.
			context.setBehaviour(player.getId(), new GameStateValueBehaviour());
			receiveCard(context, player, "spell_cascading_discovers");

			while (context.takeActionInTurn()) {
			}

			assertTrue(context.updateAndGetGameOver());
		});
	}

	@Test
	public void testCorrectOrder() {
		runGym((context, player, opponent) -> {
			GameStateValueBehaviour checkDepth = new GameStateValueBehaviour();
			checkDepth.setMaxDepth(6);
			checkDepth.setTimeout(24000L);
			checkDepth.setParallel(false);
			putOnTopOfDeck(context, opponent, "minion_bloodfen_raptor");
			opponent.getHero().setHp(4);
			// Your hero power is, Equip a 1/1 Weapon (costs 2)
			Minion cannotAttack = playMinionCard(context, player, "minion_1_1_hero_power");
			// Whenever this minion is targeted by a battlecry, gain 10,000 HP
			Minion gains = playMinionCard(context, player, "minion_gains_huge_hp");
			player.setMana(6);
			// Cost 3, Give a minion +1/+1
			receiveCard(context, player, "minion_shattered_sun_cleric");
			// Cost 1, Has Charge while you have a weapon equipped
			receiveCard(context, player, "minion_southsea_deckhand");
			context.setBehaviour(player.getId(), checkDepth);

			while (context.takeActionInTurn()) {
			}

			// Should equip weapon, play Southsea, then play Shattered Sun Cleric, battlecry target Southsea, then attack with
			// southsea, then attack with hero
			// Southsea MUST attack before weapon attacks
			// Sun cleric MUST be played after southsea and should NOT buff a minion that gains a huge amount of hp
			// This is a depth 6 puzzle.
			Assert.assertNull(player.getHero().getWeapon());
			assertTrue(context.updateAndGetGameOver());
		}, HeroClass.BLACK, HeroClass.BLACK);
	}

	@Test
	public void testDestroyAt2HP() {
		runGym((context, player, opponent) -> {
			Card fireball = receiveCard(context, player, "spell_fireball");
			player.setMana(4);
			player.setMaxMana(4);
			opponent.getHero().setHp(2);
			List<GameAction> actions = context.getValidActions();
			assertTrue(actions.stream().anyMatch(ga -> fireball.getReference().equals(ga.getSourceReference())
					&& opponent.getHero().getReference().equals(ga.getTargetReference())), "The player should be able to cast the Fireball on the opponent's hero");
			assertTrue(actions.stream().anyMatch(ga -> player.getHero().getHeroPower().getReference().equals(ga.getSourceReference())
					&& opponent.getHero().getReference().equals(ga.getTargetReference())), "The player should be able to Fireblast on the opponent's hero");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction chosen = behaviour.requestAction(context, player, actions);
			Assert.assertEquals(chosen.getSourceReference(), fireball.getReference(), "The AI should have chosen to play the Fireball");
			Assert.assertEquals(chosen.getTargetReference(), opponent.getHero().getReference(), "The AI should have chosen to cast it against the enemy hero.");
		}, /*Set hero class to mage for fireblast hero power*/ HeroClass.BLUE, HeroClass.BLUE);
	}


	@Test
	public void testAIWillPlayQuests() {
		runGym((context, player, opponent) -> {
			Card powerTrip = receiveCard(context, player, "spell_power_trip");
			receiveCard(context, player, "minion_voidwalker");
			// The AI loves Taunts
			receiveCard(context, player, "spell_mirror_image");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, player, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.SPELL);
			Assert.assertEquals(action.getSourceReference(), powerTrip.getReference());
		});
	}

	@Test
	public void testAIWillPlayCursed() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			opponent.setMana(2);
			opponent.setMaxMana(2);
			receiveCard(context, opponent, "spell_cursed");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.SPELL);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			opponent.setMana(4);
			opponent.setMaxMana(4);
			Card cursed = receiveCard(context, opponent, "spell_cursed");
			receiveCard(context, opponent, "spell_fireball");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			context.performAction(opponent.getId(), action);
			action = behaviour.requestAction(context, opponent, context.getValidActions());
			context.performAction(opponent.getId(), action);
			Assert.assertFalse(opponent.getHand().contains(cursed));
		});
	}

	@Test
	public void testAIWillNotPlayIntoDoomsayer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_doomsayer");
			context.endTurn();
			receiveCard(context, opponent, "minion_snowflipper_penguin");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			behaviour.setTriggerStartTurns(true);
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.END_TURN);
		});

		runGym((context, player, opponent) -> {
			Minion doomsayer = playMinionCard(context, player, "minion_doomsayer");
			context.endTurn();
			for (int i = 0; i < 3; i++) {
				playMinionCard(context, opponent, "minion_charge_test");
			}
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			behaviour.setTriggerStartTurns(true);
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.PHYSICAL_ATTACK);
			Assert.assertEquals(action.getTargetReference(), doomsayer.getReference());
		});

		runGym((context, player, opponent) -> {
			Minion doomsayer = playMinionCard(context, player, "minion_doomsayer");
			context.endTurn();
			playMinionCard(context, opponent, "minion_kobold_geomancer");
			receiveCard(context, opponent, "spell_fireball");
			opponent.setMaxMana(4);
			opponent.setMana(4);
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			behaviour.setTriggerStartTurns(true);
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.SPELL);
			Assert.assertEquals(action.getTargetReference(), doomsayer.getReference());
		});
	}

	@Test
	public void testAIWillPlayIntoSnakeTrap() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_snake_trap");
			Minion targetDummy = playMinionCard(context, player, "minion_target_dummy");
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.PHYSICAL_ATTACK);
			Assert.assertEquals(action.getTargetReference(), targetDummy.getReference());
			Assert.assertEquals(action.getSourceReference(), charger.getReference());
		});
	}
}
