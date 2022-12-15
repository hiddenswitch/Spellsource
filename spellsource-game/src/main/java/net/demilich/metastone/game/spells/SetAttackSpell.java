package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Sets the {@code target}'s attack-related attributes such that the result of {@link Actor#getAttack()} or {@link
 * net.demilich.metastone.game.cards.Card#getAttack()} is equal to the {@link SpellArg#VALUE}.
 * <p>
 * When {@link SpellArg#EXCLUSIVE} is {@code true} (default), temporary and conditional attack bonuses are removed. This
 * means that minions with conditional attack bonuses like Tar Creeper (+2 Attack during the opponent's turn) will be
 * lost by this effect.
 * <p>
 * When {@link SpellArg#EXCLUSIVE} is {@code false}, the {@link Attribute#BASE_ATTACK} of the {@code target} is changed.
 * This makes it immune to silencing. This may be an appropriate way to implement a Jade Golem effect of increasing base
 * stats, but is not currently used anywhere.
 * <p>
 * The {@link Attribute#ATTACK_BONUS} of the target is always removed.
 * <p>
 * For <b>example,</b> to create a one-one copy of all friendly minions, {@code SetAttackSpell} is commonly used with
 * {@link SetHpSpell}:
 * <pre>
 *     {
 *         "class": "SummonSpell",
 *         "target": "FRIENDLY_MINIONS",
 *         "spell": {
 *             "class": "MetaSpell",
 *             "target": "OUTPUT",
 *             "spells": [
 *                  {
 *                      "class": "SetAttackSpell",
 *                      "value": 1
 *                  },
 *                  {
 *                      "class": "SetHpSpell",
 *                      "value": 1
 *                  }
 *             ]
 *         }
 *     }
 * </pre>
 * To set a target's attack to its <b>current</b> health:
 * <pre>
 *     {
 *         "class": "SetAttackSpell",
 *         "value": {
 *             "class": "AttributeValueProvider",
 *             "attribute": "HP"
 *         }
 *     }
 * </pre>
 * To implement "Enrage: +2 Attack", do <b>not</b> use {@code SetAttackSpell}.
 *
 * @see SetHpSpell for the corresponding effect for hitpoints.
 * @see TemporaryAttackSpell to make a temporary attack bonus.
 * @see BuffSpell to add a permanent attack bonus.
 * @see EnrageSpell to implement Enrage effects related to attack bonuses.
 * @see Attribute#ATTACK_EQUALS_HP to enforce a Lightspawn-like effect where the minion's attack always equals its
 * 		hitpoints after damage effects have been resolved.
 */
public class SetAttackSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(SetAttackSpell.class);

	public static SpellDesc create(int value) {
		Map<SpellArg, Object> arguments = new SpellDesc(SetAttackSpell.class);
		arguments.put(SpellArg.VALUE, value);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int value, boolean immuneToSilence) {
		Map<SpellArg, Object> arguments = new SpellDesc(SetAttackSpell.class);
		arguments.put(SpellArg.VALUE, value);
		arguments.put(SpellArg.EXCLUSIVE, !immuneToSilence);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		// When exclusive, the set attack spell will overwrite bonuses. When not exclusive, the BASE attack will change
		// (to protect it from silencing) and the changed attack will honor bonuses.
		boolean exclusive = (boolean) desc.getOrDefault(SpellArg.EXCLUSIVE, true);

		target.setAttribute(Attribute.ATTACK, value);
		target.getAttributes().remove(Attribute.ATTACK_BONUS);
		if (exclusive) {
			target.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
			target.getAttributes().remove(Attribute.CONDITIONAL_ATTACK_BONUS);
		} else {
			logger.debug("onCast {} {}: Calling non-exclusively, the BASE_ATTACK was changed to {} on target {}", context.getGameId(), source, value, target);
			target.setAttribute(Attribute.BASE_ATTACK, value);
		}
	}

}