package net.demilich.metastone.game.actions;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

public class PlayChooseOneCardAction extends PlayCardAction implements HasChoiceCard {
	protected SpellDesc spell;
	protected final String chosenCard;

	public PlayChooseOneCardAction(SpellDesc spell, Card chooseOneCard, String chosenCard, TargetSelection targetSelection) {
		super(chooseOneCard.getReference());
		setActionType(ActionType.SPELL);
		setTargetRequirement(targetSelection);
		this.setSpell(spell);
		this.entityReference = chooseOneCard.getReference();
		this.chosenCard = chosenCard;
	}

	@Override
	public boolean canBeExecutedOn(GameContext context, Player player, Entity entity) {
		return CardCatalogue.getCardById(chosenCard).canBeCastOn(context, player, entity);
	}

	@Override
	@Suspendable
	public void innerExecute(GameContext context, int playerId) {
		context.getLogic().castChooseOneSpell(playerId, spell, entityReference, getTargetReference(), chosenCard, this);
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
