package net.demilich.metastone.game.spells.custom;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.CopyCardSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.utils.Attribute;

import java.util.List;
import java.util.stream.Collectors;

public final class CopyLastRoastedCardSpell extends CopyCardSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Card> cardRoastedThisTurn = player.getGraveyard()
				.stream()
				.filter(c -> c.getEntityType() == EntityType.CARD)
				.filter(c -> (int) c.getAttributes().getOrDefault(Attribute.ROASTED, -1) == context.getTurn())
				.map(Card.class::cast)
				.collect(Collectors.toList());

		if (cardRoastedThisTurn.isEmpty()) {
			return;
		}

		super.onCast(context, player, desc, source, cardRoastedThisTurn.get(cardRoastedThisTurn.size() - 1));
	}
}
