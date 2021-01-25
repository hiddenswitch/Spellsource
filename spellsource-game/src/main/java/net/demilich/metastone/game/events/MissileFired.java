package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An effect causes a missile to be fired.
 * <p>
 * This is typically rendered with a delay in the client.
 */
public final class MissileFired extends BasicGameEvent {

	private final List<WeakReference<Entity>> targets;

	public MissileFired(@NotNull GameContext context, int playerId, Entity source, List<Entity> targets) {
		super(GameEventType.MISSILE_FIRED, true, context, source, null, -1, playerId);
		this.targets = targets.stream().map(WeakReference::new).collect(Collectors.toList());
	}

	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		return targets.stream().map(Reference::get).collect(Collectors.toList());
	}
}