package com.hiddenswitch.spellsource.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.demilich.metastone.game.behaviour.UtilityBehaviour;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestBase {

	protected static class TestBehaviour extends UtilityBehaviour {

		private EntityReference targetPreference;

		@Override
		public String getName() {
			return "Null Behaviour";
		}

		public EntityReference getTargetPreference() {
			return targetPreference;
		}

		@Override
		public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
			return new ArrayList<Card>();
		}

		@Override
		public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
			if (targetPreference != null) {
				for (GameAction action : validActions) {
					if (action.getTargetReference().equals(targetPreference)) {
						return action;
					}
				}
			}

			return validActions.get(0);
		}

		public void setTargetPreference(EntityReference targetPreference) {
			this.targetPreference = targetPreference;
		}

	}

	static {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.DEBUG);

		CardCatalogue.loadCardsFromPackage();
	}

	protected static void attack(GameContext context, Player player, Entity attacker, Entity target) {
		PhysicalAttackAction physicalAttackAction = new PhysicalAttackAction(attacker.getReference());
		physicalAttackAction.setTarget(target);
		context.performAction(player.getId(), physicalAttackAction);
	}

	public static DebugContext createContext(HeroClass hero1, HeroClass hero2) {
		DeckFormat deckFormat = new DeckFormat();
		for (CardSet set : CardSet.values()) {
			deckFormat.addSet(set);
		}
		Player player1 = new Player(Deck.randomDeck(hero1, DeckFormat.WILD),"Player 1");
		Player player2 = new Player(Deck.randomDeck(hero2, DeckFormat.WILD),"Player 2");
		GameLogic logic = new GameLogic();
		DebugContext context = new DebugContext(player1, player2, logic, deckFormat);
		logic.setContext(context);
		context.init();
		return context;
	}

	protected static Entity find(GameContext context, String cardId) {
		for (Player player : context.getPlayers()) {
			for (Minion minion : player.getMinions()) {
				if (minion.getSourceCard().getCardId().equals(cardId)) {
					return minion;
				}
			}
		}
		return null;
	}

	protected static Actor getSingleMinion(List<Minion> minions) {
		for (Actor minion : minions) {
			if (minion == null) {
				continue;
			}
			return minion;
		}
		return null;
	}

	protected static Minion getSummonedMinion(List<Minion> minions) {
		List<Minion> minionList = new ArrayList<>(minions);
		Collections.sort(minionList, (m1, m2) -> Integer.compare(m1.getId(), m2.getId()));
		return minionList.get(minionList.size() - 1);
	}

	protected static void playCard(GameContext context, Player player, Card card) {
		context.getLogic().receiveCard(player.getId(), card);
		context.performAction(player.getId(), card.play());
	}

	protected static void playCardWithTarget(GameContext context, Player player, Card card, Entity target) {
		context.getLogic().receiveCard(player.getId(), card);
		GameAction action = card.play();
		action.setTarget(target);
		context.performAction(player.getId(), action);
	}

	protected static Minion playMinionCard(GameContext context, Player player, Card card) {
		context.getLogic().receiveCard(player.getId(), card);
		context.performAction(player.getId(), card.play());
		return getSummonedMinion(player.getMinions());
	}

}
