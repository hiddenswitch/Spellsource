package com.hiddenswitch.minionate;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Logic;
import com.hiddenswitch.proto3.net.PersistAttributeRequest;
import com.hiddenswitch.proto3.net.PersistAttributeResponse;
import com.hiddenswitch.proto3.net.models.EventLogicRequest;
import com.hiddenswitch.proto3.net.models.LogicResponse;
import com.hiddenswitch.proto3.net.util.RpcClient;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;

import java.util.*;
import java.util.function.Function;

/**
 * The Minionate Server API. Access it with {@link Minionate#minionate()}.
 * <p>
 * This class provides an easy way to provide a new persist attribute with {@link #persistAttribute(PersistAttributeHandler2)}.
 * <p>
 * It will provide more APIs for features in the future.
 */
public class Minionate {
	private static Minionate instance;
	private Map<String, PersistAttributeHandler2> persistAttributeHandlers2 = new HashMap<>();

	private Minionate() {
	}

	/**
	 * Gets a reference to the Minionate Server API.
	 *
	 * @return An API instance.
	 */
	public static Minionate minionate() {
		if (instance == null) {
			instance = new Minionate();
		}

		return instance;
	}

	public <T extends GameEvent> void persistAttribute(Attribute attribute, GameEventType eventType,
	                                                   PersistAttributeHandler<T> handler) {
		throw new UnsupportedOperationException("Persisting an attribute with the easy API is not ready yet.");
	}

	/**
	 * @param legacyHandler A handler for game events and logic requests.
	 *                      See {@link PersistAttributeHandler2#create(String, GameEventType, Function, Function)}
	 *                      for an easy way to create this handler.
	 * @param <T>           The event type.
	 */
	public <T extends GameEvent> void persistAttribute(PersistAttributeHandler2<T> legacyHandler) {
		persistAttributeHandlers2.put(legacyHandler.getId(), legacyHandler);
	}

	/**
	 * Access non-client features required to implement the persistence features.
	 *
	 * @return A {@link Persistence} utility.
	 */
	public Persistence persistence() {
		return new Persistence();
	}

	@FunctionalInterface
	public interface PersistAttributeHandler<T extends GameEvent> {
		@Suspendable
		void onGameEvent(T gameEvent);
	}

	/**
	 * An internal utility class for implementing persistence features.
	 */
	public class Persistence {
		@SuppressWarnings("unchecked")
		@Suspendable
		public List<LogicResponse> persistenceTrigger(RpcClient<Logic> logic, GameEvent event) {
			List<LogicResponse> responses = new ArrayList<>();
			for (PersistAttributeHandler2 handler2 : persistAttributeHandlers2.values()) {
				if (!handler2.getGameEvent().equals(event.getEventType())) {
					continue;
				}

				EventLogicRequest request = handler2.onGameEvent(event);

				if (request == null) {
					continue;
				}

				PersistAttributeResponse response = logic.uncheckedSync().persistAttribute(new
						PersistAttributeRequest()
						.withId(handler2.getId()).withRequest(request));

				if (response.getLogicResponse() != null) {
					responses.add(response.getLogicResponse());
				}
			}
			return responses;
		}

		public PersistAttributeHandler2 getHandler2(String id) {
			return persistAttributeHandlers2.get(id);
		}
	}
}
