package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Stores the {@link SpellArg#SECONDARY_TARGET}'s source card ID as a string attribute on the {@code target}.
 * <p>
 * The attribute used defaults to {@link Attribute#RESERVED_STRING_1} but can be overridden with {@link
 * SpellArg#ATTRIBUTE}.
 * <p>
 * For example, to store the chosen minion's card ID on a shuffled card (OUTPUT):
 * <pre>
 *   {
 *     "class": "custom.StoreCardIdSpell",
 *     "target": "OUTPUT",
 *     "secondaryTarget": "TARGET"
 *   }
 * </pre>
 */
public class StoreCardIdSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Attribute attribute = (Attribute) desc.getOrDefault(SpellArg.ATTRIBUTE, Attribute.RESERVED_STRING_1);
		Entity secondary = context.resolveSingleTarget(player, source, desc.getSecondaryTarget());
		if (secondary != null && target != null) {
			target.setAttribute(attribute, secondary.getSourceCard().getCardId());
		}
	}
}
