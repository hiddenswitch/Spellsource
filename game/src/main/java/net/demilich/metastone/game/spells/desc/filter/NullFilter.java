package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class NullFilter extends EntityFilter {
	public static final NullFilter INSTANCE = new NullFilter(new FilterDesc(FilterDesc.build(NullFilter.class)));

	private NullFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		return true;
	}
}
