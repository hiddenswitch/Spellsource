package com.hiddenswitch.minionate;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Logic;
import com.hiddenswitch.proto3.net.models.EventLogicRequest;
import com.hiddenswitch.proto3.net.models.LogicResponse;
import com.hiddenswitch.proto3.net.models.PersistAttributeRequest;
import com.hiddenswitch.proto3.net.models.PersistAttributeResponse;
import com.hiddenswitch.proto3.net.util.RpcClient;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.SetAttributeSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An internal utility class for implementing persistence features.
 */
public class Persistence {
	private Minionate minionate;

	Persistence(Minionate minionate) {
		this.minionate = minionate;
	}

	@SuppressWarnings("unchecked")
	@Suspendable
	public void persistenceTrigger(RpcClient<Logic> logic, GameEvent event) {
		// First, execute the regular handlers. They will persist normally.
		for (PersistenceHandler handler1 : minionate.persistAttributeHandlers.values()) {
			if (handler1.getType() != event.getEventType()) {
				continue;
			}

			handler1.getHandler().handle(new PersistenceContextImpl(event, logic, handler1.getId(), handler1.getAttribute()));
		}

		// Now, execute the legacy handlers.
		List<LogicResponse> responses = new ArrayList<>();
		for (LegacyPersistenceHandler handler2 : minionate.legacyPersistenceHandlers.values()) {
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

		for (LogicResponse response : responses) {
			GameContext context = event.getGameContext();
			for (Map.Entry<EntityReference, Map<Attribute, Object>> entry : response.getModifiedAttributes()
					.entrySet()) {

				Entity entity = context.tryFind(entry.getKey());

				if (entity == null) {
					continue;
				}

				for (Map.Entry<Attribute, Object> kv : entry.getValue().entrySet()) {
					SpellDesc spell = SetAttributeSpell.create(entry.getKey(), kv.getKey(), kv.getValue());
					// By setting childSpell to true, additional spell casting triggers don't get called
					// But target overriding effects apply, as they should.
					context.getLogic().castSpell(entity.getOwner(), spell, entity.getReference(), null, true);
				}
			}
		}
	}

	public LegacyPersistenceHandler getLogicHandler(String id) {
		return minionate.legacyPersistenceHandlers.get(id);
	}
}
