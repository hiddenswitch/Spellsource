package net.demilich.metastone.game.spells.trigger;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.MaxManaChangedEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class MaxManaChangedTrigger extends EventTrigger {
    public MaxManaChangedTrigger(EventTriggerDesc desc) {
        super(desc);
    }

    @Override
    @Suspendable
    protected boolean fire(GameEvent event, Entity host) {
        return true;
    }

    @Override
    public GameEventType interestedIn() {
        return GameEventType.MAX_MANA;
    }
}
