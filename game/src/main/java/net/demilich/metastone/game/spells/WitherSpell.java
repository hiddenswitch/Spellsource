package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.ComparisonCondition;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Reduces the attack of the {@code target} by the {@code source} entity's {@link Attribute#WITHER} value. If {@link
 * SpellArg#VALUE} is specified, use that instead. At the end of the {@link Attribute#WITHERED} minion's owner's turn,
 * if the minions attacks equal its max attacks, wither is removed.
 * <p>
 * Wither does <b>not</b> affect {@link EntityType#HERO} entities.
 * <p>
 * Wither hits shields like {@link Attribute#DIVINE_SHIELD} and {@link Attribute#DEFLECT}.
 * <p>
 * Wither stacks.
 * <p>
 * Gives the target the {@link Attribute#WITHERED} attribute for the amount it was withered.
 */
public final class WitherSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target.getEntityType() == EntityType.HERO) {
			return;
		}

		int witherAmount = source.getAttributeValue(Attribute.WITHER);
		if (desc.containsKey(SpellArg.VALUE)) {
			witherAmount = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		}

		// Try to hit shields. If they were hit, we're done. Otherwise, wither was not blocked.
		if (context.getLogic().hitShields(player, witherAmount, source, (Actor) target)) {
			return;
		}

		ConditionDesc turnEndConditionDesc = new ConditionDesc(ComparisonCondition.class);
		turnEndConditionDesc.put(ConditionArg.VALUE1, AttributeValueProvider.create(Attribute.MAX_ATTACKS, EntityReference.TRIGGER_HOST).create());
		turnEndConditionDesc.put(ConditionArg.VALUE2, AttributeValueProvider.create(Attribute.NUMBER_OF_ATTACKS, EntityReference.TRIGGER_HOST).create());
		turnEndConditionDesc.put(ConditionArg.OPERATION, ComparisonOperation.EQUAL);
		EventTriggerDesc revertTrigger = TurnEndTrigger.create(TargetPlayer.getTargetPlayerForOwner(target.getOwner()));
		revertTrigger.put(EventTriggerArg.FIRE_CONDITION, turnEndConditionDesc.create());
		SpellDesc modifyWitheredSpell = new SpellDesc(ModifyAttributeSpell.class);
		modifyWitheredSpell.put(SpellArg.ATTRIBUTE, Attribute.WITHERED);
		modifyWitheredSpell.put(SpellArg.VALUE, witherAmount);
		modifyWitheredSpell.put(SpellArg.REVERT_TRIGGER, revertTrigger);

		SpellDesc debuffAttackSpell = new SpellDesc(BuffSpell.class);
		debuffAttackSpell.put(SpellArg.ATTACK_BONUS, -witherAmount);
		debuffAttackSpell.put(SpellArg.REVERT_TRIGGER, revertTrigger);

		SpellDesc effects = MetaSpell.create(debuffAttackSpell, modifyWitheredSpell);
		SpellUtils.castChildSpell(context, player, effects, source, target);
	}
}
