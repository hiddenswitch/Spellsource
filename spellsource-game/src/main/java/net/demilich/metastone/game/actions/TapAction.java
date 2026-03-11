package net.demilich.metastone.game.actions;

import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.TapEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.io.Serializable;

public class TapAction extends GameAction implements Serializable {

	private SpellDesc spell;
	private EntityReference minionReference;
	private int tapIndex;
	private int cost;

	public TapAction(SpellDesc spell, Entity source, EntityReference minionReference, TargetSelection targetSelection, int tapIndex) {
		this(spell, source, minionReference, targetSelection, tapIndex, 0);
	}

	public TapAction(SpellDesc spell, Entity source, EntityReference minionReference, TargetSelection targetSelection, int tapIndex, int cost) {
		setActionType(ActionType.TAP);
		setSourceReference(source.getReference());
		this.spell = spell;
		this.minionReference = minionReference;
		this.tapIndex = tapIndex;
		this.cost = cost;
		setTargetRequirement(targetSelection);
	}

	@Override
	public TapAction clone() {
		TapAction clone = (TapAction) super.clone();
		clone.spell = spell.cloneAndUnfreeze();
		return clone;
	}

	@Override
	public boolean canBeExecutedOn(GameContext context, Player player, Entity entity) {
		Entity source = context.resolveSingleTarget(getSourceReference());
		if (source instanceof Card) {
			Card card = (Card) source;
			if (card.getSpell() != null) {
				return card.canBeCastOn(context, player, entity);
			}
		}
		// For tap actions, check the tap spell's filter instead
		if (spell != null && spell.getEntityFilter() != null) {
			return spell.getEntityFilter().matches(context, player, entity, source);
		}
		return true;
	}

	@Override
	public void execute(GameContext context, int playerId) {
		Entity entity = context.resolveSingleTarget(minionReference);
		if (entity == null) {
			return;
		}

		if (cost > 0) {
			context.getLogic().modifyCurrentMana(playerId, -cost, true);
		}

		context.getLogic().castSpell(playerId, spell, getSourceReference(), getTargetReference(), getTargetRequirement(), false, this);

		context.getLogic().fireGameEvent(new TapEvent(context, playerId, entity.getSourceCard()));

		if (entity instanceof Minion) {
			entity.setAttribute(Attribute.USED_THIS_TURN, 1);
		}
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		Entity entity = context.resolveSingleTarget(minionReference);
		if (entity == null) {
			return "Tap";
		}
		if (getTargetReference() != null && !getTargetReference().isTargetGroup()) {
			Entity target = context.resolveSingleTarget(getTargetReference());
			if (target != null) {
				return String.format("%s activated %s on %s", context.getActivePlayer().getName(), entity.getName(), target.getName());
			}
		}
		return String.format("%s activated %s", context.getActivePlayer().getName(), entity.getName());
	}

	public SpellDesc getSpell() {
		return spell;
	}

	public EntityReference getMinionReference() {
		return minionReference;
	}

	public int getTapIndex() {
		return tapIndex;
	}
}
