package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class HasEffectsFilter extends EntityFilter {

	public HasEffectsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		Card c = entity.getSourceCard();
		return c.hasAura()
				|| c.hasBattlecry()
				|| c.hasCardCostModifier()
				|| c.hasDeathrattle()
				|| c.hasTrigger()
				|| c.isSpell();
	}
}
