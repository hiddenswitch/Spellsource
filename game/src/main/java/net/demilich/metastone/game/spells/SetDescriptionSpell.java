package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.utils.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets the {@code target} entity's {@link Attribute#DESCRIPTION} to the string specified in {@link
 * SpellArg#DESCRIPTION}, or clears it if no description is specified.
 * <p>
 * For <b>example</b>:
 * <pre>
 *     {
 *         "class": "SetDescriptionSpell",
 *         "target": "SELF",
 *         "description": "New description."
 *     }
 * </pre>
 * This is used by The Darkness to toggle the explanation written on its card form versus its permanent form.
 */
public class SetDescriptionSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(SetDescriptionSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.DESCRIPTION);

		if (desc.containsKey(SpellArg.DESCRIPTION)) {
			final String description = desc.getString(SpellArg.DESCRIPTION);
			target.getAttributes().put(Attribute.DESCRIPTION, description);
		} else {
			target.getAttributes().remove(Attribute.DESCRIPTION);
		}
	}
}
