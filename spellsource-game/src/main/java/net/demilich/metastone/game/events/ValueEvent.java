package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * An event that contains a value.
 */
public class ValueEvent extends BasicGameEvent implements HasValue {

	private final int value;

	public ValueEvent(GameEventType eventType, boolean isClientInterested, @NotNull GameContext context, Player player, Entity source, Entity target, int value) {
		super(eventType, isClientInterested, context, player, source, target);
		this.value = value;
	}

	public ValueEvent(GameEventType eventType, @NotNull GameContext context, Player player, Entity source, Entity target, int value) {
		super(eventType, context, player, source, target);
		this.value = value;
	}

	public ValueEvent(GameEventType eventType, @NotNull GameContext context, int playerId, Entity source, Entity target, int value) {
		super(eventType, context, playerId, source, target);
		this.value = value;
	}

	public ValueEvent(GameEventType eventType, @NotNull GameContext context, int targetPlayerId, int sourcePlayerId, Entity target, int value) {
		super(eventType, context, target, targetPlayerId, sourcePlayerId);
		this.value = value;
	}

	public ValueEvent(GameEventType eventType, boolean isClientInterested, GameContext context, int targetPlayerId, int sourcePlayerId, Entity target, int value) {
		super(eventType, isClientInterested, context, target, targetPlayerId, sourcePlayerId);
		this.value = value;
	}

	@Override
	public int getValue() {
		return value;
	}
}
