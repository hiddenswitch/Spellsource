package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

/**
 * Sets the {@link Actor} or {@link Card}'s hitpoints to the specified {@link SpellArg#VALUE}, overriding any existing
 * bonuses stored in {@link Attribute#HP_BONUS}.
 * <p>
 * When an actor is targeted, the actor's hitpoints and maximum hitpoints are both increased or decreased down to the
 * new value.
 * <p>
 * When a card is targeted, the client will correctly render the change in hitpoints, and the summoned minion will have
 * a silenceable enchantment that sets its hitpoints to the value specified.
 * <p>
 * If {@link SpellArg#EXCLUSIVE} is {@code true} (the default), the effect can be silenced. If it is {@code false}, the
 * effect changes the {@link Attribute#BASE_HP} of the actor or card, and therefore cannot be silenced. Use a {@code
 * false} exclusive argument in order to create minions on the fly.
 * <p>
 * For <b>example,</b> to set a minion's hitpoints to its attack:
 * <pre>
 *     {
 *         "class": "SetHpSpell",
 *         "value": {
 *             "class": "AttributeValueProvider",
 *             "attribute": "ATTACK"
 *         }
 *     }
 * </pre>
 * Suppose we wanted to implement the text, "At the start of your turn, summon a Penguin. The next Penguin has +1/+1
 * compared to the last." The idea is that Penguins, like Jade Golems, do not have a silenceable buff but a permanent
 * number of hitpoints. Here will will use the {@link SpellArg#EXCLUSIVE} argument and set it to {@code false}:
 * <pre>
 *     "trigger": {
 *         "eventTrigger": {
 *             "class": "TurnStartTrigger",
 *             "targetPlayer": "SELF"
 *         },
 *         "spell": {
 *             "class": "MetaSpell",
 *             "spells": [
 *                  {
 *                      "class": "SummonSpell",
 *                      "card": "minion_snowflipper_penguin",
 *                      "spell": {
 *                          "class": "MetaSpell",
 *                          "spells": [
 *                              {
 *                                  "class": "SetHpSpell",
 *                                  "target": "OUTPUT",
 *                                  "exclusive": false,
 *                                  "value": {
 *                                      "class": "AttributeValueProvider",
 *                                      "attribute": "RESERVED_INTEGER_1",
 *                                      "target": "TRIGGER_HOST"
 *                                  }
 *                              },
 *                              {
 *                                  "class": "SetAttackSpell",
 *                                  "target": "OUTPUT",
 *                                  "exclusive": false,
 *                                  "value": {
 *                                      "class": "AttributeValueProvider",
 *                                      "attribute": "RESERVED_INTEGER_1",
 *                                      "target": "TRIGGER_HOST"
 *                                  }
 *                              }
 *                          ]
 *                      }
 *                  },
 *                  {
 *                      "class": "ModifyAttributeSpell",
 *                      "target": "TRIGGER_HOST",
 *                      "attribute": "RESERVED_INTEGER_1",
 *                      "value": 1
 *                  }
 *             ]
 *         }
 *     }
 * </pre>
 * The complex example above also uses {@link Attribute#RESERVED_INTEGER_1} as a counter; the special target {@link
 * net.demilich.metastone.game.targeting.EntityReference#OUTPUT} to cast a spell on the minion that was summoned by a
 * {@link SummonSpell}; and the special target {@link net.demilich.metastone.game.targeting.EntityReference#TRIGGER_HOST}
 * to modify the host of the trigger instead of the {@link GameEvent#getTarget()} entity, the default {@code target} of
 * spells cast by triggers.
 *
 * @see SetAttackSpell for the equivalent effect for attack.
 */
public class SetHpSpell extends Spell {

	public static SpellDesc create(int hp) {
		Map<SpellArg, Object> arguments = new SpellDesc(SetHpSpell.class);
		arguments.put(SpellArg.VALUE, hp);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int value, boolean immuneToSilence) {
		Map<SpellArg, Object> arguments = new SpellDesc(SetHpSpell.class);
		arguments.put(SpellArg.VALUE, value);
		arguments.put(SpellArg.EXCLUSIVE, !immuneToSilence);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int hp = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		target.getAttributes().remove(Attribute.HP_BONUS);
		// When exclusive, the set hp spell will overwrite bonuses. When not exclusive, the BASE HP will change
		// (to protect it from silencing) and the changed HP will honor bonuses.
		boolean exclusive = (boolean) desc.getOrDefault(SpellArg.EXCLUSIVE, true);

		if (target instanceof Actor) {
			context.getLogic().setHpAndMaxHp((Actor) target, hp);
		} else if (target instanceof Card) {
			target.getAttributes().put(Attribute.HP, hp);
		}

		if (!exclusive) {
			target.setAttribute(Attribute.BASE_HP, hp);
		}

		// We might be undestroying the minion
		if (hp > 0) {
			target.getAttributes().remove(Attribute.DESTROYED);
		}
	}

}
