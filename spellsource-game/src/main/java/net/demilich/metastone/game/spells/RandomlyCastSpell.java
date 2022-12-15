package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Arrays;
import java.util.Map;

/**
 * Givem a list of effects in {@link SpellArg#SPELLS}, randomly choose one to cast the {@code target}.
 * <p>
 * Implements Enhance-o Mechano.
 */
public class RandomlyCastSpell extends Spell {

	public static SpellDesc create(EntityReference target, SpellDesc... spells) {
		Map<SpellArg, Object> arguments = new SpellDesc(RandomlyCastSpell.class);
		arguments.put(SpellArg.SPELLS, spells);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc[] spells = (SpellDesc[]) desc.get(SpellArg.SPELLS);
		SpellUtils.castChildSpell(context, player, context.getLogic().getRandom(Arrays.asList(spells)), source, target);
	}
}