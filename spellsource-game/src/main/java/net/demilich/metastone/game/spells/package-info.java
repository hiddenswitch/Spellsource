/**
 * Spells, filters, conditions, value providers, card sources, auras, and triggers in Spellsource.
 */
@ComponentPackage(
		commonSpellArgs = {
				SpellArg.CLASS,
				SpellArg.FILTER,
				SpellArg.TARGET,
				SpellArg.NAME,
				SpellArg.DESCRIPTION,
				SpellArg.RANDOM_TARGET,
				SpellArg.TARGET_PLAYER,
				SpellArg.SPELL,
				SpellArg.BOARD_POSITION_RELATIVE,
				SpellArg.BOARD_POSITION_ABSOLUTE,
				SpellArg.AFTERMATH_ID,
				SpellArg.CARD
		},
		commonFilterArgs = {
				EntityFilterArg.CLASS,
				EntityFilterArg.INVERT,
				EntityFilterArg.TARGET_PLAYER,
				EntityFilterArg.AND_CONDITION,
				EntityFilterArg.TARGET
		},
		commonValueProviderArgs = {
				ValueProviderArg.CLASS,
				ValueProviderArg.TARGET_PLAYER,
				ValueProviderArg.MULTIPLIER,
				ValueProviderArg.OFFSET
		},
		commonConditionArgs = {
				ConditionArg.CLASS,
				ConditionArg.INVERT,
				ConditionArg.TARGET_PLAYER,
				ConditionArg.TARGET,
				ConditionArg.FILTER
		},
		commonTriggerArgs = {
				EventTriggerArg.CLASS,
				EventTriggerArg.TARGET_PLAYER,
				EventTriggerArg.SOURCE_PLAYER,
				EventTriggerArg.HOST_TARGET_TYPE,
				EventTriggerArg.QUEUE_CONDITION,
				EventTriggerArg.FIRE_CONDITION,
				EventTriggerArg.TARGET
		},
		mixins = {
				@ComponentPackage.Mixin(
						parameterName = "cards",
						parameterType = CardList.class,
						helperClass = SpellUtils.class,
						helperMethod = "getCards",
						consumedArgs = {"CARD", "CARDS", "CARD_SOURCE", "CARD_FILTER", "VALUE"}
				),
				@ComponentPackage.Mixin(
						parameterName = "boardPosition",
						parameterType = int.class,
						helperClass = SpellUtils.class,
						helperMethod = "getBoardPosition",
						consumedArgs = {"BOARD_POSITION_ABSOLUTE", "BOARD_POSITION_RELATIVE"}
				),
				@ComponentPackage.Mixin(
						parameterName = "targetedEntities",
						parameterType = List.class,
						helperClass = EntityFilter.class,
						helperMethod = "getTargetedEntities",
						consumedArgs = {"TARGET"}
				)
		}
)
package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg;

import java.util.List;
