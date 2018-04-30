package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds the deathrattle specified by the {@link SpellArg#SPELL} to the {@link SpellArg#TARGET} or {@code target}.
 * <p>
 * If the target is a {@link Card}, the deathrattle will be added to the resulting actor when the minion is brought into
 * play. To add an effect that occurs when a card is removed from the hand, use an {@link AddEnchantmentSpell} with a
 * {@link net.demilich.metastone.game.spells.trigger.DiscardTrigger}.
 * <p>
 * Deathrattles are equivalent to an {@link AddEnchantmentSpell} with a {@link net.demilich.metastone.game.spells.trigger.MinionDeathTrigger},
 * {@link net.demilich.metastone.game.targeting.TargetType#IGNORE_OTHER_TARGETS}.
 * <p>
 * Minions removed peacefully with {@link net.demilich.metastone.game.logic.GameLogic#removePeacefully(Entity)} do not
 * trigger deathrattles.
 * <p>
 * All weapon removals trigger deathrattles, whether by replacement, destruction or by depleting their durability.
 * <p>
 * For example, to give a minion a the deathrattle, "Resummon this minion,":
 * <pre>
 *     {
 *       "class": "AddDeathrattleSpell",
 *       "spell": {
 *         "class": "ReviveMinionSpell",
 *         "target": "SELF"
 *       }
 *     }
 * </pre>
 * <p>
 * Many jail effects, like "Battlecry: Choose a minion. Deathrattle: Summon a copy of it" require "storing" a reference
 * to an entity. This must be done in code. For this example, create a new class that extends {@link Spell} and
 * implement its {@code onCast} method to create a new deathrattle spell that summons a copy of a specific minion:
 * <pre>
 *   SpellDesc summonDesc = new SpellDesc(SummonSpell.class);
 *   // The target is the minion chosen by the battlecry. It gets baked in here.
 *   summonDesc.put(SpellArg.TARGET, target.getReference());
 *   SpellDesc addDeathrattleSpell = AddDeathrattleSpell.create(EntityReference.SELF, summonDesc);
 *   SpellUtils.castChildSpell(context, player, addDeathrattleSpell, source, target);
 * </pre>
 * Observe that the {@code target} reference gets baked in and a deathrattle is added to the casting minion by this
 * battlecry, as opposed to setting the {@code "deathrattle"} field on the minion's card JSON.
 *
 * @see AddEnchantmentSpell for a way to add any enchanment, like a trigger or an aura, to an entity.
 */
public class AddDeathrattleSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(AddDeathrattleSpell.class);

	/**
	 * Creates this spell to add the specified {@code deathrattle} to the target.
	 *
	 * @param target      The target entity reference
	 * @param deathrattle The spell to cast when that {@link Actor} dies.
	 * @return A spell instance.
	 */
	public static SpellDesc create(EntityReference target, SpellDesc deathrattle) {
		Map<SpellArg, Object> arguments = new SpellDesc(AddDeathrattleSpell.class);
		arguments.put(SpellArg.SPELL, deathrattle);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(SpellDesc deathrattle) {
		return create(null, deathrattle);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.SPELL);
		SpellDesc deathrattle = (SpellDesc) desc.get(SpellArg.SPELL);
		if (target instanceof Actor) {
			Actor actor = (Actor) target;
			actor.addDeathrattle(deathrattle.clone());
		} else if (target instanceof Card) {
			Card card = (Card) target;
			card.addDeathrattle(deathrattle.clone());
		}
	}

}
