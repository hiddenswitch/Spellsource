package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

/**
 * @deprecated Use {@link BuffSpell} instead.
 */
@Deprecated
public class BuffHeroSpell extends BuffSpell {
	public static SpellDesc create(int attackBonus, int armorBonus) {
		return create(EntityReference.FRIENDLY_HERO, attackBonus, armorBonus);
	}

	public static SpellDesc create(EntityReference target, int attackBonus, int armorBonus) {
		Map<SpellArg, Object> arguments = new SpellDesc(BuffHeroSpell.class);
		arguments.put(SpellArg.ATTACK_BONUS, attackBonus);
		arguments.put(SpellArg.ARMOR_BONUS, armorBonus);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}
}
