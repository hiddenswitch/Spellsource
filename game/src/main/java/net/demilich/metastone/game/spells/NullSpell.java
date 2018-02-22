package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * A spell that has no effects.
 * <p>
 * This is useful for situations where a spell <b>must</b> be specified, like in a {@link
 * net.demilich.metastone.game.cards.desc.SpellCardDesc#spell} property in its JSON, but when no effects are intended.
 * <p>
 * For <b>example</b>, place the following as the {@code spell} property in the card JSON for a spell card that should
 * do nothing:
 * <p>
 * <pre>
 *     {
 *         "class": "NullSpell"
 *     }
 * </pre>
 * <p>
 * This implements cards like Dimensius's Stop card ("Full Belly"), which should do nothing but still needs to be cast.
 */
public class NullSpell extends Spell {

	/**
	 * Creates a spell that does nothing.
	 *
	 * @return The spell.
	 */
	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = SpellDesc.build(NullSpell.class);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// intentionally do nothing
	}

}
