package net.demilich.metastone.game.spells.desc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.io.Serializable;

@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
public final class TapDesc implements Serializable, Cloneable {
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
		this.spell = spell;
	}

	public TargetSelection getTargetSelection() {
		return targetSelection;
	}

	public void setTargetSelection(TargetSelection targetSelection) {
		this.targetSelection = targetSelection;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	public ConditionDesc getCondition() {
		return condition;
	}

	public void setCondition(ConditionDesc condition) {
		this.condition = condition;
	}

	public Zones[] getZones() {
		return zones;
	}

	public void setZones(Zones[] zones) {
		this.zones = zones;
	}

	@Override
	public TapDesc clone() {
		try {
			TapDesc clone = (TapDesc) super.clone();
			if (spell != null) {
				clone.spell = spell.clone();
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
