package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.client.models.CardType;
import com.hiddenswitch.spellsource.client.models.Rarity;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Objects;

/**
 * Returns {@code true} if the {@link ConditionArg#TARGET} or {@code target} is not {@code null} and meets the
 * requirements specified by {@link ConditionArg#CARD_TYPE}, {@link ConditionArg#HERO_CLASS}, {@link ConditionArg#RACE}
 * and the card ID in {@link ConditionArg#CARD}.
 * <p>
 * If {@link ConditionArg#CARDS} is specified, that line's requirement is satisfied if any of the cards in the array of
 * {@link ConditionArg#CARDS} matches the target's card ID.
 */
public final class CardPropertyCondition extends Condition {

	public CardPropertyCondition(ConditionDesc desc) {
		super(desc);
	}

	public static Condition create(String cardId) {
		var desc = new ConditionDesc(CardPropertyCondition.class);
		desc.put(ConditionArg.CARD, cardId);
		return desc.create();
	}

	public static Condition create(EntityReference target, String cardId) {
		var desc = new ConditionDesc(CardPropertyCondition.class);
		desc.put(ConditionArg.TARGET, target);
		desc.put(ConditionArg.CARD, cardId);
		return desc.create();
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var card = target.getSourceCard();

		if (card == null) {
			return false;
		}

		var cardType = (CardType) desc.get(ConditionArg.CARD_TYPE);
		if (cardType != null && !GameLogic.isCardType(card.getCardType(), cardType)) {
			return false;
		}

		var cardId = (String) desc.get(ConditionArg.CARD);
		if (cardId != null && !card.getCardId().contains(cardId)) {
			return false;
		}

		var cardIds = (String[]) desc.get(ConditionArg.CARDS);
		if (cardIds != null && cardIds.length > 0) {
			var found = false;
			for (var i = 0; i < cardIds.length; i++) {
				if (Objects.equals(card.getCardId(), cardIds[i])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}

		var heroClass = (String) desc.get(ConditionArg.HERO_CLASS);
		if (heroClass != null && !card.hasHeroClass(heroClass)) {
			return false;
		}


		var race = (String) desc.get(ConditionArg.RACE);
		if (race != null && !Race.hasRace(context, card, race)) {
			return false;
		}

		var rarity = (Rarity) desc.get(ConditionArg.RARITY);
		if (rarity != null && !GameLogic.isRarity(card.getRarity(), rarity)) {
			return false;
		}

		return true;
	}

}
