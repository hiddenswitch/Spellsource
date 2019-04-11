package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.BuffEnchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * A work in progress spell that puts a buff on the {@code target} as an {@link net.demilich.metastone.game.spells.trigger.Enchantment}.
 * This makes it easier for buffs to be copied.
 */
public final class BuffAsEnchantmentSpell extends BuffSpell {

	private static Logger logger = LoggerFactory.getLogger(BuffAsEnchantmentSpell.class);

	public static SpellDesc create(EntityReference target, int value) {
		Map<SpellArg, Object> arguments = new SpellDesc(BuffAsEnchantmentSpell.class);
		arguments.put(SpellArg.VALUE, value);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference target, int attackBonus, int hpBonus) {
		Map<SpellArg, Object> arguments = new SpellDesc(BuffAsEnchantmentSpell.class);
		arguments.put(SpellArg.ATTACK_BONUS, attackBonus);
		arguments.put(SpellArg.HP_BONUS, hpBonus);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.ATTACK_BONUS, SpellArg.HP_BONUS, SpellArg.ARMOR_BONUS, SpellArg.VALUE);
		int attackBonus = desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, source, 0);
		int hpBonus = desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 0);
		int armorBonus = desc.getValue(SpellArg.ARMOR_BONUS, context, player, target, source, 0);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);

		if (value != 0) {
			if (target instanceof Hero) {
				attackBonus = armorBonus = value;
			} else {
				attackBonus = hpBonus = value;
			}
		}

		logger.debug("onCast {} {}: {} gains ({})", context.getGameId(), source, target, attackBonus + "/" + (hpBonus + armorBonus));

		if (target instanceof Hero) {
			if (armorBonus != 0) {
				context.getLogic().gainArmor(context.getPlayer(target.getOwner()), armorBonus);
			}
			if (attackBonus != 0) {
				target.modifyAttribute(Attribute.TEMPORARY_ATTACK_BONUS, attackBonus);
			}
		} else {
			Trigger revertTrigger = null;
			if (desc.containsKey(SpellArg.REVERT_TRIGGER)) {
				revertTrigger = (Trigger) desc.get(SpellArg.REVERT_TRIGGER);
			}
			String name = source.getReference().toString() + " Buff Enchantment";
			if (desc.containsKey(SpellArg.NAME)) {
				name = desc.getString(SpellArg.NAME);
			}
			BuffEnchantment buffEnchantment = new BuffEnchantment(attackBonus, hpBonus, name, revertTrigger);
			context.getLogic().addGameEventListener(player, buffEnchantment, target);
		}
	}
}
