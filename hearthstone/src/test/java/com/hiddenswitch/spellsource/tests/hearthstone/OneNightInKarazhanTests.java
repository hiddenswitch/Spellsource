package com.hiddenswitch.spellsource.tests.hearthstone;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.DebugContext;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class OneNightInKarazhanTests extends TestBase {

	@Test
	public void testEtherealPeddler() {
		runGym((context, player, opponent) -> {
			Card bloodfen = receiveCard(context, player, "minion_bloodfen_raptor");
			Card rogueCard = receiveCard(context, player, "minion_tomb_pillager");
			Card mageCard = receiveCard(context, player, "minion_water_elemental");
			playCard(context, player, "minion_ethereal_peddler");
			assertEquals(costOf(context, player, bloodfen), bloodfen.getBaseManaCost());
			assertEquals(costOf(context, player, rogueCard), rogueCard.getBaseManaCost());
			assertEquals(costOf(context, player, mageCard), mageCard.getBaseManaCost() - 2);
		}, "BLACK", "BLACK");
	}

	@Test
	public void testBarnesSilencingInteraction() {
		runGym((context, player, opponent) -> {
			String cardId = "minion_bloodfen_raptor";
			shuffleToDeck(context, player, cardId);
			playCard(context, player, "minion_barnes");
			Minion raptor = player.getMinions().get(1);
			playCard(context, player, "spell_silence", raptor);
			assertEquals(raptor.getAttack(), 3);
			assertEquals(raptor.getHp(), 2);
		});
	}

	@Test
	public void testBarnesHealingInteraction() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_barnes");
			playCard(context, player, "spell_circle_of_healing");
			assertEquals(player.getMinions().get(1).getHp(), 1);
		});
	}

	@Test
	public void testPrinceMalchezaar() {
		DebugContext context = createContext("WHITE", "WHITE", false, new DeckFormat().withCardSets(CardSet.BASIC, CardSet.CLASSIC));
		context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
			Stream.generate(() -> "minion_bloodfen_raptor")
					.map(CardCatalogue::getCardById)
					.limit(29)
					.forEach(deck::addCard);
			deck.addCard(CardCatalogue.getCardById("minion_prince_malchezaar"));
		});

		context.init();
		// Should include 10 legendaries added + the 2 Malchezaars
		assertEquals(context.getEntities().filter(c -> (c.getSourceCard().getZone() == Zones.DECK || c.getSourceCard().getZone() == Zones.HAND) && c.getSourceCard().getRarity().isRarity(Rarity.LEGENDARY)).count(), 12L);
	}

	@Test
	public void testManyPatches() {
		runGym((context, player, opponent) -> {
			Stream.generate(() -> "minion_patches_the_pirate")
					.map(CardCatalogue::getCardById)
					.limit(30)
					.forEach(card -> context.getLogic().shuffleToDeck(player, card));

			playMinionCard(context, player, "minion_patches_the_pirate");
			assertEquals(player.getMinions().stream()
					.map(Minion::getSourceCard)
					.map(Card::getCardId)
					.filter(cid -> cid.equals("minion_patches_the_pirate"))
					.count(), 7L);

			assertEquals(player.getDeck().size(), 24);
		});
	}

	@Test
	public void testMalchezaarsImp() {
		runGym((context, player, opponent) -> {
			clearHand(context, player);
			clearZone(context, player.getDeck());
			putOnTopOfDeck(context, player, "minion_acidic_swamp_ooze");
			Card minionNoviceEngineer = receiveCard(context, player, "minion_novice_engineer");
			playCard(context, player, "minion_malchezaars_imp");
			Card soulfire = receiveCard(context, player, "spell_soulfire");
			playCard(context, player, soulfire, opponent.getHero());
			assertEquals(player.getHand().get(0).getCardId(), "minion_acidic_swamp_ooze", "The player should have Acidic Swamp Ooze in their hand after playing soulfire.");
			assertEquals(minionNoviceEngineer.getZone(), Zones.GRAVEYARD, "Novice engineer should be in the graveyard.");
		});
	}
}
