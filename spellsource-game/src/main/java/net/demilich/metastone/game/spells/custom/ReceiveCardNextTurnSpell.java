package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.AddEnchantmentSpell;
import net.demilich.metastone.game.spells.ReceiveCardSpell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.TurnStartTrigger;

/**
 * Puts {@link net.demilich.metastone.game.spells.desc.SpellArg#CARD} into the caster's hand at the start of the
 * caster's next turn.
 * <p>
 * Implements Solar Power.
 */
public final class ReceiveCardNextTurnSpell extends AddEnchantmentSpell {
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
		enchantmentDesc.setSpell(ReceiveCardSpell.create((String) desc.get(SpellArg.CARD)));
		enchantmentDesc.setMaxFires(1);
		enchantmentDesc.setEventTrigger(TurnStartTrigger.create(TargetPlayer.SELF));
		desc = desc.clone();
		desc.put(SpellArg.TRIGGER, enchantmentDesc);
		super.onCast(context, player, desc, source, target);
	}
}
