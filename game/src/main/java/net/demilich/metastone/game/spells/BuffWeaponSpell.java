package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @deprecated Use {@link BuffSpell} instead.
 */
@Deprecated
public class BuffWeaponSpell extends BuffSpell {
	private static final long serialVersionUID = -451704290679497371L;
	private static Logger logger = LoggerFactory.getLogger(BuffWeaponSpell.class);

	public static SpellDesc create(int attackBonus, int durabilityBonus) {
		return create(EntityReference.FRIENDLY_WEAPON, attackBonus, durabilityBonus);
	}

	public static SpellDesc create(EntityReference target, int attackBonus, int durabilityBonus) {
		Map<SpellArg, Object> arguments = new SpellDesc(BuffWeaponSpell.class);
		arguments.put(SpellArg.ATTACK_BONUS, attackBonus);
		arguments.put(SpellArg.HP_BONUS, durabilityBonus);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}
}
