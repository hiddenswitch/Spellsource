package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

/**
 * Shorthand for a {@link ConditionalEffectSpell} that only plays the spell if the player has activated a combo (i.e.,
 * previously played a card earlier this turn).
 *
 * @see ConditionalEffectSpell for how to write this spell.
 */
public final class ComboSpell extends ConditionalEffectSpell {

	public static SpellDesc create(SpellDesc either, SpellDesc or, boolean exclusive) {
		Map<SpellArg, Object> arguments = new SpellDesc(ComboSpell.class);
		arguments.put(SpellArg.SPELL1, either);
		arguments.put(SpellArg.SPELL2, or);
		arguments.put(SpellArg.EXCLUSIVE, exclusive);
		return new SpellDesc(arguments);
	}

	@Override
	protected boolean isConditionFulfilled(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		return player.hasAttribute(Attribute.COMBO);
	}
}