package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * Modifies the target card's {@link net.demilich.metastone.game.spells.BuffSpell} bonuses with the specified {@link
 * net.demilich.metastone.game.spells.desc.aura.AuraArg#ATTACK_BONUS} and {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#HP_BONUS}
 * effects.
 * <p>
 * For example, to give the player's hero power "+1 Attack" whenever it buffs any target:
 * <pre>
 *   {
 *     "class": "ModifyBuffSpellAura",
 *     "target": "FRIENDLY_HERO_POWER",
 *     "attackBonus": 1
 *   }
 * </pre>
 */
public class ModifyBuffSpellAura extends Aura {

	public ModifyBuffSpellAura(AuraDesc desc) {
		super(new WillEndSequenceTrigger(), NullSpell.create(), NullSpell.create(), desc.getTarget(), desc.getFilter(), desc.getCondition());
		setDesc(desc);
	}
}

