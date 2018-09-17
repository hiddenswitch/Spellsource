package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Expires and removes all the enchantments whose {@link Entity#getSourceCard()} has the card ID of {@link
 * net.demilich.metastone.game.spells.desc.SpellArg#CARD} from the {@code target}.
 */
public final class RemoveEnchantmentSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(RemoveEnchantmentSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.CARD);
		CardList cards = SpellUtils.getCards(context, player, null, source, desc);
		if (cards.isEmpty()) {
			return;
		}
		Set<String> cardIds = cards.stream().map(Card::getCardId).collect(Collectors.toSet());
		for (Trigger e : context.getTriggersAssociatedWith(target.getReference())) {
			if (e instanceof Enchantment) {
				Enchantment enchantment = (Enchantment) e;
				if (enchantment.getSourceCard() != null && cardIds.contains(enchantment.getSourceCard().getCardId())) {
					enchantment.onRemove(context);
					// TODO: What about targeting effects?
					context.getTriggerManager().removeTrigger(enchantment);
				}
			}
		}
	}
}
