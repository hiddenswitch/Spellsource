package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.OpenerDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Collection;

/**
 * An action representing the playing of a minion card.
 * <p>
 * The {@link PlayCardAction#getTargetReference()} refers to the minion to whose left the minion should be summoned
 * (like inserting an element into an array). If {@code null}, the index passed to the summon function will be {@code
 * -1}, which indicates to summon the minion in the rightmost slot.
 * <p>
 * Like other {@link PlayCardAction} actions, this action is typically rolled out by {@link
 * net.demilich.metastone.game.logic.ActionLogic#rollout(GameAction, GameContext, Player, Collection)}.
 * <p>
 * Typically, the battlecry is resolved in this action. The {@link #PlayMinionCardAction(net.demilich.metastone.game.targeting.EntityReference)}
 * will retrieve the battlecry that appears once the minion is summoned. Choose one minion cards override the battlecry
 * using {@link #PlayMinionCardAction(EntityReference, OpenerDesc)}.
 */
public final class PlayMinionCardAction extends PlayCardAction implements OpenerOverridable {
	private OpenerDesc opener;
	private boolean resolveOpener = true;

	public PlayMinionCardAction(EntityReference EntityReference) {
		this(EntityReference, null);
	}

	public PlayMinionCardAction(EntityReference minionCard, OpenerDesc opener) {
		super(minionCard);
		this.opener = opener;
		setTargetRequirement(TargetSelection.FRIENDLY_MINIONS);
		setActionType(ActionType.SUMMON);
	}

	@Override
	public PlayMinionCardAction clone() {
		PlayMinionCardAction clone = (PlayMinionCardAction) super.clone();
		clone.opener = opener != null ? opener.clone() : null;
		return clone;
	}

	@Override
	@Suspendable
	public void innerExecute(GameContext context, int playerId) {
		Card card = (Card) context.resolveSingleTarget(getSourceReference());
		Actor nextTo = (Actor) (getTargetReference() != null ? context.resolveSingleTarget(getTargetReference()) : null);
		Minion minion = card.minion();
		Player player = context.getPlayer(playerId);
		int index = player.getMinions().indexOf(nextTo);
		if (card.hasAttribute(Attribute.MAGNETIC) && nextTo instanceof Minion && Race.hasRace(context, nextTo, "MECH")) {
			context.getLogic().magnetize(playerId, card, (Minion) nextTo);
		} else {
			minion.getAttributes().remove(Attribute.MAGNETIC);
			context.getLogic().summon(playerId, minion, card, index, getResolveOpener());
		}
	}

	@Override
	public OpenerDesc getOpener() {
		return opener;
	}

	@Override
	public void setOpener(OpenerDesc action) {
		opener = action;
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
