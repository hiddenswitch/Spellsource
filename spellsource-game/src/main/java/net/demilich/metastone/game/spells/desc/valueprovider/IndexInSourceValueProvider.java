package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.DeckSource;

import java.util.Objects;

public class IndexInSourceValueProvider extends ValueProvider {

	public IndexInSourceValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(
			GameContext context,
			Player player,
			Entity target,
			Entity host,
			CardSource cardSource,
			EntityFilter cardFilter) {
		var source = (CardSource) Objects.requireNonNullElseGet(cardSource, DeckSource::create);
		var filter = (EntityFilter) Objects.requireNonNullElseGet(cardFilter, AndFilter::create);
		return source.getCards(context, host, player).filtered(filter.matcher(context, player, host)).indexOf(target);
	}
}
