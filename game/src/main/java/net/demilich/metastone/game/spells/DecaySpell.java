package net.demilich.metastone.game.spells;


import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Predicate;

public final class DecaySpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(DamageSpell.class);

	public static SpellDesc create(EntityReference target) {
		return create(target, 1);
	}

	public static SpellDesc create(EntityReference target, int damage) {
		return create(target, damage, false);
	}

	public static SpellDesc create(EntityReference target, int damage, boolean randomTarget) {
		return create(target, damage, null, randomTarget);
	}

	public static SpellDesc create(EntityReference target, int damage, Predicate<Entity> targetFilter, boolean randomTarget) {
		Map<SpellArg, Object> arguments = new SpellDesc(DamageSpell.class);
		arguments.put(SpellArg.VALUE, damage);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.RANDOM_TARGET, randomTarget);
		if (targetFilter != null) {
			arguments.put(SpellArg.FILTER, targetFilter);
		}
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int damage) {
		return create(EntityReference.SELF, damage);
	}

	public static SpellDesc create() {
		return new SpellDesc(DamageSpell.class);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.IGNORE_SPELL_DAMAGE, SpellArg.VALUE);

		if (target == null) {
			target = source;
		}

		if (!(target instanceof Actor)) {
			logger.error("onCast {} {}: Cannot deal damage to non-Actor target {}", context.getGameId(), source, target);
			return;
		}

		if (target instanceof Minion) {
			int damage = getDamage(context, player, desc, source, target);
			if (damage < 0) {
				logger.error("onCast {} {}: Suspicious negative damage call", context.getGameId(), source);
			}
			context.getLogic().damage(player, (Actor) target, damage, source, true);
		} else if (target instanceof Weapon) {
			int durabilityChange = desc.getValue(SpellArg.VALUE, context, player, target, source, -1);
			context.getLogic().modifyDurability(player.getHero().getWeapon(), durabilityChange);
		}
	}

	public static int getDamage(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int damage = 0;
		// TODO Rewrite to more accurate way to grab Damage Stack damage.
		if (!desc.containsKey(SpellArg.VALUE) && !context.getDamageStack().isEmpty()) {
			Integer peek = context.getDamageStack().peek();
			if (peek != null) {
				damage = peek;
			}
		} else {
			damage = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		}
		return damage;
	}
}
