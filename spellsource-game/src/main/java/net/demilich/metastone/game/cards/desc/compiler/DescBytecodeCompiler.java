package net.demilich.metastone.game.cards.desc.compiler;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.DamageTypeMessage.DamageType;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.AddAttributeSpell;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.ConditionalEffectSpell;
import net.demilich.metastone.game.spells.ConditionalSpell;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.Default;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.DrawCardSpell;
import net.demilich.metastone.game.spells.EitherOrSpell;
import net.demilich.metastone.game.spells.GameValue;
import net.demilich.metastone.game.spells.HealSpell;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.ModifyAttributeSpell;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.PlayerAttribute;
import net.demilich.metastone.game.spells.RemoveAttributeSpell;
import net.demilich.metastone.game.spells.SetAttackSpell;
import net.demilich.metastone.game.spells.SetHpSpell;
import net.demilich.metastone.game.spells.SilenceSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.aura.IncorruptibilityAura;
import net.demilich.metastone.game.spells.aura.ModifyBuffSpellAura;
import net.demilich.metastone.game.spells.aura.SpecificCardFilterOverrideAura;
import net.demilich.metastone.game.spells.aura.SpellOverrideAura;
import net.demilich.metastone.game.spells.desc.LambdaSpellDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.condition.AndCondition;
import net.demilich.metastone.game.spells.desc.condition.AttributeCondition;
import net.demilich.metastone.game.spells.desc.condition.ComparisonCondition;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.condition.CountCondition;
import net.demilich.metastone.game.spells.desc.condition.GraveyardCountCondition;
import net.demilich.metastone.game.spells.desc.condition.IsDeadCondition;
import net.demilich.metastone.game.spells.desc.condition.MinionCountCondition;
import net.demilich.metastone.game.spells.desc.condition.OrCondition;
import net.demilich.metastone.game.spells.desc.condition.OwnedByPlayerCondition;
import net.demilich.metastone.game.spells.desc.condition.RaceCondition;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.AttributeFilter;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterArg;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterDesc;
import net.demilich.metastone.game.spells.desc.filter.OrFilter;
import net.demilich.metastone.game.spells.desc.filter.RaceFilter;
import net.demilich.metastone.game.spells.desc.filter.SpecificCardFilter;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ConditionalValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ConstValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.DiedThisTurnValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.EntityCountValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.GameValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.PlayerAttributeValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ReduceValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.SupremacyTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compiles {@link SpellDesc}, {@link ValueProviderDesc}, {@link EntityFilterDesc}, {@link ConditionDesc}, and
 * {@link EventTriggerDesc} instances into hidden JVM classes using the JDK {@link java.lang.classfile} API and
 * {@link MethodHandles.Lookup#defineHiddenClassWithClassData}.
 * <p>
 * Performs deep recursive AST bytecode inlining across nested {@link ValueProvider}, {@link EntityFilter},
 * {@link Condition}, and {@link Spell} expression trees so child nodes emit bytecode directly into the parent
 * hidden class's method body without intermediate interface dispatch or map lookups.
 */
public final class DescBytecodeCompiler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DescBytecodeCompiler.class);
	private static final String PKG = "net.demilich.metastone.game.cards.desc.compiler";
	private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
	private static final int MAX_INLINE_DEPTH = 4;
	private static final boolean IN_NATIVE_IMAGE = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

	private static final EnumSet<DamageType> MAGICAL_DAMAGE_TYPE = EnumSet.of(DamageType.MAGICAL);
	private static final CompiledSpell NOOP_SPELL = (context, player, desc, source, targets) -> {
	};
	private static final ConcurrentHashMap<Class<?>, Boolean> OVERRIDES_CAST_CACHE = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Class<?>, Optional<Option1MethodSpec>> OPTION1_SPEC_CACHE = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Desc<?, ?>, net.demilich.metastone.game.cards.desc.HasDesc<?>> INTERNED_COMPONENTS = new ConcurrentHashMap<>(4096);
	private record LoweredEntry<T>(T compiled, boolean definedHiddenClass) {
	}

	private static final ConcurrentHashMap<ValueProviderDesc, CompiledValueProvider> COMPILED_VP_CACHE = new ConcurrentHashMap<>(1024);
	private static final ConcurrentHashMap<EntityFilterDesc, CompiledEntityFilter> COMPILED_EF_CACHE = new ConcurrentHashMap<>(1024);
	private static final ConcurrentHashMap<ConditionDesc, CompiledCondition> COMPILED_COND_CACHE = new ConcurrentHashMap<>(1024);
	private static final ConcurrentHashMap<SpellDesc, CompiledSpell> COMPILED_SPELL_CACHE = new ConcurrentHashMap<>(2048);
	private static final ConcurrentHashMap<ValueProviderDesc, LoweredEntry<CompiledValueProvider>> PERSISTENT_LOWERED_VP_CACHE = new ConcurrentHashMap<>(1024);
	private static final ConcurrentHashMap<EntityFilterDesc, LoweredEntry<CompiledEntityFilter>> PERSISTENT_LOWERED_EF_CACHE = new ConcurrentHashMap<>(1024);
	private static final ConcurrentHashMap<ConditionDesc, LoweredEntry<CompiledCondition>> PERSISTENT_LOWERED_COND_CACHE = new ConcurrentHashMap<>(1024);
	private static final ConcurrentHashMap<SpellDesc, LoweredEntry<CompiledSpell>> PERSISTENT_LOWERED_SPELL_CACHE = new ConcurrentHashMap<>(2048);
	private static final ConcurrentHashMap<ValueProviderDesc, ValueProvider> PENDING_PREWARM_VP = new ConcurrentHashMap<>(1024);
	private static final ConcurrentHashMap<EntityFilterDesc, EntityFilter> PENDING_PREWARM_EF = new ConcurrentHashMap<>(1024);
	private static final ConcurrentHashMap<ConditionDesc, Condition> PENDING_PREWARM_COND = new ConcurrentHashMap<>(1024);
	private static final ConcurrentHashMap<SpellDesc, Boolean> PENDING_PREWARM_SPELL = new ConcurrentHashMap<>(2048);
	private static final java.util.concurrent.atomic.AtomicInteger HIDDEN_CLASSES_DEFINED = new java.util.concurrent.atomic.AtomicInteger();

	/**
	 * Controls whether {@link DescBytecodeCompiler} lowers Option 1 typed component methods ({@code onCast},
	 * {@code test}, {@code provideValue}, {@code isFulfilled}) directly into pre-bound hidden-class bytecode with
	 * zero runtime {@code desc.get(...)} lookups, versus delegating to the woven {@code desc.get(...)} bridge.
	 */
	public static volatile boolean ENABLE_OPTION1_LOWERING = true;

	public static int getHiddenClassesDefinedCount() {
		return HIDDEN_CLASSES_DEFINED.get();
	}

	public static int getInternedComponentCount() {
		return INTERNED_COMPONENTS.size();
	}

	public static int getCompiledCacheSize() {
		return COMPILED_VP_CACHE.size() + COMPILED_EF_CACHE.size() + COMPILED_COND_CACHE.size() + COMPILED_SPELL_CACHE.size();
	}

	public static void clearCaches() {
		INTERNED_COMPONENTS.clear();
		COMPILED_VP_CACHE.clear();
		COMPILED_EF_CACHE.clear();
		COMPILED_COND_CACHE.clear();
		COMPILED_SPELL_CACHE.clear();
		HIDDEN_CLASSES_DEFINED.set(0);
	}

	private static final java.util.concurrent.atomic.AtomicBoolean PREWARMING = new java.util.concurrent.atomic.AtomicBoolean();

	private static boolean isCompilerHiddenClass(Object compiled) {
		return compiled != null && compiled.getClass().getName().startsWith(PKG + ".Hidden");
	}

	public static void drainPendingPrewarmIfNeeded() {
		if (!ENABLE_OPTION1_LOWERING) {
			return;
		}
		if (PENDING_PREWARM_VP.isEmpty() && PENDING_PREWARM_EF.isEmpty() && PENDING_PREWARM_COND.isEmpty() && PENDING_PREWARM_SPELL.isEmpty()) {
			return;
		}
		if (!PREWARMING.compareAndSet(false, true)) {
			return;
		}
		int savedCount = HIDDEN_CLASSES_DEFINED.get();
		var savedCompiled = new java.util.IdentityHashMap<Desc<?, ?>, Object>();
		try {
			var vps = new ArrayList<>(PENDING_PREWARM_VP.entrySet());
			var efs = new ArrayList<>(PENDING_PREWARM_EF.entrySet());
			var conds = new ArrayList<>(PENDING_PREWARM_COND.entrySet());
			var spells = new ArrayList<>(PENDING_PREWARM_SPELL.keySet());
			PENDING_PREWARM_VP.clear();
			PENDING_PREWARM_EF.clear();
			PENDING_PREWARM_COND.clear();
			PENDING_PREWARM_SPELL.clear();

			for (var entry : vps) {
				ValueProviderDesc d = entry.getKey();
				savedCompiled.put(d, CompilerBridge.getCompiled(d));
				CompilerBridge.setCompiled(d, null);
			}
			for (var entry : efs) {
				EntityFilterDesc d = entry.getKey();
				savedCompiled.put(d, CompilerBridge.getCompiled(d));
				CompilerBridge.setCompiled(d, null);
			}
			for (var entry : conds) {
				ConditionDesc d = entry.getKey();
				savedCompiled.put(d, CompilerBridge.getCompiled(d));
				CompilerBridge.setCompiled(d, null);
			}
			for (SpellDesc d : spells) {
				savedCompiled.put(d, CompilerBridge.getCompiled(d));
				CompilerBridge.setCompiled(d, null);
			}

			for (var entry : vps) {
				ValueProviderDesc d = entry.getKey();
				ValueProvider vp = entry.getValue();
				PERSISTENT_LOWERED_VP_CACHE.computeIfAbsent(d, k -> {
					CompiledValueProvider c = compile(vp);
					return new LoweredEntry<>(c, isCompilerHiddenClass(c));
				});
			}
			for (var entry : efs) {
				EntityFilterDesc d = entry.getKey();
				EntityFilter ef = entry.getValue();
				PERSISTENT_LOWERED_EF_CACHE.computeIfAbsent(d, k -> {
					CompiledEntityFilter c = compile(ef);
					return new LoweredEntry<>(c, isCompilerHiddenClass(c));
				});
			}
			for (var entry : conds) {
				ConditionDesc d = entry.getKey();
				Condition cond = entry.getValue();
				PERSISTENT_LOWERED_COND_CACHE.computeIfAbsent(d, k -> {
					CompiledCondition c = compile(cond);
					return new LoweredEntry<>(c, isCompilerHiddenClass(c));
				});
			}
			for (SpellDesc d : spells) {
				PERSISTENT_LOWERED_SPELL_CACHE.computeIfAbsent(d, k -> {
					CompiledSpell c = compileSpell(k);
					return new LoweredEntry<>(c, isCompilerHiddenClass(c));
				});
			}
		} finally {
			for (var e : savedCompiled.entrySet()) {
				CompilerBridge.setCompiled(e.getKey(), e.getValue());
			}
			COMPILED_VP_CACHE.clear();
			COMPILED_EF_CACHE.clear();
			COMPILED_COND_CACHE.clear();
			COMPILED_SPELL_CACHE.clear();
			HIDDEN_CLASSES_DEFINED.set(savedCount);
			PREWARMING.set(false);
		}
	}

	public static net.demilich.metastone.game.cards.desc.HasDesc<?> internReadOnlyComponent(
			Desc<?, ?> desc,
			net.demilich.metastone.game.cards.desc.HasDesc<?> instance) {
		if (!ENABLE_OPTION1_LOWERING || DescAuditRecorder.ENABLED) {
			return instance;
		}
		if (instance instanceof Spell
				|| instance instanceof EntityFilter
				|| instance instanceof ValueProvider
				|| instance instanceof Condition) {
			var existing = INTERNED_COMPONENTS.putIfAbsent(desc, instance);
			return existing != null ? existing : instance;
		}
		return instance;
	}

	static final ClassDesc CD_COMPILER = ClassDesc.of(PKG, "DescBytecodeCompiler");
	static final ClassDesc CD_COMPILED_VP = ClassDesc.of(PKG, "CompiledValueProvider");
	static final ClassDesc CD_COMPILED_EF = ClassDesc.of(PKG, "CompiledEntityFilter");
	static final ClassDesc CD_COMPILED_COND = ClassDesc.of(PKG, "CompiledCondition");
	static final ClassDesc CD_COMPILED_SPELL = ClassDesc.of(PKG, "CompiledSpell");

	static final ClassDesc CD_GAME_CONTEXT = ClassDesc.of("net.demilich.metastone.game.GameContext");
	static final ClassDesc CD_GAME_LOGIC = ClassDesc.of("net.demilich.metastone.game.logic.GameLogic");
	static final ClassDesc CD_PLAYER = ClassDesc.of("net.demilich.metastone.game.Player");
	static final ClassDesc CD_ENTITY = ClassDesc.of("net.demilich.metastone.game.entities.Entity");
	static final ClassDesc CD_CARD = ClassDesc.of("net.demilich.metastone.game.cards.Card");
	static final ClassDesc CD_CARD_LIST = ClassDesc.of("net.demilich.metastone.game.cards.CardList");
	static final ClassDesc CD_ATTRIBUTE = ClassDesc.of("net.demilich.metastone.game.cards.Attribute");
	static final ClassDesc CD_ENTITY_REF = ClassDesc.of("net.demilich.metastone.game.targeting.EntityReference");
	static final ClassDesc CD_COMP_OP = ClassDesc.of("net.demilich.metastone.game.spells.desc.filter.ComparisonOperation");
	static final ClassDesc CD_ALG_OP = ClassDesc.of("net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation");
	static final ClassDesc CD_VP = ClassDesc.of("net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider");
	static final ClassDesc CD_VP_DESC = ClassDesc.of("net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc");
	static final ClassDesc CD_ATTR_VP = ClassDesc.of("net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider");
	static final ClassDesc CD_EF = ClassDesc.of("net.demilich.metastone.game.spells.desc.filter.EntityFilter");
	static final ClassDesc CD_EF_DESC = ClassDesc.of("net.demilich.metastone.game.spells.desc.filter.EntityFilterDesc");
	static final ClassDesc CD_COND = ClassDesc.of("net.demilich.metastone.game.spells.desc.condition.Condition");
	static final ClassDesc CD_COND_DESC = ClassDesc.of("net.demilich.metastone.game.spells.desc.condition.ConditionDesc");
	static final ClassDesc CD_SPELL = ClassDesc.of("net.demilich.metastone.game.spells.Spell");
	static final ClassDesc CD_SPELL_DESC = ClassDesc.of("net.demilich.metastone.game.spells.desc.SpellDesc");
	static final ClassDesc CD_RACE = ClassDesc.of("net.demilich.metastone.game.entities.minions.Race");
	static final ClassDesc CD_SPELL_UTILS = ClassDesc.of("net.demilich.metastone.game.spells.SpellUtils");
	static final ClassDesc CD_MATH = ClassDesc.of("java.lang.Math");
	static final ClassDesc CD_LIST = ClassDesc.of("java.util.List");
	static final ClassDesc CD_SET = ClassDesc.of("java.util.Set");
	static final ClassDesc CD_DEQUE = ClassDesc.of("java.util.Deque");

	static final MethodTypeDesc MTD_VP_COMPUTE = MethodTypeDesc.of(
			ConstantDescs.CD_int, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY);
	static final MethodTypeDesc MTD_EF_MATCHES = MethodTypeDesc.of(
			ConstantDescs.CD_boolean, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY);
	static final MethodTypeDesc MTD_COND_FULFILLED = MethodTypeDesc.of(
			ConstantDescs.CD_boolean, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY);
	static final MethodTypeDesc MTD_COND_EVAL = MethodTypeDesc.of(
			ConstantDescs.CD_boolean, CD_GAME_CONTEXT, CD_PLAYER, CD_COND_DESC, CD_ENTITY, CD_ENTITY);
	static final MethodTypeDesc MTD_SPELL_CAST = MethodTypeDesc.of(
			ConstantDescs.MTD_void.returnType(), CD_GAME_CONTEXT, CD_PLAYER, CD_SPELL_DESC, CD_ENTITY, CD_LIST);
	static final MethodTypeDesc MTD_SPELL_ONCAST = MethodTypeDesc.of(
			ConstantDescs.MTD_void.returnType(), CD_GAME_CONTEXT, CD_PLAYER, CD_SPELL_DESC, CD_ENTITY, CD_ENTITY);

	private DescBytecodeCompiler() {
	}

	static int addConstant(List<Object> classData, Object value) {
		int idx = classData.size();
		classData.add(value);
		return idx;
	}

	static void loadClassData(CodeBuilder cob, int index, ClassDesc type) {
		cob.ldc(DynamicConstantDesc.ofNamed(
				ConstantDescs.BSM_CLASS_DATA_AT,
				ConstantDescs.DEFAULT_NAME,
				type,
				index));
	}

	private static void emitDefaultConstructor(ClassBuilder clb) {
		clb.withMethodBody(ConstantDescs.INIT_NAME, ConstantDescs.MTD_void, ClassFile.ACC_PUBLIC, cob -> {
			cob.aload(0)
					.invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME, ConstantDescs.MTD_void)
					.return_();
		});
	}

	static void loadSlotOrNull(CodeBuilder cob, int slot) {
		if (slot <= 0) {
			cob.aconst_null();
		} else {
			cob.aload(slot);
		}
	}

	// =========================================================================
	// Woven Entry Points & Caching Helpers
	// =========================================================================

	public static CompiledValueProvider getCompiled(ValueProvider vp) {
		ValueProviderDesc d = vp.getDesc();
		if (d == null) {
			return (context, player, target, host) -> CompilerBridge.evaluateFullValue(vp, context, player, target, host);
		}
		CompiledValueProvider c = (CompiledValueProvider) CompilerBridge.getCompiled(d);
		if (c == null) {
			if (ENABLE_OPTION1_LOWERING && d.isReadOnly()) {
				c = COMPILED_VP_CACHE.computeIfAbsent(d, k -> {
					LoweredEntry<CompiledValueProvider> entry = PERSISTENT_LOWERED_VP_CACHE.computeIfAbsent(k, k2 -> {
						CompiledValueProvider compiled = compile(vp);
						boolean defined = isCompilerHiddenClass(compiled);
						if (defined) {
							HIDDEN_CLASSES_DEFINED.decrementAndGet();
						}
						return new LoweredEntry<>(compiled, defined);
					});
					if (entry.definedHiddenClass()) {
						HIDDEN_CLASSES_DEFINED.incrementAndGet();
					}
					return entry.compiled();
				});
			} else {
				if (d.isReadOnly() && !PERSISTENT_LOWERED_VP_CACHE.containsKey(d)) {
					PENDING_PREWARM_VP.putIfAbsent(d, vp);
				}
				c = compile(vp);
			}
			CompilerBridge.setCompiled(d, c);
		}
		return c;
	}

	public static int evalValueProvider(ValueProvider vp, GameContext context, Player player, Entity target, Entity host) {
		ValueProviderDesc d = vp.getDesc();
		if (DescAuditRecorder.ENABLED) {
			DescAuditRecorder.push(vp.getClass(), d);
			try {
				return CompilerBridge.evaluateFullValue(vp, context, player, target, host);
			} finally {
				DescAuditRecorder.pop();
			}
		}
		if (d != null) {
			CompiledValueProvider c = (CompiledValueProvider) CompilerBridge.getCompiled(d);
			if (c != null) {
				return c.compute(context, player, target, host);
			}
			if (d.isReadOnly()) {
				return getCompiled(vp).compute(context, player, target, host);
			}
		}
		return CompilerBridge.evaluateFullValue(vp, context, player, target, host);
	}

	public static CompiledEntityFilter getCompiled(EntityFilter ef) {
		EntityFilterDesc d = ef.getDesc();
		if (d == null) {
			return (context, player, entity, host) -> CompilerBridge.evaluateFullMatches(ef, context, player, entity, host);
		}
		CompiledEntityFilter c = (CompiledEntityFilter) CompilerBridge.getCompiled(d);
		if (c == null) {
			if (ENABLE_OPTION1_LOWERING && d.isReadOnly()) {
				c = COMPILED_EF_CACHE.computeIfAbsent(d, k -> {
					LoweredEntry<CompiledEntityFilter> entry = PERSISTENT_LOWERED_EF_CACHE.computeIfAbsent(k, k2 -> {
						CompiledEntityFilter compiled = compile(ef);
						boolean defined = isCompilerHiddenClass(compiled);
						if (defined) {
							HIDDEN_CLASSES_DEFINED.decrementAndGet();
						}
						return new LoweredEntry<>(compiled, defined);
					});
					if (entry.definedHiddenClass()) {
						HIDDEN_CLASSES_DEFINED.incrementAndGet();
					}
					return entry.compiled();
				});
			} else {
				if (d.isReadOnly() && !PERSISTENT_LOWERED_EF_CACHE.containsKey(d)) {
					PENDING_PREWARM_EF.putIfAbsent(d, ef);
				}
				c = compile(ef);
			}
			CompilerBridge.setCompiled(d, c);
		}
		return c;
	}

	public static boolean evalEntityFilter(EntityFilter ef, GameContext context, Player player, Entity entity, Entity host) {
		EntityFilterDesc d = ef.getDesc();
		if (DescAuditRecorder.ENABLED) {
			DescAuditRecorder.push(ef.getClass(), d);
			try {
				return CompilerBridge.evaluateFullMatches(ef, context, player, entity, host);
			} finally {
				DescAuditRecorder.pop();
			}
		}
		if (d != null) {
			CompiledEntityFilter c = (CompiledEntityFilter) CompilerBridge.getCompiled(d);
			if (c != null) {
				return c.matches(context, player, entity, host);
			}
			if (d.isReadOnly()) {
				return getCompiled(ef).matches(context, player, entity, host);
			}
		}
		return CompilerBridge.evaluateFullMatches(ef, context, player, entity, host);
	}

	public static CompiledCondition getCompiled(Condition cond) {
		ConditionDesc d = cond.getDesc();
		if (d == null) {
			return (context, player, source, target) -> CompilerBridge.evaluateFullIsFulfilled(cond, context, player, source, target);
		}
		CompiledCondition c = (CompiledCondition) CompilerBridge.getCompiled(d);
		if (c == null) {
			if (ENABLE_OPTION1_LOWERING && d.isReadOnly()) {
				c = COMPILED_COND_CACHE.computeIfAbsent(d, k -> {
					LoweredEntry<CompiledCondition> entry = PERSISTENT_LOWERED_COND_CACHE.computeIfAbsent(k, k2 -> {
						CompiledCondition compiled = compile(cond);
						boolean defined = isCompilerHiddenClass(compiled);
						if (defined) {
							HIDDEN_CLASSES_DEFINED.decrementAndGet();
						}
						return new LoweredEntry<>(compiled, defined);
					});
					if (entry.definedHiddenClass()) {
						HIDDEN_CLASSES_DEFINED.incrementAndGet();
					}
					return entry.compiled();
				});
			} else {
				if (d.isReadOnly() && !PERSISTENT_LOWERED_COND_CACHE.containsKey(d)) {
					PENDING_PREWARM_COND.putIfAbsent(d, cond);
				}
				c = compile(cond);
			}
			CompilerBridge.setCompiled(d, c);
		}
		return c;
	}

	public static boolean evalCondition(Condition cond, GameContext context, Player player, Entity source, Entity target) {
		ConditionDesc d = cond.getDesc();
		if (DescAuditRecorder.ENABLED) {
			DescAuditRecorder.push(cond.getClass(), d);
			try {
				return CompilerBridge.evaluateFullIsFulfilled(cond, context, player, source, target);
			} finally {
				DescAuditRecorder.pop();
			}
		}
		if (d != null) {
			CompiledCondition c = (CompiledCondition) CompilerBridge.getCompiled(d);
			if (c != null) {
				return c.isFulfilled(context, player, source, target);
			}
			if (d.isReadOnly()) {
				return getCompiled(cond).isFulfilled(context, player, source, target);
			}
		}
		return CompilerBridge.evaluateFullIsFulfilled(cond, context, player, source, target);
	}

	public static CompiledSpell getCompiledSpell(SpellDesc desc) {
		CompiledSpell cs = (CompiledSpell) CompilerBridge.getCompiled(desc);
		if (cs != null) {
			return cs;
		}
		if (ENABLE_OPTION1_LOWERING && desc.isReadOnly()) {
			cs = COMPILED_SPELL_CACHE.computeIfAbsent(desc, k -> {
				LoweredEntry<CompiledSpell> entry = PERSISTENT_LOWERED_SPELL_CACHE.computeIfAbsent(k, k2 -> {
					CompiledSpell compiled = compileSpell(k2);
					boolean defined = isCompilerHiddenClass(compiled);
					if (defined) {
						HIDDEN_CLASSES_DEFINED.decrementAndGet();
					}
					return new LoweredEntry<>(compiled, defined);
				});
				if (entry.definedHiddenClass()) {
					HIDDEN_CLASSES_DEFINED.incrementAndGet();
				}
				return entry.compiled();
			});
		} else {
			if (desc.isReadOnly() && !PERSISTENT_LOWERED_SPELL_CACHE.containsKey(desc)) {
				PENDING_PREWARM_SPELL.putIfAbsent(desc, Boolean.TRUE);
			}
			cs = compileSpell(desc);
		}
		CompilerBridge.setCompiled(desc, cs);
		return cs;
	}

	public static void castSpell(Spell spell, GameContext context, Player player, SpellDesc spellDesc, Entity source, List<Entity> targets) {
		if (DescAuditRecorder.ENABLED) {
			if (spell != null && spellDesc != null) {
				DescAuditRecorder.push(spell.getClass(), spellDesc);
				try {
					spell.cast(context, player, spellDesc, source, targets);
				} finally {
					DescAuditRecorder.pop();
				}
			}
			return;
		}
		CompiledSpell compiledSpell = (CompiledSpell) CompilerBridge.getCompiled(spellDesc);
		if (compiledSpell != null) {
			compiledSpell.cast(context, player, spellDesc, source, targets);
		} else if (spellDesc.isReadOnly()) {
			getCompiledSpell(spellDesc).cast(context, player, spellDesc, source, targets);
		} else if (spell != null) {
			spell.cast(context, player, spellDesc, source, targets);
		}
	}

	public static Object freezeMemoized(Object obj) {
		if (obj instanceof net.demilich.metastone.game.cards.Freezable f) {
			f.freeze();
		} else if (obj instanceof net.demilich.metastone.game.cards.desc.HasDesc<?> hd && hd.getDesc() != null) {
			hd.getDesc().freeze();
		}
		return obj;
	}

	// =========================================================================
	// Enchantment & EventTrigger Lowering
	// =========================================================================

	private static final long MASK_INITIALIZED_BIT = 1L << 63;

	public static boolean evalEnchantmentInterestedIn(Enchantment enchantment, GameEventType eventType) {
		if (!enchantment.getUsesSpellTrigger()) {
			return false;
		}
		long mask = CompilerBridge.getEnchantmentMask(enchantment);
		if (mask == 0L) {
			mask = computeEnchantmentMask(enchantment);
			CompilerBridge.setEnchantmentMask(enchantment, mask);
		}
		return (mask & (1L << eventType.ordinal())) != 0L;
	}

	private static long computeEnchantmentMask(Enchantment enchantment) {
		long mask = MASK_INITIALIZED_BIT;
		mask |= triggersMask(enchantment.getTriggers());
		mask |= triggersMask(enchantment.getExpirationTriggers());
		mask |= triggersMask(enchantment.getActivationTriggers());
		return mask;
	}

	private static long triggersMask(List<EventTrigger> triggers) {
		if (triggers == null || triggers.isEmpty()) {
			return 0L;
		}
		long mask = 0L;
		for (int i = 0, n = triggers.size(); i < n; i++) {
			EventTrigger t = triggers.get(i);
			if (t == null) {
				continue;
			}
			GameEventType interested = t.interestedIn();
			if (interested == GameEventType.ALL) {
				return ~0L;
			}
			if (interested != null) {
				mask |= (1L << interested.ordinal());
			}
		}
		return mask;
	}

	public static CompiledEventTrigger getCompiled(EventTrigger trigger) {
		EventTriggerDesc d = trigger.getDesc();
		if (d == null) {
			return compileTrigger(trigger);
		}
		CompiledEventTrigger c = (CompiledEventTrigger) CompilerBridge.getCompiled(d);
		if (c == null) {
			c = compileTrigger(trigger);
			CompilerBridge.setCompiled(d, c);
		}
		return c;
	}

	public static boolean evalTriggerQueues(EventTrigger trigger, GameEvent event, Enchantment enchantment, Entity host, int playerId) {
		EventTriggerDesc d = trigger.getDesc();
		if (DescAuditRecorder.ENABLED) {
			DescAuditRecorder.push(trigger.getClass(), d);
			try {
				return CompilerBridge.evaluateFullQueues(trigger, event, enchantment, host, playerId);
			} finally {
				DescAuditRecorder.pop();
			}
		}
		if (d != null) {
			CompiledEventTrigger c = (CompiledEventTrigger) CompilerBridge.getCompiled(d);
			if (c != null) {
				return c.queues(event, enchantment, host, playerId);
			}
			if (d.isReadOnly()) {
				return getCompiled(trigger).queues(event, enchantment, host, playerId);
			}
		}
		return CompilerBridge.evaluateFullQueues(trigger, event, enchantment, host, playerId);
	}

	public static boolean evalTriggerFires(EventTrigger trigger, GameEvent event, Entity host, int playerId) {
		EventTriggerDesc d = trigger.getDesc();
		if (DescAuditRecorder.ENABLED) {
			DescAuditRecorder.push(trigger.getClass(), d);
			try {
				return CompilerBridge.evaluateFullFires(trigger, event, host, playerId);
			} finally {
				DescAuditRecorder.pop();
			}
		}
		if (d != null) {
			CompiledEventTrigger c = (CompiledEventTrigger) CompilerBridge.getCompiled(d);
			if (c != null) {
				return c.fires(event, host, playerId);
			}
			if (d.isReadOnly()) {
				return getCompiled(trigger).fires(event, host, playerId);
			}
		}
		return CompilerBridge.evaluateFullFires(trigger, event, host, playerId);
	}

	private static boolean checkTriggerTargetPlayer(GameEvent event, TargetPlayer tp, Entity host, int targetPlayerId, int enchantmentOwnerId) {
		if (targetPlayerId == -1 || tp == null) {
			return true;
		}
		return switch (tp) {
			case ACTIVE -> event.getGameContext().getActivePlayerId() == targetPlayerId;
			case INACTIVE -> event.getGameContext().getActivePlayerId() != targetPlayerId;
			case BOTH -> true;
			case OPPONENT -> enchantmentOwnerId != targetPlayerId;
			case OWNER -> host.getOwner() == targetPlayerId;
			case SELF -> enchantmentOwnerId == targetPlayerId;
			case PLAYER_1 -> targetPlayerId == GameContext.PLAYER_1;
			case PLAYER_2 -> targetPlayerId == GameContext.PLAYER_2;
			default -> false;
		};
	}

	private static boolean checkHostTargetType(TargetType hostTargetType, GameEvent event, Entity host) {
		if (hostTargetType == null) {
			return true;
		}
		return switch (hostTargetType) {
			case IGNORE_AS_TARGET -> event.getTarget() != host;
			case IGNORE_AS_SOURCE -> event.getSource() != host;
			case IGNORE_AS_SOURCE_CARD -> event.getSource() != host.getSourceCard();
			case IGNORE_AS_TARGET_CARD -> event.getTarget() != host.getSourceCard();
			case IGNORE_OTHER_TARGETS -> event.getTarget() == host;
			case IGNORE_OTHER_TARGET_CARDS -> event.getTarget() == host.getSourceCard();
			case IGNORE_OTHER_SOURCES -> event.getSource() == host;
			default -> true;
		};
	}

	public static CompiledEventTrigger compileTrigger(EventTrigger trigger) {
		EventTriggerDesc desc = trigger.getDesc();
		if (desc == null) {
			return new CompiledEventTrigger() {
				@Override
				public boolean queues(GameEvent event, Enchantment enchantment, Entity host, int playerId) {
					return CompilerBridge.evaluateFullQueues(trigger, event, enchantment, host, playerId);
				}

				@Override
				public boolean fires(GameEvent event, Entity host, int playerId) {
					return CompilerBridge.evaluateFullFires(trigger, event, host, playerId);
				}
			};
		}

		final TargetPlayer targetPlayer = desc.getTargetPlayer();
		final TargetPlayer sourcePlayer = desc.getSourcePlayer();
		final TargetType hostTargetType = (TargetType) desc.get(EventTriggerArg.HOST_TARGET_TYPE);
		final boolean customHostCondition = trigger instanceof SupremacyTrigger;
		final EntityType sourceEntityType = (EntityType) desc.get(EventTriggerArg.SOURCE_ENTITY_TYPE);
		final EntityType targetEntityType = (EntityType) desc.get(EventTriggerArg.TARGET_ENTITY_TYPE);
		final Condition queueCondition = (Condition) desc.get(EventTriggerArg.QUEUE_CONDITION);
		final CompiledCondition compiledQueueCond = queueCondition != null && desc.isReadOnly() ? getCompiled(queueCondition) : null;
		final Condition fireCondition = (Condition) desc.get(EventTriggerArg.FIRE_CONDITION);
		final CompiledCondition compiledFireCond = fireCondition != null && desc.isReadOnly() ? getCompiled(fireCondition) : null;

		return new CompiledEventTrigger() {
			@Override
			public boolean queues(GameEvent event, Enchantment enchantment, Entity host, int playerId) {
				if (targetPlayer != null && !checkTriggerTargetPlayer(event, targetPlayer, host, event.getTargetPlayerId(), playerId)) {
					return false;
				}
				if (sourcePlayer != null && !checkTriggerTargetPlayer(event, sourcePlayer, host, event.getSourcePlayerId(), playerId)) {
					return false;
				}
				if (customHostCondition) {
					if (!CompilerBridge.evaluateHostConditionMet(trigger, event, host)) {
						return false;
					}
				} else if (hostTargetType != null && !checkHostTargetType(hostTargetType, event, host)) {
					return false;
				}

				Entity source = event.getSource();
				if (sourceEntityType != null) {
					if (source == null || sourceEntityType != source.getEntityType()) {
						return false;
					}
				}

				if (targetEntityType != null) {
					Entity target = event.getTarget();
					if (target == null || !GameLogic.isEntityType(target.getEntityType(), targetEntityType)) {
						return false;
					}
				}

				if (queueCondition != null) {
					int ownerId = enchantment.getOwner();
					if (enchantment.getSourceCard().getDesc().getFileFormatVersion() <= 1) {
						ownerId = playerId;
						if (source == null) {
							source = host;
						}
					}
					GameContext ctx = event.getGameContext();
					Player owner = ctx.getPlayer(ownerId);
					boolean fulfilled = compiledQueueCond != null
							? compiledQueueCond.isFulfilled(ctx, owner, source, event.getTarget())
							: queueCondition.isFulfilled(ctx, owner, source, event.getTarget());
					if (!fulfilled) {
						return false;
					}
				}

				Card hostCard = host.getSourceCard();
				if (hostCard != null
						&& hostCard.getCardType() == CardType.HERO_POWER
						&& event.getGameContext().getLogic().heroPowersDisabled()) {
					return false;
				}
				return CompilerBridge.evaluateInnerQueues(trigger, event, enchantment, host);
			}

			@Override
			public boolean fires(GameEvent event, Entity host, int playerId) {
				if (fireCondition == null) {
					return true;
				}
				GameContext ctx = event.getGameContext();
				Player player = ctx.getPlayer(playerId);
				Entity source = event.getSource();
				if (source == null) {
					source = host;
				}
				return compiledFireCond != null
						? compiledFireCond.isFulfilled(ctx, player, source, event.getTarget())
						: fireCondition.isFulfilled(ctx, player, source, event.getTarget());
			}
		};
	}

	// =========================================================================
	// ValueProvider Compilation & Deep AST Inlining
	// =========================================================================

	public static CompiledValueProvider compile(ValueProvider instance) {
		if (IN_NATIVE_IMAGE) {
			return (context, player, target, host) -> CompilerBridge.evaluateFullValue(instance, context, player, target, host);
		}
		try {
			List<Object> classData = new ArrayList<>(4);
			ClassDesc hiddenDesc = ClassDesc.of(PKG, "HiddenVP");

			byte[] bytes = ClassFile.of().build(hiddenDesc, clb -> {
				clb.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL | ClassFile.ACC_SUPER)
						.withInterfaceSymbols(CD_COMPILED_VP);
				emitDefaultConstructor(clb);

				clb.withMethodBody("compute", MTD_VP_COMPUTE, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
					// Slots: 0=this, 1=context, 2=player, 3=target, 4=host, 5+=temp
					emitInlineVP(cob, instance, classData, 2, 3, 4, 5, 0);
					cob.ireturn();
				});
			});

			MethodHandles.Lookup hiddenLookup = LOOKUP.defineHiddenClassWithClassData(
					bytes, List.copyOf(classData), true, MethodHandles.Lookup.ClassOption.NESTMATE);
			HIDDEN_CLASSES_DEFINED.incrementAndGet();
			return (CompiledValueProvider) hiddenLookup.findConstructor(
					hiddenLookup.lookupClass(), MethodType.methodType(void.class)).invoke();
		} catch (Throwable t) {
			return (context, player, target, host) -> CompilerBridge.evaluateFullValue(instance, context, player, target, host);
		}
	}

	static void emitInlineVP(
			CodeBuilder cob,
			ValueProvider instance,
			List<Object> classData,
			int playerSlot,
			int targetSlot,
			int hostSlot,
			int tempSlotBase,
			int depth) {
		ValueProviderDesc desc = instance.getDesc();
		if (desc == null || depth > MAX_INLINE_DEPTH) {
			emitCallCompiledVP(cob, instance, classData, playerSlot, targetSlot, hostSlot);
			return;
		}

		TargetPlayer targetPlayer = (TargetPlayer) desc.get(ValueProviderArg.TARGET_PLAYER);
		if (targetPlayer == null) {
			targetPlayer = TargetPlayer.SELF;
		}
		final Object multObj = desc.get(ValueProviderArg.MULTIPLIER);
		final Object offsetObj = desc.get(ValueProviderArg.OFFSET);
		final int scratchSlot = tempSlotBase + depth;

		if (targetPlayer == TargetPlayer.BOTH) {
			if (depth > 0 && (multObj instanceof ValueProvider || offsetObj instanceof ValueProvider)) {
				emitCallCompiledVP(cob, instance, classData, playerSlot, targetSlot, hostSlot);
				return;
			}
			int mult = multObj instanceof Number n ? n.intValue() : 1;
			int offset = offsetObj instanceof Number n ? n.intValue() : 0;
			cob.aload(1)
					.invokevirtual(CD_GAME_CONTEXT, "getPlayer1", MethodTypeDesc.of(CD_PLAYER))
					.astore(scratchSlot);
			emitProvideValueBody(cob, instance, desc, classData, scratchSlot, targetSlot, hostSlot, tempSlotBase, depth + 1);
			cob.aload(1)
					.invokevirtual(CD_GAME_CONTEXT, "getPlayer2", MethodTypeDesc.of(CD_PLAYER))
					.astore(scratchSlot);
			emitProvideValueBody(cob, instance, desc, classData, scratchSlot, targetSlot, hostSlot, tempSlotBase, depth + 1);
			cob.iadd();
			if (mult != 1) {
				cob.ldc(mult).imul();
			}
			if (offset != 0) {
				cob.ldc(offset).iadd();
			}
			return;
		}

		int resolvedPlayerSlot = emitResolveTargetPlayerForVP(cob, targetPlayer, playerSlot, hostSlot, scratchSlot);
		emitProvideValueBody(cob, instance, desc, classData, resolvedPlayerSlot, targetSlot, hostSlot, tempSlotBase, depth + 1);

		// Apply multiplier
		if (multObj instanceof Number n) {
			int mult = n.intValue();
			if (mult != 1) {
				cob.ldc(mult).imul();
			}
		} else if (multObj instanceof ValueProvider vp) {
			emitInlineVP(cob, vp, classData, playerSlot, targetSlot, hostSlot, tempSlotBase, depth + 1);
			cob.imul();
		}

		// Apply offset
		if (offsetObj instanceof Number n) {
			int offset = n.intValue();
			if (offset != 0) {
				cob.ldc(offset).iadd();
			}
		} else if (offsetObj instanceof ValueProvider vp) {
			emitInlineVP(cob, vp, classData, playerSlot, targetSlot, hostSlot, tempSlotBase, depth + 1);
			cob.iadd();
		}
	}

	private static void emitCallCompiledVP(
			CodeBuilder cob,
			ValueProvider vp,
			List<Object> classData,
			int playerSlot,
			int targetSlot,
			int hostSlot) {
		CompiledValueProvider compiled = getCompiled(vp);
		int idx = addConstant(classData, compiled);
		loadClassData(cob, idx, CD_COMPILED_VP);
		cob.aload(1)
				.aload(playerSlot);
		loadSlotOrNull(cob, targetSlot);
		loadSlotOrNull(cob, hostSlot);
		cob.invokeinterface(CD_COMPILED_VP, "compute", MTD_VP_COMPUTE);
	}

	private static int emitResolveTargetPlayerForVP(CodeBuilder cob, TargetPlayer tp, int playerSlot, int hostSlot, int destSlot) {
		switch (tp) {
			case ACTIVE -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getActivePlayer", MethodTypeDesc.of(CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			case INACTIVE -> {
				cob.aload(1)
						.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getActivePlayer", MethodTypeDesc.of(CD_PLAYER))
						.invokevirtual(CD_GAME_CONTEXT, "getOpponent", MethodTypeDesc.of(CD_PLAYER, CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			case OPPONENT -> {
				cob.aload(1)
						.aload(playerSlot)
						.invokevirtual(CD_GAME_CONTEXT, "getOpponent", MethodTypeDesc.of(CD_PLAYER, CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			case OWNER -> {
				cob.aload(1);
				loadSlotOrNull(cob, hostSlot);
				cob.invokevirtual(CD_ENTITY, "getOwner", MethodTypeDesc.of(ConstantDescs.CD_int))
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer", MethodTypeDesc.of(CD_PLAYER, ConstantDescs.CD_int))
						.astore(destSlot);
				return destSlot;
			}
			case PLAYER_1 -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer1", MethodTypeDesc.of(CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			case PLAYER_2 -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer2", MethodTypeDesc.of(CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			default -> {
				return playerSlot;
			}
		}
	}

	private static void emitProvideValueBody(
			CodeBuilder cob,
			ValueProvider instance,
			ValueProviderDesc desc,
			List<Object> classData,
			int playerSlot,
			int targetSlot,
			int hostSlot,
			int tempSlotBase,
			int depth) {
		Class<? extends ValueProvider> clazz = desc.getDescClass();
		if (instance instanceof ConstValueProvider cvp) {
			cob.ldc(cvp.getConstantValue());
			return;
		}
		if (clazz == AlgebraicValueProvider.class) {
			Object v1 = desc.get(ValueProviderArg.VALUE1);
			Object v2 = desc.get(ValueProviderArg.VALUE2);
			AlgebraicOperation op = (AlgebraicOperation) desc.get(ValueProviderArg.OPERATION);
			if (op == null) {
				op = AlgebraicOperation.ADD;
			}

			// Evaluate value1 (host is null in AlgebraicValueProvider)
			if (v1 instanceof Number n) {
				cob.ldc(n.intValue());
			} else if (v1 instanceof ValueProvider vp1) {
				emitInlineVP(cob, vp1, classData, playerSlot, targetSlot, 0, tempSlotBase, depth);
			} else {
				cob.ldc(1);
			}

			// Evaluate value2 (host is null in AlgebraicValueProvider)
			if (v2 instanceof Number n) {
				cob.ldc(n.intValue());
			} else if (v2 instanceof ValueProvider vp2) {
				emitInlineVP(cob, vp2, classData, playerSlot, targetSlot, 0, tempSlotBase, depth);
			} else {
				cob.ldc(1);
			}

			switch (op) {
				case ADD -> cob.iadd();
				case SUBTRACT -> cob.isub();
				case MULTIPLY -> cob.imul();
				case MINIMUM -> cob.invokestatic(CD_MATH, "min",
						MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int, ConstantDescs.CD_int));
				case MAXIMUM -> cob.invokestatic(CD_MATH, "max",
						MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int, ConstantDescs.CD_int));
				case NEGATE -> cob.pop().ineg();
				case SET -> cob.swap().pop();
				default -> {
					int opIdx = addConstant(classData, op);
					loadClassData(cob, opIdx, CD_ALG_OP);
					cob.invokestatic(CD_COMPILER, "evalAlgebraicOp",
							MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int, ConstantDescs.CD_int, CD_ALG_OP));
				}
			}
			return;
		}

		if (clazz == EntityCountValueProvider.class) {
			EntityReference targetRef = desc.getTarget();
			EntityFilter filter = (EntityFilter) desc.get(ValueProviderArg.FILTER);
			int refIdx = addConstant(classData, targetRef);
			int filterIdx = addConstant(classData, filter != null ? getCompiled(filter) : null);
			cob.aload(1)
					.aload(playerSlot);
			loadSlotOrNull(cob, targetSlot);
			loadSlotOrNull(cob, hostSlot);
			loadClassData(cob, refIdx, CD_ENTITY_REF);
			loadClassData(cob, filterIdx, CD_COMPILED_EF);
			cob.invokestatic(CD_COMPILER, "evalEntityCountVP",
					MethodTypeDesc.of(ConstantDescs.CD_int,
							CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY,
							CD_ENTITY_REF, CD_COMPILED_EF));
			return;
		}

		if (clazz == DiedThisTurnValueProvider.class) {
			EntityFilter userFilter = (EntityFilter) desc.get(ValueProviderArg.CARD_FILTER);
			int descIdx = addConstant(classData, desc);
			int filterIdx = addConstant(classData, userFilter != null ? getCompiled(userFilter) : null);
			cob.aload(1)
					.aload(playerSlot);
			loadSlotOrNull(cob, targetSlot);
			loadSlotOrNull(cob, hostSlot);
			loadClassData(cob, descIdx, CD_VP_DESC);
			loadClassData(cob, filterIdx, CD_COMPILED_EF);
			cob.invokestatic(CD_COMPILER, "evalDiedThisTurnVP",
					MethodTypeDesc.of(ConstantDescs.CD_int,
							CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY,
							CD_VP_DESC, CD_COMPILED_EF));
			return;
		}

		if (clazz == ConditionalValueProvider.class) {
			Condition condition = (Condition) desc.get(ValueProviderArg.CONDITION);
			Object ifTrue = desc.get(ValueProviderArg.IF_TRUE);
			Object ifFalse = desc.get(ValueProviderArg.IF_FALSE);
			if (condition != null) {
				Label falseLabel = cob.newLabel();
				Label endLabel = cob.newLabel();
				emitInlineCond(cob, condition, classData, playerSlot, hostSlot, targetSlot, tempSlotBase, depth);
				cob.ifeq(falseLabel);
				if (ifTrue instanceof Number n) {
					cob.ldc(n.intValue());
				} else if (ifTrue instanceof ValueProvider vp) {
					emitInlineVP(cob, vp, classData, playerSlot, targetSlot, hostSlot, tempSlotBase, depth);
				} else {
					cob.iconst_0();
				}
				cob.goto_(endLabel)
						.labelBinding(falseLabel);
				if (ifFalse instanceof Number n) {
					cob.ldc(n.intValue());
				} else if (ifFalse instanceof ValueProvider vp) {
					emitInlineVP(cob, vp, classData, playerSlot, targetSlot, hostSlot, tempSlotBase, depth);
				} else {
					cob.iconst_0();
				}
				cob.labelBinding(endLabel);
				return;
			}
		}

		if (clazz == GameValueProvider.class) {
			GameValue gv = (GameValue) desc.get(ValueProviderArg.GAME_VALUE);
			if (gv == GameValue.SPELL_VALUE) {
				cob.aload(1)
						.invokestatic(CD_COMPILER, "evalSpellValue",
								MethodTypeDesc.of(ConstantDescs.CD_int, CD_GAME_CONTEXT));
				return;
			} else if (gv == GameValue.LAST_MANA_COST) {
				cob.aload(1)
						.invokestatic(CD_COMPILER, "evalLastManaCost",
								MethodTypeDesc.of(ConstantDescs.CD_int, CD_GAME_CONTEXT));
				return;
			}
		}

		if (ENABLE_OPTION1_LOWERING && desc.isReadOnly() && clazz != null) {
			if (clazz == AttributeValueProvider.class && desc.get(ValueProviderArg.TARGET) != null) {
				EntityReference targetRef = (EntityReference) desc.get(ValueProviderArg.TARGET);
				Attribute attribute = (Attribute) desc.get(ValueProviderArg.ATTRIBUTE);
				EntityFilter filter = (EntityFilter) desc.get(ValueProviderArg.FILTER);
				if (targetRef == EntityReference.SELF && attribute == Attribute.ATTACK && filter == null) {
					cob.aload(1);
					loadSlotOrNull(cob, hostSlot);
					cob.invokestatic(ClassDesc.of(PKG, "TargetLogicRuntime"), "evalSelfAttributeAttack",
							MethodTypeDesc.of(ConstantDescs.CD_int, CD_GAME_CONTEXT, CD_ENTITY));
					return;
				}
				int refIdx = addConstant(classData, targetRef);
				int attrIdx = addConstant(classData, attribute);
				int filterIdx = addConstant(classData, filter);
				cob.aload(1)
						.aload(playerSlot);
				loadSlotOrNull(cob, hostSlot);
				loadClassData(cob, refIdx, CD_ENTITY_REF);
				loadClassData(cob, attrIdx, CD_ATTRIBUTE);
				loadClassData(cob, filterIdx, CD_EF);
				cob.invokestatic(ClassDesc.of(PKG, "TargetLogicRuntime"), "sumAttributeOnTarget",
						MethodTypeDesc.of(ConstantDescs.CD_int, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY_REF, CD_ATTRIBUTE, CD_EF));
				return;
			}
			if (clazz == ReduceValueProvider.class) {
				EntityReference targetRef = (EntityReference) desc.get(ValueProviderArg.TARGET);
				Attribute attribute = (Attribute) desc.get(ValueProviderArg.ATTRIBUTE);
				AlgebraicOperation op = (AlgebraicOperation) desc.getOrDefault(ValueProviderArg.OPERATION, AlgebraicOperation.MAXIMUM);
				EntityFilter filter = (EntityFilter) desc.get(ValueProviderArg.FILTER);
				Object val1Obj = desc.get(ValueProviderArg.VALUE1);
				Object val2Obj = desc.get(ValueProviderArg.VALUE2);
				boolean hasVal1 = val1Obj != null;
				CompiledValueProvider vp1 = val1Obj instanceof ValueProvider vp ? getCompiled(vp) : null;
				int constVal1 = val1Obj instanceof Number n ? n.intValue() : 0;
				boolean hasVal2 = desc.containsKey(ValueProviderArg.VALUE2);
				CompiledValueProvider vp2 = val2Obj instanceof ValueProvider vp ? getCompiled(vp) : null;
				int constVal2 = val2Obj instanceof Number n ? n.intValue() : 0;

				cob.aload(1)
						.aload(playerSlot);
				loadSlotOrNull(cob, targetSlot);
				loadSlotOrNull(cob, hostSlot);
				if (targetRef == null) {
					cob.aconst_null();
				} else {
					loadClassData(cob, addConstant(classData, targetRef), CD_ENTITY_REF);
				}
				if (attribute == null) {
					cob.aconst_null();
				} else {
					cob.getstatic(CD_ATTRIBUTE, attribute.name(), CD_ATTRIBUTE);
				}
				cob.getstatic(CD_ALG_OP, op.name(), CD_ALG_OP);
				if (filter == null) {
					cob.aconst_null();
				} else {
					loadClassData(cob, addConstant(classData, getCompiled(filter)), CD_COMPILED_EF);
				}
				if (vp1 == null) {
					cob.aconst_null();
				} else {
					loadClassData(cob, addConstant(classData, vp1), CD_COMPILED_VP);
				}
				cob.ldc(constVal1)
						.ldc(hasVal1 ? 1 : 0)
						.ldc(hasVal2 ? 1 : 0)
						.ldc(constVal2);
				if (vp2 == null) {
					cob.aconst_null();
				} else {
					loadClassData(cob, addConstant(classData, vp2), CD_COMPILED_VP);
				}
				cob.invokestatic(ClassDesc.of(PKG, "TargetLogicRuntime"), "evalReduceVP",
						MethodTypeDesc.of(ConstantDescs.CD_int,
								CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY,
								CD_ENTITY_REF, CD_ATTRIBUTE, CD_ALG_OP, CD_COMPILED_EF,
								CD_COMPILED_VP, ConstantDescs.CD_int, ConstantDescs.CD_boolean,
								ConstantDescs.CD_boolean, ConstantDescs.CD_int, CD_COMPILED_VP));
				return;
			}
			Option1MethodSpec spec = getOption1Spec(clazz);
			if (spec != null) {
				emitOption1ValueProvider(cob, spec, instance, desc, classData, playerSlot, targetSlot, hostSlot, tempSlotBase, depth);
				return;
			}
		}

		// Fallback to direct singleton provideValue invocation (woven onto ValueProvider.class)
		int vpIdx = addConstant(classData, instance);
		loadClassData(cob, vpIdx, CD_VP);
		cob.aload(1)
				.aload(playerSlot);
		loadSlotOrNull(cob, targetSlot);
		loadSlotOrNull(cob, hostSlot);
		cob.invokevirtual(CD_VP, "evaluateProvideValue", MTD_VP_COMPUTE);
	}

	public static int evalSpellValue(GameContext context) {
		Deque<Integer> stack = context.getSpellValueStack();
		return stack.isEmpty() ? 0 : stack.peekLast();
	}

	public static int evalLastManaCost(GameContext context) {
		return (int) context.getEnvironment().get(Environment.LAST_MANA_COST);
	}

	public static int evalAlgebraicOp(int v1, int v2, AlgebraicOperation op) {
		return op.performOperation(v1, v2);
	}

	public static int evalAttributeVP(
			GameContext context,
			Player player,
			Entity target,
			Entity host,
			EntityReference sourceReference,
			Attribute attribute,
			CompiledEntityFilter filter) {
		if (sourceReference == null) {
			if (filter != null && !filter.matches(context, player, target, host)) {
				return 0;
			}
			return AttributeValueProvider.provideValueForAttribute(context, attribute, target);
		}
		List<Entity> entities = context.resolveTarget(player, host, sourceReference);
		if (entities == null) {
			return 0;
		}
		int value = 0;
		for (int i = 0, size = entities.size(); i < size; i++) {
			Entity entity = entities.get(i);
			if (filter == null || filter.matches(context, player, entity, host)) {
				value += AttributeValueProvider.provideValueForAttribute(context, attribute, entity);
			}
		}
		return value;
	}

	public static int evalEntityCountVP(
			GameContext context,
			Player player,
			Entity target,
			Entity originalHost,
			EntityReference descTarget,
			CompiledEntityFilter filter) {
		return TargetLogicRuntime.countEntities(context, player, target, originalHost, descTarget, filter);
	}

	public static int evalDiedThisTurnVP(
			GameContext context,
			Player player,
			Entity target,
			Entity host,
			ValueProviderDesc desc,
			CompiledEntityFilter userFilter) {
		int targetTurn = desc.containsKey(ValueProviderArg.VALUE)
				? desc.getValue(ValueProviderArg.VALUE, context, player, target, host, context.getTurn())
				: context.getTurn();
		var graveyard = player.getGraveyard();
		int count = 0;
		for (int i = 0, size = graveyard.size(); i < size; i++) {
			Entity graveEntity = graveyard.get(i);
			if (graveEntity instanceof Actor && !graveEntity.isRemovedPeacefully()
					&& graveEntity.getAttributeValue(Attribute.DIED_ON_TURN) == targetTurn) {
				Card sourceCard = graveEntity.getSourceCard();
				if (sourceCard != null
						&& GameLogic.isCardType(sourceCard.getCardType(), CardType.MINION)
						&& (userFilter == null || userFilter.matches(context, player, sourceCard, host))) {
					count++;
				}
			}
		}
		return count;
	}

	// =========================================================================
	// EntityFilter Compilation & Deep AST Inlining
	// =========================================================================

	public static CompiledEntityFilter compile(EntityFilter instance) {
		EntityFilterDesc desc = instance.getDesc();
		try {
			if (desc == null) {
				return (context, player, entity, host) -> CompilerBridge.evaluateFullMatches(instance, context, player, entity, host);
			}
			TargetPlayer targetPlayer = (TargetPlayer) desc.get(EntityFilterArg.TARGET_PLAYER);
			if (IN_NATIVE_IMAGE || targetPlayer == TargetPlayer.BOTH || targetPlayer == TargetPlayer.EITHER) {
				return (context, player, entity, host) -> CompilerBridge.evaluateFullMatches(instance, context, player, entity, host);
			}

			List<Object> classData = new ArrayList<>(4);
			ClassDesc hiddenDesc = ClassDesc.of(PKG, "HiddenEF");

			byte[] bytes = ClassFile.of().build(hiddenDesc, clb -> {
				clb.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL | ClassFile.ACC_SUPER)
						.withInterfaceSymbols(CD_COMPILED_EF);
				emitDefaultConstructor(clb);

				clb.withMethodBody("matches", MTD_EF_MATCHES, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
					// Slots: 0=this, 1=context, 2=player, 3=entity, 4=host, 5+=temp
					emitInlineEF(cob, instance, classData, 2, 3, 4, 5, 0);
					cob.ireturn();
				});
			});

			MethodHandles.Lookup hiddenLookup = LOOKUP.defineHiddenClassWithClassData(
					bytes, List.copyOf(classData), true, MethodHandles.Lookup.ClassOption.NESTMATE);
			HIDDEN_CLASSES_DEFINED.incrementAndGet();
			return (CompiledEntityFilter) hiddenLookup.findConstructor(
					hiddenLookup.lookupClass(), MethodType.methodType(void.class)).invoke();
		} catch (Throwable t) {
			return (context, player, entity, host) -> CompilerBridge.evaluateFullMatches(instance, context, player, entity, host);
		}
	}

	private static void emitInlineEF(
			CodeBuilder cob,
			EntityFilter instance,
			List<Object> classData,
			int playerSlot,
			int entitySlot,
			int hostSlot,
			int tempSlotBase,
			int depth) {
		EntityFilterDesc desc = instance.getDesc();
		if (desc == null || depth > MAX_INLINE_DEPTH) {
			emitCallCompiledEF(cob, instance, classData, playerSlot, entitySlot, hostSlot);
			return;
		}
		TargetPlayer targetPlayer = (TargetPlayer) desc.get(EntityFilterArg.TARGET_PLAYER);
		if (targetPlayer == null) {
			targetPlayer = TargetPlayer.SELF;
		}
		if (targetPlayer == TargetPlayer.BOTH || targetPlayer == TargetPlayer.EITHER) {
			emitCallCompiledEF(cob, instance, classData, playerSlot, entitySlot, hostSlot);
			return;
		}

		final boolean invert = desc.getBool(EntityFilterArg.INVERT);
		final Condition andCondition = (Condition) desc.get(EntityFilterArg.AND_CONDITION);
		final int scratchSlot = tempSlotBase + depth;
		int resolvedPlayerSlot = emitResolveTargetPlayerForEF(cob, targetPlayer, playerSlot, entitySlot, scratchSlot);

		if (andCondition != null) {
			Label condPassed = cob.newLabel();
			Label condEnd = cob.newLabel();
			emitInlineCond(cob, andCondition, classData, playerSlot, hostSlot, entitySlot, tempSlotBase, depth + 1);
			cob.ifne(condPassed)
					.iconst_0()
					.goto_(condEnd)
					.labelBinding(condPassed);
			emitFilterTestBody(cob, instance, desc, classData, resolvedPlayerSlot, entitySlot, hostSlot, tempSlotBase, depth + 1);
			if (invert) {
				cob.iconst_1().ixor();
			}
			cob.labelBinding(condEnd);
			return;
		}

		emitFilterTestBody(cob, instance, desc, classData, resolvedPlayerSlot, entitySlot, hostSlot, tempSlotBase, depth + 1);
		if (invert) {
			cob.iconst_1().ixor();
		}
	}

	private static void emitCallCompiledEF(
			CodeBuilder cob,
			EntityFilter ef,
			List<Object> classData,
			int playerSlot,
			int entitySlot,
			int hostSlot) {
		int idx = addConstant(classData, getCompiled(ef));
		loadClassData(cob, idx, CD_COMPILED_EF);
		cob.aload(1)
				.aload(playerSlot);
		loadSlotOrNull(cob, entitySlot);
		loadSlotOrNull(cob, hostSlot);
		cob.invokeinterface(CD_COMPILED_EF, "matches", MTD_EF_MATCHES);
	}

	private static int emitResolveTargetPlayerForEF(CodeBuilder cob, TargetPlayer tp, int playerSlot, int entitySlot, int destSlot) {
		switch (tp) {
			case ACTIVE -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getActivePlayer", MethodTypeDesc.of(CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			case INACTIVE -> {
				cob.aload(1)
						.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getActivePlayer", MethodTypeDesc.of(CD_PLAYER))
						.invokevirtual(CD_GAME_CONTEXT, "getOpponent", MethodTypeDesc.of(CD_PLAYER, CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			case OPPONENT -> {
				cob.aload(1)
						.aload(playerSlot)
						.invokevirtual(CD_GAME_CONTEXT, "getOpponent", MethodTypeDesc.of(CD_PLAYER, CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			case OWNER -> {
				cob.aload(1);
				loadSlotOrNull(cob, entitySlot);
				cob.invokevirtual(CD_ENTITY, "getOwner", MethodTypeDesc.of(ConstantDescs.CD_int))
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer", MethodTypeDesc.of(CD_PLAYER, ConstantDescs.CD_int))
						.astore(destSlot);
				return destSlot;
			}
			case PLAYER_1 -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer1", MethodTypeDesc.of(CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			case PLAYER_2 -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer2", MethodTypeDesc.of(CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			default -> {
				return playerSlot;
			}
		}
	}

	private static void emitFilterTestBody(
			CodeBuilder cob,
			EntityFilter instance,
			EntityFilterDesc desc,
			List<Object> classData,
			int playerSlot,
			int entitySlot,
			int hostSlot,
			int tempSlotBase,
			int depth) {
		Class<? extends EntityFilter> clazz = desc.getDescClass();

		if (clazz == AndFilter.class) {
			EntityFilter[] filters = (EntityFilter[]) desc.get(EntityFilterArg.FILTERS);
			if (filters == null || filters.length == 0) {
				cob.iconst_1();
				return;
			}
			Label falseLabel = cob.newLabel();
			Label endLabel = cob.newLabel();
			for (EntityFilter f : filters) {
				emitInlineEF(cob, f, classData, playerSlot, entitySlot, hostSlot, tempSlotBase, depth);
				cob.ifeq(falseLabel);
			}
			cob.iconst_1()
					.goto_(endLabel)
					.labelBinding(falseLabel)
					.iconst_0()
					.labelBinding(endLabel);
			return;
		}

		if (clazz == OrFilter.class) {
			EntityFilter[] filters = (EntityFilter[]) desc.get(EntityFilterArg.FILTERS);
			if (filters == null || filters.length == 0) {
				cob.iconst_0();
				return;
			}
			Label trueLabel = cob.newLabel();
			Label endLabel = cob.newLabel();
			for (EntityFilter f : filters) {
				emitInlineEF(cob, f, classData, playerSlot, entitySlot, hostSlot, tempSlotBase, depth);
				cob.ifne(trueLabel);
			}
			cob.iconst_0()
					.goto_(endLabel)
					.labelBinding(trueLabel)
					.iconst_1()
					.labelBinding(endLabel);
			return;
		}

		if (ENABLE_OPTION1_LOWERING && desc.isReadOnly() && clazz != null) {
			Option1MethodSpec spec = getOption1Spec(clazz);
			if (spec != null) {
				emitOption1EntityFilter(cob, spec, instance, desc, classData, playerSlot, entitySlot, hostSlot, tempSlotBase, depth);
				return;
			}
		}

		int efIdx = addConstant(classData, instance);
		loadClassData(cob, efIdx, CD_EF);
		cob.aload(1)
				.aload(playerSlot);
		loadSlotOrNull(cob, entitySlot);
		loadSlotOrNull(cob, hostSlot);
		cob.invokevirtual(CD_EF, "evaluateTest", MTD_EF_MATCHES);
	}

	public static boolean evalSimpleCardFilter(GameContext context, Entity entity, CardType cardType, String race) {
		if (entity == null) {
			return false;
		}
		Card card = entity.getSourceCard();
		if (cardType != null && !GameLogic.isCardType(card.getCardType(), cardType)) {
			return false;
		}
		if (race != null && !Race.hasRace(context, card, race)) {
			return false;
		}
		return true;
	}

	public static boolean evalCardFilter(GameContext context, Player player, Entity entity, Entity host, EntityFilterDesc desc) {
		if (entity == null) {
			return false;
		}
		Card card = entity.getSourceCard();
		CardType cardType = (CardType) desc.get(EntityFilterArg.CARD_TYPE);
		if (cardType != null && !GameLogic.isCardType(card.getCardType(), cardType)) {
			return false;
		}
		String race = (String) desc.get(EntityFilterArg.RACE);
		if (race != null && !Race.hasRace(context, card, race)) {
			return false;
		}
		String[] heroClasses = (String[]) desc.get(EntityFilterArg.HERO_CLASSES);
		if (heroClasses != null && heroClasses.length > 0) {
			boolean test = false;
			for (String heroClass : heroClasses) {
				test |= !HeroClass.hasHeroClass(context, player, card, heroClass);
			}
			if (!test) {
				return false;
			}
		}
		String heroClass = (String) desc.get(EntityFilterArg.HERO_CLASS);
		if (heroClass != null && HeroClass.hasHeroClass(context, player, card, heroClass)) {
			return false;
		}
		if (desc.containsKey(EntityFilterArg.MANA_COST)) {
			int manaCost = desc.getValue(EntityFilterArg.MANA_COST, context, player, null, host, 0);
			if (manaCost != card.getBaseManaCost()) {
				return false;
			}
		}
		Rarity rarity = (Rarity) desc.get(EntityFilterArg.RARITY);
		if (rarity != null && !GameLogic.isRarity(card.getRarity(), rarity)) {
			return false;
		}
		String cardSet = (String) desc.get(EntityFilterArg.CARD_SET);
		if (cardSet != null && !cardSet.equals("ANY") && !card.getCardSet().equals(cardSet)) {
			return false;
		}
		String cardId = (String) desc.get(EntityFilterArg.CARD);
		if (cardId != null && !card.getCardId().contains(cardId)) {
			return false;
		}
		Attribute attribute = (Attribute) desc.get(EntityFilterArg.ATTRIBUTE);
		if (attribute != null) {
			ComparisonOperation operation = (ComparisonOperation) desc.get(EntityFilterArg.OPERATION);
			if (operation == null && desc.containsKey(EntityFilterArg.VALUE)) {
				operation = ComparisonOperation.EQUAL;
			}
			if (operation == ComparisonOperation.HAS || operation == null) {
				return cardFilterHasAttribute(card, attribute);
			}
			EntityReference targetRef = (EntityReference) desc.get(EntityFilterArg.TARGET);
			List<Entity> entities = targetRef != null ? context.resolveTarget(player, host, targetRef) : null;
			Entity targetEntity = entities == null ? null : entities.get(0);
			int targetValue = desc.getValue(EntityFilterArg.VALUE, context, player, targetEntity, null, 0);
			int actualValue = card.getAttributeValue(attribute);
			return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
		}
		return true;
	}

	private static boolean cardFilterHasAttribute(Card card, Attribute attribute) {
		if (!card.getAttributes().isEmpty()) {
			return card.hasAttribute(attribute);
		}
		CardDesc desc = card.getDesc();
		return switch (attribute) {
			case PASSIVE_TRIGGERS -> desc.getPassiveTrigger() != null || (desc.getPassiveTriggers() != null && desc.getPassiveTriggers().length > 0);
			case DECK_TRIGGERS -> desc.getDeckTrigger() != null || (desc.getDeckTriggers() != null && desc.getDeckTriggers().length > 0);
			case GAME_TRIGGERS -> desc.getGameTriggers() != null && desc.getGameTriggers().length > 0;
			case MANA_COST_MODIFIER -> desc.getManaCostModifier() != null;
			default -> card.hasAttribute(attribute);
		};
	}

	public static boolean evalSpecificCardFilter(
			GameContext context,
			Player player,
			Entity entity,
			Entity host,
			Set<String> staticCards,
			EntityReference comparedTo) {
		if (entity == null) {
			return false;
		}
		String cardId = entity.getSourceCard().getCardId();
		Set<String> requiredCardIds = staticCards;
		if (comparedTo != null && !comparedTo.equals(EntityReference.NONE)) {
			Entity target = context.resolveSingleTarget(player, host, comparedTo);
			if (target != null) {
				requiredCardIds = Collections.singleton(target.getSourceCard().getCardId());
			}
		}
		List<SpecificCardFilterOverrideAura> filterAuras = SpellUtils.getAuras(context, player.getId(), SpecificCardFilterOverrideAura.class);
		if (!filterAuras.isEmpty()) {
			for (int i = 0, n = filterAuras.size(); i < n; i++) {
				Aura aura = filterAuras.get(i);
				String overrideCardId = aura.getDesc().getString(AuraArg.CARD);
				if (aura.getAffectedEntities().contains(entity.getId()) && overrideCardId != null && requiredCardIds.contains(overrideCardId)) {
					return true;
				}
			}
		}
		return requiredCardIds.contains(cardId);
	}

	public static boolean evalAttributeFilter(
			GameContext context,
			Player player,
			Entity entity,
			Entity host,
			EntityReference targetRef,
			Attribute attribute,
			ComparisonOperation operation,
			int constVal,
			CompiledValueProvider compiledVp) {
		List<Entity> entities = targetRef != null ? context.resolveTarget(player, host, targetRef) : null;
		if (operation == ComparisonOperation.HAS) {
			return entity.hasAttribute(attribute);
		}
		Entity valTarget = entities == null ? null : entities.get(0);
		int targetValue = compiledVp != null ? compiledVp.compute(context, player, valTarget, host) : constVal;
		int actualValue = -1;
		if (attribute == Attribute.ATTACK) {
			if (entity instanceof Card actorCard) {
				actualValue = actorCard.getBaseAttack() + actorCard.getBonusAttack();
			} else if (entity instanceof Actor actor) {
				actualValue = actor.getAttack();
			} else {
				actualValue = entity.getAttributeValue(attribute);
			}
		} else if (attribute == Attribute.HP) {
			if (entity instanceof Card actorCard) {
				actualValue = actorCard.getBaseHp() + actorCard.getBonusHp();
			} else if (entity instanceof Actor actor) {
				actualValue = actor.getHp();
			} else {
				actualValue = entity.getAttributeValue(attribute);
			}
		} else if (attribute == Attribute.INDEX) {
			actualValue += entity.getEntityLocation().getIndex();
		} else if (attribute == Attribute.INDEX_FROM_END) {
			actualValue += entity.getEntityLocation().getIndex() - context.getPlayer(entity.getOwner()).getZone(entity.getZone()).size();
		} else {
			actualValue = entity.getAttributeValue(attribute);
		}
		return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
	}

	// =========================================================================
	// Condition Compilation & Deep AST Inlining
	// =========================================================================

	public static CompiledCondition compile(Condition instance) {
		ConditionDesc desc = instance.getDesc();
		try {
			if (IN_NATIVE_IMAGE || !canInlineCondition(instance, desc)) {
				return (context, player, source, target) -> CompilerBridge.evaluateFullIsFulfilled(instance, context, player, source, target);
			}

			List<Object> classData = new ArrayList<>(4);
			ClassDesc hiddenDesc = ClassDesc.of(PKG, "HiddenCond");

			byte[] bytes = ClassFile.of().build(hiddenDesc, clb -> {
				clb.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL | ClassFile.ACC_SUPER)
						.withInterfaceSymbols(CD_COMPILED_COND);
				emitDefaultConstructor(clb);

				clb.withMethodBody("isFulfilled", MTD_COND_FULFILLED, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
					// Slots: 0=this, 1=context, 2=player, 3=source, 4=target, 5+=temp
					emitInlineCond(cob, instance, classData, 2, 3, 4, 5, 0);
					cob.ireturn();
				});
			});

			MethodHandles.Lookup hiddenLookup = LOOKUP.defineHiddenClassWithClassData(
					bytes, List.copyOf(classData), true, MethodHandles.Lookup.ClassOption.NESTMATE);
			HIDDEN_CLASSES_DEFINED.incrementAndGet();
			return (CompiledCondition) hiddenLookup.findConstructor(
					hiddenLookup.lookupClass(), MethodType.methodType(void.class)).invoke();
		} catch (Throwable t) {
			return (context, player, source, target) -> CompilerBridge.evaluateFullIsFulfilled(instance, context, player, source, target);
		}
	}

	private static boolean canInlineCondition(Condition instance, ConditionDesc desc) {
		if (desc == null) {
			return false;
		}
		if (CompilerBridge.isTargetOverridden(instance) && desc.containsKey(ConditionArg.TARGET)) {
			return false;
		}
		if ((instance instanceof CountCondition
				&& !(instance instanceof MinionCountCondition)
				&& !(instance instanceof GraveyardCountCondition))
				|| instance instanceof OwnedByPlayerCondition) {
			return false;
		}
		TargetPlayer targetPlayer = (TargetPlayer) desc.getOrDefault(ConditionArg.TARGET_PLAYER, TargetPlayer.SELF);
		return targetPlayer != TargetPlayer.BOTH && targetPlayer != TargetPlayer.EITHER;
	}

	private static void emitInlineCond(
			CodeBuilder cob,
			Condition instance,
			List<Object> classData,
			int playerSlot,
			int sourceSlot,
			int targetSlot,
			int tempSlotBase,
			int depth) {
		ConditionDesc desc = instance.getDesc();
		if (depth > MAX_INLINE_DEPTH || !canInlineCondition(instance, desc)) {
			emitCallCompiledCond(cob, instance, classData, playerSlot, sourceSlot, targetSlot);
			return;
		}

		final boolean invert = desc.getBool(ConditionArg.INVERT);
		final TargetPlayer tp = (TargetPlayer) desc.getOrDefault(ConditionArg.TARGET_PLAYER, TargetPlayer.SELF);
		final EntityFilter filter = (CompilerBridge.isTargetOverridden(instance) && CompilerBridge.isFilterUsed(instance))
				? (EntityFilter) desc.get(ConditionArg.FILTER)
				: null;
		final boolean emptyResult = (CompilerBridge.isMultiTargetAnd(instance) && !CompilerBridge.isAtLeastOneTargetRequired(instance)) != invert;
		final int scratchSlot = tempSlotBase + depth;

		if (filter != null) {
			Label afterFilter = cob.newLabel();
			Label endFilter = cob.newLabel();
			loadSlotOrNull(cob, targetSlot);
			cob.ifnull(afterFilter);
			emitInlineEF(cob, filter, classData, playerSlot, targetSlot, sourceSlot, tempSlotBase, depth + 1);
			cob.ifne(afterFilter);
			if (emptyResult) {
				cob.iconst_1();
			} else {
				cob.iconst_0();
			}
			cob.goto_(endFilter)
					.labelBinding(afterFilter);
			int resolvedPlayerSlot = emitResolveTargetPlayerForCond(cob, tp, playerSlot, targetSlot, scratchSlot);
			emitConditionBody(cob, instance, desc, classData, playerSlot, resolvedPlayerSlot, sourceSlot, targetSlot, tempSlotBase, depth + 1);
			if (invert) {
				cob.iconst_1().ixor();
			}
			cob.labelBinding(endFilter);
			return;
		}

		int resolvedPlayerSlot = emitResolveTargetPlayerForCond(cob, tp, playerSlot, targetSlot, scratchSlot);
		emitConditionBody(cob, instance, desc, classData, playerSlot, resolvedPlayerSlot, sourceSlot, targetSlot, tempSlotBase, depth + 1);
		if (invert) {
			cob.iconst_1().ixor();
		}
	}

	private static void emitCallCompiledCond(
			CodeBuilder cob,
			Condition cond,
			List<Object> classData,
			int playerSlot,
			int sourceSlot,
			int targetSlot) {
		int idx = addConstant(classData, getCompiled(cond));
		loadClassData(cob, idx, CD_COMPILED_COND);
		cob.aload(1)
				.aload(playerSlot);
		loadSlotOrNull(cob, sourceSlot);
		loadSlotOrNull(cob, targetSlot);
		cob.invokeinterface(CD_COMPILED_COND, "isFulfilled", MTD_COND_FULFILLED);
	}

	private static int emitResolveTargetPlayerForCond(CodeBuilder cob, TargetPlayer tp, int playerSlot, int targetSlot, int destSlot) {
		switch (tp) {
			case ACTIVE -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getActivePlayer", MethodTypeDesc.of(CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			case INACTIVE -> {
				cob.aload(1)
						.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getNonActivePlayerId", MethodTypeDesc.of(ConstantDescs.CD_int))
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer", MethodTypeDesc.of(CD_PLAYER, ConstantDescs.CD_int))
						.astore(destSlot);
				return destSlot;
			}
			case OPPONENT -> {
				cob.aload(1)
						.aload(playerSlot)
						.invokevirtual(CD_GAME_CONTEXT, "getOpponent", MethodTypeDesc.of(CD_PLAYER, CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			case OWNER -> {
				cob.aload(1);
				loadSlotOrNull(cob, targetSlot);
				cob.invokevirtual(CD_ENTITY, "getOwner", MethodTypeDesc.of(ConstantDescs.CD_int))
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer", MethodTypeDesc.of(CD_PLAYER, ConstantDescs.CD_int))
						.astore(destSlot);
				return destSlot;
			}
			case PLAYER_1 -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer1", MethodTypeDesc.of(CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			case PLAYER_2 -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer2", MethodTypeDesc.of(CD_PLAYER))
						.astore(destSlot);
				return destSlot;
			}
			default -> {
				return playerSlot;
			}
		}
	}

	private static void emitConditionBody(
			CodeBuilder cob,
			Condition instance,
			ConditionDesc desc,
			List<Object> classData,
			int callerPlayerSlot,
			int playerSlot,
			int sourceSlot,
			int targetSlot,
			int tempSlotBase,
			int depth) {
		Class<? extends Condition> clazz = desc.getDescClass();

		if (clazz == IsDeadCondition.class) {
			loadSlotOrNull(cob, targetSlot);
			cob.invokevirtual(CD_ENTITY, "isDestroyed", MethodTypeDesc.of(ConstantDescs.CD_boolean));
			return;
		}

		if (ENABLE_OPTION1_LOWERING && desc.isReadOnly() && (clazz == MinionCountCondition.class || clazz == GraveyardCountCondition.class)) {
			ComparisonOperation op = (ComparisonOperation) desc.getOrDefault(ConditionArg.OPERATION, ComparisonOperation.EQUAL);
			Object valObj = desc.get(ConditionArg.VALUE);
			EntityFilter filter = (EntityFilter) desc.get(ConditionArg.FILTER);
			if (filter == null && clazz == MinionCountCondition.class) {
				filter = (EntityFilter) desc.get(ConditionArg.CARD_FILTER);
			}
			CompiledEntityFilter compiledFilter = filter != null ? getCompiled(filter) : null;
			cob.aload(1)
					.aload(playerSlot);
			loadSlotOrNull(cob, sourceSlot);
			if (compiledFilter == null) {
				cob.aconst_null();
			} else {
				int fIdx = addConstant(classData, compiledFilter);
				loadClassData(cob, fIdx, CD_COMPILED_EF);
			}
			String countMethod = (clazz == MinionCountCondition.class) ? "countMinions" : "countGraveyard";
			cob.invokestatic(ClassDesc.of(PKG, "TargetLogicRuntime"), countMethod,
					MethodTypeDesc.of(ConstantDescs.CD_int, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_COMPILED_EF));
			emitConditionArgValue(cob, valObj, classData, callerPlayerSlot, sourceSlot, targetSlot, tempSlotBase, depth);
			emitIntComparison(cob, op);
			return;
		}

		if (clazz == AndCondition.class) {
			Condition[] conditions = (Condition[]) desc.get(ConditionArg.CONDITIONS);
			if (conditions == null || conditions.length == 0) {
				cob.iconst_1();
				return;
			}
			Label falseLabel = cob.newLabel();
			Label endLabel = cob.newLabel();
			for (Condition c : conditions) {
				emitInlineCond(cob, c, classData, playerSlot, sourceSlot, targetSlot, tempSlotBase, depth);
				cob.ifeq(falseLabel);
			}
			cob.iconst_1()
					.goto_(endLabel)
					.labelBinding(falseLabel)
					.iconst_0()
					.labelBinding(endLabel);
			return;
		}

		if (clazz == OrCondition.class) {
			Condition[] conditions = (Condition[]) desc.get(ConditionArg.CONDITIONS);
			if (conditions == null || conditions.length == 0) {
				cob.iconst_0();
				return;
			}
			Label trueLabel = cob.newLabel();
			Label endLabel = cob.newLabel();
			for (Condition c : conditions) {
				emitInlineCond(cob, c, classData, playerSlot, sourceSlot, targetSlot, tempSlotBase, depth);
				cob.ifne(trueLabel);
			}
			cob.iconst_0()
					.goto_(endLabel)
					.labelBinding(trueLabel)
					.iconst_1()
					.labelBinding(endLabel);
			return;
		}

		if (ENABLE_OPTION1_LOWERING && desc.isReadOnly() && clazz != null) {
			Option1MethodSpec spec = getOption1Spec(clazz);
			if (spec != null) {
				emitOption1Condition(cob, spec, instance, desc, classData, playerSlot, sourceSlot, targetSlot, tempSlotBase, depth);
				return;
			}
		}

		int condIdx = addConstant(classData, instance);
		int descIdx = addConstant(classData, desc);
		loadClassData(cob, condIdx, CD_COND);
		cob.aload(1)
				.aload(playerSlot);
		loadClassData(cob, descIdx, CD_COND_DESC);
		loadSlotOrNull(cob, sourceSlot);
		loadSlotOrNull(cob, targetSlot);
		cob.invokevirtual(CD_COND, "evaluateIsFulfilled", MTD_COND_EVAL);
	}

	private static void emitIntComparison(CodeBuilder cob, ComparisonOperation op) {
		Label trueLabel = cob.newLabel();
		Label endLabel = cob.newLabel();
		switch (op) {
			case EQUAL -> cob.if_icmpeq(trueLabel);
			case GREATER -> cob.if_icmpgt(trueLabel);
			case GREATER_OR_EQUAL -> cob.if_icmpge(trueLabel);
			case LESS -> cob.if_icmplt(trueLabel);
			case LESS_OR_EQUAL -> cob.if_icmple(trueLabel);
			default -> {
				cob.pop2().iconst_0();
				return;
			}
		}
		cob.iconst_0()
				.goto_(endLabel)
				.labelBinding(trueLabel)
				.iconst_1()
				.labelBinding(endLabel);
	}

	private static void emitConditionArgValue(
			CodeBuilder cob,
			Object valObj,
			List<Object> classData,
			int playerSlot,
			int sourceSlot,
			int targetSlot,
			int tempSlotBase,
			int depth) {
		if (valObj instanceof Number n) {
			cob.ldc(n.intValue());
		} else if (valObj instanceof ValueProvider vp) {
			// In Condition: desc.getValue(arg, context, player, target=targetSlot, host=source=sourceSlot, 0)
			emitInlineVP(cob, vp, classData, playerSlot, targetSlot, sourceSlot, tempSlotBase, depth);
		} else {
			cob.iconst_0();
		}
	}

	// =========================================================================
	// SpellDesc Compilation & Deep AST Inlining
	// =========================================================================

	private static boolean overridesCast(Class<? extends Spell> spellClass) {
		if (spellClass == Spell.class) {
			return false;
		}
		return OVERRIDES_CAST_CACHE.computeIfAbsent(spellClass, cls -> {
			try {
				return cls.getMethod("cast", GameContext.class, Player.class, SpellDesc.class, Entity.class, List.class)
						.getDeclaringClass() != Spell.class;
			} catch (NoSuchMethodException e) {
				return true;
			}
		});
	}

	public static CompiledSpell compileSpell(SpellDesc desc) {
		try {
			if (desc instanceof LambdaSpellDesc) {
				Spell instance = desc.create();
				return instance::cast;
			}
			Class<? extends Spell> spellClass = desc.getDescClass();
			if (spellClass == null) {
				Spell instance = desc.create();
				return instance::cast;
			}
			boolean randomTarget = desc.getBool(SpellArg.RANDOM_TARGET);
			if (spellClass == NullSpell.class && !randomTarget) {
				return NOOP_SPELL;
			}
			Spell spellInstance = desc.create();
			if (IN_NATIVE_IMAGE || (spellClass != DamageSpell.class && overridesCast(spellClass))) {
				return spellInstance::cast;
			}

			List<Object> classData = new ArrayList<>(4);
			ClassDesc hiddenDesc = ClassDesc.of(PKG, "HiddenSpell");

			final EntityFilter targetFilter = desc.getEntityFilter();
			TargetPlayer tp = desc.getTargetPlayer();
			if (tp == null) {
				tp = TargetPlayer.SELF;
			}
			final TargetPlayer targetPlayer = tp;

			byte[] bytes = ClassFile.of().build(hiddenDesc, clb -> {
				clb.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL | ClassFile.ACC_SUPER)
						.withInterfaceSymbols(CD_COMPILED_SPELL);
				emitDefaultConstructor(clb);

				clb.withMethodBody("cast", MTD_SPELL_CAST, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
					// Slots:
					// 0 = this
					// 1 = context (GameContext)
					// 2 = player (Player)
					// 3 = desc (SpellDesc)
					// 4 = source (Entity)
					// 5 = targets (List<Entity>)
					// 6 = target (Entity)
					// 7 = validTargets (List<Entity>)
					// 8 = validSize (int)
					// 9 = i (int)
					// 10 = targetStack (Deque<EntityReference>)
					// 11 = castingPlayer (Player)
					// 12+ = scratch slots for onCast & inlined expressions

					if (spellClass == DamageSpell.class) {
						cob.aload(1)
								.aload(2)
								.aload(4)
								.aload(5)
								.invokestatic(CD_COMPILER, "fireMagicalMissile",
										MethodTypeDesc.of(ConstantDescs.MTD_void.returnType(),
												CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_LIST));
					}

					Label nonNullTargets = cob.newLabel();
					cob.aload(5)
							.ifnonnull(nonNullTargets);
					// targets == null -> castForPlayer with target = null (targetSlot = 0)
					emitCastForPlayerAndOnCast(cob, spellClass, spellInstance, desc, targetPlayer, classData, 0);
					cob.return_();

					cob.labelBinding(nonNullTargets);

					// Resolve validTargets into slot 7
					if (targetFilter == null) {
						cob.aload(5)
								.astore(7);
					} else {
						int filterIdx = addConstant(classData, getCompiled(targetFilter));
						cob.aload(1)
								.aload(2)
								.aload(5);
						loadClassData(cob, filterIdx, CD_COMPILED_EF);
						cob.aload(4)
								.invokestatic(CD_COMPILER, "filterTargets",
										MethodTypeDesc.of(CD_LIST, CD_GAME_CONTEXT, CD_PLAYER, CD_LIST, CD_COMPILED_EF, CD_ENTITY))
								.astore(7);
					}

					cob.aload(7)
							.invokeinterface(CD_LIST, "size", MethodTypeDesc.of(ConstantDescs.CD_int))
							.istore(8);

					if (randomTarget) {
						Label doneRandom = cob.newLabel();
						cob.iload(8)
								.ifle(doneRandom)
								.aload(1)
								.invokevirtual(CD_GAME_CONTEXT, "getLogic", MethodTypeDesc.of(CD_GAME_LOGIC))
								.aload(7)
								.invokevirtual(CD_GAME_LOGIC, "getRandom", MethodTypeDesc.of(ConstantDescs.CD_Object, CD_LIST))
								.checkcast(CD_ENTITY)
								.astore(6);
						emitCastForPlayerAndOnCast(cob, spellClass, spellInstance, desc, targetPlayer, classData, 6);
						cob.labelBinding(doneRandom)
								.return_();
						return;
					}

					// Loop over validTargets (slot 7), size in slot 8, index in slot 9
					Label loopEnd = cob.newLabel();
					Label loopStart = cob.newLabel();
					cob.iload(8)
							.ifle(loopEnd)
							.aload(1)
							.invokevirtual(CD_GAME_CONTEXT, "getSpellTargetStack", MethodTypeDesc.of(CD_DEQUE))
							.astore(10)
							.iconst_0()
							.istore(9)
							.labelBinding(loopStart)
							.iload(9)
							.iload(8)
							.if_icmpge(loopEnd)
							.aload(7)
							.iload(9)
							.invokeinterface(CD_LIST, "get", MethodTypeDesc.of(ConstantDescs.CD_Object, ConstantDescs.CD_int))
							.checkcast(CD_ENTITY)
							.astore(6)
							.aload(10)
							.aload(6)
							.invokestatic(CD_ENTITY_REF, "pointTo", MethodTypeDesc.of(CD_ENTITY_REF, CD_ENTITY))
							.invokeinterface(CD_DEQUE, "push", MethodTypeDesc.of(ConstantDescs.MTD_void.returnType(), ConstantDescs.CD_Object));

					emitCastForPlayerAndOnCast(cob, spellClass, spellInstance, desc, targetPlayer, classData, 6);

					cob.aload(10)
							.invokeinterface(CD_DEQUE, "pop", MethodTypeDesc.of(ConstantDescs.CD_Object))
							.pop()
							.iinc(9, 1)
							.goto_(loopStart)
							.labelBinding(loopEnd)
							.return_();
				});
			});

			MethodHandles.Lookup hiddenLookup = LOOKUP.defineHiddenClassWithClassData(
					bytes, List.copyOf(classData), true, MethodHandles.Lookup.ClassOption.NESTMATE);
			HIDDEN_CLASSES_DEFINED.incrementAndGet();
			return (CompiledSpell) hiddenLookup.findConstructor(
					hiddenLookup.lookupClass(), MethodType.methodType(void.class)).invoke();
		} catch (Throwable t) {
			Spell fallback = desc.create();
			return fallback::cast;
		}
	}

	private static void emitCastForPlayerAndOnCast(
			CodeBuilder cob,
			Class<? extends Spell> spellClass,
			Spell spellInstance,
			SpellDesc desc,
			TargetPlayer targetPlayer,
			List<Object> classData,
			int targetSlot) {
		switch (targetPlayer) {
			case SELF -> emitSpellOnCast(cob, spellClass, spellInstance, desc, classData, 2, targetSlot);
			case OPPONENT -> {
				cob.aload(1)
						.aload(2)
						.invokevirtual(CD_GAME_CONTEXT, "getOpponent", MethodTypeDesc.of(CD_PLAYER, CD_PLAYER))
						.astore(11);
				emitSpellOnCast(cob, spellClass, spellInstance, desc, classData, 11, targetSlot);
			}
			case BOTH -> {
				emitSpellOnCast(cob, spellClass, spellInstance, desc, classData, 2, targetSlot);
				cob.aload(1)
						.aload(2)
						.invokevirtual(CD_GAME_CONTEXT, "getOpponent", MethodTypeDesc.of(CD_PLAYER, CD_PLAYER))
						.astore(11);
				emitSpellOnCast(cob, spellClass, spellInstance, desc, classData, 11, targetSlot);
			}
			case OWNER -> {
				cob.aload(1)
						.aload(targetSlot)
						.invokevirtual(CD_ENTITY, "getOwner", MethodTypeDesc.of(ConstantDescs.CD_int))
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer", MethodTypeDesc.of(CD_PLAYER, ConstantDescs.CD_int))
						.astore(11);
				emitSpellOnCast(cob, spellClass, spellInstance, desc, classData, 11, targetSlot);
			}
			case ACTIVE -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getActivePlayer", MethodTypeDesc.of(CD_PLAYER))
						.astore(11);
				emitSpellOnCast(cob, spellClass, spellInstance, desc, classData, 11, targetSlot);
			}
			case INACTIVE -> {
				cob.aload(1)
						.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getActivePlayer", MethodTypeDesc.of(CD_PLAYER))
						.invokevirtual(CD_GAME_CONTEXT, "getOpponent", MethodTypeDesc.of(CD_PLAYER, CD_PLAYER))
						.astore(11);
				emitSpellOnCast(cob, spellClass, spellInstance, desc, classData, 11, targetSlot);
			}
			case PLAYER_1 -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer1", MethodTypeDesc.of(CD_PLAYER))
						.astore(11);
				emitSpellOnCast(cob, spellClass, spellInstance, desc, classData, 11, targetSlot);
			}
			case PLAYER_2 -> {
				cob.aload(1)
						.invokevirtual(CD_GAME_CONTEXT, "getPlayer2", MethodTypeDesc.of(CD_PLAYER))
						.astore(11);
				emitSpellOnCast(cob, spellClass, spellInstance, desc, classData, 11, targetSlot);
			}
		}
	}

	private static void emitSpellArgValue(
			CodeBuilder cob,
			SpellDesc desc,
			SpellArg arg,
			int defaultValue,
			List<Object> classData,
			int playerSlot,
			int targetSlot,
			int sourceSlot) {
		Object val = desc.get(arg);
		if (val == null) {
			cob.ldc(defaultValue);
		} else if (val instanceof Number n) {
			cob.ldc(n.intValue());
		} else if (val instanceof ValueProvider vp) {
			emitInlineVP(cob, vp, classData, playerSlot, targetSlot, sourceSlot, 14, 0);
		} else {
			cob.ldc(defaultValue);
		}
	}

	private static void emitSpellOnCast(
			CodeBuilder cob,
			Class<? extends Spell> spellClass,
			Spell spellInstance,
			SpellDesc desc,
			List<Object> classData,
			int playerSlot,
			int targetSlot) {
		if (spellClass == MetaSpell.class) {
			boolean hasValue = desc.containsKey(SpellArg.VALUE);
			if (hasValue) {
				cob.aload(1);
				emitSpellArgValue(cob, desc, SpellArg.VALUE, 0, classData, playerSlot, targetSlot, 4);
				cob.invokestatic(CD_COMPILER, "pushSpellValue",
						MethodTypeDesc.of(ConstantDescs.MTD_void.returnType(), CD_GAME_CONTEXT, ConstantDescs.CD_int));
			}

			List<SpellDesc> childSpells = new ArrayList<>(4);
			SpellDesc s0 = (SpellDesc) desc.get(SpellArg.SPELL);
			if (s0 != null) {
				childSpells.add(s0);
			}
			SpellDesc[] sArr = (SpellDesc[]) desc.get(SpellArg.SPELLS);
			if (sArr != null) {
				for (SpellDesc s : sArr) {
					if (s != null) {
						childSpells.add(s);
					}
				}
			}
			SpellDesc s1 = (SpellDesc) desc.get(SpellArg.SPELL1);
			if (s1 != null) {
				childSpells.add(s1);
			}
			SpellDesc s2 = (SpellDesc) desc.get(SpellArg.SPELL2);
			if (s2 != null) {
				childSpells.add(s2);
			}
			if (desc.containsKey(SpellArg.CARD)) {
				Object cardArg = desc.get(SpellArg.CARD);
				for (int i = 0; i < childSpells.size(); i++) {
					childSpells.set(i, childSpells.get(i).addArg(SpellArg.CARD, cardArg));
				}
			}

			Object howManyObj = desc.get(SpellArg.HOW_MANY);
			if (howManyObj == null || (howManyObj instanceof Number n && n.intValue() == 1)) {
				for (SpellDesc sub : childSpells) {
					emitCastChildSpell(cob, sub, classData, playerSlot, targetSlot);
				}
			} else {
				// Use slots 12=howMany, 13=loopIdx
				emitSpellArgValue(cob, desc, SpellArg.HOW_MANY, 1, classData, playerSlot, targetSlot, 4);
				cob.istore(12)
						.iconst_0()
						.istore(13);
				Label loopStart = cob.newLabel();
				Label loopEnd = cob.newLabel();
				cob.labelBinding(loopStart)
						.iload(13)
						.iload(12)
						.if_icmpge(loopEnd);
				for (SpellDesc sub : childSpells) {
					emitCastChildSpell(cob, sub, classData, playerSlot, targetSlot);
				}
				cob.iinc(13, 1)
						.goto_(loopStart)
						.labelBinding(loopEnd);
			}

			if (hasValue) {
				cob.aload(1)
						.invokestatic(CD_COMPILER, "popSpellValue",
								MethodTypeDesc.of(ConstantDescs.MTD_void.returnType(), CD_GAME_CONTEXT));
			}
			return;
		}

		if (spellClass == DamageSpell.class) {
			boolean ignoreSpellDamage = desc.getBool(SpellArg.IGNORE_SPELL_DAMAGE);
			if (desc.containsKey(SpellArg.VALUE)) {
				cob.aload(1)
						.aload(playerSlot)
						.aload(4)
						.aload(targetSlot);
				emitSpellArgValue(cob, desc, SpellArg.VALUE, 0, classData, playerSlot, targetSlot, 4);
				cob.ldc(ignoreSpellDamage ? 1 : 0)
						.invokestatic(CD_COMPILER, "execDamageSpellWithVal",
								MethodTypeDesc.of(ConstantDescs.MTD_void.returnType(),
										CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY,
										ConstantDescs.CD_int, ConstantDescs.CD_boolean));
			} else {
				cob.aload(1)
						.aload(playerSlot)
						.aload(4)
						.aload(targetSlot)
						.ldc(ignoreSpellDamage ? 1 : 0)
						.invokestatic(CD_COMPILER, "execDamageSpellFromStack",
								MethodTypeDesc.of(ConstantDescs.MTD_void.returnType(),
										CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY,
										ConstantDescs.CD_boolean));
			}
			return;
		}

		if (spellClass == ConditionalSpell.class) {
			Condition condition = (Condition) desc.get(SpellArg.CONDITION);
			if (condition != null) {
				SpellDesc subSpell = (SpellDesc) desc.get(SpellArg.SPELL);
				Label skipLabel = cob.newLabel();
				emitInlineCond(cob, condition, classData, playerSlot, 4, targetSlot, 14, 0);
				cob.ifeq(skipLabel);
				emitCastChildSpell(cob, subSpell, classData, playerSlot, targetSlot);
				cob.labelBinding(skipLabel);
				return;
			}
			Condition[] conditions = (Condition[]) desc.get(SpellArg.CONDITIONS);
			SpellDesc[] spells = (SpellDesc[]) desc.get(SpellArg.SPELLS);
			if (conditions != null && spells != null) {
				boolean exclusiveFallback = desc.getBool(SpellArg.EXCLUSIVE) && desc.containsKey(SpellArg.SPELL);
				if (exclusiveFallback) {
					cob.iconst_0().istore(12);
				}
				for (int i = 0; i < conditions.length; i++) {
					Label nextCond = cob.newLabel();
					emitInlineCond(cob, conditions[i], classData, playerSlot, 4, targetSlot, 14, 0);
					cob.ifeq(nextCond);
					if (exclusiveFallback) {
						cob.iconst_1().istore(12);
					}
					emitCastChildSpell(cob, spells[i], classData, playerSlot, targetSlot);
					cob.labelBinding(nextCond);
				}
				if (exclusiveFallback) {
					Label skipFallback = cob.newLabel();
					cob.iload(12).ifne(skipFallback);
					emitCastChildSpell(cob, (SpellDesc) desc.get(SpellArg.SPELL), classData, playerSlot, targetSlot);
					cob.labelBinding(skipFallback);
				}
				return;
			}
		}

		if (spellClass == ConditionalEffectSpell.class) {
			Condition condition = (Condition) desc.get(SpellArg.CONDITION);
			SpellDesc primarySpell = (SpellDesc) desc.get(SpellArg.SPELL1);
			SpellDesc secondarySpell = (SpellDesc) desc.get(SpellArg.SPELL2);
			boolean exclusive = desc.getBool(SpellArg.EXCLUSIVE);
			if (condition != null) {
				if (exclusive) {
					Label falseLabel = cob.newLabel();
					Label endLabel = cob.newLabel();
					emitInlineCond(cob, condition, classData, playerSlot, 4, targetSlot, 14, 0);
					cob.ifeq(falseLabel);
					emitCastChildSpell(cob, secondarySpell, classData, playerSlot, targetSlot);
					cob.goto_(endLabel)
							.labelBinding(falseLabel);
					emitCastChildSpell(cob, primarySpell, classData, playerSlot, targetSlot);
					cob.labelBinding(endLabel);
				} else {
					Label skipSecondary = cob.newLabel();
					emitCastChildSpell(cob, primarySpell, classData, playerSlot, targetSlot);
					emitInlineCond(cob, condition, classData, playerSlot, 4, targetSlot, 14, 0);
					cob.ifeq(skipSecondary);
					emitCastChildSpell(cob, secondarySpell, classData, playerSlot, targetSlot);
					cob.labelBinding(skipSecondary);
				}
				return;
			}
		}

		if (spellClass == EitherOrSpell.class) {
			Condition condition = (Condition) desc.get(SpellArg.CONDITION);
			SpellDesc either = (SpellDesc) desc.get(SpellArg.SPELL1);
			SpellDesc or = (SpellDesc) desc.get(SpellArg.SPELL2);
			if (condition != null && either != null && or != null) {
				if (desc.containsKey(SpellArg.CARD)) {
					Object cardArg = desc.get(SpellArg.CARD);
					either = either.addArg(SpellArg.CARD, cardArg);
					or = or.addArg(SpellArg.CARD, cardArg);
				}
				Label falseLabel = cob.newLabel();
				Label endLabel = cob.newLabel();
				emitInlineCond(cob, condition, classData, playerSlot, 4, targetSlot, 14, 0);
				cob.ifeq(falseLabel);
				emitCastChildSpell(cob, either, classData, playerSlot, targetSlot);
				cob.goto_(endLabel)
						.labelBinding(falseLabel);
				emitCastChildSpell(cob, or, classData, playerSlot, targetSlot);
				cob.labelBinding(endLabel);
				return;
			}
		}

		boolean isTopLevelSpell = spellInstance != null;
		Spell targetInstance = isTopLevelSpell ? spellInstance : desc.create();

		if (ENABLE_OPTION1_LOWERING && desc.isReadOnly() && spellClass != null) {
			if (spellClass == BuffSpell.class
					&& !desc.containsKey(SpellArg.VALUE)
					&& !desc.containsKey(SpellArg.ARMOR_BONUS)
					&& !desc.containsKey(SpellArg.REVERT_TRIGGER)
					&& !desc.containsKey(SpellArg.SECOND_REVERT_TRIGGER)) {
				if (targetSlot == 0) {
					return;
				}
				boolean hasAttack = desc.containsKey(SpellArg.ATTACK_BONUS);
				boolean hasHp = desc.containsKey(SpellArg.HP_BONUS);
				cob.aload(1)
						.aload(playerSlot);
				emitLoadSpellDesc(cob, desc, isTopLevelSpell, classData);
				cob.aload(4)
						.aload(targetSlot);
				emitSpellArgValue(cob, desc, SpellArg.ATTACK_BONUS, 0, classData, playerSlot, targetSlot, 4);
				emitSpellArgValue(cob, desc, SpellArg.HP_BONUS, 0, classData, playerSlot, targetSlot, 4);
				cob.ldc(hasAttack ? 1 : 0)
						.ldc(hasHp ? 1 : 0)
						.invokestatic(ClassDesc.of(PKG, "TargetLogicRuntime"), "execFastBuffSpell",
								MethodTypeDesc.of(ConstantDescs.MTD_void.returnType(),
										CD_GAME_CONTEXT, CD_PLAYER, CD_SPELL_DESC, CD_ENTITY, CD_ENTITY,
										ConstantDescs.CD_int, ConstantDescs.CD_int,
										ConstantDescs.CD_boolean, ConstantDescs.CD_boolean));
				return;
			}
			Option1MethodSpec spec = getOption1Spec(spellClass);
			if (spec != null) {
				emitOption1Spell(cob, spec, targetInstance, desc, isTopLevelSpell, classData, playerSlot, targetSlot);
				return;
			}
		}

		// Default: invoke targetInstance.evaluateOnCast(context, player, desc, source, target)
		int spellIdx = addConstant(classData, targetInstance);
		loadClassData(cob, spellIdx, CD_SPELL);
		cob.aload(1)
				.aload(playerSlot);
		emitLoadSpellDesc(cob, desc, isTopLevelSpell, classData);
		cob.aload(4)
				.aload(targetSlot)
				.invokevirtual(CD_SPELL, "evaluateOnCast", MTD_SPELL_ONCAST);
	}

	private static boolean canCompileChildSpellInline(SpellDesc subSpell) {
		if (!ENABLE_OPTION1_LOWERING || subSpell == null || !subSpell.isReadOnly() || subSpell instanceof LambdaSpellDesc) {
			return false;
		}
		if (subSpell.hasPredefinedTarget()
				|| subSpell.containsKey(SpellArg.FILTER)
				|| subSpell.getBool(SpellArg.RANDOM_TARGET)
				|| subSpell.containsKey(SpellArg.SOURCE)) {
			return false;
		}
		TargetPlayer tp = subSpell.getTargetPlayer();
		if (tp != null && tp != TargetPlayer.SELF) {
			return false;
		}
		Class<? extends Spell> c = subSpell.getDescClass();
		if (c == null || overridesCast(c)) {
			return false;
		}
		return getOption1Spec(c) != null;
	}

	private static void emitCastChildSpell(
			CodeBuilder cob,
			SpellDesc subSpell,
			List<Object> classData,
			int playerSlot,
			int targetSlot) {
		if (targetSlot > 0 && canCompileChildSpellInline(subSpell)) {
			Label fallbackLabel = cob.newLabel();
			Label doneLabel = cob.newLabel();
			cob.aload(1)
					.aload(4)
					.aload(targetSlot)
					.invokestatic(CD_COMPILER, "beginInlineChildSpell",
							MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_GAME_CONTEXT, CD_ENTITY, CD_ENTITY))
					.ifeq(fallbackLabel);
			emitSpellOnCast(cob, subSpell.getDescClass(), null, subSpell, classData, playerSlot, targetSlot);
			cob.aload(1)
					.invokestatic(CD_COMPILER, "endInlineChildSpell",
							MethodTypeDesc.of(ConstantDescs.MTD_void.returnType(), CD_GAME_CONTEXT))
					.goto_(doneLabel)
					.labelBinding(fallbackLabel);
			int subIdx = addConstant(classData, subSpell);
			cob.aload(1)
					.aload(playerSlot);
			loadClassData(cob, subIdx, CD_SPELL_DESC);
			cob.aload(4)
					.aload(targetSlot)
					.invokestatic(CD_SPELL_UTILS, "castChildSpell",
							MethodTypeDesc.of(ConstantDescs.MTD_void.returnType(),
									CD_GAME_CONTEXT, CD_PLAYER, CD_SPELL_DESC, CD_ENTITY, CD_ENTITY))
					.labelBinding(doneLabel);
			return;
		}
		int subIdx = addConstant(classData, subSpell);
		cob.aload(1)
				.aload(playerSlot);
		loadClassData(cob, subIdx, CD_SPELL_DESC);
		cob.aload(4);
		loadSlotOrNull(cob, targetSlot);
		cob.invokestatic(CD_SPELL_UTILS, "castChildSpell",
				MethodTypeDesc.of(ConstantDescs.MTD_void.returnType(),
						CD_GAME_CONTEXT, CD_PLAYER, CD_SPELL_DESC, CD_ENTITY, CD_ENTITY));
	}

	public static boolean beginInlineChildSpell(GameContext context, Entity source, Entity target) {
		if (Thread.currentThread().isInterrupted() || target == null) {
			return false;
		}
		if (source != null && (source.getZone() == Zones.REMOVED_FROM_PLAY || source.getAttribute(Attribute.TRANSFORM_REFERENCE) != null)) {
			return false;
		}
		if (target.getAttribute(Attribute.TRANSFORM_REFERENCE) != null) {
			return false;
		}
		GameStateArena.TriggerList trigs = GameStateArena.getContextArena(context).triggers;
		if ((trigs.kindMask & GameStateArena.KIND_SPELL_OVERRIDE_OR_INCORRUPT_AURA) != 0) {
			for (int i = 0, n = trigs.size; i < n; i++) {
				var t = trigs.elements[i];
				if ((t instanceof SpellOverrideAura || t instanceof IncorruptibilityAura) && !t.isExpired() && t.isActivated()) {
					return false;
				}
			}
		}
		context.getLogic().incrementProgramCounter(source);
		context.getSpellTargetStack().push(target.getReference());
		return true;
	}

	public static void endInlineChildSpell(GameContext context) {
		context.getSpellTargetStack().pop();
	}

	// =========================================================================
	// Universal Option 1 Typed Signature Reflection & Bytecode Lowering
	// =========================================================================

	enum Option1ParamKind {
		MIXIN_CARDS,
		MIXIN_CARD,
		MIXIN_BOARD_POSITION,
		MIXIN_TARGETED_ENTITIES,
		INT_VALUE,
		BOXED_INT_VALUE,
		BOOLEAN,
		VALUE_PROVIDER,
		STRING,
		ENUM_OR_REF
	}

	record Option1ParamSpec(
			Option1ParamKind kind,
			Enum<?> argKey,
			ClassDesc paramDesc,
			int defaultInt,
			boolean defaultBool,
			boolean hasDefault,
			String defaultStr,
			Object defaultRefValue
	) {
	}

	record Option1MethodSpec(
			ComponentSignatureAnalyzer.ComponentKind kind,
			ClassDesc ownerDesc,
			String methodName,
			MethodTypeDesc methodTypeDesc,
			boolean hasDescInPrefix,
			List<Option1ParamSpec> params
	) {
	}

	private static Option1MethodSpec getOption1Spec(Class<?> clazz) {
		return OPTION1_SPEC_CACHE.computeIfAbsent(clazz, DescBytecodeCompiler::resolveOption1Spec).orElse(null);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Optional<Option1MethodSpec> resolveOption1Spec(Class<?> clazz) {
		try {
			ComponentSignatureAnalyzer.ComponentKind kind;
			String expectedMethodName;
			Class<? extends Enum> argEnumClass;
			if (Spell.class.isAssignableFrom(clazz)) {
				kind = ComponentSignatureAnalyzer.ComponentKind.SPELL;
				expectedMethodName = "onCast";
				argEnumClass = SpellArg.class;
			} else if (EntityFilter.class.isAssignableFrom(clazz)) {
				kind = ComponentSignatureAnalyzer.ComponentKind.ENTITY_FILTER;
				expectedMethodName = "test";
				argEnumClass = EntityFilterArg.class;
			} else if (ValueProvider.class.isAssignableFrom(clazz)) {
				kind = ComponentSignatureAnalyzer.ComponentKind.VALUE_PROVIDER;
				expectedMethodName = "provideValue";
				argEnumClass = ValueProviderArg.class;
			} else if (Condition.class.isAssignableFrom(clazz)) {
				kind = ComponentSignatureAnalyzer.ComponentKind.CONDITION;
				expectedMethodName = "isFulfilled";
				argEnumClass = ConditionArg.class;
			} else {
				return Optional.empty();
			}

			Method typedMethod = null;
			for (Class<?> curr = clazz;
				 curr != null && curr != Object.class
						 && curr != Spell.class
						 && curr != EntityFilter.class
						 && curr != ValueProvider.class
						 && curr != Condition.class;
				 curr = curr.getSuperclass()) {
				boolean declaresBaseMethod = false;
				for (Method m : curr.getDeclaredMethods()) {
					String mName = m.getName();
					if ((!mName.equals(expectedMethodName)
							&& !(kind == ComponentSignatureAnalyzer.ComponentKind.CONDITION && mName.equals("isFulfilled$0")))
							|| m.isSynthetic()
							|| m.isBridge()
							|| Modifier.isStatic(m.getModifiers())) {
						continue;
					}
					declaresBaseMethod = true;
					if (!Modifier.isPublic(m.getModifiers())) {
						continue;
					}
					Class<?>[] pTypes = m.getParameterTypes();
					boolean isTyped = switch (kind) {
						case SPELL -> {
							if (pTypes.length == 5
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == SpellDesc.class
									&& pTypes[3] == Entity.class
									&& pTypes[4] == Entity.class) {
								yield false;
							}
							yield (pTypes.length > 5
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == SpellDesc.class
									&& pTypes[3] == Entity.class
									&& pTypes[4] == Entity.class)
									|| (pTypes.length >= 4
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == Entity.class
									&& pTypes[3] == Entity.class);
						}
						case ENTITY_FILTER -> {
							if (pTypes.length == 4
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == Entity.class
									&& pTypes[3] == Entity.class) {
								yield false;
							}
							yield (pTypes.length > 4
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == Entity.class
									&& pTypes[3] == Entity.class)
									|| (pTypes.length >= 5
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == EntityFilterDesc.class
									&& pTypes[3] == Entity.class
									&& pTypes[4] == Entity.class);
						}
						case VALUE_PROVIDER -> {
							if (pTypes.length == 4
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == Entity.class
									&& pTypes[3] == Entity.class) {
								yield false;
							}
							yield (pTypes.length > 4
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == Entity.class
									&& pTypes[3] == Entity.class)
									|| (pTypes.length >= 5
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == ValueProviderDesc.class
									&& pTypes[3] == Entity.class
									&& pTypes[4] == Entity.class);
						}
						case CONDITION -> {
							if (mName.equals("isFulfilled$0")) {
								yield pTypes.length == 4
										&& pTypes[0] == GameContext.class
										&& pTypes[1] == Player.class
										&& pTypes[2] == Entity.class
										&& pTypes[3] == Entity.class;
							}
							if (pTypes.length == 5
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == ConditionDesc.class
									&& pTypes[3] == Entity.class
									&& pTypes[4] == Entity.class) {
								yield false;
							}
							if (pTypes.length == 4) {
								yield false;
							}
							yield (pTypes.length > 5
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == ConditionDesc.class
									&& pTypes[3] == Entity.class
									&& pTypes[4] == Entity.class)
									|| (pTypes.length > 4
									&& pTypes[0] == GameContext.class
									&& pTypes[1] == Player.class
									&& pTypes[2] == Entity.class
									&& pTypes[3] == Entity.class);
						}
						default -> false;
					};
					if (isTyped) {
						typedMethod = m;
						break;
					}
				}
				if (typedMethod != null || declaresBaseMethod) {
					break;
				}
			}

			if (typedMethod == null) {
				return Optional.empty();
			}

			Parameter[] parameters = typedMethod.getParameters();
			Class<?>[] pTypes = typedMethod.getParameterTypes();
			boolean hasDescInPrefix = pTypes.length >= 5 && Desc.class.isAssignableFrom(pTypes[2]);
			int prefixLen = hasDescInPrefix ? 5 : 4;

			ClassDesc[] paramDescs = new ClassDesc[pTypes.length];
			for (int i = 0; i < pTypes.length; i++) {
				paramDescs[i] = ClassDesc.ofDescriptor(pTypes[i].descriptorString());
			}
			MethodTypeDesc mtd = MethodTypeDesc.of(
					ClassDesc.ofDescriptor(typedMethod.getReturnType().descriptorString()),
					paramDescs);

			List<Option1ParamSpec> paramSpecs = new ArrayList<>(pTypes.length - prefixLen);
			for (int i = prefixLen; i < pTypes.length; i++) {
				Parameter p = parameters[i];
				String pName = p.getName();
				Class<?> pClass = pTypes[i];
				ClassDesc pDesc = paramDescs[i];
				Default defAnn = p.getAnnotation(Default.class);
				boolean hasDef = defAnn != null;
				int defInt = hasDef ? defAnn.value() : 0;
				boolean defBool = hasDef && defAnn.bool();
				String defStr = hasDef ? defAnn.string() : null;

				if (kind == ComponentSignatureAnalyzer.ComponentKind.SPELL && pName.equals("cards") && pClass == CardList.class) {
					paramSpecs.add(new Option1ParamSpec(Option1ParamKind.MIXIN_CARDS, null, pDesc, 0, false, false, null, null));
					continue;
				}
				if (kind == ComponentSignatureAnalyzer.ComponentKind.SPELL && pName.equals("card") && pClass == Card.class) {
					paramSpecs.add(new Option1ParamSpec(Option1ParamKind.MIXIN_CARD, null, pDesc, 0, false, false, null, null));
					continue;
				}
				if (kind == ComponentSignatureAnalyzer.ComponentKind.SPELL && pName.equals("boardPosition") && pClass == int.class) {
					paramSpecs.add(new Option1ParamSpec(Option1ParamKind.MIXIN_BOARD_POSITION, null, pDesc, 0, false, false, null, null));
					continue;
				}
				if (kind == ComponentSignatureAnalyzer.ComponentKind.ENTITY_FILTER
						&& (pName.equals("entities") || pName.equals("targetedEntities"))
						&& pClass == List.class) {
					paramSpecs.add(new Option1ParamSpec(Option1ParamKind.MIXIN_TARGETED_ENTITIES, null, pDesc, 0, false, false, null, null));
					continue;
				}

				String keyName = ComponentSignatureAnalyzer.camelToUpperSnake(pName);
				if (keyName.equals("TARGET_REFERENCE") && pClass == EntityReference.class) {
					keyName = "TARGET";
				} else if (keyName.equals("SOURCE_REFERENCE") && pClass == EntityReference.class) {
					keyName = "SOURCE";
				}
				Enum<?> argKey = Enum.valueOf(argEnumClass, keyName);

				if (pClass == int.class) {
					paramSpecs.add(new Option1ParamSpec(Option1ParamKind.INT_VALUE, argKey, pDesc, defInt, false, hasDef, null, null));
				} else if (pClass == Integer.class) {
					paramSpecs.add(new Option1ParamSpec(Option1ParamKind.BOXED_INT_VALUE, argKey, pDesc, defInt, false, hasDef, null, null));
				} else if (pClass == boolean.class) {
					paramSpecs.add(new Option1ParamSpec(Option1ParamKind.BOOLEAN, argKey, pDesc, 0, defBool, hasDef, null, null));
				} else if (pClass == ValueProvider.class) {
					paramSpecs.add(new Option1ParamSpec(Option1ParamKind.VALUE_PROVIDER, argKey, pDesc, 0, false, hasDef, null, null));
				} else if (pClass == String.class) {
					paramSpecs.add(new Option1ParamSpec(Option1ParamKind.STRING, argKey, pDesc, 0, false, hasDef, defStr, null));
				} else {
					Object defRef = null;
					if (hasDef && defStr != null && !defStr.isEmpty() && pClass.isEnum()) {
						defRef = Enum.valueOf((Class<? extends Enum>) pClass, defStr);
					}
					paramSpecs.add(new Option1ParamSpec(Option1ParamKind.ENUM_OR_REF, argKey, pDesc, 0, false, hasDef, defStr, defRef));
				}
			}

			return Optional.of(new Option1MethodSpec(
					kind,
					ClassDesc.ofDescriptor(clazz.descriptorString()),
					typedMethod.getName(),
					mtd,
					hasDescInPrefix,
					List.copyOf(paramSpecs)
			));
		} catch (Throwable t) {
			return Optional.empty();
		}
	}

	static void emitLoadSpellDesc(CodeBuilder cob, SpellDesc desc, boolean isTopLevelSpell, List<Object> classData) {
		if (isTopLevelSpell) {
			cob.aload(3);
		} else {
			int descIdx = addConstant(classData, desc);
			loadClassData(cob, descIdx, CD_SPELL_DESC);
		}
	}

	private static void emitOption1ValueProvider(
			CodeBuilder cob,
			Option1MethodSpec spec,
			ValueProvider instance,
			ValueProviderDesc desc,
			List<Object> classData,
			int playerSlot,
			int targetSlot,
			int hostSlot,
			int tempSlotBase,
			int depth) {
		if (Option1BytecodeSpecializer.tryEmitSpecialized(
				cob, spec, instance, desc, false, classData,
				playerSlot, targetSlot, hostSlot, targetSlot, hostSlot, tempSlotBase, depth)) {
			return;
		}
		int instIdx = addConstant(classData, instance);
		loadClassData(cob, instIdx, spec.ownerDesc);
		cob.aload(1)
				.aload(playerSlot);
		if (spec.hasDescInPrefix) {
			int descIdx = addConstant(classData, desc);
			loadClassData(cob, descIdx, CD_VP_DESC);
		}
		loadSlotOrNull(cob, targetSlot);
		loadSlotOrNull(cob, hostSlot);
		emitOption1ExtraParams(cob, spec, instance, desc, false, classData, playerSlot, targetSlot, hostSlot, targetSlot, hostSlot, tempSlotBase, depth);
		cob.invokevirtual(spec.ownerDesc, spec.methodName, spec.methodTypeDesc);
	}

	private static void emitOption1EntityFilter(
			CodeBuilder cob,
			Option1MethodSpec spec,
			EntityFilter instance,
			EntityFilterDesc desc,
			List<Object> classData,
			int playerSlot,
			int entitySlot,
			int hostSlot,
			int tempSlotBase,
			int depth) {
		// If EntityFilter has TARGET set AND at least one INT_VALUE/BOXED_INT_VALUE parameter uses a dynamic ValueProvider,
		// resolve getTargetedEntity(context, player, host) into a temp slot before pushing arguments.
		int vpTargetSlot = 0;
		if (desc.containsKey(EntityFilterArg.TARGET)) {
			boolean needsVpTarget = false;
			for (int i = 0, n = spec.params.size(); i < n; i++) {
				Option1ParamSpec p = spec.params.get(i);
				if ((p.kind == Option1ParamKind.INT_VALUE || p.kind == Option1ParamKind.BOXED_INT_VALUE)
						&& desc.get(p.argKey) instanceof ValueProvider) {
					needsVpTarget = true;
					break;
				}
			}
			if (needsVpTarget) {
				vpTargetSlot = tempSlotBase + depth;
				depth++;
				int instIdx = addConstant(classData, instance);
				loadClassData(cob, instIdx, CD_EF);
				cob.aload(1)
						.aload(playerSlot);
				loadSlotOrNull(cob, hostSlot);
				cob.invokevirtual(CD_EF, "getTargetedEntity",
						MethodTypeDesc.of(CD_ENTITY, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY))
						.astore(vpTargetSlot);
			}
		}

		if (Option1BytecodeSpecializer.tryEmitSpecialized(
				cob, spec, instance, desc, false, classData,
				playerSlot, entitySlot, hostSlot, vpTargetSlot, hostSlot, tempSlotBase, depth)) {
			return;
		}

		int instIdx = addConstant(classData, instance);
		loadClassData(cob, instIdx, spec.ownerDesc);
		cob.aload(1)
				.aload(playerSlot);
		if (spec.hasDescInPrefix) {
			int descIdx = addConstant(classData, desc);
			loadClassData(cob, descIdx, CD_EF_DESC);
		}
		loadSlotOrNull(cob, entitySlot);
		loadSlotOrNull(cob, hostSlot);
		emitOption1ExtraParams(cob, spec, instance, desc, false, classData, playerSlot, entitySlot, hostSlot, vpTargetSlot, hostSlot, tempSlotBase, depth);
		cob.invokevirtual(spec.ownerDesc, spec.methodName, spec.methodTypeDesc);
	}

	private static void emitOption1Condition(
			CodeBuilder cob,
			Option1MethodSpec spec,
			Condition instance,
			ConditionDesc desc,
			List<Object> classData,
			int playerSlot,
			int sourceSlot,
			int targetSlot,
			int tempSlotBase,
			int depth) {
		if (Option1BytecodeSpecializer.tryEmitSpecialized(
				cob, spec, instance, desc, false, classData,
				playerSlot, sourceSlot, targetSlot, targetSlot, sourceSlot, tempSlotBase, depth)) {
			return;
		}
		int instIdx = addConstant(classData, instance);
		loadClassData(cob, instIdx, spec.ownerDesc);
		cob.aload(1)
				.aload(playerSlot);
		if (spec.hasDescInPrefix) {
			int descIdx = addConstant(classData, desc);
			loadClassData(cob, descIdx, CD_COND_DESC);
		}
		loadSlotOrNull(cob, sourceSlot);
		loadSlotOrNull(cob, targetSlot);
		// In Condition: ValueProvider evaluation uses target=targetSlot, host=sourceSlot
		emitOption1ExtraParams(cob, spec, instance, desc, false, classData, playerSlot, targetSlot, sourceSlot, targetSlot, sourceSlot, tempSlotBase, depth);
		cob.invokevirtual(spec.ownerDesc, spec.methodName, spec.methodTypeDesc);
	}

	private static void emitOption1Spell(
			CodeBuilder cob,
			Option1MethodSpec spec,
			Spell instance,
			SpellDesc desc,
			boolean isTopLevelSpell,
			List<Object> classData,
			int playerSlot,
			int targetSlot) {
		int instIdx = addConstant(classData, instance);
		if (!Option1BytecodeSpecializer.tryEmitSpecialized(
				cob, spec, instance, desc, isTopLevelSpell, classData,
				playerSlot, 4, targetSlot, targetSlot, 4, 14, 0)) {
			loadClassData(cob, instIdx, spec.ownerDesc);
			cob.aload(1)
					.aload(playerSlot);
			if (spec.hasDescInPrefix) {
				emitLoadSpellDesc(cob, desc, isTopLevelSpell, classData);
			}
			cob.aload(4);
			loadSlotOrNull(cob, targetSlot);
			// In Spell: sourceSlot=4, targetSlot=targetSlot; tempSlotBase=14
			emitOption1ExtraParams(cob, spec, instance, desc, isTopLevelSpell, classData, playerSlot, targetSlot, 4, targetSlot, 4, 14, 0);
			cob.invokevirtual(spec.ownerDesc, spec.methodName, spec.methodTypeDesc);
		}
	}

	private static void emitOption1ExtraParams(
			CodeBuilder cob,
			Option1MethodSpec spec,
			Object instance,
			Desc<?, ?> desc,
			boolean isTopLevelSpell,
			List<Object> classData,
			int playerSlot,
			int targetOrEntitySlot,
			int sourceOrHostSlot,
			int vpTargetSlot,
			int vpHostSlot,
			int tempSlotBase,
			int depth) {
		List<Option1ParamSpec> params = spec.params;
		for (int i = 0, n = params.size(); i < n; i++) {
			Option1ParamSpec param = params.get(i);
			switch (param.kind) {
				case MIXIN_CARDS -> {
					cob.aload(1)
							.aload(playerSlot);
					loadSlotOrNull(cob, targetOrEntitySlot);
					loadSlotOrNull(cob, sourceOrHostSlot);
					emitLoadSpellDesc(cob, (SpellDesc) desc, isTopLevelSpell, classData);
					cob.invokestatic(CD_SPELL_UTILS, "getCards",
							MethodTypeDesc.of(CD_CARD_LIST, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY, CD_SPELL_DESC));
				}
				case MIXIN_CARD -> {
					cob.aload(1);
					emitLoadSpellDesc(cob, (SpellDesc) desc, isTopLevelSpell, classData);
					cob.invokestatic(CD_SPELL_UTILS, "getCard",
							MethodTypeDesc.of(CD_CARD, CD_GAME_CONTEXT, CD_SPELL_DESC));
				}
				case MIXIN_BOARD_POSITION -> {
					cob.aload(1)
							.aload(playerSlot);
					emitLoadSpellDesc(cob, (SpellDesc) desc, isTopLevelSpell, classData);
					loadSlotOrNull(cob, sourceOrHostSlot);
					cob.invokestatic(CD_SPELL_UTILS, "getBoardPosition",
							MethodTypeDesc.of(ConstantDescs.CD_int, CD_GAME_CONTEXT, CD_PLAYER, CD_SPELL_DESC, CD_ENTITY));
				}
				case MIXIN_TARGETED_ENTITIES -> {
					int efIdx = addConstant(classData, instance);
					loadClassData(cob, efIdx, CD_EF);
					cob.aload(1)
							.aload(playerSlot);
					loadSlotOrNull(cob, sourceOrHostSlot);
					cob.invokevirtual(CD_EF, "getTargetedEntities",
							MethodTypeDesc.of(CD_LIST, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY));
				}
				case INT_VALUE -> {
					Object raw = desc.get(param.argKey);
					if (raw == null) {
						cob.ldc(param.defaultInt);
					} else if (raw instanceof Number num) {
						cob.ldc(num.intValue());
					} else if (raw instanceof ValueProvider vp) {
						emitInlineVP(cob, vp, classData, playerSlot, vpTargetSlot, vpHostSlot, tempSlotBase, depth);
					} else {
						cob.ldc(param.defaultInt);
					}
				}
				case BOXED_INT_VALUE -> {
					Object raw = desc.get(param.argKey);
					if (raw == null) {
						cob.aconst_null();
					} else if (raw instanceof Number num) {
						Integer boxed = Integer.valueOf(num.intValue());
						int idx = addConstant(classData, boxed);
						loadClassData(cob, idx, ConstantDescs.CD_Integer);
					} else if (raw instanceof ValueProvider vp) {
						emitInlineVP(cob, vp, classData, playerSlot, vpTargetSlot, vpHostSlot, tempSlotBase, depth);
						cob.invokestatic(ConstantDescs.CD_Integer, "valueOf",
								MethodTypeDesc.of(ConstantDescs.CD_Integer, ConstantDescs.CD_int));
					} else {
						cob.aconst_null();
					}
				}
				case BOOLEAN -> {
					Object raw = desc.get(param.argKey);
					boolean b = raw instanceof Boolean bv ? bv : param.defaultBool;
					if (b) {
						cob.iconst_1();
					} else {
						cob.iconst_0();
					}
				}
				case VALUE_PROVIDER -> {
					Object raw = desc.get(param.argKey);
					ValueProvider vp = ConstValueProvider.fromObject(raw);
					if (vp == null) {
						cob.aconst_null();
					} else {
						int idx = addConstant(classData, vp);
						loadClassData(cob, idx, CD_VP);
					}
				}
				case STRING -> {
					Object raw = desc.get(param.argKey);
					String str = raw instanceof String s ? s : (param.hasDefault ? (param.defaultStr != null ? param.defaultStr : "") : null);
					if (str == null) {
						cob.aconst_null();
					} else {
						cob.ldc(str);
					}
				}
				case ENUM_OR_REF -> {
					Object raw = desc.get(param.argKey);
					if (raw == null) {
						raw = param.defaultRefValue;
					}
					if (raw == null) {
						cob.aconst_null();
					} else {
						int idx = addConstant(classData, raw);
						loadClassData(cob, idx, param.paramDesc);
					}
				}
			}
		}
	}

	// =========================================================================
	// Runtime Helpers for CompiledSpell Bytecode
	// =========================================================================

	public static void fireMagicalMissile(GameContext context, Player player, Entity source, List<Entity> targets) {
		GameLogic.fireMissileEvent(context, player, source, targets, MAGICAL_DAMAGE_TYPE);
	}

	public static List<Entity> filterTargets(
			GameContext context,
			Player player,
			List<Entity> allTargets,
			CompiledEntityFilter filter,
			Entity host) {
		if (filter == null) {
			return allTargets;
		}
		int size = allTargets.size();
		if (size == 0) {
			return allTargets;
		}
		List<Entity> validTargets = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Entity entity = allTargets.get(i);
			if (filter.matches(context, player, entity, host)) {
				validTargets.add(entity);
			}
		}
		return validTargets;
	}

	public static void pushSpellValue(GameContext context, int value) {
		context.getSpellValueStack().addLast(value);
	}

	public static void popSpellValue(GameContext context) {
		context.getSpellValueStack().pollLast();
	}

	public static void execDamageSpellWithVal(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			int damage,
			boolean ignoreSpellDamage) {
		if (!(target instanceof Actor actor)) {
			LOGGER.error("onCast {} {}: Cannot deal damage to non-Actor target {}", context.getGameId(), source, target);
			return;
		}
		context.getLogic().damage(player, actor, damage, source, ignoreSpellDamage, MAGICAL_DAMAGE_TYPE);
	}

	public static void execDamageSpellFromStack(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			boolean ignoreSpellDamage) {
		if (!(target instanceof Actor actor)) {
			LOGGER.error("onCast {} {}: Cannot deal damage to non-Actor target {}", context.getGameId(), source, target);
			return;
		}
		int damage = 0;
		Deque<Integer> stack = context.getDamageStack();
		if (!stack.isEmpty()) {
			Integer peek = stack.peek();
			if (peek != null) {
				damage = peek;
			}
		}
		context.getLogic().damage(player, actor, damage, source, ignoreSpellDamage, MAGICAL_DAMAGE_TYPE);
	}

	public static void execHealSpell(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			int healing) {
		context.getLogic().heal(player, (Actor) target, Math.max(0, healing), source);
	}

	public static void execBuffSpell(
			GameContext context,
			Player player,
			SpellDesc desc,
			Entity source,
			Entity target,
			int attackBonus,
			int hpBonus,
			int totalArmorBonus,
			int value,
			boolean hasValue,
			boolean hasAttack,
			boolean hasHp) {
		if (target == null) {
			return;
		}
		if (value != 0) {
			if (target instanceof Hero) {
				attackBonus = totalArmorBonus = value;
			} else {
				attackBonus = hpBonus = value;
			}
		}

		// Fast indexed check for ModifyBuffSpellAura without stream allocation
		GameStateArena.TriggerList trigs = GameStateArena.getContextArena(context).triggers;
		if ((trigs.kindMask & GameStateArena.KIND_MODIFY_BUFF_AURA) != 0) {
			for (int i = 0, n = trigs.size; i < n; i++) {
				var t = trigs.elements[i];
				if (t instanceof ModifyBuffSpellAura aura && !aura.isExpired() && aura.isActivated()) {
					var affected = aura.getAffectedEntities();
					if (source != null && (affected.contains(source.getId())
							|| (source.getSourceCard() != null && affected.contains(source.getSourceCard().getId())))) {
						var spellValueStack = context.getSpellValueStack();
						spellValueStack.push(attackBonus);
						int auraAttackBonus = aura.getDesc().getValue(AuraArg.ATTACK_BONUS, context, player, target, source, 0);
						spellValueStack.pop();
						spellValueStack.push(hpBonus);
						int auraHpBonus = aura.getDesc().getValue(AuraArg.HP_BONUS, context, player, target, source, 0);
						spellValueStack.pop();
						int auraValueBonus = aura.getDesc().getValue(AuraArg.VALUE, context, player, target, source, 0);
						if (hasValue) {
							attackBonus += auraAttackBonus + auraValueBonus;
							hpBonus += auraHpBonus + auraValueBonus;
						} else {
							if (hasAttack) {
								attackBonus += auraAttackBonus + auraValueBonus;
								hpBonus += auraValueBonus;
							}
							if (hasHp) {
								attackBonus += auraValueBonus;
								hpBonus += auraHpBonus + auraValueBonus;
							}
						}
					}
				}
			}
		}

		if (attackBonus != 0 || hpBonus != 0) {
			Enchantment.attachBuffEnchantment(context, player, desc, source, target, attackBonus, hpBonus, 0);
		}

		if (attackBonus != 0) {
			if (target instanceof Hero) {
				target.modifyAttribute(Attribute.TEMPORARY_ATTACK_BONUS, attackBonus);
			} else {
				target.modifyAttribute(Attribute.ATTACK_BONUS, attackBonus);
			}
		}

		if (hpBonus != 0) {
			if (target instanceof Weapon weapon) {
				context.getLogic().modifyDurability(weapon, hpBonus);
			} else {
				context.getLogic().modifyHpSpell(source, target, hpBonus);
			}
		}

		if (totalArmorBonus != 0) {
			if (target.getEntityType() == EntityType.HERO) {
				context.getLogic().gainArmor(context.getPlayer(target.getOwner()), totalArmorBonus);
			} else {
				context.getLogic().gainArmor(player, totalArmorBonus);
			}
		}
	}

	public static void execAddAttributeSpell(GameContext context, Entity source, Entity target, Attribute tag) {
		Objects.requireNonNull(target, () -> source.getSourceCard().getCardId());
		context.getLogic().applyAttribute(target, tag, source);
	}

	public static void execRemoveAttributeSpell(GameContext context, Player player, Entity target, Attribute tag) {
		context.getLogic().removeAttribute(player, null, target, tag);
	}

	public static void execSetAttackSpell(Entity target, int value, boolean exclusive) {
		target.setAttribute(Attribute.ATTACK, value);
		target.getAttributes().remove(Attribute.ATTACK_BONUS);
		if (exclusive) {
			target.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
			if (!target.hasAttribute(Attribute.ENRAGED)) {
				target.getAttributes().remove(Attribute.CONDITIONAL_ATTACK_BONUS);
			}
		} else {
			target.setAttribute(Attribute.BASE_ATTACK, value);
		}
	}

	public static void execSetHpSpell(GameContext context, Entity target, int hp, boolean exclusive) {
		target.getAttributes().remove(Attribute.HP_BONUS);
		if (target instanceof Actor actor) {
			context.getLogic().setHpAndMaxHp(actor, hp);
		} else if (target instanceof Card) {
			target.getAttributes().put(Attribute.HP, hp);
		}
		if (!exclusive) {
			target.setAttribute(Attribute.BASE_HP, hp);
		}
		if (hp > 0) {
			target.getAttributes().remove(Attribute.DESTROYED);
		}
	}

	public static void execDestroySpell(GameContext context, Entity source, Entity target) {
		if (target == null) {
			throw new UnsupportedOperationException("must specify a target");
		}
		if (!target.isDestroyed()) {
			source.modifyAttribute(Attribute.TOTAL_KILLS, 1);
		}
		if (target instanceof Card card) {
			context.getLogic().removeCard(card);
		} else {
			context.getLogic().markAsDestroyed((Actor) target, source);
		}
	}

	public static void execSilenceSpell(GameContext context, Player player, Entity source, Entity target) {
		if (!(target instanceof Actor actor)) {
			LOGGER.error("onCast {} {}: The specified target {} is not an actor, so it cannot be silenced", context.getGameId(), source, target);
			return;
		}
		context.getLogic().silence(player.getId(), actor);
	}

	public static void execDrawCardSpell(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			int cardCount,
			SpellDesc subSpell) {
		int playerId = player.getId();
		GameLogic logic = context.getLogic();
		for (int i = 0; i < cardCount; i++) {
			Card card = logic.drawCard(playerId, source);
			if (card == null || card.getZone() != Zones.HAND) {
				continue;
			}
			if (subSpell != null) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
			}
		}
	}
}
