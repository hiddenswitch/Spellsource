package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.Collections;
import java.util.List;

public class SameHeroClassFilter extends EntityFilter {

	private static final long serialVersionUID = -4648718274167747558L;

	public SameHeroClassFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		List<Entity> entities = getTargetedEntities(context, player, host);
		if (entities == null) {
			// Assume we're saying the same hero power as the hero
			entities = Collections.singletonList(player.getHero());
		}

		HeroClass targetClass = entities.get(0).getSourceCard().getHeroClass();
		return entity.getSourceCard().hasHeroClass(targetClass);
	}
}
