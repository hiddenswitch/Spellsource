package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Indicates playing a hero card from the hand. This will eventually call {@link net.demilich.metastone.game.logic.GameLogic#changeHero(Player,
 * Hero, boolean)}. Since it is played from the hand, battlecries are resolved.
 */
public class PlayHeroCardAction extends PlayCardAction implements HasBattlecry {
	protected BattlecryDesc battlecry;

	public PlayHeroCardAction(EntityReference card) {
		super(card);
		setActionType(ActionType.HERO);
		battlecry = null;
	}

	public PlayHeroCardAction(EntityReference EntityReference, BattlecryDesc battlecry) {
		super(EntityReference);
		setActionType(ActionType.HERO);
		this.battlecry = battlecry;
	}

	@Override
	public PlayHeroCardAction clone() {
		PlayHeroCardAction clone = (PlayHeroCardAction) super.clone();
		clone.battlecry = battlecry != null ? battlecry.clone() : null;
		return clone;
	}

	@Override
	@Suspendable
	public void innerExecute(GameContext context, int playerId) {
		Card heroCard = (Card) context.resolveSingleTarget(getSourceReference());
		Hero hero = heroCard.createHero(context.getPlayer(playerId));
		if (battlecry != null) {
			hero.setBattlecry(battlecry);
		}
		context.getLogic().changeHero(context.getPlayer(playerId), hero, true);
	}

	@Override
	public BattlecryDesc getBattlecry() {
		return battlecry;
	}

	@Override
	public void setBattlecry(BattlecryDesc action) {
		battlecry = action;
	}
}
