package net.demilich.metastone.game.spells;

import java.util.List;
import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.apache.commons.lang3.ArrayUtils;

/**
 * A class that defines a collection of spells that should be executed one after another. Includes information useful to
 * the subspells in the {@link SpellArg#VALUE} property.
 *
 * @see GameContext#getSpellValueStack() for more about the spell value stack.
 */
public class MetaSpell extends Spell {
	public static SpellDesc create(EntityReference target, boolean randomTarget, SpellDesc... spells) {
		Map<SpellArg, Object> arguments = new SpellDesc(MetaSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELLS, spells);
		arguments.put(SpellArg.RANDOM_TARGET, randomTarget);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(SpellDesc... spells) {
		Map<SpellArg, Object> arguments = new SpellDesc(MetaSpell.class);
		arguments.put(SpellArg.SPELLS, spells);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(SpellDesc[] spells, SpellDesc... spells1) {
		Map<SpellArg, Object> arguments = new SpellDesc(MetaSpell.class);
		arguments.put(SpellArg.SPELLS, ArrayUtils.addAll(spells1, spells));
		return new SpellDesc(arguments);
	}

    @Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		context.getSpellValueStack().addLast(desc.getValue(SpellArg.VALUE, context, player, target, source, 0));
		// Manually obtain sub spells for performance reasons, this is accessed very often
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		SpellDesc[] spells = (SpellDesc[]) desc.get(SpellArg.SPELLS);
		SpellDesc spell1 = (SpellDesc) desc.get(SpellArg.SPELL1);
		SpellDesc spell2 = (SpellDesc) desc.get(SpellArg.SPELL2);
		int howMany = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		for (int i = 0; i < howMany; i++) {
			if (spell != null) {
				SpellUtils.castChildSpell(context, player, spell, source, target);
			}
			if (spells != null && spells.length > 0) {
				for (SpellDesc subSpell : spells) {
					SpellUtils.castChildSpell(context, player, subSpell, source, target);
				}
			}
			if (spell1 != null) {
				SpellUtils.castChildSpell(context, player, spell1, source, target);
			}
			if (spell2 != null) {
				SpellUtils.castChildSpell(context, player, spell2, source, target);
			}
		}
		context.getSpellValueStack().pollLast();
	}

}
