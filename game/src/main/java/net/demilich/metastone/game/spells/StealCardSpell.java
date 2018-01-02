package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.Zones;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class StealCardSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		context.getLogic().stealCard(player, source, (Card) target, (Zones) desc.get(SpellArg.CARD_LOCATION));
	}
}
