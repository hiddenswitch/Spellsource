package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Casts a copy of the card played during the firing of a {@link net.demilich.metastone.game.spells.trigger.SpellCastedTrigger}.
 * <p>
 * Implements the Maelstrom.
 */
public final class CastSpellCopySpell extends PlayCardsRandomlySpell {

	@Override
	protected CardList getCards(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardArrayList cards = new CardArrayList();
		Entity entity = context.resolveSingleTarget(player, source, EntityReference.EVENT_SOURCE);
		if (entity.getEntityType() != EntityType.CARD) {
			return cards;
		}
		cards.add((Card) entity);
		return cards;
	}
}
