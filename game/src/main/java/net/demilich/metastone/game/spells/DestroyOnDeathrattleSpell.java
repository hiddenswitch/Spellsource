package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements "Battlecry: Choose a minion. Deathrattle: Destroy the chosen minion.":
 * <pre>
 *   "battlecry": {
 *     "targetSelection": "MINIONS",
 *     "spell": {
 *       "class": "DestroyOnDeathrattleSpell"
 *     }
 *   }
 * </pre>
 * Internally, this spell creates a new deathrattle on the {@code source} that calls a {@link DestroySpell} whose {@link
 * SpellArg#TARGET} is the targeted minion.
 */
public class DestroyOnDeathrattleSpell extends AddDeathrattleSpell {

	public static Logger logger = LoggerFactory.getLogger(DestroyOnDeathrattleSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc);
		SpellDesc newDesc = new SpellDesc(AddDeathrattleSpell.class);
		SpellDesc destroySpell = new SpellDesc(DestroySpell.class);
		destroySpell.put(SpellArg.TARGET, target.getReference());
		newDesc.put(SpellArg.SPELL, destroySpell);
		super.onCast(context, player, newDesc, source, source);
	}
}
