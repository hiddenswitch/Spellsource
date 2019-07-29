package com.hiddenswitch.hearthstone;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.spellsource.util.Logging;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.tests.util.TestBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.List;

public class GameStateValueBehaviourTest extends TestBase implements Serializable {

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
}
