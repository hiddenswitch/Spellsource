package com.hiddenswitch.minionate;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Created by bberman on 6/7/17.
 */
public interface PersistenceContext<T extends GameEvent> {
	T event();

	@Suspendable
	long update(EntityReference reference, Object newValue);
}
