package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRoastedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;

/**
 * Roasting a card removes the card from the top of the deck and adds the {@link Attribute#ROASTED} to it. Always
 * reveals the card.
 * <p>
 * Removing the {@link Attribute#ROASTED} attribute from the {@link EntityReference#EVENT_TARGET} during a {@link
 * net.demilich.metastone.game.spells.trigger.RoastTrigger} spell; removing the attribute from the {@link
 * EntityReference#OUTPUT} this spell's sub spells; or, moving the {@link EntityReference#OUTPUT} from the {@link
 * Zones#DECK} will cancel the roasting and cause the card to remain where it is (the deck, or if it was moved, wherever
 * it was moved to).
 */
public final class RoastSpell extends RemoveCardSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int howMany = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		for (int i = 0; i < howMany; i++) {
			SpellDesc roast = desc.clone();
			// Use the TARGET_PLAYER to indicate whose card should be roasted.
			if (desc.containsKey(SpellArg.CARD_FILTER) && !player.getDeck().isEmpty()) {
				EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
				target = context.getLogic().getRandom(player.getDeck().filtered(cardFilter.matcher(context, player, source)));
				roast.put(SpellArg.TARGET, target.getReference());
			} else if (!desc.containsKey(SpellArg.TARGET)) {
				roast.put(SpellArg.TARGET, EntityReference.FRIENDLY_TOP_CARD);
				target = player.getDeck().peek();
			}

			if (target == null) {
				return;
			}

			SpellDesc addRoastAttribute = SetAttributeSpell.create(target.getReference(), Attribute.ROASTED, context.getTurn());
			context.getLogic().revealCard(player, target.getSourceCard());
			SpellUtils.castChildSpell(context, player, addRoastAttribute, source, target);
			context.fireGameEvent(new CardRoastedEvent(context, target.getSourceCard()));
			if (target.hasAttribute(Attribute.ROASTED)) {
				// Only actually remove the card if it was successfully roasted and not interrupted. Similar to the rules for
				// discard (provides a way to interrupt roasting).
				super.onCast(context, player, roast, source, target);
			}
		}
	}
}
