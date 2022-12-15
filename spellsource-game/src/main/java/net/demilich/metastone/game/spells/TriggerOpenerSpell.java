package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.custom.RepeatAllOtherBattlecriesSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Triggers the opener written on each {@code target} entity with random targets (except itself).
 */
public class TriggerOpenerSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target.getSourceCard() == null || !target.getSourceCard().hasBattlecry()) {
			return;
		}

		if (!(target instanceof Actor)) {
			return;
		}

		RepeatAllOtherBattlecriesSpell.castBattlecryRandomly(context, player, target.getSourceCard(), (Actor) target);
	}
}
