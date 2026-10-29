package net.demilich.metastone.game.spells.desc.valueprovider;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.AttributeFilter;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.GraveyardActorsSource;

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

	protected int provideValue(
			GameContext context,
			Player player,
			Entity target,
			Entity host,
			Integer value,
			EntityFilter cardFilter) {
		CardSource source = GraveyardActorsSource.create();
		int turn = value != null ? value : context.getTurn();
		EntityFilter diedThisTurnFilter = AttributeFilter.create(Attribute.DIED_ON_TURN, turn);
		EntityFilter userFilter = cardFilter != null ? cardFilter : AndFilter.create();
		EntityFilter minionFilter = CardFilter.create(CardType.MINION);
		return source
				.getCards(context, host, player)
				.filtered(AndFilter.create(diedThisTurnFilter, userFilter, minionFilter).matcher(context, player, host))
				.getCount();
	}
}
