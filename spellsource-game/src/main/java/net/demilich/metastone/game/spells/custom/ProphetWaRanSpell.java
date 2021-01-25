package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

import java.util.ArrayList;
import java.util.List;

/**
 * Shuffles 5/5 copies of all minions in the casting player's deck.
 * <p>
 * Implements Prophet Wa Ran.
 */
public final class ProphetWaRanSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList minionCards = player.getDeck().filtered(c -> c.getCardType() == CardType.MINION);
		List<Card> cardsToShuffle = new ArrayList<>();
		for (Card card : minionCards) {
			card = card.getCopy();
			card.setId(context.getLogic().generateId());
			card.setOwner(player.getId());
			card.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
			SpellDesc setAttack = SetAttackSpell.create(5);
			SpellDesc setHp = SetHpSpell.create(5);
			for (SpellDesc subSpell : new SpellDesc[]{setAttack, setHp}) {
				SpellUtils.castChildSpell(context, player, subSpell, source, card);
			}
			cardsToShuffle.add(card);
		}
		for (Card card : cardsToShuffle) {
			context.getLogic().shuffleToDeck(player, card);
		}
	}
}

