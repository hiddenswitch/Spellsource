package net.demilich.metastone.game.actions;

/**
 * Indicates this action specifies one of possibly many cards as the choice that the player made to play.
 * <p>
 * This corresponds to the choice a player made for a choose-one spell.
 */
public interface HasChoiceCard {
	String getChoiceCardId();
}
