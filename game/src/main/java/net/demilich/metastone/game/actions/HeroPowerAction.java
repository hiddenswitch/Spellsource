package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.io.Serializable;

public class HeroPowerAction extends PlaySpellCardAction implements HasChoiceCard, Serializable {
	private final String choiceCardId;

	private HeroPowerAction() {
		choiceCardId = null;
		setActionType(ActionType.HERO_POWER);
	}

	public HeroPowerAction(SpellDesc spell, Card card, TargetSelection targetRequirement, Card chosenCard) {
		super(spell, card, targetRequirement);
		this.choiceCardId = chosenCard.getCardId();
		setActionType(ActionType.HERO_POWER);
	}

	public HeroPowerAction(SpellDesc spell, Card card, TargetSelection targetSelection) {
		super(spell, card, targetSelection);
		setActionType(ActionType.HERO_POWER);
		choiceCardId = null;
	}

	@Override
	@Suspendable
	public void execute(GameContext context, int playerId) {
		innerExecute(context, playerId);
		context.getLogic().useHeroPower(playerId);
	}

	@Override
	@Suspendable
	public void innerExecute(GameContext context, int playerId) {
		context.getLogic().castSpell(playerId, getSpell(), entityReference, getTargetReference(), getTargetRequirement(), false, this);
	}

	@Override
	public String getChoiceCardId() {
		return choiceCardId;
	}
}
