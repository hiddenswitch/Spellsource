package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.ShuffledEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.ShuffledOnlyOriginalCopiesTrigger;
import net.demilich.metastone.game.spells.trigger.ShuffledTrigger;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.AttributeMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Shuffles copies of the specified {@code target} or {@link SpellArg#CARD_SOURCE} &amp; {@link SpellArg#CARD_FILTER}
 * cards into the deck. Creates {@link SpellArg#HOW_MANY} copies (default is 1).
 * <p>
 * When {@link SpellArg#EXCLUSIVE} is {@code true}, marks the shuffled card as an extra copy for the purposes of the
 * {@link ShuffledTrigger}. To trigger only on non-extra copies, use {@link ShuffledOnlyOriginalCopiesTrigger}.
 * <p>
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

	public static SpellDesc create(String card) {
		SpellDesc desc = new SpellDesc(ShuffleToDeckSpell.class);
		desc.put(SpellArg.CARD, card);
		return desc;
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int copies = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		SpellDesc subSpell = (SpellDesc) (desc.getOrDefault(SpellArg.SPELL, NullSpell.create()));
		boolean extraCopy = desc.getBool(SpellArg.EXCLUSIVE);

		if (target != null
				&& !desc.containsKey(SpellArg.CARD)
				&& !desc.containsKey(SpellArg.CARDS)
				&& !desc.containsKey(SpellArg.CARD_SOURCE)
				&& !desc.containsKey(SpellArg.CARD_FILTER)) {
			// Implements Kingsbane in a very basic way, since weapons pretty much only get enchanted for attack,
			// durability, windfury, lifesteal and poisonous bonuses.
			AttributeMap map = SpellUtils.processKeptEnchantments(target, new AttributeMap());
			for (int i = 0; i < copies; i++) {
				Card copy = shuffle(context, player, null, target.getSourceCard(), extraCopy, source.getOwner());
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
				Card copy = shuffle(context, player, null, original, extraCopy, source.getOwner());
				didShuffle.put(copy, copy.getZone() == Zones.DECK);
			}
		}

		for (Card card : didShuffle.keySet()) {
			if (didShuffle.get(card)) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
			}
		}
	}

	@Suspendable
	protected Card shuffle(GameContext context, Player player, Entity targetEntity, Card targetCard, boolean extraCopy, int sourcePlayerId) {
		return CopyCardSpell.copyCard(context, player, targetCard, (playerId, card) -> context.getLogic().shuffleToDeck(player, card, extraCopy, sourcePlayerId));
	}
}

