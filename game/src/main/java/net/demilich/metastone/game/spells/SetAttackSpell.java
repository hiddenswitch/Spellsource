package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class SetAttackSpell extends Spell {

	public static SpellDesc create(int value) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SetAttackSpell.class);
		arguments.put(SpellArg.VALUE, value);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int value, boolean immuneToSilence) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SetAttackSpell.class);
		arguments.put(SpellArg.VALUE, value);
		arguments.put(SpellArg.EXCLUSIVE, !immuneToSilence);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		// When exclusive, the set attack spell will overwrite bonuses. When not exclusive, the BASE attack will change
		// (to protect it from silencing) and the changed attack will honor bonuses.
		boolean exclusive = (boolean) desc.getOrDefault(SpellArg.EXCLUSIVE, true);

		target.setAttribute(Attribute.ATTACK, value);
		target.getAttributes().remove(Attribute.ATTACK_BONUS);
		if (exclusive) {
			target.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
			target.getAttributes().remove(Attribute.CONDITIONAL_ATTACK_BONUS);
		} else {
			target.setAttribute(Attribute.BASE_ATTACK, value);
		}
	}

}