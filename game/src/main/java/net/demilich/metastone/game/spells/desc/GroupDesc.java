package net.demilich.metastone.game.spells.desc;

import java.util.EnumMap;
import java.util.Map;

import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

public class GroupDesc extends Desc<SpellArg> {

	public GroupDesc(Map<SpellArg, Object> arguments) {
		super(arguments);
	}

	public static Map<SpellArg, Object> build(Class<? extends Spell> spellClass) {
		final Map<SpellArg, Object> arguments = new EnumMap<>(SpellArg.class);
		arguments.put(SpellArg.CLASS, spellClass);
		return arguments;
	}

	public GroupDesc addArg(SpellArg spellArg, Object value) {
		GroupDesc clone = clone();
		clone.put(spellArg, value);
		return clone;
	}
	
	public GroupDesc removeArg(SpellArg spellArg) {
		GroupDesc clone = clone();
		clone.remove(spellArg);
		return clone;
	}

	@Override
	public GroupDesc clone() {
		GroupDesc clone = new GroupDesc(build(getSpellClass()));
		for (SpellArg spellArg : super.keySet()) {
			Object value = super.get(spellArg);
			if (value instanceof CustomCloneable) {
				CustomCloneable cloneable = (CustomCloneable) value;
				clone.put(spellArg, cloneable.clone());
			} else {
				clone.put(spellArg, value);
			}
		}
		return clone;
	}

	public EntityFilter getEntityFilter() {
		return (EntityFilter) get(SpellArg.FILTER);
	}

	public int getInt(SpellArg spellArg, int defaultValue) {
		return this.containsKey(spellArg) ? (int) get(spellArg) : defaultValue;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Spell> getSpellClass() {
		return (Class<? extends Spell>) super.get(SpellArg.CLASS);
	}

	public EntityReference getTarget() {
		return (EntityReference) super.get(SpellArg.TARGET);
	}

	public TargetPlayer getTargetPlayer() {
		return (TargetPlayer) get(SpellArg.TARGET_PLAYER);
	}

	public boolean hasPredefinedTarget() {
		return super.get(SpellArg.TARGET) != null;
	}

	@Override
	public String toString() {
		String result = "[SpellDesc arguments= {\n";
		for (SpellArg spellArg : super.keySet()) {
			result += "\t" + spellArg + ": " + super.get(spellArg) + "\n";
		}
		result += "}";
		return result;
	}

}
