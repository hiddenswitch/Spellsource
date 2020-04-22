package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Since choose one hero cards are implemented as different battlecry choices, a card reference to a choose one card is
 * not stored here.
 */
public class PlayHeroCardChooseOneAction extends PlayHeroCardAction {

	public PlayHeroCardChooseOneAction(EntityReference entityReference) {
		super(entityReference);
	}

	@Override
	public PlayHeroCardChooseOneAction clone() {
		return (PlayHeroCardChooseOneAction) super.clone();
	}
}
