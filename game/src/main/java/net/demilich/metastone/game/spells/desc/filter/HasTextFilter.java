package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class HasTextFilter extends EntityFilter {

	public HasTextFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		// For now, use the description, because that will be most consistent with what the player expects
		final String description = entity.getSourceCard().getDescription();
		return description != null
				&& !description.equals("");
	}
}
