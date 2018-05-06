package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.HasChooseOneActions;
import net.demilich.metastone.game.targeting.EntityReference;

public class PlayHeroCardChooseOneAction extends PlayHeroCardAction {

	public PlayHeroCardChooseOneAction(Card heroCard, EntityReference EntityReference) {
		super(EntityReference);
	}
}
