package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HasEnchantmentFilter extends EntityFilter {
	private static Logger logger = LoggerFactory.getLogger(HasEnchantmentFilter.class);

	public HasEnchantmentFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		String cardId = getDesc().getString(EntityFilterArg.CARD);
		if (cardId == null) {
			logger.error("test {} {}: No cardId specified", context.getGameId(), host);
			throw new NullPointerException("card");
		}

		for (Trigger e : context.getLogic().getActiveTriggers(entity.getReference())) {
			if (e instanceof Enchantment) {
				Enchantment enchantment = (Enchantment) e;
				if (enchantment.getSourceCard() != null && enchantment.getSourceCard().getCardId().equals(cardId)) {
					return true;
				}
			}
		}

		return false;
	}
}
