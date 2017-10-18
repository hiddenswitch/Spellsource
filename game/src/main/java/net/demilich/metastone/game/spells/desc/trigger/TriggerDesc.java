package net.demilich.metastone.game.spells.desc.trigger;

import java.io.Serializable;

import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;

public class TriggerDesc implements Serializable {

	public EventTriggerDesc eventTrigger;
	public SpellDesc spell;
	public boolean oneTurn;
	public boolean persistentOwner;
	public int turnDelay;

	public Enchantment create() {
		Enchantment trigger = new Enchantment(eventTrigger.create(), spell, oneTurn, turnDelay);
		trigger.setPersistentOwner(persistentOwner);
		return trigger;
	}

}
