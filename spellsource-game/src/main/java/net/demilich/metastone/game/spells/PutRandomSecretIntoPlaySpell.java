package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CardSourceDesc;
import net.demilich.metastone.game.spells.desc.source.DeckSource;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

/**
 * Takes {@link SpellArg#HOW_MANY} secret cards from the {@link SpellArg#CARD_SOURCE} (defaulting to a {@link
 * DeckSource}) and puts those secrets directly into play without triggering a {@link
 * net.demilich.metastone.game.spells.trigger.SecretPlayedTrigger}. Removes the secrets from the source if they were not
 * generated (e.g., if they came from the deck, the secret cards are removed from the deck).
 * <p>
 * This <b>example</b> implements the text, "Put one of each Secret from your deck into the battlefield:"
 * <pre>
 *   {
 *     "class": "PutRandomSecretIntoPlaySpell",
 *     "cardSource": {
 *       "class": "DeckSource"
 *     },
 *     "howMany": 60
 *   },
 * </pre>
 * Observe {@code "howMany"} is just the maximum number of cards in the deck.
 */
public class PutRandomSecretIntoPlaySpell extends Spell {

	private CardList findSecretCards(CardList cardList) {
		CardList secretCards = new CardArrayList();
		for (Card card : cardList) {
			if (card.isSecret()) {
				secretCards.addCard(card);
			}
		}
		return secretCards;
	}

	protected void onCast(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			CardSource cardSource,
			EntityFilter cardFilter,
			@Default(1) int howMany) {
		if (cardSource == null) {
			cardSource = new CardSourceDesc(DeckSource.class).create();
		}

		CardList secretCards = findSecretCards(cardSource.getCards(context, source, player));
		if (cardFilter != null) {
			secretCards = secretCards.filtered(cardFilter.matcher(context, player, source));
		}

		for (int i = 0; i < howMany; i++) {
			secretCards = secretCards.filtered(secretCard -> context.getLogic().canPlaySecret(player, secretCard));

			if (secretCards.isEmpty()) {
				return;
			}

			Card secretCard = context.getLogic().removeRandom(secretCards);

			putSecretIntoPlay(context, player, secretCard);
		}
	}

	protected void putSecretIntoPlay(GameContext context, Player player, Card secretCard) {
		if (secretCard.getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
			secretCard = secretCard.getCopy();
			secretCard.setId(context.getLogic().generateId());
			secretCard.setOwner(player.getId());
			secretCard.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
		}

		SpellDesc secretSpellDesc = secretCard.getSpell();
		Secret secret = (Secret) secretSpellDesc.get(SpellArg.SECRET);
		context.getLogic().playSecret(player, secret, false);
		if (secretCard.getZone() == Zones.DECK) {
			context.getLogic().removeCard(secretCard);
		} else {
			secretCard.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
			context.getLogic().removeCard(secretCard);
		}
	}
}