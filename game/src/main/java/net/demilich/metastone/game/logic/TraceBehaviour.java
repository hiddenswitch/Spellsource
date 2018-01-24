package net.demilich.metastone.game.logic;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.AbstractBehaviour;
import net.demilich.metastone.game.cards.Card;
import org.apache.commons.math3.exception.NullArgumentException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class TraceBehaviour extends AbstractBehaviour {
	private int playerId;
	private int[][] mulligans;
	private AtomicInteger nextAction;
	private List<Integer> actions;

	TraceBehaviour(int playerId, int[][] mulligans, AtomicInteger nextAction, List<Integer> actions) {
		this.playerId = playerId;
		this.mulligans = mulligans;
		this.nextAction = nextAction;
		this.actions = actions;
	}

	@Override
	public String getName() {
		return "Trace Behaviour";
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		if (playerId != player.getId()) {
			return Collections.emptyList();
		}
		return Arrays.stream(mulligans[player.getId()])
				.boxed()
				.map(i -> cards.stream().filter(c -> c.getId() == i).findFirst().orElseThrow(NullPointerException::new)).collect(Collectors.toList());
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		if (playerId != player.getId()) {
			return null;
		}
		int i = nextAction.getAndIncrement();
		if (i >= actions.size()) {
			throw new CancellationException();
		}
		Integer j = actions.get(i);
		return validActions.stream().filter(f -> f.getId() == j).findFirst().orElseThrow(NullPointerException::new);
	}
}
