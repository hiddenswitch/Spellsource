package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import com.google.gson.annotations.SerializedName;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.List;

public class PlaySpellCardAction extends PlayCardAction {

	private SpellDesc spell;
	@SerializedName("cardReference2")
	protected EntityReference cardReference;

	protected PlaySpellCardAction() {
		super();
		setActionType(ActionType.SPELL);
	}

	public PlaySpellCardAction(SpellDesc spell, Card card, TargetSelection targetSelection) {
		super(card.getCardReference());
		setActionType(ActionType.SPELL);
		setTargetRequirement(targetSelection);
		this.setSpell(spell);
		this.cardReference = card.getReference();
	}

	@Override
	@Suspendable
	public void play(GameContext context, int playerId) {
		context.getLogic().castSpell(playerId, spell, cardReference, getTargetReference(), getTargetRequirement(), false);
	}

	public SpellDesc getSpell() {
		return spell;
	}

	public void setSpell(SpellDesc spell) {
		this.spell = spell;
	}

	public EntityReference getSourceCardEntityId() {
		return cardReference;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		if (getTargetReference() == null
				|| getTargetReference().isTargetGroup()) {
			return super.getDescription(context, playerId);
		}

		final Card source = context.resolveCardReference(getCardReference());
		final Entity target = context.resolveSingleTarget(getTargetReference());
		return String.format("%s played %s on %s", context.getActivePlayer().getName(), source.getName(), target.getName());
	}
}
