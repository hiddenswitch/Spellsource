package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

/**
 * Created by bberman on 3/17/17.
 */
public class SummonCardFromAttributeSpell extends Spell {
	public static SpellDesc create(Attribute attributeContainingCardId, String defaultCardId, EntityReference target) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SummonCardFromAttributeSpell.class);
		arguments.put(SpellArg.ATTRIBUTE, attributeContainingCardId);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.CARD, defaultCardId);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		String cardId = (String) target.getAttribute((Attribute) desc.get(SpellArg.ATTRIBUTE));
		Card card = null;
		if (cardId == null) {
			// Check if there is a default card
			card = SpellUtils.getCard(context, desc);
		} else {
			card = context.getCardById(cardId);
		}
		if (card == null) {
			return;
		}
		for (int i = 0; i < count; i++) {
			MinionCard minionCard = count == 1 ? (MinionCard) card : (MinionCard) card.clone();
			context.getLogic().summon(player.getId(), minionCard.summon(), null, boardPosition, false);
		}
	}
}
