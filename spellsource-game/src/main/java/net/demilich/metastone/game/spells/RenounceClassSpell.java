package net.demilich.metastone.game.spells;

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

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
	}
}
