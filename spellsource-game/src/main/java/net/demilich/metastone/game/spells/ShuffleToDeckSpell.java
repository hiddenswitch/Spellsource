package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.ShuffledOnlyOriginalCopiesTrigger;
import net.demilich.metastone.game.spells.trigger.ShuffledTrigger;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.cards.AttributeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shuffles copies of the specified {@code target} or {@link SpellArg#CARD_SOURCE} &amp; {@link SpellArg#CARD_FILTER}
 * cards into the deck. Creates {@link SpellArg#HOW_MANY} copies (default is 1).
 * <p>
 * When {@link SpellArg#EXCLUSIVE} is {@code true}, marks the shuffled card as an extra copy for the purposes of the
 * {@link ShuffledTrigger}. To trigger only on non-extra copies, use {@link ShuffledOnlyOriginalCopiesTrigger}.
 * <p>
 * Shuffling cards to the deck when generated from a {@link net.demilich.metastone.game.spells.desc.source.HasCardCreationSideEffects}
 * card source is done <b>without replacement</b>.
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

	private static Logger LOGGER = LoggerFactory.getLogger(ShuffleToDeckSpell.class);

	public static SpellDesc create(String card) {
		SpellDesc desc = new SpellDesc(ShuffleToDeckSpell.class);
		desc.put(SpellArg.CARD, card);
		return desc;
	}

	protected void onCast(
			GameContext context,
			Player player,
			SpellDesc desc,
			Entity source,
			Entity target,
			@Default(1) int howMany,
			SpellDesc spell,
			boolean exclusive) {
		SpellDesc subSpell = spell != null ? spell : NullSpell.create();

		if (target != null
				&& !desc.containsKey(SpellArg.CARD)
				&& !desc.containsKey(SpellArg.CARDS)
				&& !desc.containsKey(SpellArg.CARD_SOURCE)
				&& !desc.containsKey(SpellArg.CARD_FILTER)) {
			// Implements Kingsbane in a very basic way, since weapons pretty much only get enchanted for attack,
			// durability, windfury, lifesteal and poisonous bonuses.
			AttributeMap map = SpellUtils.processKeptEnchantments(target, new AttributeMap());
			var keptEnchantments = SpellUtils.extractKeptEnchantments(context, target);
			Card baseTargetCard = (target instanceof net.demilich.metastone.game.entities.Actor && target.hasAttribute(net.demilich.metastone.game.cards.Attribute.KEEPS_ENCHANTMENTS))
					? context.getCardById(target.getSourceCard().getCardId())
					: target.getSourceCard();
			for (int i = 0; i < howMany; i++) {
				Card copy = shuffle(context, player, source, null, baseTargetCard, exclusive, source.getOwner());
				if (copy == null) {
					LOGGER.warn("onCast {} {}: Failed to shuffle {}, deck size was {}", context.getGameId(), source, target, player.getDeck().size());
					continue;
				}
				copy.getAttributes().putAll(map);
				SpellUtils.applyKeptEnchantments(context, player, source, copy, keptEnchantments);
				if (copy.getZone() == Zones.DECK) {
					SpellUtils.castChildSpell(context, player, subSpell, source, target, copy);
				}
			}
			return;
		}

		CardList cards = SpellUtils.getCards(context, player, target, source, desc,
				desc.getValue(SpellArg.VALUE, context, player, target, source, 1));

		Map<Card, Boolean> didShuffle = new LinkedHashMap<>();
		for (int i = 0; i < howMany; i++) {
			for (Card original : cards) {
				Card copy = shuffle(context, player, source, null, original, exclusive, source.getOwner());
				if (copy != null) {
					didShuffle.put(copy, copy.getZone() == Zones.DECK);
				}
			}
		}

		for (Card card : didShuffle.keySet()) {
			if (didShuffle.get(card)) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
			}
		}
	}

	protected Card shuffle(GameContext context, Player player, Entity source, Entity targetEntity, Card targetCard, boolean extraCopy, int sourcePlayerId) {
		return CopyCardSpell.copyCard(context, player, source, targetCard, (playerId, card) -> context.getLogic().shuffleToDeck(player, card, extraCopy, sourcePlayerId));
	}
}

