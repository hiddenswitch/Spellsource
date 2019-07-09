package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.behaviour.FiberBehaviour;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.fibers.SuspendableGameContext;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.*;

public class SuspendableGameContextTests extends TestBase {

	@Test
	public void testBasicSuspendableContext() throws ExecutionException, InterruptedException {
		SuspendableGameContext context = new SuspendableGameContext(HeroClass.BLACK, HeroClass.BROWN);
		context.play();
		context.setMulligan(context.getActivePlayerId(), Collections.emptyList());
		context.setMulligan(context.getNonActivePlayerId(), Collections.emptyList());
		while (!context.updateAndGetGameOver()) {
			context.performAction(context.getActivePlayerId(), context.getValidActions().get(0));
		}
		assertTrue(context.updateAndGetGameOver());

		context = new SuspendableGameContext(HeroClass.BLACK, HeroClass.BROWN);
		context.play();
		context.setMulligan(context.getNonActivePlayerId(), Collections.emptyList());
		context.setMulligan(context.getActivePlayerId(), Collections.emptyList());
		while (!context.updateAndGetGameOver()) {
			context.performAction(context.getActivePlayerId(), context.getValidActions().get(0));
		}
		assertTrue(context.updateAndGetGameOver());
	}

	@Test
	public void testCopySuspendableContext() throws ExecutionException, InterruptedException {
		SuspendableGameContext context1 = new SuspendableGameContext(HeroClass.BLACK, HeroClass.BROWN);
		SuspendableGameContext context2 = context1.clone();

		assertEquals(context1.getPlayer(0).getHero().getHeroClass(), context2.getPlayer(0).getHero().getHeroClass());
		assertEquals(HeroClass.BLACK, context2.getPlayer(0).getHero().getHeroClass());
		assertNull(context1.getFiber());
		assertNull(context2.getFiber());

		// Correct cloning just before mulligan
		context1 = new SuspendableGameContext();
		context1.setDeck(0, Deck.randomDeck());
		context1.setDeck(1, Deck.randomDeck());
		context1.play();
		context2 = context1.clone();
		assertNotNull(context2.getFiber());
		FiberBehaviour behaviour1 = context1.getBehaviour(context1.getActivePlayerId());
		FiberBehaviour behaviour2 = context2.getBehaviour(context2.getActivePlayerId());
		assertEquals(behaviour1.getMulliganCards().get(0).compareTo(behaviour2.getMulliganCards().get(0)), 0);
		assertEquals(behaviour1.getMulliganCards().get(1).compareTo(behaviour2.getMulliganCards().get(1)), 0);
		assertEquals(behaviour1.getMulliganCards().get(2).compareTo(behaviour2.getMulliganCards().get(2)), 0);

		// Correctly clone start of game
		context1 = new SuspendableGameContext();
		context1.setDeck(0, Deck.randomDeck());
		context1.setDeck(1, Deck.randomDeck());
		context1.play();
		context1.setMulligan(context1.getActivePlayerId(),
				Collections.singletonList(context1.getMulliganChoices(context1.getActivePlayerId()).get(0)));
		context1.setMulligan(context1.getNonActivePlayerId(),
				Collections.singletonList(context1.getMulliganChoices(context1.getNonActivePlayerId()).get(1)));
		context2 = context1.clone();
		assertEquals(context1.getActivePlayer().getHand().compareTo(context2.getActivePlayer().getHand()), 0);

		// Correctly clone a full sequence of actions
		context1 = new SuspendableGameContext();
		context1.setDeck(0, Deck.randomDeck());
		context1.setDeck(1, Deck.randomDeck());
		context1.play();
		context1.setMulligan(context1.getActivePlayerId(),
				Collections.singletonList(context1.getMulliganChoices(context1.getActivePlayerId()).get(0)));
		context1.setMulligan(context1.getNonActivePlayerId(),
				Collections.singletonList(context1.getMulliganChoices(context1.getNonActivePlayerId()).get(1)));
		// Play out a match
		while (!context1.updateAndGetGameOver()) {
			context1.performAction(context1.getActivePlayerId(), context1.getValidActions().get(0));
		}

		context2 = context1.clone();

		assertEquals(context1.getPlayer1().getGraveyard().compareTo(context2.getPlayer1().getGraveyard()), 0);
	}
}
