package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

/**
 * Use a {@link SetAttributeSpell} instead:
 * <pre>
 *   {
 *     "class": "SetAttributeSpell",
 *     "target": "FRIENDLY_PLAYER",
 *     "attribute": "OVERLOAD",
 *     "value": 0
 *   }
 * </pre>
 * Resets the player's overload status.
 *
 * @deprecated
 */
@Deprecated
public class ClearOverloadSpell extends Spell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(ClearOverloadSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int lockedMana = player.getLockedMana();
		if (lockedMana > 0) {
			context.getLogic().modifyCurrentMana(player.getId(), lockedMana, false);
			player.setLockedMana(0);
		}

		player.getAttributes().remove(Attribute.OVERLOAD);
	}

}
