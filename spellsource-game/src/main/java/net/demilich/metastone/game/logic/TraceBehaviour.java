package net.demilich.metastone.game.logic;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.UtilityBehaviour;
import net.demilich.metastone.game.cards.Card;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class TraceBehaviour extends UtilityBehaviour {
	private final int playerId;
	private final List<MulliganTrace> mulligans;
	private final AtomicInteger nextAction;
	private final List<Integer> actions;
	private final Consumer<GameContext> recorder;

	/**
	 * @param playerId                   The player that this behaviour is "playing" for.
	 * @param mulligans                  The list of mulligans from the traced game.
	 * @param nextAction                 The index of the next action to take (shared between two {@link
	 *                                   TraceBehaviour}s).
	 * @param actions                    The list of action (indices) from the traced game.
	 * @param beforeRequestActionHandler [Optional] consumer to be called on every {@link GameContext} before each action
	 *                                   is taken.
	 */
	TraceBehaviour(
			int playerId,
			@Nullable List<MulliganTrace> mulligans,
			AtomicInteger nextAction,
			List<Integer> actions,
			@Nullable Consumer<GameContext> beforeRequestActionHandler) {
		this.actions = actions;
		this.mulligans = mulligans;
		this.nextAction = nextAction;
		this.playerId = playerId;
		this.recorder = beforeRequestActionHandler;
	}

	@Override
	public String getName() {
		return "Trace Behaviour";
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) throws CancellationException {
		if (recorder != null) {
			recorder.accept(context);
		}
		if (mulligans == null || mulligans.isEmpty()) {
			throw new CancellationException();
		}
		int id = player.getId();
		int index = 0;
		if (mulligans.get(1).getPlayerId() == id) {
			index = 1;
		}
		if (playerId != id) {
			return Collections.emptyList();
		}
		return mulligans.get(index)
				.getEntityIds()
				.stream()
				.map(i -> {
					Optional<Card> card = cards
							.stream()
							.filter(c -> c.getId() == i)
							.findFirst();
					if (card.isPresent()) {
						return card.get();
					} else {
						throw new NullPointerException();
					}
				})
				.collect(Collectors.toList());
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) throws CancellationException {
		if (recorder != null) {
			recorder.accept(context);
		}
		if (playerId != player.getId()) {
			return null;
		}
		int i = nextAction.getAndIncrement();
		if (actions == null) {
			throw new CancellationException();
		}
		if (i >= actions.size()) {
			throw new CancellationException();
		}
		Integer j = actions.get(i);
		return validActions.stream().filter(f -> f.getId() == j).findFirst().orElseThrow(() -> {
			return new CancellationException();
		});
	}
}
