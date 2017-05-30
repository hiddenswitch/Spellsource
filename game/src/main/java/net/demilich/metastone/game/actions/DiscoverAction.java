package net.demilich.metastone.game.actions;

import java.util.Collection;
import java.util.function.Predicate;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A DiscoverAction is a card and spell tuple that corresponds to a particular card selected by the player and the
 * spell that will take that card as an argument.
 * <p>
 * Typically, discover actions have a {@link net.demilich.metastone.game.spells.ReceiveCardSpell} that puts the
 * card in {@link #getCard()} into the player's hand. But any kind of spell that takes a {@link net.demilich.metastone.game.spells.desc.SpellArg#CARD}
 * argument will work with a DiscoverAction.
 */
public class DiscoverAction extends GameAction {
	/**
	 * Creates a discover action from the given spell description.
	 * @param spell A spell that takes {@link net.demilich.metastone.game.spells.desc.SpellArg#CARD} as an argument.
	 * @return A DiscoverAction.
	 */
	public static DiscoverAction createDiscover(SpellDesc spell) {
		DiscoverAction discover = new DiscoverAction(spell);
		discover.setTargetRequirement(TargetSelection.NONE);
		return discover;
	}

	private SpellDesc spell;
	private Condition condition;
	private Card card;
	private String name = "";
	private String description = "";

	private DiscoverAction() {
		setActionType(ActionType.DISCOVER);
	}

	protected DiscoverAction(SpellDesc spell) {
		this.spell = spell;
		setActionType(ActionType.DISCOVER);
	}

	/**
	 * Some discover actions cannot be called on certain kinds of cards. This is not currently used because
	 * DiscoverActions are not unrolled in {@link net.demilich.metastone.game.logic.ActionLogic#rollout(GameAction, GameContext, Player, Collection)}.
	 * @param context The context
	 * @param player The player
	 * @param entity The entity to test
	 * @return {@code true} if the discover action can be called on a particular entity.
	 */
	@Override
	public final boolean canBeExecutedOn(GameContext context, Player player, Entity entity) {
		if (!super.canBeExecutedOn(context, player, entity)) {
			return false;
		}
		if (getSource().getId() == entity.getId()) {
			return false;
		}
		if (getEntityFilter() == null) {
			return true;
		}
		return getEntityFilter().matches(context, player, entity);
	}

	@Override
	public DiscoverAction clone() {
		DiscoverAction clone = DiscoverAction.createDiscover(getSpell().clone());
		clone.setActionSuffix(getActionSuffix());
		clone.setSource(getSource());
		return clone;
	}

	@Override
	@Suspendable
	public void execute(GameContext context, int playerId) {
		EntityReference target = getSpell().hasPredefinedTarget() ? getSpell().getTarget() : getTargetKey();
		context.getLogic().castSpell(playerId, getSpell(), getSource(), target, false);
	}

	/**
	 * Gets a reference to the card this discover action corresponds to.
	 * @return The card.
	 */
	public Card getCard() {
		return card;
	}

	/**
	 * Gets a plain English description of this discover action for UI purposes, if required.
	 * @return A description of the card being discovered.
	 */
	@Deprecated
	public String getDescription() {
		return description;
	}

	/**
	 * Unused.
	 * @return An entity filter.
	 */
	public EntityFilter getEntityFilter() {
		return spell.getEntityFilter();
	}

	public String getName() {
		return name;
	}

	@Override
	public String getPromptText() {
		return "[Discover]";
	}

	public SpellDesc getSpell() {
		return spell;
	}

	@Override
	public boolean isSameActionGroup(GameAction anotherAction) {
		return false;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEntityFilter(Predicate<Entity> entityFilter) {
		// this.entityFilter = entityFilter;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("[%s '%s' %s]", getActionType(), getSpell().getSpellClass().getSimpleName(), "Test");
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(spell)
				.append(condition)
				.append(card)
				.toHashCode();
	}
}
