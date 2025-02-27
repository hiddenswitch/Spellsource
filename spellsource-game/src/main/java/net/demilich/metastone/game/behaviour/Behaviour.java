package net.demilich.metastone.game.behaviour;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.logic.GameLogic;

import java.util.List;
import java.util.function.Consumer;

/**
 * Behaviours specify a delegate for player action and mulligan requests.
 * <p>
 * Behaviours live inside {@link Player} objects, and they're used by the {@link GameContext} to determine what action
 * or mulligan a player chose to do. In this model, the game context is responsible for calling the methods in this
 * class. The {@link GameLogic} typically executes as many game rules as possible until it reaches the next point where
 * an action is requested. The {@link GameContext} is then responsible for getting the next action.
 *
 * @see AbstractBehaviour for default implementations of some of these requests.
 * @see GameStateValueBehaviour for an example of an artificial-intelligence based behaviour.
 */
public interface Behaviour extends Cloneable {
	/***
	 * Clones the behaviour, typically with its internal state.
	 * @return A clone of the {@link Behaviour} instance.
	 */
	Behaviour clone();

	/**
	 * Gets a name for the behaviour. This should correspond to how the decisions are being made, e.g., a
	 * {@code "Human Behaviour"} or an {@code "AI Behaviour}.
	 *
	 * @return A {@link String} description of the behaviour.
	 */
	String getName();

	/**
	 * Use the provided context, player and first hand cards to determine which cards to discard during a mulligan phase.
	 *
	 * @param context The game context.
	 * @param player  The player who's mulliganing.
	 * @param cards   The cards in the player's first hand.
	 * @return The cards the player chose to discard.
	 */
	List<Card> mulligan(GameContext context, Player player, List<Card> cards);

	/**
	 * Notify the behaviour that the game is over, allowing it to clean up any state.
	 *
	 * @param context         The context of the game that has ended.
	 * @param playerId        The player that corresponds to this behaviour.
	 * @param winningPlayerId The winning player.
	 */
	void onGameOver(GameContext context, int playerId, int winningPlayerId);

	/**
	 * Requests an action from the player.
	 *
	 * @param context      The game context where the choice is being made.
	 * @param player       The player who is making the choice.
	 * @param validActions The valid actions the player has to choose from.
	 * @return One of the {@code validActions} that correspond to the player's choice.
	 */
	GameAction requestAction(GameContext context, Player player, List<GameAction> validActions);

	/**
	 * Asynchronously request a mulligan.
	 *
	 * @param context The game context.
	 * @param player  The player who is mulliganing.
	 * @param cards   The cards in the player's first hand.
	 * @param handler The callback when the player chose which cards to discard.
	 * @see #mulligan(GameContext, Player, List) for a complete description of this method.
	 */
	default void mulliganAsync(GameContext context, Player player, List<Card> cards, Consumer<List<Card>> handler) {
		final List<Card> mulligan = mulligan(context, player, cards);
		if (handler != null) {
			handler.accept(mulligan);
		}
	}

	/**
	 * Determines whether this behaviour's actions were determined by a human.
	 *
	 * @return {@code true} if the actions are determined by a human, {@code false} otherwise.
	 */
	default boolean isHuman() {
		return false;
	}
}
