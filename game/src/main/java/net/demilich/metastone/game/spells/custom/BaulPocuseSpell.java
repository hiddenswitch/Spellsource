package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Puts the last three cards the player {@link Attribute#ROASTED} into the player's {@link
 * net.demilich.metastone.game.targeting.Zones#HAND}). Shuffles all the other roasted cards into the player's {@link
 * net.demilich.metastone.game.targeting.Zones#DECK}.
 */
public final class BaulPocuseSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Card> roasted = player.getGraveyard()
				.stream()
				.filter(e -> e.getEntityType() == EntityType.CARD)
				.map(Card.class::cast)
				.filter(e -> e.hasAttribute(Attribute.ROASTED))
				.collect(toList());

		int amount = desc.getValue(SpellArg.VALUE, context, player, target, source, 3);
		int fromIndex = Math.max(roasted.size() - amount, 0);
		List<Card> lastThreeRoasted = roasted.subList(fromIndex, roasted.size());
		List<Card> allOthers = roasted.subList(0, fromIndex);
		SpellDesc receiveCard = new SpellDesc(CopyCardSpell.class);
		SpellDesc shuffleCard = new SpellDesc(ShuffleToDeckSpell.class);
		for (Card card : lastThreeRoasted) {
			SpellUtils.castChildSpell(context, player, receiveCard, source, card);
		}

		for (Card card : allOthers) {
			// The cards are copied out of the graveyard
			SpellUtils.castChildSpell(context, player, shuffleCard, source, card);
		}
	}
}

