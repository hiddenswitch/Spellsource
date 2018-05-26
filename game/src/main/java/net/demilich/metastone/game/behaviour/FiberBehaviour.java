package net.demilich.metastone.game.behaviour;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.QueueChannel;
import co.paralleluniverse.strands.channels.QueueObjectChannel;
import co.paralleluniverse.strands.queues.SingleConsumerLinkedObjectQueue;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * A behaviour that will suspend until {@link #setAction(GameAction)} and {@link #setMulligan(List)} are called.
 */
public class FiberBehaviour extends UtilityBehaviour {
	private QueueChannel<GameAction> requestActionResult = new QueueObjectChannel<>(new SingleConsumerLinkedObjectQueue<>(), Channels.OverflowPolicy.THROW, true, true);
	private QueueChannel<List<Card>> mulliganResult = new QueueObjectChannel<>(new SingleConsumerLinkedObjectQueue<>(), Channels.OverflowPolicy.THROW, true, true);
	private List<GameAction> validActions = new ArrayList<>();
	private List<Card> mulliganCards = new ArrayList<>();

	@Override
	public String getName() {
		return "Fiber behaviour";
	}

	@Override
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		this.mulliganCards = new ArrayList<>(cards);
		try {
			return mulliganResult.receive();
		} catch (SuspendExecution | InterruptedException execution) {
			throw new RuntimeException(execution);
		} finally {
			mulliganCards.clear();
		}
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		this.validActions = new ArrayList<>(validActions);
		try {
			return requestActionResult.receive();
		} catch (SuspendExecution | InterruptedException execution) {
			throw new RuntimeException(execution);
		} finally {
			this.validActions.clear();
		}
	}

	public List<GameAction> getValidActions() {
		return validActions;
	}

	public List<Card> getMulliganCards() {
		return mulliganCards;
	}

	@Suspendable
	public void setAction(GameAction action) throws InterruptedException, SuspendExecution {
		requestActionResult.send(action);
	}

	@Suspendable
	public void setMulligan(List<Card> cardsToDiscard) throws InterruptedException, SuspendExecution {
		mulliganResult.send(cardsToDiscard);
	}
}
