package net.demilich.metastone.game.cards.desc;

import com.google.common.collect.Sets;
import net.demilich.metastone.game.spells.desc.RogueInfoDescArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.immutableEntry;

public final class RogueInfoDesc implements Serializable, HasEntrySet<RogueInfoDescArg, Object>, Cloneable {

	private float weight;
	private EnchantmentDesc[] triggers = new EnchantmentDesc[0];
	private SpellDesc chosenSpell;

	@Override
	@SuppressWarnings("unchecked")
	public Set<Map.Entry<RogueInfoDescArg, Object>> entrySet() {
		return Sets.newHashSet(
				immutableEntry(RogueInfoDescArg.TRIGGERS, triggers),
				immutableEntry(RogueInfoDescArg.WEIGHT, weight),
				immutableEntry(RogueInfoDescArg.WEIGHT, weight)
		);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		try {
			var desc = (RogueInfoDesc) super.clone();

			if (chosenSpell != null) desc.chosenSpell = chosenSpell.clone();

			return desc;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}


	public EnchantmentDesc[] getTriggers() {
		return triggers;
	}

	public void setTriggers(EnchantmentDesc[] triggers) {
		this.triggers = triggers;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public SpellDesc getChosenSpell() {
		return chosenSpell;
	}

	public void setChosenSpell(SpellDesc chosenSpell) {
		this.chosenSpell = chosenSpell;
	}
}
