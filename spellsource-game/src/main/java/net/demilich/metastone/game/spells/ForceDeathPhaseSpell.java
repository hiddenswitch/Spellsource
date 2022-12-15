package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Map;

/**
 * At the moment this spell is invoked, the sequence is ended and dead entities are cleaned up from the board,
 * triggering deathrattles.
 * <p>
 * Ordinarily, "death phase" (the end of a sequence) is only run at the end of all of a {@link
 * net.demilich.metastone.game.actions.GameAction} effects.
 * <p>
 * This can help implement effects that "merge" minions into new ones. For example, to implement the text, "Destroy all
 * enemy minions. Summon a treant for each minion destroyed.":
 * <pre>
 *   {
 *     "class": "MetaSpell",
 *     "value": {
 *       "class": "EntityCountValueProvider",
 *       "target": "ENEMY_MINIONS"
 *     },
 *     "spells": [
 *      {
 *        "class": "DestroySpell",
 *        "target": "ENEMY_MINIONS"
 *      },
 *      {
 *        "class": "ForceDeathPhaseSpell"
 *      },
 *      {
 *        "class": "SummonSpell",
 *        "card": "token_treant",
 *        "targetPlayer": "OPPONENT",
 *        "value": {
 *          "class": "GameValueProvider",
 *          "gameValue": "SPELL_VALUE"
 *        }
 *      }
 *     ]
 *   }
 * </pre>
 */
public class ForceDeathPhaseSpell extends Spell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(ForceDeathPhaseSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		//an easy way to set certain value providers at a specific time
		for (Card card : player.getHand()) {
			context.getLogic().getModifiedManaCost(player, card);
		}
		context.getLogic().endOfSequence();
	}

}
