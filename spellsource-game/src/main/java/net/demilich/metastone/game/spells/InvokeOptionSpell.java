package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.InvokedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Casts an Invoke effect specified in {@link SpellArg#SPELL} for {@link SpellArg#MANA} mana cost.
 * <p>
 * When presenting choices for the invoke effect, the card's name and description are read from {@link SpellArg#NAME}
 * and {@link SpellArg#DESCRIPTION} on this spell.
 */
public class InvokeOptionSpell extends ChooseOneOptionSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		var mana = desc.getInt(SpellArg.MANA, 0);
		context.getLogic().modifyCurrentMana(player.getId(), -mana, true);
		super.onCast(context, player, desc, source, target);
		if (mana > 0) {
			player.modifyAttribute(Attribute.INVOKED, 1);
			context.getLogic().fireGameEvent(new InvokedEvent(context, player.getId(), source.getSourceCard(), mana));
		}
	}


	@Override
	public Card getTempCard(GameContext context, SpellDesc spellDesc, Card sourceCard) {
		return ChooseOneOptionSpell.getTempCard(context, spellDesc, sourceCard, "invoke_");
	}
}
