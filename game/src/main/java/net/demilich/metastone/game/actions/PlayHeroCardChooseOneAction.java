package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.cards.IChooseOneCard;
import net.demilich.metastone.game.targeting.CardReference;

public class PlayHeroCardChooseOneAction extends PlayHeroCardAction implements IChooseOneCard {
	public PlayHeroCardChooseOneAction(HeroCard heroCard, CardReference cardReference) {
		super(cardReference);
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
}
