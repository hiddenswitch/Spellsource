package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Logic;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.models.EventLogicRequest;
import com.hiddenswitch.spellsource.models.LogicResponse;
import com.hiddenswitch.spellsource.models.PersistAttributeRequest;
import com.hiddenswitch.spellsource.models.PersistAttributeResponse;
import com.hiddenswitch.spellsource.impl.PersistenceContextImpl;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.targeting.EntityReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An internal utility class for implementing persistence features.
 */
public class Persistence {
	private Spellsource spellsource;

	public Persistence(Spellsource spellsource) {
		this.spellsource = spellsource;
	}

	@SuppressWarnings("unchecked")
	@Suspendable
	public void persistenceTrigger(GameEvent event) {
		// First, execute the regular handlers. They will persist normally.
		for (PersistenceHandler handler1 : spellsource.getPersistAttributeHandlers().values()) {
			if (handler1.getType() != event.getEventType()) {
				continue;
			}

			handler1.getHandler().handle(new PersistenceContextImpl(event, handler1.getId(), handler1.getAttribute()));
		}
	}

}
