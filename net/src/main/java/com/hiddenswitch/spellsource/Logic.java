package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.impl.util.LegacyPersistenceHandler;
import com.hiddenswitch.spellsource.impl.util.PersistenceTrigger;
import com.hiddenswitch.spellsource.models.*;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.AttributeMap;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * A Logic service that handles complex game logic.
 * <p>
 * To implement a new persistence effect, see {@link Spellsource#persistAttribute(LegacyPersistenceHandler)}.
 */
public interface Logic {

	/**
	 * Performs account creation action side effects, like adding the first cards to the player's collection, defining
	 * their starting decks and in the future, creating friend recommendations.
	 * <p>
	 * Some users, like test users or some kinds of bots, will not need starting decks or starting inventory.
	 *
	 * @param request The user to "initialize" for.
	 * @return Information about what this method did, like which decks it created and which cards the user was awarded.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	InitializeUserResponse initializeUser(InitializeUserRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Gracefully ends a game. This will return decks that are currently in use.
	 *
	 * @param request The game to end
	 * @return Information about the game that was ended.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	EndGameResponse endGame(EndGameRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Starts a game for the given two users and their deck selections. This generates information the Games service can
	 * use to actually create a game. It does not create a connectable game. But it does convert a deck ID into an
	 * actual deck of cards. It fills in various attributes for the cards that are used for alliance / persistence
	 * effects.
	 *
	 * @param request The users and their chosen deck IDs.
	 * @return Information that can be used to create a game.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	StartGameResponse startGame(StartGameRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Converts an inventory record into a {@link CardDesc}, that eventually gets turned into an {@link
	 * net.demilich.metastone.game.cards.Card} in the game.
	 *
	 * @param cardRecord The record from the database describing a card in a player's collection.
	 * @param userId     The player to whom this card belongs.
	 * @param deckId     The deck that the caller is requesting this description record for.
	 * @return A completed card description.
	 * @see PersistenceTrigger for more about how this method is used.
	 */
	static CardDesc getDescriptionFromRecord(InventoryRecord cardRecord, String userId, String deckId) {
		// Set up the attributes
		// Hearthstone cards are read directly from the database, since they do not support any mutability  rules.
		CardDesc desc = cardRecord.getCardDesc();
		if (desc.set.isHearthstoneSet()) {
			desc = CardCatalogue.getCardById(desc.id).getDesc();
		}

		if (desc.attributes == null) {
			desc.attributes = new AttributeMap();
		}

		desc.attributes.put(Attribute.USER_ID, userId);
		desc.attributes.put(Attribute.CARD_INVENTORY_ID, cardRecord.getId());
		desc.attributes.put(Attribute.DECK_ID, deckId);
		desc.attributes.put(Attribute.DONOR_ID, cardRecord.getDonorUserId());
		desc.attributes.put(Attribute.CHAMPION_ID, userId);
		desc.attributes.put(Attribute.COLLECTION_IDS, cardRecord.getCollectionIds());
		desc.attributes.put(Attribute.ALLIANCE_ID, cardRecord.getAllianceId());
		desc.attributes.put(Attribute.ENTITY_INSTANCE_ID, RandomStringUtils.randomAlphanumeric(20).toLowerCase());

		// Collect the persistent attributes
		desc.attributes.putAll(cardRecord.getPersistentAttributes());
		return desc;
	}

	/**
	 * Persists the requested attribute for an inventory ID.
	 *
	 * @param request The information abotu the attribute, inventory item and game necessary to save it to the
	 *                database.
	 * @return The results to apply to the entity.
	 */
	@Suspendable
	PersistAttributeResponse persistAttribute(PersistAttributeRequest request);
}
