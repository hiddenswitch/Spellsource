package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;
import net.demilich.metastone.game.spells.trigger.TurnStartTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Reduces the health of the {@code target} by the {@code source} entity's {@link Attribute#WITHER} value. If {@link
 * SpellArg#VALUE} is specified, use that instead. At the start of the casting {@code player}'s next turn, restores
 * (without triggering healing) the health of the minion.
 * <p>
 * Wither stacks.
 * <p>
 * Gives the target the {@link Attribute#WITHERED} attribute for the amount it was withered.
 */
public final class WitherSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int witherAmount = source.getAttributeValue(Attribute.WITHER);
		if (desc.containsKey(SpellArg.VALUE)) {
			witherAmount = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		}

		EventTriggerDesc revertTrigger = TurnStartTrigger.create(player.getId() == GameContext.PLAYER_1 ? TargetPlayer.PLAYER_1 : TargetPlayer.PLAYER_2);
		SpellDesc modifyWitheredSpell = new SpellDesc(ModifyAttributeSpell.class);
		modifyWitheredSpell.put(SpellArg.ATTRIBUTE, Attribute.WITHERED);
		modifyWitheredSpell.put(SpellArg.VALUE, witherAmount);

		SpellDesc reduceHpSpell = new SpellDesc(ModifyAttributeSpell.class);
		reduceHpSpell.put(SpellArg.ATTRIBUTE, Attribute.HP);
		reduceHpSpell.put(SpellArg.VALUE, -witherAmount);

		SpellDesc debuffAttackSpell = new SpellDesc(BuffSpell.class);
		debuffAttackSpell.put(SpellArg.ATTACK_BONUS, -witherAmount);
		debuffAttackSpell.put(SpellArg.REVERT_TRIGGER, revertTrigger);

		EnchantmentDesc restoreHpAndReduceWitheredEnchantment = new EnchantmentDesc();
		restoreHpAndReduceWitheredEnchantment.eventTrigger = revertTrigger;
		restoreHpAndReduceWitheredEnchantment.maxFires = 1;
		restoreHpAndReduceWitheredEnchantment.persistentOwner = true;
		SpellDesc restoreHealthPeacefullySpell = new SpellDesc(RestoreHealthPeacefullySpell.class, EntityReference.SELF, null, false);
		restoreHealthPeacefullySpell.put(SpellArg.VALUE, witherAmount);
		restoreHpAndReduceWitheredEnchantment.spell = MetaSpell.create(
				ModifyAttributeSpell.create(EntityReference.SELF, Attribute.WITHERED, witherAmount),
				restoreHealthPeacefullySpell);
		SpellDesc addRebuffEnchantmentSpell = AddEnchantmentSpell.create(target.getReference(), restoreHpAndReduceWitheredEnchantment);
		SpellDesc effects = MetaSpell.create(debuffAttackSpell, reduceHpSpell, modifyWitheredSpell, addRebuffEnchantmentSpell);
		SpellUtils.castChildSpell(context, player, effects, source, target);
	}
}
