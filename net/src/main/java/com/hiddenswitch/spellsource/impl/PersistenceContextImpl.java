package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Logic;
import com.hiddenswitch.spellsource.impl.util.PersistenceContext;
import com.hiddenswitch.spellsource.models.PersistAttributeRequest;
import com.hiddenswitch.spellsource.models.PersistAttributeResponse;
import com.hiddenswitch.spellsource.util.RpcClient;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.SetAttributeSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by bberman on 6/7/17.
 */
public class PersistenceContextImpl<T extends GameEvent> implements PersistenceContext<T> {
	private T event;
	private RpcClient<Logic> logic;
	private Attribute attribute;

	@Override
	@Suspendable
	public T event() {
		return event;
	}

	public PersistenceContextImpl(T event, RpcClient<Logic> logic, String id, Attribute attribute) {
		this.event = event;
		this.logic = logic;
		this.attribute = attribute;
	}

	@Override
	@Suspendable
	public long update(EntityReference reference, Object newValue) {
		final GameContext gameContext = event().getGameContext();
		List<Entity> entities = gameContext.resolveTarget(gameContext.getActivePlayer(), event().getEventSource(), reference);
		if (entities == null || entities.isEmpty()) {
			return 0L;
		}

		List<String> inventoryIds = entities.stream()
				.filter(Entity::hasPersistentEffects)
				.map(Entity::getCardInventoryId)
				.filter(ci -> ci != null).collect(Collectors.toList());

		if (inventoryIds.isEmpty()) {
			return 0L;
		}

		// TODO: We should probably queue these calls until the end of the match, and then execute only the latest
		// value when the match is over. We don't have to save this stuff in real time. Maybe a new queued Rpc primitive?
		PersistAttributeResponse response = logic.uncheckedSync().persistAttribute(new PersistAttributeRequest()
				.withInventoryIds(inventoryIds)
				.withAttribute(attribute)
				.withNewValue(newValue));

		// TODO: Are we interested in the number of inventory items modified?

		for (Entity entity : entities) {
			SpellDesc spell = SetAttributeSpell.create(entity.getReference(), attribute, newValue);
			// By setting childSpell to true, additional spell casting triggers don't get called
			// But target overriding effects apply, as they should.
			gameContext.getLogic().castSpell(entity.getOwner(), spell, entity.getReference(), null, true);
		}

		return response.getUpdated();
	}

	@Override
	public Attribute attribute() {
		return attribute;
	}

	@Override
	@Suspendable
	public Logic logic() {
		return logic.uncheckedSync();
	}
}
