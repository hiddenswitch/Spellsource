package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

public class PlayMinionCardAction extends PlayCardAction {

	private BattlecryAction battlecry;

	private PlayMinionCardAction() {
		super(null);
		setTargetRequirement(TargetSelection.FRIENDLY_MINIONS);
		setActionType(ActionType.SUMMON);
	}

	public PlayMinionCardAction(EntityReference EntityReference) {
		this(EntityReference, null);
	}

	public PlayMinionCardAction(EntityReference EntityReference, BattlecryAction battlecry) {
		super(EntityReference);
		this.battlecry = battlecry;
		setTargetRequirement(TargetSelection.FRIENDLY_MINIONS);
		setActionType(ActionType.SUMMON);
	}

	@Override
	@Suspendable
	protected void play(GameContext context, int playerId) {
		MinionCard minionCard = (MinionCard) context.getPendingCard();
		Actor nextTo = (Actor) (getTargetReference() != null ? context.resolveSingleTarget(getTargetReference()) : null);
		Minion minion = minionCard.summon();
		if (battlecry != null) {
			minion.setBattlecry(battlecry);
		}
		Player player = context.getPlayer(playerId);
		int index = player.getMinions().indexOf(nextTo);
		context.getLogic().summon(playerId, minion, minionCard, index, true);
	}

}
