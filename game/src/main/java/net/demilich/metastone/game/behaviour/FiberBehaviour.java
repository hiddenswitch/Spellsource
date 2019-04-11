package net.demilich.metastone.game.behaviour;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Condition;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.QueueChannel;
import co.paralleluniverse.strands.channels.QueueObjectChannel;
import co.paralleluniverse.strands.queues.SingleConsumerLinkedObjectQueue;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A behaviour that will suspend until {@link #setAction(GameAction)} and {@link #setMulligan(List)} are called.
 * <p>
 * The underlying implementation relies on channels.
 */
public class FiberBehaviour extends UtilityBehaviour {
	private Condition next;
	private List<GameAction> validActions;
	private QueueChannel<GameAction> requestActionResult = new QueueObjectChannel<>(new SingleConsumerLinkedObjectQueue<>(), Channels.OverflowPolicy.THROW, true, true);
	private QueueChannel<List<Card>> mulliganResult = new QueueObjectChannel<>(new SingleConsumerLinkedObjectQueue<>(), Channels.OverflowPolicy.THROW, true, true);
	private List<Card> mulliganCards = new ArrayList<>();
	private transient GameContext context;
	private transient int playerId;

	public FiberBehaviour(Condition next) {
		super();
		this.next = next;
	}

	public FiberBehaviour() {
		this(null);
	}

	@Override
	public String getName() {
		return "Fiber behaviour";
	}

	@Override
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		this.context = context;
		this.playerId = player.getId();
		this.mulliganCards = new ArrayList<>(cards);
		try {
			if (next != null) {
				next.signalAll();
			}
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
		this.context = context;
		this.playerId = player.getId();
		this.validActions = validActions;
		try {
			if (next != null) {
				next.signalAll();
			}
			return requestActionResult.receive();
		} catch (SuspendExecution | InterruptedException execution) {
			throw new RuntimeException(execution);
		}
	}

	public List<GameAction> getValidActions() {
		return validActions == null ? Collections.emptyList() : validActions;
	}

	public List<Card> getMulliganCards() {
		return mulliganCards;
	}

	@Suspendable
	public void setAction(GameAction action) throws InterruptedException, SuspendExecution {
		requestActionResult.send0(action, true, false, 0);
	}

	@Suspendable
	public void setMulligan(List<Card> cardsToDiscard) throws InterruptedException, SuspendExecution {
		mulliganResult.send0(cardsToDiscard, true, false, 0);
	}

	public Condition getNext() {
		return next;
	}

	public FiberBehaviour setNext(Condition next) {
		this.next = next;
		return this;
	}
}
