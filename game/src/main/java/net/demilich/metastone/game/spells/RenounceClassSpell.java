package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * @deprecated Has no implementation.
 * 		<p>
 * 		Renounce Darkness is now implemented as a combination of other spells and effects.
 */
@Deprecated
public class RenounceClassSpell extends Spell {

	private static final long serialVersionUID = 7177056326346575669L;

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
	}
}
