package net.demilich.metastone.game.logic;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Stateless service provider interface for pluggable game ruleset mechanics (such as Hearthstone-specific keywords).
 * <p>
 * Implementations are discovered once via {@link ServiceLoader} and invoked at key lifecycle points in
 * {@link GameLogic}.
 */
public interface GameLogicPlugin {
	GameLogicPlugin[] PLUGINS = loadPlugins();

	private static GameLogicPlugin[] loadPlugins() {
		List<GameLogicPlugin> list = new ArrayList<>();
		for (GameLogicPlugin plugin : ServiceLoader.load(GameLogicPlugin.class)) {
			list.add(plugin);
		}
		return list.toArray(new GameLogicPlugin[0]);
	}

	/**
	 * Invoked for each player during {@link GameLogic#startGameForPlayer(Player)} after deck and hand enchantments are
	 * initialized.
	 */
	default void onGameStart(GameContext context, Player player) {
	}

	/**
	 * Invoked during {@link GameLogic#startTurn(int)} before the turn start event is fired.
	 */
	default void onTurnStart(GameContext context, Player player) {
	}

	/**
	 * Invoked during {@link GameLogic#endTurn(int)} after the turn end event is fired.
	 */
	default void onTurnEnd(GameContext context, Player player) {
	}

	/**
	 * Invoked during {@link GameLogic#playCard} when a card is played from hand, before it is moved out of the hand.
	 */
	default void onCardPlayedFromHand(GameContext context, Player player, Card card, int handIndex, int handSize) {
	}

	/**
	 * Invoked during {@link GameLogic#summon} after the minion's summon events and enchantments have been processed.
	 */
	default void onMinionSummoned(GameContext context, Player player, Minion minion, Entity source, boolean resolveOpener) {
	}

	/**
	 * Invoked during {@link GameLogic#damage} after damage is dealt to an actor.
	 */
	default void onAfterDamage(GameContext context, Player player, Actor target, int damageDealt, Entity source, boolean startedDestroyed) {
	}

	/**
	 * Invoked during {@link GameLogic#destroy(Actor...)} after a destroyed minion's aftermaths have been resolved.
	 */
	default void onMinionDeathResolved(GameContext context, Player owner, Actor target, EntityLocation previousLocation) {
	}

	/**
	 * Invoked during {@link GameLogic#addEnchantments} after enchantments from an enchantment source card have been
	 * applied to a host entity.
	 */
	default void onEnchantmentAdded(GameContext context, Player player, Entity effectSource, Card enchantmentSource, Entity host) {
	}

	/**
	 * Invoked during {@link GameLogic#castSpell} after a spell is cast.
	 */
	default void onSpellCasted(GameContext context, Player player, Entity source, SpellDesc spellDesc, List<Entity> targets, TargetSelection targetSelection, GameAction sourceAction, boolean childSpell) {
	}

	/**
	 * Invoked during {@link GameLogic#secretTriggered} when a secret is triggered and revealed.
	 */
	default void onSecretRevealed(GameContext context, Player player, Card secretCard) {
	}
}
