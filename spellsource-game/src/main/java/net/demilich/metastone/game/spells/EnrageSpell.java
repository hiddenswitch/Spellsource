package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

/**
 * Implements the attack bonus portion of a complete Enrage effect. The {@code target}'s attack is increased by  {@link
 * SpellArg#ATTACK_BONUS}. Optionally, an {@link SpellArg#ATTRIBUTE} is also applied when specified.
 * <p>
 * To implement "Enrage: +2 Attack", the {@link net.demilich.metastone.game.cards.Card} needs to have a {@link
 * EnchantmentDesc} specified in its {@link net.demilich.metastone.game.cards.desc.CardDesc#trigger} field:
 * <pre>
 *      "trigger": {
 *          "eventTrigger": {
 *              "class": "EnrageChangedTrigger"
 *          },
 *          "spell": {
 *              "class": "EnrageSpell",
 *              "target": "SELF",
 *              "attackBonus": 2
 *          }
 *      }
 * </pre>
 */
public class EnrageSpell extends Spell {

	/**
	 * Creates this spell to increase the attack by {@code attackBonus} when the minion takes damage
	 *
	 * @param attackBonus The amount of attack bonus
	 * @return This spell
	 */
	public static SpellDesc create(int attackBonus) {
		return create(attackBonus, null);
	}

	/**
	 * Creates this spell to increase the attack and apply an attribute when the minion takes damage. Removes the
	 * attribute if the minion lost its {@link Attribute#ENRAGED} status.
	 *
	 * @param attackBonus The attack bonus
	 * @param tag         The attribute to add
	 * @return The spell
	 */
	public static SpellDesc create(int attackBonus, Attribute tag) {
		Map<SpellArg, Object> arguments = new SpellDesc(EnrageSpell.class);
		arguments.put(SpellArg.ATTACK_BONUS, attackBonus);
		arguments.put(SpellArg.TARGET, EntityReference.SELF);
		arguments.put(SpellArg.ATTRIBUTE, tag);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int attackBonus = desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, source, 0);
		boolean enraged = target.hasAttribute(Attribute.ENRAGED);
		target.setAttribute(Attribute.CONDITIONAL_ATTACK_BONUS, enraged ? attackBonus : 0);
		Attribute tag = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		if (tag != null) {
			if (enraged) {
				context.getLogic().applyAttribute(target, tag);
			} else {
				context.getLogic().removeAttribute(player, null, target, tag);
			}
		}
	}

}
