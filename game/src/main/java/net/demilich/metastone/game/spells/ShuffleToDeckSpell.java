package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.utils.AttributeMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Shuffles copies of the specified {@code target} or {@link SpellArg#CARD_SOURCE} & {@link SpellArg#CARD_FILTER} cards
 * into the deck. Creates {@link SpellArg#HOW_MANY} copies (default is 1).
 * <p>
 * When {@link SpellArg#EXCLUSIVE} is {@code true}, doesn't trigger a {@link net.demilich.metastone.game.events.CardShuffledEvent}.
 */
public class ShuffleToDeckSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int copies = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		SpellDesc subSpell = (SpellDesc) (desc.getOrDefault(SpellArg.SPELL, NullSpell.create()));
		boolean quiet = desc.getBool(SpellArg.EXCLUSIVE);

		if (target != null) {
			// Implements Kingsbane in a very basic way, since weapons pretty much only get enchanted for attack,
			// durability, windfury, lifesteal and poisonous bonuses.
			AttributeMap map = SpellUtils.processKeptEnchantments(target, new AttributeMap());
			for (int i = 0; i < copies; i++) {
				final Card copy = target.getSourceCard().getCopy();
				copy.getAttributes().putAll(map);
				if (context.getLogic().shuffleToDeck(player, copy, quiet)) {
					SpellUtils.castChildSpell(context, player, subSpell, source, target, copy);
				}
			}
			return;
		}

		CardList cards = SpellUtils.getCards(context, player, target, source, desc,
				desc.getValue(SpellArg.VALUE, context, player, target, source, 1));

		Map<Card, Boolean> didShuffle = new HashMap<>();
		for (int i = 0; i < copies; i++) {
			for (Card original : cards) {
				Card copy = original.getCopy();
				didShuffle.put(copy, context.getLogic().shuffleToDeck(player, copy, quiet));
			}
		}

		for (Card card : didShuffle.keySet()) {
			if (didShuffle.get(card)) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
			}
		}
	}
}

