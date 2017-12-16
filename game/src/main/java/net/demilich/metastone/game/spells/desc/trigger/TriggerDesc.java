package net.demilich.metastone.game.spells.desc.trigger;

import java.io.Serializable;

import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;

public class TriggerDesc implements Serializable, Cloneable {

	public EventTriggerDesc eventTrigger;
	public SpellDesc spell;
	public boolean oneTurn;
	public boolean persistentOwner;
	public int turnDelay;
	public boolean keepAfterTransform;
	public Integer maxFires;
	public Integer countUntilCast;
	public boolean countByValue;

	public Enchantment create() {
		Enchantment trigger = new Enchantment(eventTrigger.create(), spell, oneTurn, turnDelay);
		trigger.setMaxFires(maxFires);
		trigger.setPersistentOwner(persistentOwner);
		trigger.setKeepAfterTransform(keepAfterTransform);
		trigger.setCountUntilCast(countUntilCast);
		trigger.setCountByValue(countByValue);
		return trigger;
	}

}
