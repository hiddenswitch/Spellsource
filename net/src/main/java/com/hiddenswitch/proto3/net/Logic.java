package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.impl.util.InventoryRecord;
import com.hiddenswitch.proto3.net.models.*;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.events.AfterPhysicalAttackEvent;
import net.demilich.metastone.game.events.BeforeSummonEvent;
import net.demilich.metastone.game.utils.AttributeMap;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * A Logic service that handles complex game logic.
 * <p>
 * To implement a new persistence effect, see {@link com.hiddenswitch.proto3.net.impl.util.PersistenceTrigger}.
 */
public interface Logic {
	/**
	 * A constant specifying the default starting decks for every new player.
	 */
	String[] STARTING_DECKS = {"Basic Resurrector", "Basic Octopod Demo", "Basic Cyborg", "Basic Biologist", "Basic Gamer",/* "Test Discoveries",  "Test Battlecries"*/};

	/**
	 * Performs account creation action side effects, like adding the first cards to the player's collection,
	 * defining their starting decks and in the future, creating friend recommendations.
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
	 * Starts a game for the given two users and their deck selections. This generates information the Games service
	 * can use to actually create a game. It does not create a connectable game. But it does convert a deck ID into
	 * an actual deck of cards. It fills in various attributes for the cards that are used for alliance / persistence
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
	 * Handles the networked effects when a minion is summoned.
	 * <p>
	 * For example, The Forever Post-Doc is a minion whose text reads:
	 * <p>
	 * <code>Call to Arms: If this is the first time you've played this minion, permanently cost (1) less.</code>
	 * <p>
	 * Every time Forever Post-Doc is summoned, the Games service knows it must call beforeSummon to process the
	 * minion's persistent side effects. It will return the correct change in its cost for the Games service to apply
	 * to the live running game.
	 *
	 * @param request Information about the summoned minion.
	 * @return The side effects of summoning the minion which affect the game.
	 * @see com.hiddenswitch.proto3.net.impl.util.PersistenceTrigger for more about how this method is used.
	 */
	@Suspendable
	LogicResponse beforeSummon(EventLogicRequest<BeforeSummonEvent> request);

	/**
	 * Converts an inventory record into a {@link CardDesc}, that eventually gets turned into an {@link
	 * net.demilich.metastone.game.cards.Card} in the game.
	 *
	 * @param cardRecord The record from the database describing a card in a player's collection.
	 * @param userId     The player to whom this card belongs.
	 * @param deckId     The deck that the caller is requesting this description record for.
	 * @return A completed card description.
	 * @see com.hiddenswitch.proto3.net.impl.util.PersistenceTrigger for more about how this method is used.
	 */
	static CardDesc getDescriptionFromRecord(InventoryRecord cardRecord, String userId, String deckId) {
		// Set up the attributes
		CardDesc desc = cardRecord.getCardDesc();
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
		// Collect the facts
		desc.attributes.put(Attribute.FIRST_TIME_PLAYS, cardRecord.getFirstTimePlays());
		desc.attributes.put(Attribute.ENTITY_INSTANCE_ID, RandomStringUtils.randomAlphanumeric(20).toLowerCase());
		desc.attributes.put(Attribute.LAST_MINION_DESTROYED_CARD_ID, cardRecord.getLastMinionDestroyedCardId());
		desc.attributes.put(Attribute.LAST_MINION_DESTROYED_INVENTORY_ID, cardRecord.getLastMinionDestroyedInventoryId());
		return desc;
	}

	/**
	 * Handles the networked effects when an actor attacks another actor.
	 * <p>
	 * For example, Sourcing Specialist is a minion whose text reads:
	 * <p>
	 * <code>Call to Arms: Summon the last minion Sourcing Specialist destroyed.</code>
	 * <p>
	 * Whenever Sourcing Specialist attacks and destroys its target, this method will correctly record the last minion
	 * it destroyed. Other code inside Sourcing Specialist looks up the attribute "LAST_MINION_DESTROYED_ID" to
	 * summon the actual minion. The purpose of this method is to record the last minion destroyed, but not to actually
	 * perform in-game summoning.
	 *
	 * @param logicRequest Information about the physical attack.
	 * @return The side effects of the physical attack which affect the game.
	 * @see com.hiddenswitch.proto3.net.impl.util.PersistenceTrigger for more about how this method is used.
	 */
	@Suspendable
	LogicResponse afterPhysicalAttack(EventLogicRequest<AfterPhysicalAttackEvent> logicRequest);
}
