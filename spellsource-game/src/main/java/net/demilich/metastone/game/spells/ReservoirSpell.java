package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import static net.demilich.metastone.game.spells.desc.condition.ReservoirCondition.reservoirsForced;

/**
 * Shorthand for a {@link ConditionalEffectSpell} that only plays the conditional (second) spell if the caster's deck
 * has a number of cards greater than or equal to the {@link SpellArg#VALUE}.
 * <p>
 * If a {@link SpellArg#SECONDARY_VALUE} is specified, this is the upper bound on the number of cards.
 *
 * @see ConditionalEffectSpell for more on writing this spell.
 */
public final class ReservoirSpell extends ConditionalEffectSpell {
	@Override
	protected boolean isConditionFulfilled(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Boolean forced = reservoirsForced(context, player, source);
		if (forced != null) {
			return forced;
		}

		if (desc.containsKey(SpellArg.SECONDARY_VALUE) && desc.containsKey(SpellArg.VALUE)) {
			return player.getDeck().size() >= desc.getValue(SpellArg.VALUE, context, player, target, source, 0)
					&& player.getDeck().size() <= desc.getValue(SpellArg.SECONDARY_VALUE, context, player, target, source, 15);
		} else {
			return player.getDeck().size() >= desc.getValue(SpellArg.VALUE, context, player, target, source, 20);
		}
	}
}
