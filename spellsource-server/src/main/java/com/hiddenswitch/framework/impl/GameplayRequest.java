package com.hiddenswitch.framework.impl;

import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Models an in-flight server prompt requesting a choice from a player (either a mulligan or a game action),
 * unifying dispatch, reconnect re-emission, response validation, and timeout fallback.
 */
public abstract class GameplayRequest<T> {
	private final String callbackId;
	private final Consumer<T> callback;

	protected GameplayRequest(String callbackId, Consumer<T> callback) {
		this.callbackId = Objects.requireNonNull(callbackId, "callbackId");
		this.callback = Objects.requireNonNull(callback, "callback");
	}

	public final String getCallbackId() {
		return callbackId;
	}

	public final void complete(T result) {
		callback.accept(result);
	}

	public final void elapse(Random random) {
		complete(defaultChoice(random));
	}

	public abstract T defaultChoice(Random random);

	public abstract void send(Client client, GameState state);

	/**
	 * A prompt asking a player to select which starter cards to discard during the mulligan phase.
	 */
	public static final class Mulligan extends GameplayRequest<List<Card>> {
		private final List<Card> starterCards;

		public Mulligan(String callbackId, List<Card> starterCards, Consumer<List<Card>> callback) {
			super(callbackId, callback);
			this.starterCards = starterCards;
		}

		public List<Card> getStarterCards() {
			return starterCards;
		}

		@Override
		public List<Card> defaultChoice(Random random) {
			return new ArrayList<>();
		}

		@Override
		public void send(Client client, GameState state) {
			client.onMulligan(getCallbackId(), state, starterCards, client.getPlayerId());
		}

		public void resolveIndices(List<Integer> discardedCardIndices) {
			List<Card> discardedCards = (discardedCardIndices == null ? Collections.<Integer>emptyList() : discardedCardIndices)
					.stream()
					.filter(Objects::nonNull)
					.distinct()
					.flatMap(i -> (starterCards == null || i < 0 || i >= starterCards.size())
							? Stream.empty()
							: Stream.of(starterCards.get(i)))
					.collect(toList());
			complete(discardedCards);
		}
	}

	/**
	 * A prompt asking a player to select a {@link GameAction} during their turn.
	 */
	public static final class Action extends GameplayRequest<GameAction> {
		private final List<GameAction> actions;

		public Action(String callbackId, List<GameAction> actions, Consumer<GameAction> callback) {
			super(callbackId, callback);
			this.actions = actions;
		}

		public List<GameAction> getActions() {
			return actions;
		}

		@Override
		public GameAction defaultChoice(Random random) {
			var r = random != null ? random : ThreadLocalRandom.current();
			return actions.stream()
					.filter(ga -> ga.getActionType() == ActionType.END_TURN)
					.findFirst()
					.orElseGet(() -> actions.get(r.nextInt(actions.size())));
		}

		@Override
		public void send(Client client, GameState state) {
			client.onRequestAction(getCallbackId(), state, actions);
		}

		public boolean isValidIndex(int actionIndex) {
			return actions != null && actionIndex >= 0 && actionIndex < actions.size();
		}

		public void resolveIndex(int actionIndex) {
			complete(actions.get(actionIndex));
		}
	}
}
