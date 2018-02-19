package net.demilich.metastone.game.spells;

import java.util.Arrays;
import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

@Deprecated
public class ReceiveRandomCardSpell extends ReceiveCardSpell {

	public static SpellDesc create(TargetPlayer targetPlayer, Card... cards) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReceiveRandomCardSpell.class);
		arguments.put(SpellArg.CARDS, cards);
		arguments.put(SpellArg.TARGET_PLAYER, targetPlayer);
		arguments.put(SpellArg.RANDOM_TARGET, true);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}
}
