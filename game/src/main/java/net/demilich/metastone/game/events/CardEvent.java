package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

/**
 * A base class for events that have cards associated with them.
 * <p>
 * Sometimes the card is the {@link #getTarget()} and sometimes it is the {@link #getSource()}. Depends on the
 * implementation.
 */
public class CardEvent extends BasicGameEvent implements HasCard {

	private final WeakReference<Card> card;

	public CardEvent(GameEvent.EventTypeEnum eventType, boolean isClientInterested, @NotNull GameContext context, Player player, Entity source, Entity target, Card card) {
		super(eventType, isClientInterested, context, player, source, target);
		this.card = new WeakReference<>(card);
	}

	public CardEvent(GameEvent.EventTypeEnum eventType, @NotNull GameContext context, Player player, Entity source, Entity target, Card card) {
		super(eventType, context, player, source, target);
		this.card = new WeakReference<>(card);
	}


	public CardEvent(GameEvent.EventTypeEnum eventType, @NotNull GameContext context, int playerId, Entity source, Entity target, Card card) {
		super(eventType, context, playerId, source, target);
		this.card = new WeakReference<>(card);
	}

	public CardEvent(GameEvent.EventTypeEnum eventType, @NotNull GameContext context, int targetPlayerId, int sourcePlayerId, Card card) {
		super(eventType, context, card, targetPlayerId, sourcePlayerId);
		this.card = new WeakReference<>(card);
	}

	public CardEvent(GameEvent.EventTypeEnum eventType, boolean isClientInterested, @NotNull GameContext context, int targetPlayerId, int sourcePlayerId, Card card) {
		super(eventType, isClientInterested, context, card, targetPlayerId, sourcePlayerId);
		this.card = new WeakReference<>(card);
	}

	public CardEvent(GameEvent.EventTypeEnum eventType, GameContext context, int targetPlayerId, int sourcePlayerId, Card card, Entity target) {
		super(eventType, context, target, targetPlayerId, sourcePlayerId);
		this.card = new WeakReference<>(card);
	}

	@Override
	public final Card getSourceCard() {
		return card.get();
	}
}
