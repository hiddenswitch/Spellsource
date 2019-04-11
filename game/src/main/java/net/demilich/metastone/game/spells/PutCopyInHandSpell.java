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
 * Makes {@link SpellArg#HOW_MANY} copies of the base card of the {@code target}.
 * <p>
 * For <b>example</b>, to copy your hand (as a base card):
 * <pre>
 *   {
 *     "class": "PutCopyInHandSpell",
 *     "target": "FRIENDLY_HAND"
 *   }
 * </pre>
 *
 * @see CopyCardSpell for an effect that copies the actual card, with its card cost modifiers, instead of the base card.
 */
public class PutCopyInHandSpell extends Spell {

	public static SpellDesc create() {
		return create(1);
	}

	public static SpellDesc create(EntityReference target, int amount) {
		Map<SpellArg, Object> arguments = new SpellDesc(PutCopyInHandSpell.class);
		arguments.put(SpellArg.HOW_MANY, amount);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int amount) {
		return create(null, amount);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int amount = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		Card sourceCard = target.getSourceCard();
		for (int i = 0; i < amount; i++) {
			context.getLogic().receiveCard(player.getId(), sourceCard.getCopy());
		}
	}

}