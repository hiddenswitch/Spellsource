package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Logic;
import com.hiddenswitch.spellsource.impl.util.PersistenceContext;
import com.hiddenswitch.spellsource.models.PersistAttributeRequest;
import com.hiddenswitch.spellsource.models.PersistAttributeResponse;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.vertx.core.Vertx;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.SetAttributeSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
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
		Entity entity = gameContext.tryFind(reference);
		if (entity == null) {
			logger.error("update: Could not find entity {} to persist {}", reference, id);
			return 0L;
		}

		// TODO: We should probably queue these calls until the end of the match, and then execute only the latest
		// value when the match is over. We don't have to save this stuff in real time. Maybe a new queued Rpc primitive?
		PersistAttributeResponse response = Logic.persistAttribute(new PersistAttributeRequest()
				.withInventoryIds(Collections.singletonList(entity.getCardInventoryId()))
				.withAttribute(attribute)
				.withNewValue(newValue));

		// TODO: Are we interested in the number of inventory items modified?

		entity.setAttribute(attribute, newValue);

		return response.getUpdated();
	}

	@Override
	public Attribute attribute() {
		return attribute;
	}
}
