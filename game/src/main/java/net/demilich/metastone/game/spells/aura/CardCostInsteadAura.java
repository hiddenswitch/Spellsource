package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.trigger.CardReceivedTrigger;
import net.demilich.metastone.game.cards.Attribute;

/**
 * When this is in play, the player can only afford to play a card if {@link AuraArg#CAN_AFFORD_CONDITION} is met. If it
 * is, the {@link AuraArg#PAY_EFFECT} will be cast with the card as the {@code target}.
 * <p>
 * To use the mana cost in the pay affect, use a {@link net.demilich.metastone.game.spells.desc.valueprovider.ManaCostProvider}.
 * <p>
 * Some effects like {@link Attribute#INVOKE} need to know how much "currency" (mana or whatever) you have to spend is.
 * Specify the current amount of currency using {@link AuraArg#AMOUNT_OF_CURRENCY}.
 * <p>
 * Currently, legacy card cost substitution effects like paying for spells or murlocs using health ({@link
 * Attribute#MURLOCS_COST_HEALTH}, {@link Attribute#SPELLS_COST_HEALTH} and {@link
 * Attribute#COSTS_HEALTH_INSTEAD_OF_MANA} and {@link Attribute#AURA_COSTS_HEALTH_INSTEAD_OF_MANA}) take precedence over
 * any {@link CardCostInsteadAura}.
 * <p>
 * For example, to make the player pay for a card by milling cards:
 * <pre>
 *   {
 *     "class": "CardCostInsteadAura",
 *     "target": "FRIENDLY_HAND",
 *     "amountOfCurrency": {
 *       "class": "EntityCountValueProvider",
 *       "target": "FRIENDLY_DECK"
 *     },
 *     "canAffordCondition": {
 *       "class": "ComparisonCondition",
 *       "operation": "GREATER_OR_EQUAL",
 *       "value1": {
 *         "class": "EntityCountValueProvider",
 *         "target": "FRIENDLY_DECK"
 *       },
 *       "value2": {
 *         "class": "ManaCostProvider"
 *       }
 *     },
 *     "payEffect": {
 *       "class": "RoastSpell",
 *       "value": {
 *         "class": "ManaCostProvider"
 *       }
 *     }
 *   }
 * </pre>
 * Observe {@code "amountOfCurrency"} returns the number of cards in the deck, because that's the amount of cards
 * available to roast.
 * <p>
 * To put this aura into "play" while the host card is ordinarily in the hand, use a passive trigger to put the aura
 * onto the player entity. For example, suppose we had a card "spell_cheap_damage", a cost 2 that reads: "Deal $6
 * damage. Pay for this by destroying random friendly minions instead of spending mana.":
 * <pre>
 *   "baseManaCost": 2,
 *   "spell": {
 *     "class": "MetaSpell",
 *     "spells": [
 *       {
 *         "class": "DamageSpell",
 *         "value": 6
 *       },
 *       {
 *         "class": "RemoveEnchantmentSpell",
 *         "target": "FRIENDLY_PLAYER",
 *         "card": "spell_cheap_damage",
 *         "howMany": 1
 *       }
 *     ]
 *   },
 *   "passiveTriggers": {
 *     "eventTrigger": {
 *       "class": "CardReceivedTrigger",
 *       "hostTargetType": "IGNORE_OTHER_TARGETS"
 *     },
 *     "spell": {
 *       "class": "AddEnchantmentSpell",
 *       "target": "FRIENDLY_PLAYER",
 *       "aura": {
 *         "class": "CardCostInsteadAura",
 *         "target": "FRIENDLY_HAND",
 *         "filter": {
 *           "class": "SpecificCardFilter",
 *           "card": "spell_cheap_damage"
 *         },
 *         "amountOfCurrency": {
 *           "class": "EntityCountValueProvider",
 *           "target": "FRIENDLY_MINIONS"
 *         },
 *         "canAffordCondition": {
 *           "class": "ComparisonCondition",
 *           "operation": "GREATER_OR_EQUAL",
 *           "value1": {
 *             "class": "EntityCountValueProvider",
 *             "target": "FRIENDLY_MINIONS"
 *           },
 *           "value2": {
 *             "class": "ManaCostProvider"
 *           }
 *         },
 *         "payEffect": {
 *           "class": "RecastWhileSpell",
 *           "howMany": {
 *             "class": "ManaCostProvider"
 *           },
 *           "spell": {
 *             "class": "DestroySpell",
 *             "target": "FRIENDLY_MINIONS",
 *             "randomTarget": true
 *           }
 *         }
 *       }
 *     }
 *   }
 * </pre>
 * Observe that a passive trigger adds an aura to the friendly player. This aura implements the cost substitution. Then,
 * whenever the card is cast, removes just one instance of the aura from the friendly player.
 */
public class CardCostInsteadAura extends Aura {

	public CardCostInsteadAura(AuraDesc desc) {
		super(desc);
		this.applyAuraEffect = NullSpell.create();
		this.removeAuraEffect = NullSpell.create();
		this.getTriggers().add(CardReceivedTrigger.create());
	}

	public int getAmountOfCurrency(GameContext context, Player player, Entity target, Entity host) {
		return getDesc().getValue(AuraArg.AMOUNT_OF_CURRENCY, context, player, target, host, 0);
	}

	public Condition getCanAffordCondition() {
		return (Condition) getDesc().get(AuraArg.CAN_AFFORD_CONDITION);
	}

	public SpellDesc getPayEffect() {
		return (SpellDesc) getDesc().get(AuraArg.PAY_EFFECT);
	}
}
