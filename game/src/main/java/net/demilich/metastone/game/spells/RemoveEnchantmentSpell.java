package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Expires and removes all the enchantments whose {@link Entity#getSourceCard()} has the card ID of {@link
 * net.demilich.metastone.game.spells.desc.SpellArg#CARD} from the {@code target}.
 */
public final class RemoveEnchantmentSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(RemoveEnchantmentSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.CARD);
		Card card = SpellUtils.getCard(context, desc);
		for (Trigger e : context.getTriggersAssociatedWith(target.getReference())) {
			if (e instanceof Enchantment) {
				Enchantment enchantment = (Enchantment) e;
				if (enchantment.getSourceCard() != null && enchantment.getSourceCard().getCardId().equals(card.getCardId())) {
					enchantment.onRemove(context);
					// TODO: What about targeting effects?
					context.getTriggerManager().removeTrigger(enchantment);
				}
			}
		}
	}
}
