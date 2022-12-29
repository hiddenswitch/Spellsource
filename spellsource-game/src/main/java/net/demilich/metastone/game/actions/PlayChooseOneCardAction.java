package net.demilich.metastone.game.actions;

import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

/**
 * Indicates the choice of a choose one card. The {@link Card#getCardId()} is stored in {@link #getChoiceCardId()}.
 */
public class PlayChooseOneCardAction extends PlayCardAction implements HasChoiceCard {

	protected SpellDesc spell;
	protected final String chosenCard;

	public PlayChooseOneCardAction(SpellDesc spell, Card chooseOneCard, String chosenCard, TargetSelection targetSelection) {
		super(chooseOneCard.getReference());
		setActionType(ActionType.SPELL);
		setTargetRequirement(targetSelection);
		this.setSpell(spell);
		setSourceReference(chooseOneCard.getReference());
		this.chosenCard = chosenCard;
	}

	@Override
	public PlayChooseOneCardAction clone() {
		return (PlayChooseOneCardAction) super.clone();
	}

	@Override
	public boolean canBeExecutedOn(GameContext context, Player player, Entity entity) {
		return CardCatalogue.getCardById(chosenCard).canBeCastOn(context, player, entity);
	}

	@Override
	public void innerExecute(GameContext context, int playerId) {
		context.getLogic().castChooseOneSpell(playerId, spell, getSourceReference(), getTargetReference(), chosenCard, this);
	}

	public SpellDesc getSpell() {
		return spell;
	}

	public void setSpell(SpellDesc spell) {
		this.spell = spell;
	}

	@Override
	public String getChoiceCardId() {
		return chosenCard;
	}
}
