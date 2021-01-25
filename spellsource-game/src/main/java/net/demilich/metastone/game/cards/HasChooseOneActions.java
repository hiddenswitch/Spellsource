package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.actions.PlayCardAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface that specifies that the object has choose one behaviour.
 */
public interface HasChooseOneActions {
	@NotNull
	PlayCardAction[] playOptions();

	/**
	 * @return An action that corresponds to playing all the options together.
	 */
	@Nullable
	PlayCardAction playBothOptions();

	boolean hasBothOptions();
}
