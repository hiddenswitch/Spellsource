package net.demilich.metastone.game.spells.desc;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Sets;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.OpenerAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.HasEntrySet;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.trigger.Opener;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Maps.immutableEntry;

/**
 * The object describing a battlecry.
 * <p>
 * Like a spell, battlecries take {@link #targetSelection}, assumed to be {@link TargetSelection#NONE} if not specified
 * in the JSON.
 * <p>
 * For <b>example,</b> this battlecry summons a 2/1 minion:
 * <pre>
 *      {
 *          "spell": {
 *              "class": "SummonSpell",
 *              "boardPositionRelative": "RIGHT",
 *              "card": "token_ooze",
 *              "targetPlayer": "SELF"
 *          }
 *      }
 * </pre>
 * This battlecry deals 2 damage to the chosen minion:
 * <pre>
 *     {
 *         "targetSelection": "MINIONS",
 *         "spell": {
 *             "class": "DamageSpell",
 *             "value": 2
 *         }
 *     }
 * </pre>
 * This battlecry implements, "If you're holding a spell, deal 1 damage."
 * <pre>
 *     {
 *         "targetSelection": "ANY",
 *         "spell": {
 *             "class": "DamageSpell",
 *             "value": 1
 *         },
 *         "condition": {
 *             "class": "HoldsCardCondition",
 *             "cardFilter": {
 *                 "class": "CardFilter",
 *                 "cardType": "SPELL"
 *             }
 *         }
 *     }
 * </pre>
 * This battlecry is one of two for "Choose One - Deal 2 damage, or Draw a Card". Notice the flavorful name.
 * <pre>
 *     {
 *         "spell": {
 *             "class": "DrawCardSpell"
 *         },
 *         "name": "Study in the Library",
 *         "description": "Draw a Card"
 *     }
 * </pre>
 *
 * @see net.demilich.metastone.game.logic.GameLogic#performBattlecryAction(int, Actor, Player, OpenerAction) to see
 * 		how the battlecry action is processed.
 */
@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
public final class OpenerDesc implements Serializable, HasEntrySet<BattlecryDescArg, Object>, Cloneable, AbstractEnchantmentDesc<Opener> {
	public SpellDesc spell;
	public TargetSelection targetSelection = TargetSelection.NONE;
	public ConditionDesc condition;
	public String name;
	public String description;
	public TargetSelection targetSelectionOverride;
	public ConditionDesc targetSelectionCondition;

	public OpenerDesc() {
		super();
	}

	/**
	 * The targets the battlecry can choose from.
	 * <p>
	 * Battlecries only go into target selection if their {@link #condition} is met.
	 * <p>
	 * If target selection is specified and no valid targets are available, the battlecry is not cast.
	 */
	public TargetSelection getTargetSelection() {
		return targetSelection != null ? targetSelection : TargetSelection.NONE;
	}

	@JsonIgnore
	public OpenerAction toOpenerAction() {
		OpenerAction battlecry = OpenerAction.createBattlecry(getSpell(), getTargetSelection());
		if (condition != null) {
			battlecry.setCondition(condition.create());
		}
		if (targetSelectionOverride != null) {
			battlecry.setTargetSelectionOverride(targetSelectionOverride);
		}
		if (targetSelectionCondition != null) {
			battlecry.setTargetSelectionCondition(targetSelectionCondition.create());
		}
		return battlecry;
	}

	/**
	 * The spell to cast when this battlecry's {@link #condition} is true (or always cast if no condition is specified and
	 * a valid target is available).
	 */
	public SpellDesc getSpell() {
		return spell;
	}

	public void setSpell(SpellDesc spell) {
		this.spell = spell;
	}

	public void setTargetSelection(TargetSelection targetSelection) {
		this.targetSelection = targetSelection;
	}

	/**
	 * The condition to evaluate if the player will be prompted to make a battlecry action.
	 * <p>
	 * The condition is also used to determine if the {@link net.demilich.metastone.game.cards.Card} in the player's hand
	 * should receive a "yellow glow" indicating its condition is met.
	 * <p>
	 * In order to implement this glow, it is preferred to specify a condition here rather than using a {@link
	 * net.demilich.metastone.game.spells.ConditionalSpell} in the {@link #spell} field.
	 */
	public ConditionDesc getCondition() {
		return condition;
	}

	public void setCondition(ConditionDesc condition) {
		this.condition = condition;
	}

	/**
	 * A name used to render a card representing the battlecry. When not specified, the description is used instead. Used
	 * for choose-one battlecries.
	 * <p>
	 * If the {@link #spell} is a {@link net.demilich.metastone.game.spells.TransformMinionSpell}, the {@link
	 * SpellArg#CARD} of that spell (the minion the choose-one minion will be transformed into) will be used to render the
	 * choice instead, regardless of your specification of name.
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * A description used to render a card representing the battlecry. Used for choose-one battlecries.
	 * <p>
	 * If the {@link #spell} is a {@link net.demilich.metastone.game.spells.TransformMinionSpell}, the {@link
	 * SpellArg#CARD} of that spell (the minion the choose-one minion will be transformed into) will be used to render the
	 * choice instead, regardless of your specification of description.
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Map.Entry<BattlecryDescArg, Object>> entrySet() {
		@SuppressWarnings("unchecked")
		HashSet<Map.Entry<BattlecryDescArg, Object>> entries = Sets.newHashSet(
				immutableEntry(BattlecryDescArg.SPELL, spell),
				immutableEntry(BattlecryDescArg.TARGET_SELECTION, targetSelection),
				immutableEntry(BattlecryDescArg.CONDITION, condition),
				immutableEntry(BattlecryDescArg.NAME, name),
				immutableEntry(BattlecryDescArg.DESCRIPTION, description),
				immutableEntry(BattlecryDescArg.TARGET_SELECTION_OVERRIDE, targetSelectionOverride),
				immutableEntry(BattlecryDescArg.TARGET_SELECTION_CONDITION, targetSelectionCondition)
		);
		return entries;
	}

	@Override
	public OpenerDesc clone() {
		try {
			OpenerDesc desc = (OpenerDesc) super.clone();
			if (desc.spell != null) {
				desc.spell = spell.clone();
			}
			if (desc.condition != null) {
				desc.condition = condition.clone();
			}
			return desc;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public ConditionDesc getTargetSelectionCondition() {
		return targetSelectionCondition;
	}

	public TargetSelection getTargetSelectionOverride() {
		return targetSelectionOverride;
	}

	public void setTargetSelectionCondition(ConditionDesc targetSelectionCondition) {
		this.targetSelectionCondition = targetSelectionCondition;
	}

	public void setTargetSelectionOverride(TargetSelection targetSelectionOverride) {
		this.targetSelectionOverride = targetSelectionOverride;
	}

	@Override
	@Suspendable
	public Optional<Opener> tryCreate(GameContext context, Player player, Entity effectSource, Card enchantmentSource, Entity host, boolean force) {
		return context.getLogic().tryCreateOpener(player,this, effectSource,enchantmentSource,host, force);
	}
}
