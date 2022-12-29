package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.spells.CardCostModifierSpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

/**
 * Generates the cards retrieved by {@link SpellUtils#getCards(GameContext, Player, Entity, Entity, SpellDesc)} rules,
 * moves each card to the {@link Zones#SET_ASIDE_ZONE}, casts the {@link SpellArg#SPELL} sub-spell on each as the {@code
 * target}, and then moves the card to the {@link Zones#REMOVED_FROM_PLAY} zone if they have not been moved out of set
 * aside.
 * <p>
 * This card is useful for performing effects on cards before they are moved into various places, rather than after.
 * <p>
 * Implements Academic Espionage.
 */
public final class ShuffleWithCardCostModifierSpell extends CardCostModifierSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = SpellUtils.getCards(context, player, target, source, desc);
		for (Card card : cards) {
			if (!card.getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
				card = card.getCopy();
			}
			card.setOwner(player.getId());
			card.setId(context.getLogic().generateId());
			card.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
			desc = desc.clone();
			desc.remove(SpellArg.CARD_FILTER);
			desc.remove(SpellArg.CARD_SOURCE);
			desc.remove(SpellArg.VALUE);
			desc.remove(SpellArg.TARGET);
			super.onCast(context, player, desc, source, card);
			context.getLogic().shuffleToDeck(player, null, card, false, true);
		}
	}
}
