package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRoastedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;

/**
 * Roasting a card removes the card from the top of the deck and adds the roasted enchantment to it. Always reveals the
 * card.
 */
public final class RoastCardSpell extends RemoveCardSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc roast = desc.clone();
		// Use the TARGET_PLAYER to indicate whose card should be roasted.
		if (!desc.containsKey(SpellArg.TARGET)) {
			roast.put(SpellArg.TARGET, EntityReference.FRIENDLY_TOP_CARD);
			target = player.getDeck().peek();
		}
		if (target == null) {
			return;
		}
		SpellDesc addRoastAttribute = SetAttributeSpell.create(target.getReference(), Attribute.ROASTED, context.getTurn());
		SpellUtils.castChildSpell(context, player, addRoastAttribute, source, target);
		context.getLogic().revealCard(player, target.getSourceCard());
		super.onCast(context, player, roast, source, target);
		context.fireGameEvent(new CardRoastedEvent(context, target.getSourceCard()));
	}
}
