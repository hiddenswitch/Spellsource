package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlayChooseOneCardAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

/**
 * Entities affected by this aura, like minions with battlecries and spells, have their {@link TargetSelection} set to
 * this aura's {@link AuraArg#TARGET_SELECTION}.
 *
 * @see net.demilich.metastone.game.logic.GameLogic#processTargetModifiers(GameAction) for more about the target
 * modification logic.
 */
public final class TargetSelectionOverrideAura extends EffectlessAura {
	public TargetSelectionOverrideAura(AuraDesc desc) {
		super(desc);
	}

	public TargetSelection getTargetSelection() {
		return (TargetSelection) getDesc().get(AuraArg.TARGET_SELECTION);
	}

	public void processTargetModification(Entity target, GameAction action) {
		if (!getAffectedEntities().contains(target.getId())) {
			return;
		}

		TargetSelection targetSelection = getTargetSelection();
		switch (action.getActionType()) {
			case HERO_POWER:
			case SPELL:
				if (action instanceof PlayChooseOneCardAction) {
					PlayChooseOneCardAction chooseOneCardAction = (PlayChooseOneCardAction) action;
					if (chooseOneCardAction.getSpell().hasPredefinedTarget()) {
						SpellDesc targetChangedSpell = chooseOneCardAction.getSpell().removeArg(SpellArg.TARGET);
						chooseOneCardAction.setSpell(targetChangedSpell);
					}
				} else {
					PlaySpellCardAction spellCardAction = (PlaySpellCardAction) action;
					if (spellCardAction.getSpell().hasPredefinedTarget()) {
						SpellDesc targetChangedSpell = spellCardAction.getSpell().removeArg(SpellArg.TARGET);
						spellCardAction.setSpell(targetChangedSpell);
					}
				}
				break;
			default:
				break;
		}
		action.setTargetRequirement(targetSelection);
	}
}
