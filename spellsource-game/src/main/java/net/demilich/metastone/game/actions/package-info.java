/**
 * Game actions are things players can do.
 * <p>
 * Game actions are typically emitted by the {@link net.demilich.metastone.game.GameContext#getValidActions()} method.
 * This will determine all the available actions for the active player. Then, actions are sent to {@link
 * net.demilich.metastone.game.behaviour.Behaviour} objects in the context, which decides which action to perform using
 * {@link net.demilich.metastone.game.behaviour.Behaviour#requestAction(net.demilich.metastone.game.GameContext,
 * net.demilich.metastone.game.Player, java.util.List)}.
 * <p>
 * Cards emit an action using {@link net.demilich.metastone.game.cards.Card#play()}. Minions do not emit actions;
 * instead, the {@link net.demilich.metastone.game.logic.ActionLogic} computes the physical attack actions using its
 * internal methods.
 *
 * @see net.demilich.metastone.game.actions.GameAction for the base class of all player actions.
 */
package net.demilich.metastone.game.actions;