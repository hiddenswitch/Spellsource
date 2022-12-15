package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * When the player has at least {@link Attribute#INVOKE} additional unspent mana, source an extra bonus effect for that
 * cost.
 * <p>
 * By default, casts the effect in {@link SpellArg#SPELL1}. If the player can afford the extra cost, {@link
 * SpellArg#SPELL2} is printed on a card, and the player is given a discover choice between the two effects.
 */
public class InvokeSpell extends ChooseOneSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		var manaRemaining = player.getMana();
		var invoke = source.getAttributeValue(Attribute.INVOKE);
		if (source.hasAttribute(Attribute.AURA_INVOKE)) {
			invoke = Math.min(invoke, source.getAttributeValue(Attribute.AURA_INVOKE));
		}
		if (manaRemaining < invoke) {
			if (desc.containsKey(SpellArg.SPELL)) {
				SpellUtils.castChildSpell(context, player, desc.getSpell(), source, target);
			}
			return;
		}
		super.onCast(context, player, desc, source, target);
	}

	@Override
	public boolean shouldRemoveCard(Card card, Player player, GameContext context) {
		return card.getBaseManaCost() > player.getMana();
	}

	@Override
	public Card getTempCard(GameContext context, SpellDesc spellDesc, Card sourceCard) {
		return ChooseOneOptionSpell.getTempCard(context, spellDesc, sourceCard, "invoke_");
	}

}
