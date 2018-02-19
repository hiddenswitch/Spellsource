package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.impl.util.Spell;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

import java.io.Serializable;

public class Trigger implements Serializable {
	private final EventTriggerDesc eventTriggerDesc;
	private final String spellId;

	public Trigger(EventTriggerDesc eventTriggerDesc, String spellId) {
		this.eventTriggerDesc = eventTriggerDesc;
		this.spellId = spellId;
	}

	public EventTriggerDesc getEventTriggerDesc() {
		return eventTriggerDesc;
	}

	public String getSpellId() {
		return spellId;
	}

	public Spell getSpell() {
		return Spellsource.spellsource().getSpells().get(getSpellId());
	}
}
