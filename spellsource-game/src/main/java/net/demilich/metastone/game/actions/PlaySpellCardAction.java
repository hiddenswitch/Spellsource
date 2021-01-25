package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.Objects;

/**
 * An action indicating a spell is being cast.
 * <p>
 * The spell effect is referenced in the {@link #getSpell()} field.
 */
public class PlaySpellCardAction extends PlayCardAction {

	private SpellDesc spell;

	public PlaySpellCardAction(SpellDesc spell, Card card, TargetSelection targetSelection) {
		super(card.getReference());
		setActionType(ActionType.SPELL);
		setTargetRequirement(targetSelection);
		this.setSpell(spell);
	}

	@Override
	public PlaySpellCardAction clone() {
		return (PlaySpellCardAction) super.clone();
	}

	@Override
	@Suspendable
	public void innerExecute(GameContext context, int playerId) {
		Entity source = Objects.equals(getSourceReference(), EntityReference.NONE) ? null : context.resolveSingleTarget(getSourceReference());
		Entity target = Objects.equals(getTargetReference(), EntityReference.NONE) ? null : context.resolveSingleTarget(getTargetReference());
		if (context.getLogic().spellsCastThrice(context.getPlayer(playerId), (Card) source, target)) {
			context.getLogic().castSpell(playerId, spell, getSourceReference(), getTargetReference(), getTargetRequirement(), true, this);
		}
		if (context.getLogic().spellsCastTwice(context.getPlayer(playerId), (Card) source, target)
				|| context.getLogic().spellsCastThrice(context.getPlayer(playerId), (Card) source, target)) {
			context.getLogic().castSpell(playerId, spell, getSourceReference(), getTargetReference(), getTargetRequirement(), true, this);
		}
		context.getLogic().castSpell(playerId, spell, getSourceReference(), getTargetReference(), getTargetRequirement(), false, this);
	}

	public SpellDesc getSpell() {
		return spell;
	}

	public void setSpell(SpellDesc spell) {
		this.spell = spell;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		if (getTargetReference() == null
				|| getTargetReference().isTargetGroup()) {
			return super.getDescription(context, playerId);
		}

		final Card source = (Card) context.resolveSingleTarget(getSourceReference());
		final Entity target = context.resolveSingleTarget(getTargetReference());
		return String.format("%s played %s on %s", context.getActivePlayer().getName(), source.getName(), target.getName());
	}
}

