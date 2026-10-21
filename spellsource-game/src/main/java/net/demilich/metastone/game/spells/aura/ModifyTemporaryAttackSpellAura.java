package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * Whenever a card affected by this aura has a {@link net.demilich.metastone.game.spells.TemporaryAttackSpell} effect,
 * the value of temporary attack is increased by the {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#VALUE}.
 */
public class ModifyTemporaryAttackSpellAura extends EffectlessAura {

	public ModifyTemporaryAttackSpellAura(AuraDesc desc) {
		super(desc);
	}

	public int getValue(GameContext context, Player player, Entity target, Entity host) {
		return getDesc().getValue(AuraArg.VALUE, context, player, target, host, 0);
	}
}
