package net.demilich.metastone.game.cards.desc.compiler;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.EventTrigger;

/**
 * Compile-time stubs whose bytecode bodies are woven by {@link SpellsourceClassfileWeaver} into direct field and
 * virtual method accesses on {@link Desc}, {@link ValueProvider}, {@link EntityFilter}, {@link Condition},
 * {@link Spell}, {@link EventTrigger}, and {@link Enchantment}.
 */
public final class CompilerBridge {
	private CompilerBridge() {
	}

	public static Object getCompiled(Desc<?, ?> desc) {
		throw new AssertionError("woven at build time");
	}

	public static void setCompiled(Desc<?, ?> desc, Object compiled) {
		throw new AssertionError("woven at build time");
	}

	public static int evaluateProvideValue(ValueProvider vp, GameContext context, Player player, Entity target, Entity host) {
		throw new AssertionError("woven at build time");
	}

	public static int evaluateFullValue(ValueProvider vp, GameContext context, Player player, Entity target, Entity host) {
		throw new AssertionError("woven at build time");
	}

	public static boolean evaluateTest(EntityFilter ef, GameContext context, Player player, Entity entity, Entity host) {
		throw new AssertionError("woven at build time");
	}

	public static boolean evaluateFullMatches(EntityFilter ef, GameContext context, Player player, Entity entity, Entity host) {
		throw new AssertionError("woven at build time");
	}

	public static boolean evaluateIsFulfilled(Condition cond, GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		throw new AssertionError("woven at build time");
	}

	public static boolean evaluateFullIsFulfilled(Condition cond, GameContext context, Player player, Entity source, Entity target) {
		throw new AssertionError("woven at build time");
	}

	public static boolean isTargetOverridden(Condition cond) {
		throw new AssertionError("woven at build time");
	}

	public static boolean isMultiTargetAnd(Condition cond) {
		throw new AssertionError("woven at build time");
	}

	public static boolean isAtLeastOneTargetRequired(Condition cond) {
		throw new AssertionError("woven at build time");
	}

	public static boolean isFilterUsed(Condition cond) {
		throw new AssertionError("woven at build time");
	}

	public static void evaluateOnCast(Spell spell, GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		throw new AssertionError("woven at build time");
	}

	public static boolean evaluateInnerQueues(EventTrigger trigger, GameEvent event, Enchantment enchantment, Entity host) {
		throw new AssertionError("woven at build time");
	}

	public static boolean evaluateHostConditionMet(EventTrigger trigger, GameEvent event, Entity host) {
		throw new AssertionError("woven at build time");
	}

	public static boolean evaluateFullQueues(EventTrigger trigger, GameEvent event, Enchantment enchantment, Entity host, int playerId) {
		throw new AssertionError("woven at build time");
	}

	public static boolean evaluateFullFires(EventTrigger trigger, GameEvent event, Entity host, int playerId) {
		throw new AssertionError("woven at build time");
	}

	public static long getEnchantmentMask(Enchantment enchantment) {
		throw new AssertionError("woven at build time");
	}

	public static void setEnchantmentMask(Enchantment enchantment, long mask) {
		throw new AssertionError("woven at build time");
	}
}
