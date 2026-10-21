package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.MissilesSpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;
import java.util.List;

/**
 * Fires {@link SpellArg#HOW_MANY} missiles at the {@link SpellArg#TARGET} entities. If all the targets actually hit
 * match {@link SpellArg#CARD_FILTER}, call the subspell {@link SpellArg#SPELL1}.
 * <p>
 * Implements Formless Agony.
 */
public final class FormlessAgonyMissilesSpell extends MissilesSpell {

	@Override
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		List<Actor> hits = new ArrayList<>(targets != null ? targets.size() : 4);
		fireMissiles(context, player, desc, source, targets, hits);
		var cardFilter = desc.getCardFilter();
		boolean allMatch = true;
		if (cardFilter != null) {
			for (int i = 0, size = hits.size(); i < size; i++) {
				if (!cardFilter.matches(context, player, hits.get(i), source)) {
					allMatch = false;
					break;
				}
			}
		}
		if (allMatch) {
			SpellUtils.castChildSpell(context, player, (SpellDesc) desc.get(SpellArg.SPELL1), source, null);
		}
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
	}
}
