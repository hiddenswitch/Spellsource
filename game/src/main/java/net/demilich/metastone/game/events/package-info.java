/**
 * Events correspond to things that happen inside the game that other rules can react to.
 *
 * @see net.demilich.metastone.game.GameContext#fireGameEvent(net.demilich.metastone.game.events.GameEvent) for the
 * 		method where events are fired.
 * @see net.demilich.metastone.game.events.GameEvent for the base class of all events.
 * @see net.demilich.metastone.game.spells.trigger.Trigger for the interface that defines how things react to events.
 * @see net.demilich.metastone.game.spells.trigger.TriggerManager for how effects in response to events are evaluated.
 */
package net.demilich.metastone.game.events;