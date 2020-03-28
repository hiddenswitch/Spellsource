package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Adds the specified {@link SpellArg#ATTRIBUTE} ({@link Attribute}) to the {@code target}.
 * <p>
 * If a {@link SpellArg#REVERT_TRIGGER} is specified, calls the {@link RemoveAttributeSpell} on the target when the
 * trigger fires.
 * <p>
 * For example, to give all friendly minions stealth until the start of the player's next turn:
 * <pre>
 * {
 *  "class": "AddAttributeSpell",
 *  "target": "FRIENDLY_MINIONS",
 *  "attribute": "STEALTH",
 *    "filter": {
 *      "class": "AttributeFilter",
 *      "attribute": "STEALTH",
 *      "invert": true,
 *      "operation": "HAS"
 *    },
 *    "revertTrigger": {
 *    "class": "TurnStartTrigger",
 *    "targetPlayer": "SELF"
 *  }
 * }
 * </pre>
 *
 * @see ModifyAttributeSpell for the spell appropriate for an integer-valued attribute.
 */
public class AddAttributeSpell extends RevertableSpell {

	private static Logger LOGGER = LoggerFactory.getLogger(AddAttributeSpell.class);

	/**
	 * Creates an instance of this spell without a target specified.
	 *
	 * @param tag The {@link Attribute} to set.
	 * @return A spell instance
	 */
	public static SpellDesc create(Attribute tag) {
		return create(tag, null);
	}

	/**
	 * Creates an instance of this spell with the specified attribute and a revert trigger
	 *
	 * @param tag           The attribute
	 * @param revertTrigger The trigger that, when fired, will revert this attribute
	 * @return A spell instance
	 */
	public static SpellDesc create(Attribute tag, EventTrigger revertTrigger) {
		return create(null, tag, revertTrigger);
	}

	/**
	 * Creates an instance of this spell targeting the specified {@code target}
	 *
	 * @param target The target
	 * @param tag    The attribute
	 * @return A spell instance
	 */
	public static SpellDesc create(EntityReference target, Attribute tag) {
		return create(target, tag, null);
	}

	/**
	 * Creates an instance of this spell with the specified target, attribute and revert trigger
	 *
	 * @param target        The target
	 * @param tag           The attribute to add to the target
	 * @param revertTrigger The trigger that, when fired, will revert this attribute
	 * @return A spell instance
	 */
	public static SpellDesc create(EntityReference target, Attribute tag, EventTrigger revertTrigger) {
		Map<SpellArg, Object> arguments = new SpellDesc(AddAttributeSpell.class);
		arguments.put(SpellArg.ATTRIBUTE, tag);
		if (revertTrigger != null) {
			arguments.put(SpellArg.REVERT_TRIGGER, revertTrigger);
		}
		if (target != null) {
			arguments.put(SpellArg.TARGET, target);
		}
		return new SpellDesc(arguments);
	}

	@Override
	protected SpellDesc getReverseSpell(GameContext context, Player player, Entity source, SpellDesc desc, EntityReference target) {
		return RemoveAttributeSpell.create(target, (Attribute) desc.get(SpellArg.ATTRIBUTE));
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(LOGGER, context, source, desc, SpellArg.ATTRIBUTE, SpellArg.REVERT_TRIGGER, SpellArg.SECOND_REVERT_TRIGGER);
		Objects.requireNonNull(target, source.getSourceCard().getCardId());
		if (desc.containsKey(SpellArg.VALUE)) {
			LOGGER.error("onCast {} {}: Cannot use an integer value in an AddAttributeSpell. Use ModifyAttributeSpell instead.", context.getGameId(), source);
			throw new IllegalArgumentException("VALUE");
		}
		Attribute tag = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		context.getLogic().applyAttribute(target, tag, source);
		super.onCast(context, player, desc, source, target);
	}
}

