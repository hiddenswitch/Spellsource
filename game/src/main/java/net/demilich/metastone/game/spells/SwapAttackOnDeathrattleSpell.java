package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements "Battlecry: Swap Attack with another minion. Deathrattle: Swap back.":
 * <pre>
 *   "battlecry": {
 *     "targetSelection": "MINIONS",
 *     "spell": {
 *       "class": "SwapAttackOnDeathrattleSpell"
 *     }
 *   }
 * </pre>
 * Internally, this spell creates a new deathrattle on the {@code source} that calls a {@link SwapAttackSpell} whose {@link
 * SpellArg#TARGET} is the targeted minion.
 */
public class SwapAttackOnDeathrattleSpell extends AddDeathrattleSpell {

	public static Logger logger = LoggerFactory.getLogger(DestroyOnDeathrattleSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc);
		SpellDesc newDesc = new SpellDesc(AddDeathrattleSpell.class);
		SpellDesc swapAttackSpell = new SpellDesc(SwapAttackSpell.class);
		swapAttackSpell.put(SpellArg.TARGET, target.getReference());
		newDesc.put(SpellArg.SPELL, swapAttackSpell);
		super.onCast(context, player, newDesc, source, source);
	}
}
