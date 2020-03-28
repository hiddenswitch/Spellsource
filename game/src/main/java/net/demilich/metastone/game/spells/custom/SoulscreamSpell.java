package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.source.CardSourceDesc;
import net.demilich.metastone.game.spells.desc.source.GraveyardDiedMinionsSource;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.HashMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static net.demilich.metastone.game.spells.custom.TriggerDistinctAftermathsInGraveyard.distinctByKey;

/**
 * Discover a minion in your graveyard. If it has an Aftermath, trigger it.
 */
public final class SoulscreamSpell extends TriggerDeathrattleSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		var minions = player
				.getGraveyard()
				.stream()
				.filter(Entity::diedOnBattlefield)
				.collect(toList());
		if (minions.isEmpty()) {
			return;
		}
		var choices = new HashMap<String, Entity>();
		for (var minion : minions) {
			choices.putIfAbsent(minion.getSourceCard().getCardId(), minion);
		}
		// These will be copied by the discover action
		var cards = minions.stream().map(Entity::getSourceCard).collect(toCollection(CardArrayList::new));
		var chosenAction = SpellUtils.discoverCard(context, player, source, NullSpell.create().addArg(SpellArg.SPELL, NullSpell.create()), cards);
		var chosenCardEntityId = chosenAction.getCard().getCardId();
		var chosenEntity = choices.get(chosenCardEntityId);
		SpellUtils.castChildSpell(context, player, ReceiveCardSpell.create(chosenEntity.getSourceCard().getCardId()), source, target);
		if (chosenEntity.getSourceCard().hasAttribute(Attribute.DEATHRATTLES)) {
			super.onCast(context, player, TriggerDeathrattleSpell.create(chosenEntity.getReference()), source, chosenEntity);
		}
	}
}
