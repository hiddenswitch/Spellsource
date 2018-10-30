package com.hiddenswitch.spellsource.impl.util;

import com.github.fromage.quasi.fibers.Suspendable;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.impl.PersistenceContextImpl;
import net.demilich.metastone.game.events.GameEvent;

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
