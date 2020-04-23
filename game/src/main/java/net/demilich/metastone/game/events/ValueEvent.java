package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import org.jetbrains.annotations.NotNull;

/**
 * An event that contains a value.
 */
public class ValueEvent extends BasicGameEvent implements HasValue {

	private final int value;

	public ValueEvent(GameEvent.EventTypeEnum eventType, boolean isClientInterested, @NotNull GameContext context, Player player, Entity source, Entity target, int value) {
		super(eventType, isClientInterested, context, player, source, target);
		this.value = value;
	}

	public ValueEvent(GameEvent.EventTypeEnum eventType, @NotNull GameContext context, Player player, Entity source, Entity target, int value) {
		super(eventType, context, player, source, target);
		this.value = value;
	}

	public ValueEvent(GameEvent.EventTypeEnum eventType, @NotNull GameContext context, int playerId, Entity source, Entity target, int value) {
		super(eventType, context, playerId, source, target);
		this.value = value;
	}

	public ValueEvent(GameEvent.EventTypeEnum eventType, @NotNull GameContext context, int targetPlayerId, int sourcePlayerId, Entity target, int value) {
		super(eventType, context, target, targetPlayerId, sourcePlayerId);
		this.value = value;
	}

	public ValueEvent(GameEvent.EventTypeEnum eventType, boolean isClientInterested, GameContext context, int targetPlayerId, int sourcePlayerId, Entity target, int value) {
		super(eventType, isClientInterested, context, target, targetPlayerId, sourcePlayerId);
		this.value = value;
	}

	@Override
	public int getValue() {
		return value;
	}
}
