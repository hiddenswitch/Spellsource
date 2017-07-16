package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.models.EventLogicRequest;
import com.hiddenswitch.spellsource.models.LogicResponse;
import com.hiddenswitch.spellsource.impl.util.PersistenceTrigger;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;

import java.util.function.Function;

/**
 * A more powerful persistent attribute handler.
 * @param <T>
 */
public interface LegacyPersistenceHandler<T extends GameEvent> {
	/**
	 * Returns the game event that triggers a change in the attribute value.
	 *
	 * @return The game event type.
	 */
	GameEventType getGameEvent();

	/**
	 * An ID by which to reference this particular handler.
	 *
	 * @return An ID.
	 */
	String getId();

	/**
	 * A handler for an event logic request. It should evaluate whether and how the attribute should be modified,
	 * given information about the game.
	 *
	 * @param request The request.
	 * @return A logic response containing attributes in the game state to change.
	 */
	@Suspendable
	LogicResponse onLogicRequest(EventLogicRequest<T> request);

	/**
	 * Converts a game event into an appropriate event logic request.
	 *
	 * @param event The event.
	 * @return An event logic request.
	 */
	@Suspendable
	EventLogicRequest<T> onGameEvent(T event);

	/**
	 * Creates a persistent attribute handler.
	 *
	 * @param id             An ID to reference the handler.
	 * @param type           The game event this handler reacts to.
	 * @param onLogicRequest The meat of the logic code that saves new stuff to the database and returns changes a
	 *                       {@link PersistenceTrigger} should make to a list of
	 *                       {@link net.demilich.metastone.game.entities.Entity} attributes. This runs locally to the
	 *                       {@link Logic} service.
	 * @param onGameEvent    A converter from a game event to a logic response that runs locally to the {@link
	 *                       Games} service.
	 * @param <R>            The type of the event.
	 * @return A handler to pass to {@link Spellsource#persistAttribute(LegacyPersistenceHandler)}.
	 */
	static <R extends GameEvent> LegacyPersistenceHandler<R> create(String id, GameEventType type,
	                                                                Function<EventLogicRequest<R>, LogicResponse> onLogicRequest, Function<R, EventLogicRequest<R>>
			onGameEvent) {
		return new LegacyPersistenceHandlerImpl<R>(type, id, onLogicRequest, onGameEvent);
	}
}