package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.targeting.EntityReference;

public class PlayHeroCardAction extends PlayCardAction implements HasBattlecry {
	protected BattlecryAction battlecryAction;

	public PlayHeroCardAction(EntityReference EntityReference) {
		super(EntityReference);
		setActionType(ActionType.HERO);
		battlecryAction = null;
	}

	public PlayHeroCardAction(EntityReference EntityReference, BattlecryAction battlecryAction) {
		super(EntityReference);
		setActionType(ActionType.HERO);
		this.battlecryAction = battlecryAction;
	}

	@Override
	public void innerExecute(GameContext context, int playerId) {
		Card heroCard = context.getPendingCard();
		Hero hero = heroCard.createHero();
		if (battlecryAction != null) {
			hero.setBattlecry(battlecryAction);
		}
		context.getLogic().changeHero(context.getPlayer(playerId), hero, true);
	}

	@Override
	public BattlecryAction getBattlecryAction() {
		return battlecryAction;
	}

	@Override
	public void setBattlecryAction(BattlecryAction action) {
		battlecryAction = action;
	}
}
