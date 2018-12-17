package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.AttributeMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Shuffles copies of the specified {@code target} or {@link SpellArg#CARD_SOURCE} &amp; {@link SpellArg#CARD_FILTER} cards
 * into the deck. Creates {@link SpellArg#HOW_MANY} copies (default is 1).
 * <p>
 * When {@link SpellArg#EXCLUSIVE} is {@code true}, doesn't trigger a {@link net.demilich.metastone.game.events.CardShuffledEvent}.
 *
 * For <b>example,</b> this shuffles 3 Mur'Ghouls into the caster's deck:
 * <pre>
 *   {
 *     "class": "ShuffleToDeckSpell",
 *     "card": "token_mur'ghoul",
 *     "howMany": 3,
 *     "targetPlayer": "SELF"
 *   }
 * </pre>
 */
public class ShuffleToDeckSpell extends Spell {

	private static final long serialVersionUID = 6574621548720165755L;

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
				Card copy = CopyCardSpell.copyCard(context, player, target.getSourceCard(), (playerId, card) -> context.getLogic().shuffleToDeck(player, card, quiet));
				copy.getAttributes().putAll(map);
				if (copy.getZone() == Zones.DECK) {
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
				Card copy = CopyCardSpell.copyCard(context, player, original, (playerId, card) -> context.getLogic().shuffleToDeck(player, card, quiet));
				didShuffle.put(copy, copy.getZone() == Zones.DECK);
			}
		}

		for (Card card : didShuffle.keySet()) {
			if (didShuffle.get(card)) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
			}
		}
	}
}

