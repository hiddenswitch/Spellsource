package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
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

	protected boolean test(GameContext context, Player player, Entity entity, Entity host, String card) {
		if (card == null) {
			logger.error("test {} {}: No cardId specified", context.getGameId(), host);
			throw new NullPointerException("card");
		}

		for (Trigger e : context.getLogic().getActiveTriggers(entity.getReference())) {
			if (e instanceof Enchantment enchantment) {
				Card srcCard = enchantment.getSourceCard();
				if (srcCard != null) {
					String cid = srcCard.getCardId();
					if (card.equals(cid)) {
						return true;
					}
					if (cid != null) {
						int idx = cid.indexOf("__ench_");
						if (idx > 0 && card.equals(cid.substring(0, idx))) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}
}
