package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.DrainEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Collections;
import java.util.List;

/**
 * Deals {@link SpellArg#VALUE} damage to the {@code target}. Heals the {@code source} (if a minion) or {@link
 * net.demilich.metastone.game.targeting.EntityReference#FRIENDLY_HERO} (if a spell) for the amount of damage dealt. Any
 * excess healing beyond the max health of the {@code source} is converted into a {@link BuffSpell} whose {@link
 * SpellArg#HP_BONUS} is equal to the excess.
 * <p>
 * If a {@link SpellArg#SECONDARY_TARGET} is specified, heals and buffs those targets instead, splitting the amount
 * restored amongst all the targets matched. Excess health split this way has the remainder given to the first entity in
 * the secondary target list.
 * <p>
 * First, all the damage is dealt. Then, healing and buffing is applied to each healing target.
 * <p>
 * Fires a {@link DrainEvent} <b>once</b>, not for every target, which contains the total amount damage dealt.
 */
public final class DrainSpell extends Spell {

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		if (targets.isEmpty()) {
			return;
		}
		if (desc.getBool(SpellArg.RANDOM_TARGET)) {
			targets = Collections.singletonList(context.getLogic().getRandom(targets));
		}

		int damageDealt = 0;
		for (Entity target : targets) {
			SpellDesc damageSpell = DamageSpell.create(target.getReference(), desc.getValue(SpellArg.VALUE, context, player, target, source, 0));
			SpellUtils.castChildSpell(context, player, damageSpell, source, target);
			damageDealt += target.getAttributeValue(Attribute.LAST_HIT);
		}

		Entity defaultSource;
		if (source.getSourceCard().getCardType() == CardType.SPELL || source.getEntityType() == EntityType.WEAPON) {
			defaultSource = context.resolveSingleTarget(context.getPlayer(source.getOwner()), source, EntityReference.FRIENDLY_HERO);
		} else {
			defaultSource = source;
		}
		List<Entity> targetsOfHealing = desc.getSecondaryTarget() == null ? Collections.singletonList(defaultSource) : context.resolveTarget(player, source, desc.getSecondaryTarget());
		int healingAmount = damageDealt / targetsOfHealing.size();
		int healingAmountRemainder = damageDealt / targetsOfHealing.size() + damageDealt % targetsOfHealing.size();

		drain(context, player, source, healingAmountRemainder, targetsOfHealing.get(0));
		for (int i = 1; i < targetsOfHealing.size(); i++) {
			drain(context, player, source, healingAmount, targetsOfHealing.get(i));
		}
		context.fireGameEvent(new DrainEvent(context, source, player.getId(), damageDealt));
	}

	@Suspendable
	public static void drain(GameContext context, Player player, Entity source, int amount, Entity healingTarget) {
		SpellDesc healSpell = HealSpell.create(healingTarget.getReference(), amount);
		SpellUtils.castChildSpell(context, player, healSpell, source, healingTarget);
		int excess = amount - healingTarget.getAttributeValue(Attribute.LAST_HEAL);
		if (excess > 0) {
			SpellDesc buffSpell = BuffSpell.create(healingTarget.getReference(), 0, excess);
			SpellUtils.castChildSpell(context, player, buffSpell, source, healingTarget);
			healingTarget.modifyAttribute(Attribute.DRAINED_THIS_TURN, excess);
			healingTarget.modifyAttribute(Attribute.TOTAL_DRAINED, excess);
		}
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		throw new UnsupportedOperationException("should not call onCast");
	}
}

