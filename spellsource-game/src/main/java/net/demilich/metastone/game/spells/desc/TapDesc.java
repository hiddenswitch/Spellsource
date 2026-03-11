package net.demilich.metastone.game.spells.desc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

import net.demilich.metastone.game.cards.Freezable;

import java.io.Serializable;

@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
public final class TapDesc implements Serializable, Cloneable, Freezable {
	private transient boolean readOnly;
	private SpellDesc spell;
	private TargetSelection targetSelection = TargetSelection.NONE;
	private int cost = 1;
	private int cooldown = 1;
	private ConditionDesc condition;
	private Zones[] zones;

	public SpellDesc getSpell() {
		return spell;
	}

	public void setSpell(SpellDesc spell) {
		checkNotFrozen();
		this.spell = spell;
	}

	public TargetSelection getTargetSelection() {
		return targetSelection;
	}

	public void setTargetSelection(TargetSelection targetSelection) {
		checkNotFrozen();
		this.targetSelection = targetSelection;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		checkNotFrozen();
		this.cost = cost;
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int cooldown) {
		checkNotFrozen();
		this.cooldown = cooldown;
	}

	public ConditionDesc getCondition() {
		return condition;
	}

	public void setCondition(ConditionDesc condition) {
		checkNotFrozen();
		this.condition = condition;
	}

	public Zones[] getZones() {
		return zones;
	}

	public void setZones(Zones[] zones) {
		checkNotFrozen();
		this.zones = zones;
	}

	@Override
	public TapDesc clone() {
		try {
			TapDesc clone = (TapDesc) super.clone();
			clone.readOnly = this.readOnly;
			if (spell != null) {
				clone.spell = spell.clone();
			}
			if (condition != null) {
				clone.condition = condition.clone();
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	protected void checkNotFrozen() {
		if (readOnly) {
			throw new UnsupportedOperationException("This TapDesc is frozen (read-only) and cannot be modified.");
		}
	}

	@Override
	public void freeze() {
		readOnly = true;
		if (spell != null) {
			spell.freeze();
		}
		if (condition != null) {
			condition.freeze();
		}
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	public TapDesc cloneAndUnfreeze() {
		try {
			TapDesc clone = (TapDesc) super.clone();
			clone.readOnly = false;
			if (spell != null) {
				clone.spell = spell.cloneAndUnfreeze();
			}
			if (condition != null) {
				clone.condition = condition.cloneAndUnfreeze();
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
