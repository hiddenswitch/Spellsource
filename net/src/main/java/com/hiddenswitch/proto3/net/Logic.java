package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.*;
import net.demilich.metastone.game.events.AfterPhysicalAttackEvent;
import net.demilich.metastone.game.events.BeforeSummonEvent;

/**
 * A Logic service that handles complex game logic.
 */
public interface Logic {
	/**
	 * A constant specifying the default starting decks for every new player.
	 */
	String[] STARTING_DECKS = {"Basic Resurrector", "Basic Octopod Demo", "Basic Cyborg", "Basic Biologist", "Basic Gamer",/* "Test Discoveries",  "Test Battlecries"*/};

	/**
	 * Performs account creation action side effects, like adding the first cards to the player's collection,
	 * defining their starting decks and in the future, creating friend recommendations.
	 *
	 * Some users, like test users or some kinds of bots, will not need starting decks or starting inventory.
	 * @param request The user to "initialize" for.
	 * @return Information about what this method did, like which decks it created and which cards the user was awarded.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	InitializeUserResponse initializeUser(InitializeUserRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Gracefully ends a game. This will return decks that are currently in use.
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
	 * @param request The users and their chosen deck IDs.
	 * @return Information that can be used to create a game.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	StartGameResponse startGame(StartGameRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Handles the networked effects when a minion is summoned.
	 *
	 * For example, The Forever Post-Doc is a minion whose text reads:
	 *
	 * <code>Call to Arms: If this is the first time you've played this minion, permanently cost (1) less.</code>
	 *
	 * Every time Forever Post-Doc is summoned, the Games service knows it must call beforeSummon to process the
	 * minion's persistent side effects. It will return the correct change in its cost for the Games service to apply
	 * to the live running game.
	 * @param request Information about the summoned minion.
	 * @return The side effects of summoning the minion which affect the game.
	 */
	@Suspendable
	LogicResponse beforeSummon(EventLogicRequest<BeforeSummonEvent> request);

	/**
	 * Handles the networked effects when an actor attacks another actor.
	 *
	 * For example, Sourcing Specialist is a minion whose text reads:
	 *
	 * <code>Call to Arms: Summon the last minion Sourcing Specialist destroyed.</code>
	 *
	 * Whenever Sourcing Specialist attacks and destroys its target, this method will correctly record the last minion
	 * it destroyed. Other code inside Sourcing Specialist looks up the attribute "LAST_MINION_DESTROYED_ID" to
	 * summon the actual minion. The purpose of this method is to record the last minion destroyed, but not to actually
	 * perform in-game summoning.
	 * @param logicRequest Information about the physical attack.
	 * @return The side effects of the physical attack which affect the game.
	 */
	@Suspendable
	LogicResponse afterPhysicalAttack(EventLogicRequest<AfterPhysicalAttackEvent> logicRequest);
}
