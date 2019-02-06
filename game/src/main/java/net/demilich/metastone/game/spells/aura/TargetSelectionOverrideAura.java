package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;
import net.demilich.metastone.game.targeting.TargetSelection;

/**
 * Entities affected by this aura, like minions with battlecries and spells, have their {@link TargetSelection} set to
 * this aura's {@link AuraArg#TARGET_SELECTION}.
 *
 * @see net.demilich.metastone.game.logic.GameLogic#processTargetModifiers(GameAction) for more about the target
 * 		modification logic.
 */
public final class TargetSelectionOverrideAura extends Aura {
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
