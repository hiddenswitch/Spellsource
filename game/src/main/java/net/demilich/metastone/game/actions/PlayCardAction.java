package net.demilich.metastone.game.actions;

import com.github.fromage.quasi.fibers.Suspendable;
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

	/**
	 * Plays a card from the hand. Evaluates whether the card was countered, increments combos, and deducts mana.
	 *
	 * @param context
	 * @param playerId
	 */
	@Override
	@Suspendable
	public void execute(GameContext context, int playerId) {
		Card card = (Card) context.resolveSingleTarget(getEntityReference());

		context.getLogic().playCard(playerId, getEntityReference());
		// card was countered, do not actually resolve its effects
		if (!card.hasAttribute(Attribute.COUNTERED)) {
			innerExecute(context, playerId);
			if (card.hasAttribute(Attribute.ECHO)
					|| card.hasAttribute(Attribute.AURA_ECHO)) {
				Card copy = card.getCopy();
				copy.setAttribute(Attribute.REMOVES_SELF_AT_END_OF_TURN);
				context.getLogic().receiveCard(playerId, copy);
			}
		}

		context.getLogic().afterCardPlayed(playerId, getEntityReference());
	}

	public EntityReference getEntityReference() {
		return entityReference;
	}

	/**
	 * Represents the consequences of playing a spell card, minion card, hero card, hero power card, etc.
	 * <p>
	 * <p>
	 * Unlike {@link #execute(GameContext, int)}, this method will not deduct mana, will not be counterable, and will not
	 * increment combos. In other words, this method omits the effects of playing a card from the hand.
	 * <p>
	 * However, by using the action, a {@link net.demilich.metastone.game.targeting.TargetSelection} will still occur.
	 * <p>
	 * The {@link EntityReference#TARGET} will refer to whatever is the {@link #getTargetReference()}.
	 * <p>
	 * The {@link #getTargetRequirement()} indicates whether or not these effects require a target to be selected.
	 *
	 * @param context
	 * @param playerId
	 */
	@Suspendable
	public abstract void innerExecute(GameContext context, int playerId);


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
