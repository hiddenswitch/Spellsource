package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Construct and fire this event from an {@link GameEventType} and the desired {@code source} and {@code target}.
 */
public class BasicGameEvent extends GameEvent {
	private final GameEventType eventType;
	private final boolean isClientInterested;

	public BasicGameEvent(GameEventType eventType, boolean isClientInterested, @NotNull GameContext context, Player player, Entity source, Entity target) {
		super(context, player, source, target);
		this.eventType = eventType;
		this.isClientInterested = isClientInterested;
	}

	public BasicGameEvent(GameEventType eventType, @NotNull GameContext context, Player player, Entity source, Entity target) {
		super(context, player, source, target);
		this.eventType = eventType;
		this.isClientInterested = false;
	}

	public BasicGameEvent(GameEventType eventType, @NotNull GameContext context, int playerId, Entity source, Entity target) {
		super(context, context.getPlayer(playerId), source, target);
		this.eventType = eventType;
		this.isClientInterested = false;
	}

	public BasicGameEvent(GameEventType typeEnum, GameContext context, int targetPlayerId, int sourcePlayerId) {
		super(context, null, null, sourcePlayerId, targetPlayerId);
		eventType = typeEnum;
		isClientInterested = false;
	}

	public BasicGameEvent(GameEventType typeEnum, GameContext context, Entity target, int targetPlayerId, int sourcePlayerId) {
		super(context, context.getPlayer(targetPlayerId), target, sourcePlayerId, targetPlayerId);
		eventType = typeEnum;
		isClientInterested = false;
	}

	public BasicGameEvent(GameEventType typeEnum, GameContext context, Entity source, Entity target, int targetPlayerId, int sourcePlayerId) {
		super(context, source, target, sourcePlayerId, targetPlayerId);
		eventType = typeEnum;
		isClientInterested = false;
	}

	public BasicGameEvent(GameEventType typeEnum, boolean isClientInterested, GameContext context, Entity source, Entity target, int targetPlayerId, int sourcePlayerId) {
		super(context, source, target, sourcePlayerId, targetPlayerId);
		eventType = typeEnum;
		this.isClientInterested = isClientInterested;
	}

	public BasicGameEvent(GameEventType typeEnum, boolean isClientInterested, GameContext context, Entity target, int targetPlayerId, int sourcePlayerId) {
		super(context, context.getPlayer(targetPlayerId), target, sourcePlayerId, targetPlayerId);
		eventType = typeEnum;
		this.isClientInterested = isClientInterested;
	}

	@Override
	public final GameEventType getEventType() {
		return eventType;
	}

	@Override
	public final boolean isClientInterested() {
		return isClientInterested;
	}
}

