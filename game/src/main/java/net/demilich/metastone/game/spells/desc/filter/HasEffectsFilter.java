package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

/**
 * Matches an entity if it has an aura, battlecry, card cost modifier, deathrattle, any kind of trigger or is a spell.
 */
public class HasEffectsFilter extends EntityFilter {

	public HasEffectsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		Card c = entity.getSourceCard();
		return c.getDesc().getEnchantmentDescs().count() > 0L;
	}
}
