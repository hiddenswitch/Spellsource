package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.HeroPowerUsedEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.io.Serializable;

/**
 * Indicates an action that is a hero power card.
 */
public final class HeroPowerAction extends PlaySpellCardAction implements HasChoiceCard, Serializable {

	private final String choiceCardId;

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
	public HeroPowerAction clone() {
		return (HeroPowerAction) super.clone();
	}

	@Override
	@Suspendable
	public void execute(GameContext context, int playerId) {
		GameLogic gameLogic = context.getLogic();
		Player player = context.getPlayer(playerId);
		Card power = (Card) getSource(context);
		// Hero powers could also cost health
		int modifiedManaCost = gameLogic.getModifiedManaCost(player, power);
		boolean cardCostsHealth = gameLogic.doesCardCostHealth(player, power);
		if (cardCostsHealth) {
			gameLogic.damage(player, (Actor) player.getHero(), modifiedManaCost, (Entity) power, true);
		} else {
			gameLogic.modifyCurrentMana(playerId, -modifiedManaCost, true);
			player.getStatistics().manaSpent(modifiedManaCost);
		}
		player.getStatistics().cardPlayed(power, context.getTurn());

		innerExecute(context, playerId);
		power.markUsed();
		context.getLogic().fireGameEvent(new HeroPowerUsedEvent(context, playerId, power));
	}

	@Override
	@Suspendable
	public void innerExecute(GameContext context, int playerId) {
		context.getLogic().castSpell(playerId, getSpell(), getSourceReference(), getTargetReference(), getTargetRequirement(), false, this);
	}

	@Override
	public String getChoiceCardId() {
		return choiceCardId;
	}
}
