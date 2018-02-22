package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a {@link CardCostModifier} specified by {@link SpellArg#CARD_COST_MODIFIER} that is hosted by the specified
 * {@link SpellArg#TARGET}.
 * <p>
 * The following <b>example</b> makes the cards in the player's hand cost zero for the rest of the game. When the player
 * draws new cards, those cards still cost zero. The "rest of the game" part is enforced by the {@link SpellArg#TARGET}
 * of {@link EntityReference#FRIENDLY_PLAYER}, the entity that hosts rest of the game triggers and effects like card
 * cost modification.
 * <pre>
 *     {
 *         "class": "CardCostModifierSpell",
 *         "target": "FRIENDLY_PLAYER",
 *         "cardCostModifier": {
 *             "class": "CardCostModifier",
 *             "target": "FRIENDLY_HAND",
 *             "operation": "SET",
 *             "value": 0
 *         }
 *     }
 * </pre>
 * However, this <b>example</b> sets the cost of all cards <b>currently</b> in the player's hand to zero. Later cards
 * that are drawn do not get their cost reduced.
 * <pre>
 *     {
 *         "class": "CardCostModifierSpell",
 *         "target": "FRIENDLY_HAND",
 *         "cardCostModifier": {
 *             "class": "CardCostModifier",
 *             "target": "SELF",
 *             "operation": "SET",
 *             "value": 0
 *         }
 *     }
 * </pre>
 * Notice that the target of the {@link CardCostModifierSpell} is the entity that hosts the effect, while the {@link
 * SpellArg#CARD_COST_MODIFIER} is the actual modifier that should go into play. By using {@link
 * CardCostModifierArg#TARGET} of {@link EntityReference#SELF}, the hosting entity modifies itself.
 *
 * @see CardCostModifier for a description of the format for a card cost modifier.
 */
public class CardCostModifierSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(CardCostModifierSpell.class);

	/**
	 * Creates this spell.
	 *
	 * @param cardCostModifierDesc A configuration for the {@link CardCostModifier}. The {@link
	 *                             CardCostModifierDesc#create()} method is used to actually create the {@link
	 *                             CardCostModifier} instance.
	 * @param host                 The {@link Entity} that should host the modifier. This should resolve to a single
	 *                             target.
	 * @return The spell.
	 */
	public static SpellDesc create(CardCostModifierDesc cardCostModifierDesc, EntityReference host) {
		Map<SpellArg, Object> arguments = SpellDesc.build(CardCostModifierSpell.class);
		arguments.put(SpellArg.CARD_COST_MODIFIER, cardCostModifierDesc);
		arguments.put(SpellArg.TARGET, host);
		return new SpellDesc(arguments);
	}

	/**
	 * Creates this spell to modify the cost of the target with the given operation and value. The target is also the
	 * host, so the {@link CardCostModifierArg#TARGET} is set to {@link EntityReference#SELF}.
	 *
	 * @param target    The {@link EntityReference} whose cost should be modified.
	 * @param operation The way the {@code value} should be interpreted. To increase the cost, use a positive {@code
	 *                  value} and the {@link AlgebraicOperation#ADD}; to set the value, use {@link
	 *                  AlgebraicOperation#SET}.
	 * @param value     The value that should be used for modifying the cost.
	 * @return The spell.
	 */
	public static SpellDesc create(EntityReference target, AlgebraicOperation operation, int value) {
		CardCostModifierDesc manaModifierDesc = new CardCostModifierDesc(CardCostModifierDesc.build(CardCostModifier.class));
		manaModifierDesc = manaModifierDesc.addArg(CardCostModifierArg.OPERATION, operation);
		manaModifierDesc = manaModifierDesc.addArg(CardCostModifierArg.TARGET, EntityReference.SELF);
		manaModifierDesc = manaModifierDesc.addArg(CardCostModifierArg.VALUE, value);
		return create(manaModifierDesc, target);
	}


	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.CARD_COST_MODIFIER);
		CardCostModifierDesc manaModifierDesc = (CardCostModifierDesc) desc.get(SpellArg.CARD_COST_MODIFIER);

		if (manaModifierDesc.containsKey(CardCostModifierArg.TARGET)
				&& target != null
				&& !target.getReference().equals(manaModifierDesc.get(CardCostModifierArg.TARGET))) {
			logger.debug("onCast {} {}: The target of this spell, {}, and the mana cost modifier's target, {}, do not match.",
					context.getGameId(), source, target, manaModifierDesc.get(CardCostModifierArg.TARGET));
		}
		// The target is the host of the mana cost modifier.
		context.getLogic().addGameEventListener(player, manaModifierDesc.create(), target == null ? player : target);
	}

}
