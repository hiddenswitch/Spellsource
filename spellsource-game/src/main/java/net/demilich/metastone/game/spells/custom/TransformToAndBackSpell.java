package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

public final class TransformToAndBackSpell extends AddEnchantmentSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		String originalName = target.getName();
		SpellDesc transformTo = new SpellDesc(TransformMinionSpell.class, target.getReference(), null, false);
		SpellDesc transformBack = new SpellDesc(TransformMinionSpell.class, EntityReference.SELF, null, false);
		transformTo.put(SpellArg.CARD, desc.get(SpellArg.CARD));
		transformBack.put(SpellArg.CARD, target.getSourceCard().getCardId());
		SpellUtils.castChildSpell(context, player, transformTo, source, target);
		target = target.transformResolved(context);
		SpellDesc setDescription = SetDescriptionSpell.create(desc.getString(SpellArg.DESCRIPTION) + originalName);
		SpellUtils.castChildSpell(context, player, setDescription, source, target);
		EnchantmentDesc trigger = new EnchantmentDesc();
		trigger.setMaxFires(2);
		trigger.setSpell(transformBack);
		trigger.setEventTrigger(TurnEndTrigger.create(TargetPlayer.SELF));
		trigger.setCountUntilCast(2);
		SpellDesc enchant = AddEnchantmentSpell.create(trigger);
		super.onCast(context, player, enchant, source, target);
	}
}
