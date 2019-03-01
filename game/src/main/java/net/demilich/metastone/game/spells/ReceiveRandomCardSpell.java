package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

/**
 * @deprecated by {@link ReceiveCardSpell} with the argument {@link SpellArg#RANDOM_TARGET} set to {@code true}.
 */
@Deprecated
public class ReceiveRandomCardSpell extends ReceiveCardSpell {

	public static SpellDesc create(TargetPlayer targetPlayer, Card... cards) {
		Map<SpellArg, Object> arguments = new SpellDesc(ReceiveRandomCardSpell.class);
		arguments.put(SpellArg.CARDS, cards);
		arguments.put(SpellArg.TARGET_PLAYER, targetPlayer);
		arguments.put(SpellArg.RANDOM_TARGET, true);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		super.onCast(context, player, ReceiveRandomCardSpell.create(player.toTargetPlayer(), (Card[]) desc.get(SpellArg.CARDS)), source, target);
	}
}
