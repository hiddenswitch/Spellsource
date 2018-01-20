package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.weapons.Weapon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class BuffSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(BuffSpell.class);

	public static SpellDesc create(EntityReference target, int value) {
		Map<SpellArg, Object> arguments = SpellDesc.build(BuffSpell.class);
		arguments.put(SpellArg.VALUE, value);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference target, int attackBonus, int hpBonus) {
		Map<SpellArg, Object> arguments = SpellDesc.build(BuffSpell.class);
		arguments.put(SpellArg.ATTACK_BONUS, attackBonus);
		arguments.put(SpellArg.HP_BONUS, hpBonus);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
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

		logger.debug("{} gains ({})", target, attackBonus + "/" + (hpBonus + armorBonus));

		if (attackBonus != 0) {
			if (target instanceof Hero) {
				target.modifyAttribute(Attribute.TEMPORARY_ATTACK_BONUS, attackBonus);
			} else {
				target.modifyAttribute(Attribute.ATTACK_BONUS, attackBonus);
			}
		}

		if (hpBonus != 0) {
			if (target instanceof Weapon) {
				context.getLogic().modifyDurability((Weapon) target, hpBonus);
			} else {
				target.modifyHpBonus(hpBonus);
			}
		}

		if (armorBonus != 0) {
			context.getLogic().gainArmor(player, armorBonus);
		}
	}

}
