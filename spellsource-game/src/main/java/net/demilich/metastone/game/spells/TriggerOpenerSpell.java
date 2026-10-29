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
		if (!(target instanceof Actor actor)) {
			return;
		}

		if (actor.getSourceCard() != null && actor.getSourceCard().hasChoices()) {
			RepeatAllOtherBattlecriesSpell.castBattlecryRandomly(context, player, actor.getSourceCard(), actor);
			return;
		}

		var openers = context.getLogic().getOpeners(actor);
		for (var opener : openers) {
			var srcCard = opener.getSourceCard() != null ? opener.getSourceCard() : actor.getSourceCard();
			RepeatAllOtherBattlecriesSpell.castOpenerRandomly(context, player, srcCard, actor, opener.getOpenerDesc());
		}
	}
}
