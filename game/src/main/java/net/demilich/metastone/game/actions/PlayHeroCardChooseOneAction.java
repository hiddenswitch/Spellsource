package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.HasChooseOneActions;
import net.demilich.metastone.game.targeting.EntityReference;

public class PlayHeroCardChooseOneAction extends PlayHeroCardAction implements HasChooseOneActions {

	public PlayHeroCardChooseOneAction(Card heroCard, EntityReference EntityReference) {
		super(EntityReference);
	}

	@Override
	public PlayCardAction[] playOptions() {
		return new PlayCardAction[0];
	}

	@Override
	public PlayCardAction playBothOptions() {
		return null;
	}

	@Override
	public boolean hasBothOptions() {
		return false;
	}

	public void setBattlecryAction(BattlecryAction battlecry) {
		this.battlecryAction = battlecry;
	}
}
