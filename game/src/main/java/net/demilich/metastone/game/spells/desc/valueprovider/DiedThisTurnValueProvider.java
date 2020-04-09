package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.*;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.GraveyardActorsSource;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Indicates how many of the {@link ValueProviderArg#FILTER} filtered actors were destroyed this turn.
 * <p>
 * Use {@link ValueProviderArg#TARGET_PLAYER} to specify which graveyards to use. For example, checks both graveyards if
 * {@link ValueProviderArg#TARGET_PLAYER} is {@link net.demilich.metastone.game.spells.TargetPlayer#BOTH}.
 */
public final class DiedThisTurnValueProvider extends ValueProvider {

	public DiedThisTurnValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		CardSource source = GraveyardActorsSource.create();
		EntityFilter diedThisTurnFilter = AttributeFilter.create(Attribute.DIED_ON_TURN, getDesc().getValue(ValueProviderArg.VALUE, context, player, target, host, context.getTurn()));
		EntityFilter userFilter = (EntityFilter) getDesc().getOrDefault(ValueProviderArg.CARD_FILTER, AndFilter.create());
		EntityFilter minionFilter = CardFilter.create(CardType.MINION);
		return source
				.getCards(context, host, player)
				.filtered(AndFilter.create(diedThisTurnFilter, userFilter, minionFilter).matcher(context, player, host))
				.getCount();
	}
}
