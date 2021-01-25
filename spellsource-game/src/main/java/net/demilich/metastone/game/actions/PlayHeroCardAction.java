package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.spells.desc.OpenerDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Indicates playing a hero card from the hand. This will eventually call {@link net.demilich.metastone.game.logic.GameLogic#changeHero(Player,
 * net.demilich.metastone.game.entities.Entity, Hero, boolean)}. Since it is played from the hand, battlecries are
 * resolved.
 */
public class PlayHeroCardAction extends PlayCardAction implements OpenerOverridable {
	protected OpenerDesc opener;
	private boolean resolveOpener = true;

	public PlayHeroCardAction(EntityReference card) {
		super(card);
		setActionType(ActionType.HERO);
	}

	public PlayHeroCardAction(EntityReference EntityReference, OpenerDesc opener) {
		super(EntityReference);
		setActionType(ActionType.HERO);
		this.opener = opener;
	}

	@Override
	public PlayHeroCardAction clone() {
		PlayHeroCardAction clone = (PlayHeroCardAction) super.clone();
		clone.opener = opener != null ? opener.clone() : null;
		return clone;
	}

	@Override
	@Suspendable
	public void innerExecute(GameContext context, int playerId) {
		Card heroCard = (Card) context.resolveSingleTarget(getSourceReference());
		Hero hero = heroCard.hero();
		context.getLogic().changeHero(context.getPlayer(playerId), heroCard, hero, getResolveOpener());
	}

	@Override
	public OpenerDesc getOpener() {
		return opener;
	}

	@Override
	public void setOpener(OpenerDesc desc) {
		opener = desc;
	}


	@Override
	public void setResolveOpener(boolean resolveOpener) {
		this.resolveOpener = resolveOpener;
	}

	@Override
	public boolean getResolveOpener() {
		return resolveOpener;
	}
}
