package net.demilich.metastone.game.spells;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

@Deprecated
public class BuffWeaponSpell extends BuffSpell {
	private static Logger logger = LoggerFactory.getLogger(BuffWeaponSpell.class);

	public static SpellDesc create(int attackBonus, int durabilityBonus) {
		return create(EntityReference.FRIENDLY_WEAPON, attackBonus, durabilityBonus);
	}

	public static SpellDesc create(EntityReference target, int attackBonus, int durabilityBonus) {
		Map<SpellArg, Object> arguments = SpellDesc.build(BuffWeaponSpell.class);
		arguments.put(SpellArg.ATTACK_BONUS, attackBonus);
		arguments.put(SpellArg.HP_BONUS, durabilityBonus);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}
}
