package com.blizzard.hearthstone;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardZone;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.stream.Stream;

public class OneNightInKarazhanTests extends TestBase {
	@Test
	@Ignore
	public void testIvoryKnight() {
		Assert.fail("Needs test.");
	}

	@Test
	public void testPrinceMalchezaar() {
		DebugContext context = createContext(HeroClass.WHITE, HeroClass.WHITE, false);
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
		Assert.assertEquals(context.getEntities().filter(c -> c.getSourceCard().getRarity() == Rarity.LEGENDARY).count(), 12L);
	}

	@Test
	public void testManyPatches() {
		runGym((context, player, opponent) -> {
			Stream.generate(() -> "minion_patches_the_pirate")
					.map(CardCatalogue::getCardById)
					.limit(30)
					.forEach(card -> context.getLogic().shuffleToDeck(player, card));

			playMinionCard(context, player, "minion_patches_the_pirate");
			Assert.assertEquals(player.getMinions().stream()
					.map(Minion::getSourceCard)
					.map(Card::getCardId)
					.filter(cid -> cid.equals("minion_patches_the_pirate"))
					.count(), 7L);

			Assert.assertEquals(player.getDeck().size(), 24);
		});
	}

	@Test
	public void testMalchezaarsImp() {
		runGym((context, player, opponent) -> {
			clearHand(context, player);
			clearZone(context, player.getDeck());
			final Card acidicSwampOoze = CardCatalogue.getCardById("minion_acidic_swamp_ooze");
			acidicSwampOoze.setOwner(player.getId());
			acidicSwampOoze.setId(context.getLogic().getIdFactory().generateId());
			player.getDeck().addCard(acidicSwampOoze);
			final Card minionNoviceEngineer = CardCatalogue.getCardById("minion_novice_engineer");
			context.getLogic().receiveCard(player.getId(), minionNoviceEngineer);
			final Card malchezaarsImpl = CardCatalogue.getCardById("minion_malchezaars_imp");
			final Card soulfire = CardCatalogue.getCardById("spell_soulfire");
			playCard(context, player, malchezaarsImpl);
			context.getLogic().receiveCard(player.getId(), soulfire);
			PlayCardAction action = soulfire.play();
			action.setTarget(context.getPlayer2().getHero());
			context.getLogic().performGameAction(player.getId(), action);
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_acidic_swamp_ooze", "The player should have Acidic Swamp Ooze in their hand after playing soulfire.");
			Assert.assertEquals(minionNoviceEngineer.getZone(), Zones.GRAVEYARD, "Novice engineer should be in the graveyard.");
		});
	}
}
