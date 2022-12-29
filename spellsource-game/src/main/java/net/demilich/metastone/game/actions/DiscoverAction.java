package net.demilich.metastone.game.actions;

import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A DiscoverAction is a card and spell tuple that corresponds to a particular card selected by the player and the spell
 * that will take that card as an argument.
 * <p>
 * Typically, discover actions have a {@link net.demilich.metastone.game.spells.ReceiveCardSpell} that puts the card in
 * {@link #getCard()} into the player's hand. But any kind of spell that takes a {@link
 * net.demilich.metastone.game.spells.desc.SpellArg#CARD} argument will work with a DiscoverAction.
 * <p>
 * Discover cards should never be executed directly. They are presented to the {@link Behaviour} in {@link
 * GameLogic#requestAction(Player, List)}, so they will always be some kind of "recursive" call inside a {@link
 * Behaviour}.
 */
public class DiscoverAction extends GameAction implements HasCard {

	private final SpellDesc spell;
	private Card card;

	private static final String DISCOVERED_NAME = "discovered";

	/**
	 * Creates a discover action from the given spell description.
	 *
	 * @param spell A spell that takes {@link net.demilich.metastone.game.spells.desc.SpellArg#CARD} as an argument.
	 * @return A DiscoverAction.
	 */
	public static DiscoverAction createDiscover(@NotNull SpellDesc spell) {
		DiscoverAction discover = new DiscoverAction(spell);
		discover.setTargetRequirement(TargetSelection.NONE);
		return discover;
	}

	protected DiscoverAction(@NotNull SpellDesc spell) {
		this.spell = spell;
		setActionType(ActionType.DISCOVER);
	}

	@Override
	public DiscoverAction clone() {
		return (DiscoverAction) super.clone();
	}

	/**
	 * Some discover actions cannot be called on certain kinds of cards. This is not currently used because
	 * DiscoverActions are not unrolled in {@link net.demilich.metastone.game.logic.ActionLogic#rollout(GameAction,
	 * GameContext, Player, Collection)}.
	 *
	 * @param context The context
	 * @param player  The player
	 * @param entity  The entity to test
	 * @return {@code true} if the discover action can be called on a particular entity.
	 */
	@Override
	public final boolean canBeExecutedOn(GameContext context, Player player, Entity entity) {
		return false;
	}

	@Override
	public void execute(GameContext context, int playerId) {
	}

	/**
	 * Gets a reference to the card this discover action corresponds to.
	 *
	 * @return The card.
	 */
	public Card getCard() {
		return card;
	}

	@Override
	public Entity getSource(GameContext context) {
		return context.resolveSingleTarget(getSourceReference());
	}

	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		return Collections.singletonList(context.resolveSingleTarget(getTargetReference()));
	}

	public String getDescription(GameContext context, int playerId) {
		if (playerId == context.getActivePlayerId()) {
			return String.format("With %s, %s %s %s.", getSource(context).getName(), context.getPlayer(playerId).getName(), DISCOVERED_NAME, getCard().getName());
		} else {
			return String.format("With %s, %s %s a card.", getSource(context).getName(), context.getPlayer(context.getActivePlayerId()).getName(), DISCOVERED_NAME, getCard().getName());
		}
	}

	@NotNull
	public SpellDesc getSpell() {
		return spell;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("card", getCard() != null ? getCard().getCardId() : "(none)")
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DiscoverAction)) return false;
		if (!super.equals(o)) return false;
		DiscoverAction that = (DiscoverAction) o;
		return Objects.equals(getSpell(), that.getSpell()) &&
				Objects.compare(getCard(), that.getCard(), Card::compareTo) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getSpell(), getCard());
	}

	@Override
	public Card getSourceCard() {
		return card;
	}
}
