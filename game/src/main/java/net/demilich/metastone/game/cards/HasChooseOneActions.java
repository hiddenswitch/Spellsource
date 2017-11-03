package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.actions.PlayCardAction;

/**
 * An interface that specifies that the object has choose one behaviour.
 */
public interface HasChooseOneActions {
	PlayCardAction[] playOptions();

	/**
	 * @return An action that corresponds to playing all the options together.
	 */
	PlayCardAction playBothOptions();

	boolean hasBothOptions();
}
