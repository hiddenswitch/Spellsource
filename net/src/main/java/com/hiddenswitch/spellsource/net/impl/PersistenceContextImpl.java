package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.Logic;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.impl.util.PersistenceContext;
import com.hiddenswitch.spellsource.net.models.PersistAttributeRequest;
import com.hiddenswitch.spellsource.net.models.PersistAttributeResponse;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Vertx;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bberman on 6/7/17.
 */
public class PersistenceContextImpl<T extends GameEvent> implements PersistenceContext<T> {
	private static Logger logger = LoggerFactory.getLogger(PersistenceContext.class);
	private final String id;
	private final T event;
	private final Attribute attribute;

	@Override
	@Suspendable
	public T event() {
		return event;
	}

	public PersistenceContextImpl(T event, String id, Attribute attribute) {
		this.id = id;
		this.event = event;
		this.attribute = attribute;
	}

	@Override
	@Suspendable
	public long update(EntityReference reference, Object newValue) {
		if (Vertx.currentContext() == null) {
			logger.error("update: Not in vertx context for {}", id);
			return 0L;
		}

		if (!Fiber.isCurrentFiber()) {
			logger.error("update: Not in fiber for {}", id);
			return 0L;
		}

		final GameContext gameContext = event().getGameContext();

		Player activePlayer;
		Tracer tracer = GlobalTracer.get();
		Tracer.SpanBuilder spanBuilder = tracer.buildSpan("PersistenceContextImpl/update")
				.withTag("id", id)
				.withTag("attribute", attribute.toKeyCase())
				.withTag("gameEvent", event.getEventType().toString())
				.withTag(Tags.SAMPLING_PRIORITY, 0)
				.withTag("value", newValue == null ? "" : newValue.toString());

		if (gameContext instanceof Server) {
			final Server server = (Server) gameContext;
			spanBuilder.asChildOf(server.getSpanContext());
		}

		Span span = spanBuilder
				.start();

		try {
			try {
				if (event().getTargetPlayerId() != -1) {
					// Game isn't ready yet, try to recover gracefully here
					activePlayer = gameContext.getPlayer(event().getTargetPlayerId());
				} else if (gameContext.getActivePlayerId() != -1) {
					activePlayer = gameContext.getActivePlayer();
				} else {
					throw new NullPointerException("invalid player");
				}
			} catch (NullPointerException | IndexOutOfBoundsException ex) {
				try {
					// Game is over
					if (gameContext.updateAndGetGameOver()) {
						logger.warn("update: Trying to update an entity in a game that's over.");
						return 0L;
					} else {
						activePlayer = gameContext.getPlayer(event().getTargetPlayerId());
					}
				} catch (Throwable other) {
					logger.error("update: Game isn't sufficiently ready yet to do an update for {}", id);
					return 0L;
				}
			}

			List<Entity> entities = gameContext.resolveTarget(activePlayer, event().getEventSource(), reference);
			if (entities == null || entities.isEmpty()) {
				return 0L;
			}

			List<String> inventoryIds = new ArrayList<>();
			for (Entity entity1 : entities) {
				if (entity1 != null) {
					if (entity1.hasPersistentEffects()) {
						String cardInventoryId = entity1.getCardInventoryId();
						if (cardInventoryId != null) {
							inventoryIds.add(cardInventoryId);
						}
					}
				}
			}

			if (inventoryIds.isEmpty()) {
				return 0L;
			}

			// TODO: We should probably queue these calls until the end of the match, and then execute only the latest
			// value when the match is over. We don't have to save this stuff in real time. Maybe a new queued Rpc primitive?
			PersistAttributeResponse response = Logic.persistAttribute(new PersistAttributeRequest()
					.withInventoryIds(inventoryIds)
					.withAttribute(attribute)
					.withNewValue(newValue));

			// TODO: Are we interested in the number of inventory items modified?

			for (Entity entity : entities) {
				entity.setAttribute(attribute, newValue);
			}

			return response.getUpdated();
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			return 0L;
		} finally {
			span.finish();
		}
	}

	@Override
	public Attribute attribute() {
		return attribute;
	}
}
