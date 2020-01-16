package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.TycheBehaviour;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.fibers.SuspendableGameContext;
import net.demilich.metastone.game.statistics.SimulationResult;
import net.demilich.metastone.game.statistics.Statistic;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TycheBehaviourTest extends TestBase implements Serializable {

	private static Logger LOGGER = LoggerFactory.getLogger(TycheBehaviourTest.class);

	@Test
	@Disabled
	public void testTycheBehaviour() {
		SuspendableGameContext context = new SuspendableGameContext(Deck.randomDeck(), Deck.randomDeck());
		context.play();
		context.setMulligan(context.getActivePlayerId(), Collections.emptyList());
		context.setMulligan(context.getNonActivePlayerId(), Collections.emptyList());
		TycheBehaviour behaviour1 = new TycheBehaviour();
		TycheBehaviour behaviour2 = new TycheBehaviour();
		int i = 10;
		while (!context.updateAndGetGameOver() && i > 0) {
			TycheBehaviour behaviour = context.getActivePlayerId() == 1 ? behaviour2 : behaviour1;
			GameAction action = behaviour.requestAction(context, context.getActivePlayer(), context.getValidActions());
			context.performAction(context.getActivePlayerId(), action);
			i--;
		}
	}

	@Test
	@Disabled
	public void testTycheBehaviourHandlesOwnSuspendableGameContext() {
		GameContext context = new GameContext();
		context.setDeck(0, Deck.randomDeck());
		context.setDeck(1, Deck.randomDeck());
		context.setBehaviour(0, new GameStateValueBehaviour());
		context.setBehaviour(0, new TycheBehaviour());
		context.play();
		assertTrue(context.updateAndGetGameOver());
	}

	@Test
	@Disabled("No more midrange shaman")
	public void testMidrangeShamanMirrorMatch() {
		List<GameDeck> decks = Collections.singletonList(TycheBehaviour.midrangeShaman().toGameDeck());
		SimulationResult res = GameContext.simulate(decks, TycheBehaviour::new, () -> {
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			behaviour.setMaxDepth(4);
			behaviour.setTimeout(8000);
			behaviour.setLethalTimeout(15000);
			behaviour.setParallel(false);
			return behaviour;
		}, 2, true, true);
		LOGGER.info("testMidrangeShaman: TycheBehaviour winrate was {}", res.getPlayer1Stats().get(Statistic.WIN_RATE));
	}

	@Test
	@Disabled
	public void testCorrectOrder() {
		runGym((context, player, opponent) -> {
			TycheBehaviour checkDepth = new TycheBehaviour();
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
			assertNull(player.getHero().getWeapon());
			assertTrue(context.updateAndGetGameOver());
		}, "BLACK", "BLACK");
	}
}
