package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Logic;
import com.hiddenswitch.spellsource.Spellsource;
import io.vertx.core.Handler;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * A context for the persistence API.
 * <p>
 * Use this context to get information about the {@link GameEvent}, including the {@link
 * net.demilich.metastone.game.GameContext} where the event was fired; and, to update inventory items corresponding to
 * an entity on the board (i.e., the card).
 *
 * @param <T> The {@link GameEvent} class that this {@link Spellsource#persistAttribute(String, GameEventType, Attribute,
 *            Handler)} handler argument is handling.
 */
public interface PersistenceContext<T extends GameEvent> {
	/**
	 * A reference to the event. Access the {@link net.demilich.metastone.game.GameContext} with {@link
	 * GameEvent#getGameContext()}. Note, you can mutate the game context and entities with the referenced game context,
	 * since it is the actual context running the game.
	 *
	 * @return An event.
	 */
	@Suspendable
	T event();

	/**
	 * Updates the entity (or entities) pointed to by the reference with a new value for {@link #attribute()} this
	 * context handles.
	 *
	 * @param reference An {@link EntityReference}. Typically, you will use {@link EntityReference#pointTo(Entity)} to
	 *                  create a reference that points to a specific entity; but, you may also use references like
	 *                  {@link EntityReference#ALL_MINIONS} to update an attribute on all minions currently on the
	 *                  board. See {@link net.demilich.metastone.game.logic.TargetLogic#resolveTargetKey(GameContext,
	 *                  Player, Entity, EntityReference)} for the underlying logic of how an {@link EntityReference} is
	 *                  interpreted.
	 * @param newValue  The new value for the attribute.
	 * @return The number of inventory records that were updated.
	 */
	@Suspendable
	long update(EntityReference reference, Object newValue);

	/**
	 * The attribute this handler was configured to update.
	 *
	 * @return The attribute.
	 */
	Attribute attribute();

	/**
	 * Make a call to the {@link Logic} service. Make sure to get a new {@link Logic} from this method this every time
	 * you want to make a new call.
	 *
	 * @return A {@link Logic} that's good for one call.
	 */
	@Suspendable
	Logic logic();
}
