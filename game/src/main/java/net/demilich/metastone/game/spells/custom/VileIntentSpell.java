package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.source.GraveyardCardAndActorSourceCardSource;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Buffs all minions in your hand and deck that have a copy in the graveyard.
 */
public final class VileIntentSpell extends BuffSpell {

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		var minionsInGraveyard = GraveyardCardAndActorSourceCardSource.graveyardCards(context, player).stream()
				.filter(c -> GameLogic.isCardType(c.getCardType(), CardType.MINION))
				.map(Card::getCardId)
				.collect(toSet());
		var iterator = Stream.concat(player.getHand().stream(), player.getDeck().stream()).iterator();
		while (iterator.hasNext()) {
			var card = iterator.next();
			if (minionsInGraveyard.contains(card.getCardId())) {
				super.onCast(context, player, desc, source, card);
			}
		}
	}
}
