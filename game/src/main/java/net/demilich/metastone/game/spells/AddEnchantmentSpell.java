package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds an {@link SpellArg#AURA} ({@link Aura}) or a {@link Enchantment} (in the {@link SpellArg#TRIGGER} due to
 * history) to the specified {@code target} and immediately puts that aura/enchantment into play (i.e., activates it).
 * <p>
 * This example implements the text, "At the start of your next turn, summon four 1/1 Silver Hand Recruits." Notice that
 * {@link EntityReference#FRIENDLY_PLAYER} is used as the target of an enchantment for effects that don't really belong
 * to specific minions.
 * <pre>
 *   {
 *     "class": "AddEnchantmentSpell",
 *     "target": "FRIENDLY_PLAYER",
 *     "trigger": {
 *       "eventTrigger": {
 *         "class": "TurnStartTrigger",
 *         "targetPlayer": "SELF"
 *       },
 *       "spell": {
 *         "class": "SummonSpell",
 *         "cards": [
 *           "token_silver_hand_recruit",
 *           "token_silver_hand_recruit",
 *           "token_silver_hand_recruit",
 *           "token_silver_hand_recruit"
 *         ]
 *       },
 *       "oneTurn": true
 *     }
 *   }
 * </pre>
 *
 * @see AddDeathrattleSpell for a simple way to add a deathrattle to a minion/card.
 * @see Aura for more about what auras should look like.
 * @see Enchantment for more about what enchantments should look like.
 */
public class AddEnchantmentSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(AddEnchantmentSpell.class);

	public static SpellDesc create(EntityReference target, EnchantmentDesc trigger) {
		Map<SpellArg, Object> arguments = new SpellDesc(AddEnchantmentSpell.class);
		arguments.put(SpellArg.TRIGGER, trigger);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference target, Aura aura) {
		Map<SpellArg, Object> arguments = new SpellDesc(AddEnchantmentSpell.class);
		arguments.put(SpellArg.AURA, aura);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EnchantmentDesc trigger) {
		return create(null, trigger);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.AURA, SpellArg.TRIGGER);
		EnchantmentDesc enchantmentDesc = (EnchantmentDesc) desc.get(SpellArg.TRIGGER);
		Aura aura = (Aura) desc.get(SpellArg.AURA);

		if (enchantmentDesc != null) {
			Enchantment enchantment = enchantmentDesc.create();
			enchantment.setOwner(player.getId());
			context.getLogic().addGameEventListener(player, enchantment, target);
		}

		if (aura != null) {
			aura = aura.clone();
			aura.setOwner(player.getId());
			// Enchantments added this way should trigger a board changed event.
			context.getLogic().addGameEventListener(player, aura, target);
		}
	}

}
