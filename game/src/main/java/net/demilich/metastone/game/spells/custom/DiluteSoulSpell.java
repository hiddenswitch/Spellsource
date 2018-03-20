package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

public class DiluteSoulSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card toDiscard = context.getLogic().getRandom(player.getHand());

		if (toDiscard == null) {
			return;
		}

		context.getLogic().discardCard(player, toDiscard);
		EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
		enchantmentDesc.eventTrigger = TurnEndTrigger.create(TargetPlayer.SELF);
		enchantmentDesc.spell = CopyCardSpell.create(toDiscard, desc.getValue(SpellArg.VALUE, context, player, target, source, 2));
		enchantmentDesc.maxFires = 1;
		SpellDesc addEnchantment = AddEnchantmentSpell.create(EntityReference.FRIENDLY_PLAYER, enchantmentDesc);
		SpellUtils.castChildSpell(context, player, addEnchantment, source, target);
	}
}
