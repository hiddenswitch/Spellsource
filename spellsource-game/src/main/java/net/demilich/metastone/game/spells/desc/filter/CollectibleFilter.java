package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Filter entities based on whether their source card is collectible or not
 */
public class CollectibleFilter extends EntityFilter {

    public CollectibleFilter(EntityFilterDesc desc) {
        super(desc);
    }

    @Override
    protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
        return entity != null && entity.getSourceCard() != null && entity.getSourceCard().isCollectible();
    }
}
