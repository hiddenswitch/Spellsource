package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Modifies the amount of damage that is currently about to be dealt.
 * <p>
 * The expression for the modification is {@code DAMAGE OPERATION VALUE}. In other words, if {@link SpellArg#OPERATION}
 * is {@link AlgebraicOperation#ADD}, the damage is 2 and the value is 4, the new damage will be {@code 2 + 4 = 6}.
 * <p>
 * Several triggers cast their spells at the right moment to modify damage. For <b>example</b>, a common trigger is a
 * {@link net.demilich.metastone.game.spells.trigger.PreDamageTrigger}, for text like "Your minions can only take 1
 * damage at a time":
 * <pre>
 *   {
 *      "eventTrigger": {
 *        "class": "PreDamageTrigger",
 *        "fireCondition": {
 *          "class": "OwnedByPlayerCondition",
 *          "targetPlayer": "SELF"
 *        },
 *        "targetEntityType": "MINION",
 *        "targetPlayer": "SELF"
 *      },
 *      "spell": {
 *        "class": "ModifyDamageSpell",
 *        "value": 1,
 *        "operation": "SET"
 *      }
 *   }
 * </pre>
 * Or, for <b>example</b>, the {@link net.demilich.metastone.game.spells.trigger.FatalDamageTrigger} is useful for text
 * like "When your hero takes fatal damage, heal for the amount instead.":
 * <pre>
 *   {
 *     "eventTrigger": {
 *       "class": "FatalDamageTrigger",
 *       "sourcePlayer": "BOTH",
 *       "targetEntityType": "HERO",
 *       "targetPlayer": "SELF"
 *     },
 *     "spell": {
 *       "class": "MetaSpell",
 *       "spells": [
 *         {
 *           "class": "ModifyDamageSpell",
 *           "value": 0,
 *           "operation": "SET"
 *         },
 *         {
 *           "class": "HealSpell",
 *           "target": "FRIENDLY_HERO",
 *           "value": {
 *             "class": "EventValueProvider"
 *           }
 *         }
 *       ]
 *     }
 *   }
 * </pre>
 * Finally, to <b>double</b> damage dealt by this minion to heroes:
 * <pre>
 *   {
 *     "eventTrigger": {
 *       "class": "PreDamageTrigger",
 *       "hostTargetType": "IGNORE_OTHER_SOURCES",
 *       "targetEntityType": "HERO"
 *     },
 *     "spell": {
 *       "class": "ModifyDamageSpell",
 *       "value": 2,
 *       "operation": "MULTIPLY"
 *     }
 *   }
 * </pre>
 */
public class ModifyDamageSpell extends Spell {

	public static SpellDesc create(int value, AlgebraicOperation operation) {
		SpellDesc desc = new SpellDesc(ModifyDamageSpell.class);
		desc.put(SpellArg.VALUE, value);
		desc.put(SpellArg.OPERATION, operation);
		return desc;
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (context.getDamageStack().isEmpty()) {
			return;
		}

		int damage = context.getDamageStack().pop();
		AlgebraicOperation operation = (AlgebraicOperation) desc.get(SpellArg.OPERATION);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		int minDamage = 0;

		// Fel's grip will modify all incoming damage when played as a spell.
		EntityReference eventTarget = context.getEventTargetStack().peek();
		boolean hasTakeDoubleDamage = false;
		if (eventTarget != null) {
			Entity damageTarget = context.resolveSingleTarget(eventTarget);
			hasTakeDoubleDamage = damageTarget.hasAttribute(Attribute.TAKE_DOUBLE_DAMAGE) || damageTarget.hasAttribute(Attribute.AURA_TAKE_DOUBLE_DAMAGE);
		}

		switch (operation) {
			case ADD:
				if (hasTakeDoubleDamage) {
					value *= 2;
				}
				damage += value;
				break;
			case SUBTRACT:
				if (hasTakeDoubleDamage) {
					value *= 2;
				}
				damage -= value;
				damage = Math.max(minDamage, damage);
				break;
			case MODULO:
				if (hasTakeDoubleDamage) {
					damage /= 2;
				}
				damage %= value;
				if (hasTakeDoubleDamage) {
					damage *= 2;
				}
				break;
			case MULTIPLY:
				damage *= value;
				break;
			case DIVIDE:
				damage /= value;
				damage = Math.max(minDamage, damage);
				break;
			case NEGATE:
				damage = -damage;
				break;
			case SET:
				if (hasTakeDoubleDamage) {
					value *= 2;
				}
				damage = value;
				break;
			case MINIMUM:
				if (hasTakeDoubleDamage) {
					value *= 2;
				}
				if (damage < value) {
					damage = value;
				}
				break;
			case MAXIMUM:
				if (hasTakeDoubleDamage) {
					value *= 2;
				}
				if (damage > value) {
					damage = value;
				}
				break;
			default:
				break;
		}

		context.getDamageStack().push(damage);
	}

}
