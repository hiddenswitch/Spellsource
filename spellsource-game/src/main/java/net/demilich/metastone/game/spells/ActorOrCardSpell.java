package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Simply performs {@link SpellArg#SPELL1} if the target entity is an Actor, or {@link SpellArg#SPELL2}
 * if they are a Card.
 *
 * Useful for {@link WhereverTheyAreSpell}s.
 */

public class ActorOrCardSpell extends Spell {

	@Suspendable
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target instanceof Actor) {
			SpellUtils.castChildSpell(context, player, (SpellDesc)desc.get(SpellArg.SPELL1), source, target);
		} else if (target instanceof Card) {
			SpellUtils.castChildSpell(context, player, (SpellDesc)desc.get(SpellArg.SPELL2), source, target);
		}
	}
}
