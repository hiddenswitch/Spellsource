package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
@Deprecated
public class RecastMinionSpells extends Spell {

	private static Logger logger = LoggerFactory.getLogger(RecastMinionSpells.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, final Entity target) {
		List<Card> cards = EnvironmentEntityList.getList(context, Environment.LYNESSA_SUNSORROW_ENTITY_LIST).getCards(context, player);
		for (Card card : cards) {
			// If a previous spell caused the target minion to leave the battlefield or be destroyed, stop
			if (target.getZone() != Zones.BATTLEFIELD
					|| target.isDestroyed()) {
				return;
			}

			if (!(card.isSpell())) {
				logger.warn("onCast {} {}: Trying to recast {} on Lynessa, which isn't a spell", context.getGameId(), source, card);
				continue;
			}

			context.getLogic().revealCard(player, card);
			player.modifyAttribute(Attribute.RANDOM_CHOICES, 1);
			SpellUtils.castChildSpell(context, player, card.getSpell(), card, target);
			player.modifyAttribute(Attribute.RANDOM_CHOICES, -1);
		}
	}
}
