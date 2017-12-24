package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

@Deprecated
public class BuffHeroSpell extends BuffSpell {
	public static SpellDesc create(int attackBonus, int armorBonus) {
		return create(EntityReference.FRIENDLY_HERO, attackBonus, armorBonus);
	}

	public static SpellDesc create(EntityReference target, int attackBonus, int armorBonus) {
		Map<SpellArg, Object> arguments = SpellDesc.build(BuffHeroSpell.class);
		arguments.put(SpellArg.ATTACK_BONUS, attackBonus);
		arguments.put(SpellArg.ARMOR_BONUS, armorBonus);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}
}
