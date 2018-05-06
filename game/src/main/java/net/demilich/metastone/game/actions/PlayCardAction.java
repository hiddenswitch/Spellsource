package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.CardType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Collections;
import java.util.List;

/**
 * A play card action stores a card and possibly choose one option index for a card and target.
 */
public abstract class PlayCardAction extends GameAction {

	public static Logger logger = LoggerFactory.getLogger(PlayCardAction.class);
	protected EntityReference entityReference;

	protected PlayCardAction() {
	}

	public PlayCardAction(EntityReference EntityReference) {
		this.entityReference = EntityReference;
	}

	@Override
	public boolean canBeExecutedOn(GameContext context, Player player, Entity entity) {
		Card card = (Card) context.resolveSingleTarget(getEntityReference());
		if (card.isSpell()) {
			return card.canBeCastOn(context, player, entity);
		}

		return true;
	}

	@Override
	@Suspendable
	public void execute(GameContext context, int playerId) {
		Card card = (Card) context.resolveSingleTarget(getEntityReference());
		context.setPendingCard(card);

		context.getLogic().playCard(playerId, getEntityReference());
		// card was countered, do not actually resolve its effects
		if (!card.hasAttribute(Attribute.COUNTERED)) {
			play(context, playerId);
		}

		context.getLogic().afterCardPlayed(playerId, getEntityReference());
		context.setPendingCard(null);
	}

	public EntityReference getEntityReference() {
		return entityReference;
	}

	@Suspendable
	protected abstract void play(GameContext context, int playerId);

	@Override
	public String toString() {
		return String.format("%s Card: %s Target: %s", getActionType(), entityReference, getTargetReference());
	}

	@Override
	public Entity getSource(GameContext context) {
		return (Card) context.resolveSingleTarget(getEntityReference());
	}

	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		final List<Entity> entities = context.resolveTarget(context.getPlayer(player), getSource(context), getTargetReference());
		return entities == null ? Collections.emptyList() : entities;
	}

	@Override
	public EntityReference getSourceReference() {
		return entityReference;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		Card playedCard = (Card) context.resolveSingleTarget(getEntityReference());
		String cardName = playedCard != null ? playedCard.getName() : "an unknown card";
		if (playedCard.getCardType() == CardType.SPELL
				&& playedCard.isSecret()) {
			cardName = "a secret";
		}
		return String.format("%s played %s.", context.getActivePlayer().getName(), cardName);
	}

	@Override
	public PlayCardAction clone() {
		return (PlayCardAction) super.clone();
	}
}
