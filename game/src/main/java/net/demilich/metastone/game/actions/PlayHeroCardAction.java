package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.targeting.CardReference;

public class PlayHeroCardAction extends PlayCardAction {
	private final BattlecryAction battlecryAction;

	public PlayHeroCardAction(CardReference cardReference) {
		super(cardReference);
		setActionType(ActionType.HERO);
		battlecryAction = null;
	}

	public PlayHeroCardAction(CardReference cardReference, BattlecryAction battlecryAction) {
		super(cardReference);
		setActionType(ActionType.HERO);
		this.battlecryAction = battlecryAction;
	}

	@Override
	protected void play(GameContext context, int playerId) {
		HeroCard heroCard = (HeroCard) context.getPendingCard();
		Hero hero = heroCard.createHero();
		if (battlecryAction != null) {
			hero.setBattlecry(battlecryAction);
		}
		context.getLogic().changeHero(context.getPlayer(playerId), hero, true);
	}
}
