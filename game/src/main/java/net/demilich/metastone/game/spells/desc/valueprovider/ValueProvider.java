package net.demilich.metastone.game.spells.desc.valueprovider;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterArg;

import java.io.Serializable;

/**
 * Value providers compute an integer value given {@link ValueProviderArg}, an underlying implementation, and the {@code
 * player}, {@code host} and {@code target} from whose point of view the value should be calculated.
 * <p>
 * Value providers are typically executed by {@link net.demilich.metastone.game.cards.desc.Desc#getValue(Enum,
 * GameContext, Player, Entity, Entity, int)}.
 * <p>
 * All value providers support the following features: <ul> <li>{@link ValueProviderArg#OFFSET}: Specifies an amount to
 * always add (possibly negative) to the calculated value. Defaults to {@code 0}.</li> <li>{@link
 * ValueProviderArg#MULTIPLIER}: Specifies an amount to always multiply (possible negative) to the calculate value.
 * Defaults to {@code 1}. This is applied before the offset.</li> <li>{@link ValueProviderArg#TARGET_PLAYER}: Specifies
 * a change to whose point of view the value provider should be computed. For example, if the {@code player} is casting
 * a spell that calculates something from the {@link TargetPlayer#BOTH} point of view, the result of this value provider
 * for both player's points of view are added together.</li> </ul>
 * <p>
 * Observe that {@link ValueProviderArg#TARGET} is not available to all value providers. The {@code target} that the
 * value provider is evaluated against depends on the context and the {@link ValueProviderArg#TARGET} is ignored unless
 * the underlying class (like e.g. {@link AttributeValueProvider}) specifically handles the {@link
 * ValueProviderArg#TARGET} argument. It is almost always the {@link net.demilich.metastone.game.spells.desc.SpellArg#TARGET}.
 * <p>
 * The {@code target} is interpreted as: <ul> <li>The {@link net.demilich.metastone.game.spells.desc.SpellArg#TARGET} of
 * the spell calling this value provider.</li> <li>The {@link GameEvent#getTarget()} when a value provider is used in a
 * condition on a {@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc}.</li> <li>The {@link
 * net.demilich.metastone.game.cards.Card} affected by the {@link net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg#TARGET}
 * argument of a card cost modifier.</li> <li>{@code null} in {@link net.demilich.metastone.game.spells.MissilesSpell}
 * and {@link net.demilich.metastone.game.spells.HealingMissilesSpell}'s {@code getValue} calls.</li> <li>{@code null}
 * if the {@link net.demilich.metastone.game.spells.desc.filter.AttributeFilter} has a {@link EntityFilterArg#TARGET}
 * that evaluates to zero entities.</li> <li>{@code null} in all other situations.</li> </ul>
 * <p>
 * Refer to the class hierarchy of this class for all the possible value providers.
 */
public abstract class ValueProvider implements Serializable, HasDesc<ValueProviderDesc> {
	private ValueProviderDesc desc;

	public ValueProvider(ValueProviderDesc desc) {
		this.desc = desc;
	}

	@Suspendable
	public int getValue(GameContext context, Player player, Entity target, Entity host) {
		TargetPlayer targetPlayer = (TargetPlayer) getDesc().get(ValueProviderArg.TARGET_PLAYER);
		if (targetPlayer == null) {
			targetPlayer = TargetPlayer.SELF;
		}
		Player providingPlayer = null;
		switch (targetPlayer) {
			case ACTIVE:
				providingPlayer = context.getActivePlayer();
				break;
			case BOTH:
				int multiplier = getDesc().containsKey(ValueProviderArg.MULTIPLIER) ? getDesc().getInt(ValueProviderArg.MULTIPLIER) : 1;
				int offset = getDesc().containsKey(ValueProviderArg.OFFSET) ? getDesc().getInt(ValueProviderArg.OFFSET) : 0;
				int value = 0;
				for (Player selectedPlayer : context.getPlayers()) {
					value += provideValue(context, selectedPlayer, target, host);
				}
				value = value * multiplier + offset;
				return value;
			case INACTIVE:
				providingPlayer = context.getOpponent(context.getActivePlayer());
				break;
			case OPPONENT:
				providingPlayer = context.getOpponent(player);
				break;
			case OWNER:
				providingPlayer = context.getPlayer(host.getOwner());
				break;
			case PLAYER_1:
				providingPlayer = context.getPlayer1();
				break;
			case PLAYER_2:
				providingPlayer = context.getPlayer2();
				break;
			case SELF:
			default:
				providingPlayer = player;
				break;
		}
		int multiplier = getDesc().getValue(ValueProviderArg.MULTIPLIER, context, player, target, host, 1);
		int offset = getDesc().getValue(ValueProviderArg.OFFSET, context, player, target, host, 0);
		int value = provideValue(context, providingPlayer, target, host) * multiplier + offset;
		return value;
	}

	@Suspendable
	protected abstract int provideValue(GameContext context, Player player, Entity target, Entity host);

	@Override
	public ValueProviderDesc getDesc() {
		return desc;
	}

	@Override
	public void setDesc(Desc<?, ?> desc) {
		this.desc = (ValueProviderDesc) desc;
	}
}

