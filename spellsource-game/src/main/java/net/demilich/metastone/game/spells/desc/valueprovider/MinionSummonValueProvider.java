package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

import java.util.Map;

public class MinionSummonValueProvider extends ValueProvider {

	public MinionSummonValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(GameContext context, Player player, Entity target, Entity host, EntityFilter filter) {
		Map<String, Integer> minionIds = player.getStatistics().getMinionsSummoned();
		int count = 0;
		for (String minionId : minionIds.keySet()) {
			Entity entity = context.getCardById(minionId);
			if (filter == null || filter.matches(context, player, entity, host)) {
				count += minionIds.get(minionId);
			}
		}
		return count;
	}

}

