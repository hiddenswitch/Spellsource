package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.PlayCardAction;
import org.jetbrains.annotations.Nullable;

/**
 * An interface that specifies that the object has choose one behaviour.
 */
public interface HasChooseOneActions {
	PlayCardAction[] playOptions(GameContext context);

	/**
	 * @return An action that corresponds to playing all the options together.
	 */
	@Nullable
	PlayCardAction playBothOptions(GameContext context);

	boolean hasBothOptions();
}
