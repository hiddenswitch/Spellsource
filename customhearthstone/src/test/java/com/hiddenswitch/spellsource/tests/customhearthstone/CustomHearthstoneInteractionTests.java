package com.hiddenswitch.spellsource.tests.customhearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.FixedCardsDeckFormat;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.GameStartEvent;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.TestBase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


public class CustomHearthstoneInteractionTests extends TestBase {

	@Test
	public void testDeathwhelpHatchesInteraction() {
		runGym((context, player, opponent) -> {
			Card hatches = receiveCard(context, player, "minion_hatches_the_dragon");
			playCard(context, player, "minion_deathwhelp");
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_deathwhelp", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testRenoJackson() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_the_coin");
			shuffleToDeck(context, player, "spell_mirror_image");
			player.getHero().setHp(1);
			playCard(context, player, "minion_reno_jackson");
			assertEquals(player.getHero().getHp(), 30);
		});

		// Test a specific deck from a user
		{
			String deckListReno = "### Full Legend Paladin\n" +
					"# Class: GOLD\n" +
					"# Format: Custom\n" +
					"#\n" +
					"# 1x (1) Sir Finley Mrrgglton\n" +
					"# 1x (2) Prince Keleseth\n" +
					"# 1x (3) Moroes\n" +
					"# 1x (3) Wickerflame Burnbristle\n" +
					"# 1x (3) Zola the Gorgon\n" +
					"# 1x (4) Spiritsinger Umbra\n" +
					"# 1x (5) Elise the Trailblazer\n" +
					"# 1x (5) Feugen\n" +
					"# 1x (5) Nexus-Champion Saraad\n" +
					"# 1x (5) Prince Malchezaar\n" +
					"# 1x (5) Stalagg\n" +
					"# 1x (5) Unnerfed Sylvanas Windrunner\n" +
					"# 1x (6) Cairne Bloodhoof\n" +
					"# 1x (6) Emperor Thaurissan\n" +
					"# 1x (6) Justicar Trueheart\n" +
					"# 1x (6) Reno Jackson\n" +
					"# 1x (6) Sunkeeper Tarim\n" +
					"# 1x (6) Val'anyr\n" +
					"# 1x (7) Dr. Boom\n" +
					"# 1x (7) Nexus-King Salhadaar\n" +
					"# 1x (8) Kel'Thuzad\n" +
					"# 1x (8) Ragnaros the Firelord\n" +
					"# 1x (8) The Lich King\n" +
					"# 1x (8) Tirion Fordring\n" +
					"# 1x (9) King Krush\n" +
					"# 1x (9) Uther of the Ebon Blade\n" +
					"# 1x (9) Ysera\n" +
					"# 1x (10) N'Zoth, the Corruptor\n" +
					"# 1x (10) Y'Shaarj, Rage Unbound\n" +
					"# 1x (30) Rise of the Ancient Ones";

			GameContext context = GameContext.fromDeckLists(Arrays.asList(deckListReno, deckListReno));
			// Prevent Prince Malchezaar from shuffling a repeat legendary into your deck
			context.setDeckFormat(new FixedCardsDeckFormat("minion_legendary_test"));
			context.init();
			Player player = context.getActivePlayer();
			context.startTurn(player.getId());
			// Put Reno into the player's hand
			Card reno = Stream.concat(player.getDeck().stream(), player.getHand().stream()).filter(c -> c.getCardId().equals("minion_reno_jackson"))
					.findFirst()
					.orElseThrow(AssertionError::new);
			if (reno.getZone() != Zones.HAND) {
				context.getLogic().receiveCard(player.getId(), reno);
			}
			player.getHero().setHp(1);
			assertEquals(player.getDeck().stream().map(Card::getCardId).distinct().count(), (long) player.getDeck().size());
			playCard(context, player, reno);
			assertEquals(player.getHero().getHp(), 30);
		}
	}

	@Test
	public void testShudderwockBloodCultistInteraction() {
		runGym((context, player, opponent) -> {
			Minion remove = playMinionCard(context, player, "minion_blood_cultist");
			context.getLogic().removePeacefully(remove);
			context.getLogic().endOfSequence();
			overrideDiscover(context, player, discoverActions -> {
				fail("Shouldn't prompt to discover");
				return null;
			});
			playMinionCard(context, player, "minion_shudderwock");
		});
	}

	@Test
	public void testZilchGodOfNothing() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_zilch_god_of_nothing");
			shuffleToDeck(context, player, "minion_wax_elemental");
			shuffleToDeck(context, player, "minion_public_defender");


			shuffleToDeck(context, opponent, "minion_zilch_god_of_nothing");
			shuffleToDeck(context, opponent, "minion_neutral_test_1");

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
	public void testParadoxKingTogwaggleInteraction() {
		runGym((context, player, opponent) -> {
			Minion paradox = playMinionCard(context, player, "minion_paradox");
			playCard(context, player, "minion_king_togwaggle");
			assertEquals(player.getSetAsideZone().size(), 0);
			assertEquals(player.getHand().get(0).getCardId(), "minion_paradox");
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
}
