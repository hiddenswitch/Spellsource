package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.events.DrainEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import com.hiddenswitch.spellsource.client.models.DamageTypeEnum;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Deals {@link SpellArg#VALUE} damage to the {@code target}. Buffs the hitpoints of the {@code source} (if a minion) or
 * {@link net.demilich.metastone.game.targeting.EntityReference#FRIENDLY_HERO} (if a spell) for the amount of damage
 * dealt using a {@link BuffSpell} whose {@link SpellArg#HP_BONUS} is equal to the excess.
 * <p>
 * If a {@link SpellArg#SECONDARY_TARGET} is specified, buffs those targets instead, splitting the amount restored
 * amongst all the targets matched. Excess health split this way has the remainder given to the first entity in the
 * secondary target list.
 * <p>
 * First, all the damage is dealt. Then, buffing is applied to each secondary target / source.
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

		if (desc.getEntityFilter() != null) {
			targets = SpellUtils.getValidTargets(context, player, targets, desc.getEntityFilter(), source);
		}

		int damageDealt = 0;
		List<DrainEvent> events = new ArrayList<>(targets.size());
		for (Entity target : targets) {
			int damage = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
			int thisDamageDealt = drainDamage(context, player, source, target, damage, events);
			damageDealt += thisDamageDealt;
		}

		Entity defaultSource;
		if (GameLogic.isCardType(source.getSourceCard().getCardType(), CardType.SPELL)
				|| GameLogic.isCardType(source.getSourceCard().getCardType(), CardType.HERO_POWER)
				|| source.getEntityType() == EntityType.WEAPON) {
			defaultSource = context.resolveSingleTarget(context.getPlayer(source.getOwner()), source, EntityReference.FRIENDLY_HERO);
		} else {
			defaultSource = source;
		}
		List<Entity> targetsOfHealing = desc.getSecondaryTarget() == null ? Collections.singletonList(defaultSource) : context.resolveTarget(player, source, desc.getSecondaryTarget());
		int buffingAmount = damageDealt / targetsOfHealing.size();
		int buffingAmountRemainder = damageDealt / targetsOfHealing.size() + damageDealt % targetsOfHealing.size();

		drain(context, player, source, buffingAmountRemainder, targetsOfHealing.get(0));
		for (int i = 1; i < targetsOfHealing.size(); i++) {
			drain(context, player, source, buffingAmount, targetsOfHealing.get(i));
		}

		for (DrainEvent event : events) {
			context.getLogic().fireGameEvent(event);
		}
	}

	/**
	 * Gives an HP buff for the specified target.
	 *
	 * @param context
	 * @param player
	 * @param source
	 * @param amount
	 * @param healingTarget
	 */
	@Suspendable
	public static void drain(GameContext context, Player player, Entity source, int amount, Entity healingTarget) {
		if (amount > 0) {
			SpellDesc buffSpell = BuffSpell.create(healingTarget.getReference(), 0, amount);
			SpellUtils.castChildSpell(context, player, buffSpell, source, healingTarget);
		}
		healingTarget.modifyAttribute(Attribute.DRAINED_THIS_TURN, amount);
		healingTarget.modifyAttribute(Attribute.TOTAL_DRAINED, amount);
		player.modifyAttribute(Attribute.DRAINED_THIS_TURN, amount);
		player.modifyAttribute(Attribute.TOTAL_DRAINED, amount);
	}

	@Suspendable
	public static int drainDamage(GameContext context, Player player, Entity source, Entity target, int damage, List<DrainEvent> events) {
		int thisDamageDealt = context.getLogic().damage(player, (Actor) target, damage, source, true, EnumSet.of(DamageTypeEnum.DRAIN));
		events.add(new DrainEvent(context, source, target, player.getId(), thisDamageDealt));
		return thisDamageDealt;
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		throw new UnsupportedOperationException("should not call onCast");
	}
}

