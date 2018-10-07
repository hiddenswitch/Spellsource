package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;
import net.demilich.metastone.game.targeting.TargetSelection;

public class TargetSelectionOverrideAura extends Aura {
	public TargetSelectionOverrideAura(AuraDesc desc) {
		super(desc);
		this.triggers.add(new WillEndSequenceTrigger());
		applyAuraEffect = NullSpell.create();
		removeAuraEffect = NullSpell.create();
	}

	public TargetSelection getTargetSelection() {
		return (TargetSelection) getDesc().get(AuraArg.TARGET_SELECTION);
	}
}
