package net.demilich.metastone.game.cards.desc.compiler;

import java.io.IOException;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Opcode;
import java.lang.classfile.attribute.MethodParameterInfo;
import java.lang.classfile.attribute.MethodParametersAttribute;
import java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.TypeCheckInstruction;
import java.lang.constant.ClassDesc;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Static bytecode and signature analyzer for {@code Spell}, {@code EntityFilter}, {@code ValueProvider},
 * {@code Condition}, {@code EventTrigger}, {@code CardCostModifier}, {@code Aura}, and {@code CardSource}
 * components.
 * <p>
 * Used both at build time by {@link SpellsourceClassfileWeaver} to emit
 * {@code META-INF/spellsource/component-signatures.json} and by the static + runtime gameplay audit.
 */
public final class ComponentSignatureAnalyzer {

	public enum ComponentKind {
		SPELL(
				"Spell",
				"net/demilich/metastone/game/spells/Spell",
				"net/demilich/metastone/game/spells/desc/SpellArg",
				"net/demilich/metastone/game/spells/desc/SpellDesc",
				Set.of("CLASS", "FILTER", "TARGET", "NAME", "DESCRIPTION", "RANDOM_TARGET", "TARGET_PLAYER",
						"SPELL", "BOARD_POSITION_RELATIVE", "BOARD_POSITION_ABSOLUTE", "AFTERMATH_ID", "CARD")
		),
		ENTITY_FILTER(
				"EntityFilter",
				"net/demilich/metastone/game/spells/desc/filter/EntityFilter",
				"net/demilich/metastone/game/spells/desc/filter/EntityFilterArg",
				"net/demilich/metastone/game/spells/desc/filter/EntityFilterDesc",
				Set.of("CLASS", "INVERT", "TARGET_PLAYER", "AND_CONDITION", "TARGET")
		),
		VALUE_PROVIDER(
				"ValueProvider",
				"net/demilich/metastone/game/spells/desc/valueprovider/ValueProvider",
				"net/demilich/metastone/game/spells/desc/valueprovider/ValueProviderArg",
				"net/demilich/metastone/game/spells/desc/valueprovider/ValueProviderDesc",
				Set.of("CLASS", "TARGET_PLAYER", "MULTIPLIER", "OFFSET")
		),
		CONDITION(
				"Condition",
				"net/demilich/metastone/game/spells/desc/condition/Condition",
				"net/demilich/metastone/game/spells/desc/condition/ConditionArg",
				"net/demilich/metastone/game/spells/desc/condition/ConditionDesc",
				Set.of("CLASS", "INVERT", "TARGET_PLAYER", "TARGET", "FILTER", "DESCRIPTION")
		),
		EVENT_TRIGGER(
				"EventTrigger",
				"net/demilich/metastone/game/spells/trigger/EventTrigger",
				"net/demilich/metastone/game/spells/desc/trigger/EventTriggerArg",
				"net/demilich/metastone/game/spells/desc/trigger/EventTriggerDesc",
				Set.of("CLASS", "RACE", "TARGET_PLAYER", "SOURCE_PLAYER", "HOST_TARGET_TYPE",
						"QUEUE_CONDITION", "FIRE_CONDITION", "TARGET", "SOURCE_ENTITY_TYPE", "TARGET_ENTITY_TYPE")
		),
		CARD_COST_MODIFIER(
				"CardCostModifier",
				"net/demilich/metastone/game/cards/costmodifier/CardCostModifier",
				"net/demilich/metastone/game/spells/desc/manamodifier/CardCostModifierArg",
				"net/demilich/metastone/game/spells/desc/manamodifier/CardCostModifierDesc",
				Set.of("CLASS", "TARGET_PLAYER", "RACE", "VALUE", "MIN_VALUE", "CARD_TYPE",
						"REQUIRED_ATTRIBUTE", "EXPIRATION_TRIGGER", "EXPIRATION_TRIGGERS",
						"TOGGLE_ON_TRIGGER", "TOGGLE_OFF_TRIGGER", "TARGET", "OPERATION", "FILTER", "CONDITION")
		),
		AURA(
				"Aura",
				"net/demilich/metastone/game/spells/aura/Aura",
				"net/demilich/metastone/game/spells/desc/aura/AuraArg",
				"net/demilich/metastone/game/spells/desc/aura/AuraDesc",
				Set.of("CLASS", "FILTER", "TARGET", "ATTRIBUTE", "APPLY_EFFECT", "REMOVE_EFFECT",
						"SECONDARY_TRIGGER", "TRIGGERS", "REVERT_TRIGGER", "CONDITION", "ALWAYS_APPLY",
						"CAN_AFFORD_CONDITION", "AMOUNT_OF_CURRENCY", "PAY_EFFECT", "NAME", "DESCRIPTION", "ZONES")
		),
		CARD_SOURCE(
				"CardSource",
				"net/demilich/metastone/game/spells/desc/source/CardSource",
				"net/demilich/metastone/game/spells/desc/source/CardSourceArg",
				"net/demilich/metastone/game/spells/desc/source/CardSourceDesc",
				Set.of("CLASS", "TARGET_PLAYER", "DISTINCT", "INVERT")
		);

		public final String label;
		public final String baseInternalName;
		public final String argEnumInternalName;
		public final String descInternalName;
		public final Set<String> envelopeArgs;

		ComponentKind(String label, String baseInternalName, String argEnumInternalName, String descInternalName, Set<String> envelopeArgs) {
			this.label = label;
			this.baseInternalName = baseInternalName;
			this.argEnumInternalName = argEnumInternalName;
			this.descInternalName = descInternalName;
			this.envelopeArgs = envelopeArgs;
		}
	}

	private static final Set<String> ALL_ARG_ENUM_INTERNAL_NAMES = Set.of(
			"net/demilich/metastone/game/spells/desc/SpellArg",
			"net/demilich/metastone/game/spells/desc/filter/EntityFilterArg",
			"net/demilich/metastone/game/spells/desc/valueprovider/ValueProviderArg",
			"net/demilich/metastone/game/spells/desc/condition/ConditionArg",
			"net/demilich/metastone/game/spells/desc/trigger/EventTriggerArg",
			"net/demilich/metastone/game/spells/desc/manamodifier/CardCostModifierArg",
			"net/demilich/metastone/game/spells/desc/aura/AuraArg",
			"net/demilich/metastone/game/spells/desc/source/CardSourceArg"
	);

	public record TypedParamInfo(
			String name,
			String javaType,
			String mappedArg,
			boolean isMixin,
			Set<String> mixinArgs,
			Integer defaultInt,
			String defaultString
	) {
	}

	public record ComponentSignature(
			String className,
			String simpleName,
			ComponentKind kind,
			boolean isAbstract,
			String superClassName,
			Set<String> directArgs,
			Set<String> inheritedArgs,
			Set<String> helperMixinArgs,
			Set<String> crossComponentReads,
			Set<String> staticMutations,
			Map<String, String> inferredArgTypes,
			Map<String, Integer> inferredDefaultInts,
			List<TypedParamInfo> typedParameters
	) {
		public Set<String> allAcceptedArgs() {
			Set<String> all = new TreeSet<>(kind.envelopeArgs);
			all.addAll(directArgs);
			all.addAll(inheritedArgs);
			all.addAll(helperMixinArgs);
			return all;
		}

		public Set<String> allEvaluatedArgs() {
			Set<String> all = new TreeSet<>(directArgs);
			all.addAll(inheritedArgs);
			all.addAll(helperMixinArgs);
			return all;
		}
	}

	private static Map<ComponentKind, Map<String, String>> extractDeserializerTypes(Map<String, ClassModel> models) {
		Map<ComponentKind, Map<String, String>> result = new EnumMap<>(ComponentKind.class);
		Map<String, ComponentKind> deserClasses = Map.of(
				"net/demilich/metastone/game/cards/desc/SpellDescDeserializer", ComponentKind.SPELL,
				"net/demilich/metastone/game/cards/desc/EntityFilterDescDeserializer", ComponentKind.ENTITY_FILTER,
				"net/demilich/metastone/game/cards/desc/ValueProviderDescDeserializer", ComponentKind.VALUE_PROVIDER,
				"net/demilich/metastone/game/cards/desc/ConditionDescDeserializer", ComponentKind.CONDITION,
				"net/demilich/metastone/game/cards/desc/EventTriggerDescDeserializer", ComponentKind.EVENT_TRIGGER,
				"net/demilich/metastone/game/cards/desc/CardCostModifierDescDeserializer", ComponentKind.CARD_COST_MODIFIER,
				"net/demilich/metastone/game/cards/desc/AuraDescDeserializer", ComponentKind.AURA,
				"net/demilich/metastone/game/cards/desc/CardSourceDescDeserializer", ComponentKind.CARD_SOURCE
		);
		for (Map.Entry<String, ComponentKind> entry : deserClasses.entrySet()) {
			ClassModel cm = models.get(entry.getKey());
			if (cm == null) {
				continue;
			}
			Map<String, String> argToType = new LinkedHashMap<>();
			for (MethodModel mm : cm.methods()) {
				if (mm.methodName().equalsString("init") && mm.code().isPresent()) {
					String lastArg = null;
					for (CodeElement ce : mm.code().get()) {
						if (ce instanceof FieldInstruction fi && fi.opcode() == Opcode.GETSTATIC) {
							String owner = fi.owner().asInternalName();
							if (ALL_ARG_ENUM_INTERNAL_NAMES.contains(owner)) {
								lastArg = fi.name().stringValue();
							} else if (owner.equals("net/demilich/metastone/game/cards/desc/ParseValueType") && lastArg != null) {
								argToType.put(lastArg, parseValueTypeToJavaType(fi.name().stringValue()));
								lastArg = null;
							}
						}
					}
				}
			}
			result.put(entry.getValue(), argToType);
		}
		return result;
	}

	private static String parseValueTypeToJavaType(String pvtName) {
		return switch (pvtName) {
			case "BOOLEAN" -> "boolean";
			case "INTEGER", "VALUE" -> "int";
			case "INTEGER_ARRAY" -> "int[]";
			case "STRING" -> "String";
			case "STRING_ARRAY" -> "String[]";
			case "TARGET_SELECTION" -> "TargetSelection";
			case "TARGET_REFERENCE" -> "EntityReference";
			case "TARGET_PLAYER" -> "TargetPlayer";
			case "SPELL" -> "SpellDesc";
			case "SPELL_ARRAY" -> "SpellDesc[]";
			case "ATTRIBUTE" -> "Attribute";
			case "PLAYER_ATTRIBUTE" -> "PlayerAttribute";
			case "VALUE_PROVIDER" -> "ValueProvider";
			case "ENTITY_FILTER" -> "EntityFilter";
			case "ENTITY_FILTER_ARRAY" -> "EntityFilter[]";
			case "BOARD_POSITION_RELATIVE" -> "BoardPositionRelative";
			case "CARD_LOCATION" -> "Zones";
			case "ZONES" -> "Zones[]";
			case "OPERATION" -> "ComparisonOperation";
			case "ALGEBRAIC_OPERATION" -> "AlgebraicOperation";
			case "CONDITION" -> "Condition";
			case "CONDITION_ARRAY" -> "Condition[]";
			case "CARD_TYPE", "CARD_DESC_TYPE" -> "CardType";
			case "ENTITY_TYPE" -> "EntityType";
			case "ACTION_TYPE" -> "ActionType";
			case "TARGET_TYPE" -> "TargetType";
			case "TRIGGER", "QUEST", "SECRET" -> "EnchantmentDesc";
			case "TRIGGERS" -> "EnchantmentDesc[]";
			case "EVENT_TRIGGER" -> "EventTriggerDesc";
			case "EVENT_TRIGGER_ARRAY" -> "EventTriggerDesc[]";
			case "CARD_COST_MODIFIER" -> "CardCostModifierDesc";
			case "RARITY" -> "Rarity";
			case "CARD_SOURCE" -> "CardSource";
			case "CARD_SOURCE_ARRAY" -> "CardSource[]";
			case "GAME_VALUE" -> "GameValue";
			case "AURA" -> "AuraDesc";
			case "CHOOSE_ONE_OVERRIDE" -> "ChooseOneOverride";
			case "BATTLECRY" -> "OpenerDesc";
			case "DYNAMIC_DESCRIPTION" -> "DynamicDescriptionDesc";
			case "DYNAMIC_DESCRIPTION_ARRAY" -> "DynamicDescriptionDesc[]";
			default -> "Object";
		};
	}

	public static Map<String, ComponentSignature> analyzeDirectories(Path... classesDirs) throws IOException {
		ClassFile cf = ClassFile.of();
		Map<String, ClassModel> models = new LinkedHashMap<>();

		for (Path dir : classesDirs) {
			if (dir == null || !Files.exists(dir)) {
				continue;
			}
			Files.walkFileTree(dir, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().endsWith(".class") && !file.getFileName().toString().equals("package-info.class")) {
						ClassModel cm = cf.parse(file);
						models.put(cm.thisClass().asInternalName(), cm);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}

		// Map each class to its direct raw analysis
		Map<String, RawClassAnalysis> rawMap = new LinkedHashMap<>();
		for (Map.Entry<String, ClassModel> entry : models.entrySet()) {
			String internalName = entry.getKey();
			ClassModel cm = entry.getValue();
			ComponentKind kind = resolveKind(internalName, models);
			if (kind != null) {
				rawMap.put(internalName, analyzeClassModel(cm, kind, models));
			}
		}

		Map<ComponentKind, Map<String, String>> deserializerTypes = extractDeserializerTypes(models);

		// Assemble full ComponentSignature with inheritance resolution
		Map<String, ComponentSignature> result = new TreeMap<>();
		for (Map.Entry<String, RawClassAnalysis> entry : rawMap.entrySet()) {
			String internalName = entry.getKey();
			RawClassAnalysis raw = entry.getValue();
			ComponentKind kind = raw.kind;

			Set<String> inheritedArgs = new TreeSet<>();
			Set<String> helperMixinArgs = new TreeSet<>(raw.helperMixinArgs);
			Map<String, String> inferredTypes = new TreeMap<>();
			Map<String, Integer> inferredDefaults = new TreeMap<>();

			// Walk up superclass chain
			String currSuper = raw.superInternalName;
			while (currSuper != null && !currSuper.equals("java/lang/Object")) {
				RawClassAnalysis superRaw = rawMap.get(currSuper);
				if (superRaw != null) {
					// If currSuper is not the root abstract class (or for Aura/CardCostModifier which are concrete roots)
					if (!currSuper.equals(kind.baseInternalName)
							|| kind == ComponentKind.AURA
							|| kind == ComponentKind.CARD_COST_MODIFIER) {
						inheritedArgs.addAll(superRaw.directArgs);
						inheritedArgs.addAll(superRaw.helperMixinArgs);
						inferredTypes.putAll(superRaw.inferredArgTypes);
						inferredDefaults.putAll(superRaw.inferredDefaultInts);
					}
					currSuper = superRaw.superInternalName;
				} else {
					ClassModel scm = models.get(currSuper);
					currSuper = scm != null ? scm.superclass().map(e -> e.asInternalName()).orElse(null) : null;
				}
			}

			inferredTypes.putAll(raw.inferredArgTypes);
			inferredDefaults.putAll(raw.inferredDefaultInts);

			// Fill any missing types from Deserializer map
			Map<String, String> deserMap = deserializerTypes.getOrDefault(kind, Collections.emptyMap());
			Set<String> allEval = new TreeSet<>(raw.directArgs);
			allEval.addAll(inheritedArgs);
			allEval.addAll(helperMixinArgs);
			for (String arg : allEval) {
				inferredTypes.putIfAbsent(arg, deserMap.getOrDefault(arg, "Object"));
			}

			String dotName = internalName.replace('/', '.');
			String simpleName = dotName.substring(dotName.lastIndexOf('.') + 1);
			String superDotName = raw.superInternalName != null ? raw.superInternalName.replace('/', '.') : "java.lang.Object";

			ComponentSignature sig = new ComponentSignature(
					dotName,
					simpleName,
					kind,
					raw.isAbstract,
					superDotName,
					Collections.unmodifiableSet(raw.directArgs),
					Collections.unmodifiableSet(inheritedArgs),
					Collections.unmodifiableSet(helperMixinArgs),
					Collections.unmodifiableSet(raw.crossComponentReads),
					Collections.unmodifiableSet(raw.staticMutations),
					Collections.unmodifiableMap(inferredTypes),
					Collections.unmodifiableMap(inferredDefaults),
					Collections.unmodifiableList(raw.typedParameters)
			);
			result.put(dotName, sig);
		}

		return result;
	}

	private static ComponentKind resolveKind(String internalName, Map<String, ClassModel> models) {
		String curr = internalName;
		while (curr != null && !curr.equals("java/lang/Object")) {
			for (ComponentKind kind : ComponentKind.values()) {
				if (curr.equals(kind.baseInternalName)) {
					return kind;
				}
			}
			ClassModel cm = models.get(curr);
			if (cm == null) {
				break;
			}
			curr = cm.superclass().map(e -> e.asInternalName()).orElse(null);
		}
		return null;
	}

	private static final class RawClassAnalysis {
		final ComponentKind kind;
		final boolean isAbstract;
		final String superInternalName;
		final Set<String> directArgs = new TreeSet<>();
		final Set<String> helperMixinArgs = new TreeSet<>();
		final Set<String> crossComponentReads = new TreeSet<>();
		final Set<String> staticMutations = new TreeSet<>();
		final Map<String, String> inferredArgTypes = new TreeMap<>();
		final Map<String, Integer> inferredDefaultInts = new TreeMap<>();
		final List<TypedParamInfo> typedParameters = new ArrayList<>();

		RawClassAnalysis(ComponentKind kind, boolean isAbstract, String superInternalName) {
			this.kind = kind;
			this.isAbstract = isAbstract;
			this.superInternalName = superInternalName;
		}
	}

	private static RawClassAnalysis analyzeClassModel(
			ClassModel cm,
			ComponentKind kind,
			Map<String, ClassModel> allModels) {
		boolean isAbstract = (cm.flags().flagsMask() & ClassFile.ACC_ABSTRACT) != 0;
		String superInternal = cm.superclass().map(e -> e.asInternalName()).orElse(null);
		RawClassAnalysis out = new RawClassAnalysis(kind, isAbstract, superInternal);

		// 1. Check for Option 1 Typed Signature methods (extended onCast / test / provideValue / isFulfilled)
		for (MethodModel mm : cm.methods()) {
			if (isTypedComponentMethod(mm, kind)) {
				extractTypedParameters(mm, kind, out);
			}
		}

		// 2. Scan all evaluation & helper methods (excluding static create* / build* factory methods)
		Set<String> visitedHelperMethods = new LinkedHashSet<>();
		for (MethodModel mm : cm.methods()) {
			String mName = mm.methodName().stringValue();
			boolean isStatic = (mm.flags().flagsMask() & ClassFile.ACC_STATIC) != 0;
			if (isStatic && (mName.startsWith("create") || mName.startsWith("build") || mName.equals("<clinit>"))) {
				continue;
			}
			// Skip synthetic woven bridge if we already extracted typed parameters, or scan both (scanning both is idempotent!)
			scanMethodBytecode(cm.thisClass().asInternalName(), mm, kind, out, allModels, visitedHelperMethods, 0);
		}

		return out;
	}

	public static boolean isTypedComponentMethod(MethodModel mm, ComponentKind kind) {
		if ((mm.flags().flagsMask() & ClassFile.ACC_STATIC) != 0
				|| (mm.flags().flagsMask() & ClassFile.ACC_SYNTHETIC) != 0
				|| (mm.flags().flagsMask() & ClassFile.ACC_BRIDGE) != 0) {
			return false;
		}
		String name = mm.methodName().stringValue();
		List<ClassDesc> params = mm.methodTypeSymbol().parameterList();
		return switch (kind) {
			case SPELL -> {
				if (!name.equals("onCast")) {
					yield false;
				}
				// Standard 5-arg is (GameContext, Player, SpellDesc, Entity, Entity)
				if (params.size() == 5
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("SpellDesc;")
						&& params.get(3).descriptorString().endsWith("Entity;")
						&& params.get(4).descriptorString().endsWith("Entity;")) {
					yield false;
				}
				// Typed onCast: either (GameContext, Player, SpellDesc, Entity, Entity, ...) with size > 5
				// OR (GameContext, Player, Entity, Entity, ...) with size >= 4
				yield (params.size() > 5
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("SpellDesc;")
						&& params.get(3).descriptorString().endsWith("Entity;")
						&& params.get(4).descriptorString().endsWith("Entity;"))
						|| (params.size() >= 4
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("Entity;")
						&& params.get(3).descriptorString().endsWith("Entity;"));
			}
			case ENTITY_FILTER -> {
				if (!name.equals("test")) {
					yield false;
				}
				// Standard 4-arg is (GameContext, Player, Entity, Entity)
				if (params.size() == 4
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("Entity;")
						&& params.get(3).descriptorString().endsWith("Entity;")) {
					yield false;
				}
				yield (params.size() > 4
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("Entity;")
						&& params.get(3).descriptorString().endsWith("Entity;"))
						|| (params.size() >= 5
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("EntityFilterDesc;")
						&& params.get(3).descriptorString().endsWith("Entity;")
						&& params.get(4).descriptorString().endsWith("Entity;"));
			}
			case VALUE_PROVIDER -> {
				if (!name.equals("provideValue")) {
					yield false;
				}
				if (params.size() == 4
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("Entity;")
						&& params.get(3).descriptorString().endsWith("Entity;")) {
					yield false;
				}
				yield (params.size() > 4
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("Entity;")
						&& params.get(3).descriptorString().endsWith("Entity;"))
						|| (params.size() >= 5
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("ValueProviderDesc;")
						&& params.get(3).descriptorString().endsWith("Entity;")
						&& params.get(4).descriptorString().endsWith("Entity;"));
			}
			case CONDITION -> {
				if (!name.equals("isFulfilled") && !name.equals("isFulfilled$0")) {
					yield false;
				}
				if (name.equals("isFulfilled$0")) {
					yield params.size() == 4
							&& params.get(0).descriptorString().endsWith("GameContext;")
							&& params.get(1).descriptorString().endsWith("Player;")
							&& params.get(2).descriptorString().endsWith("Entity;")
							&& params.get(3).descriptorString().endsWith("Entity;");
				}
				// Standard 5-arg is (GameContext, Player, ConditionDesc, Entity, Entity)
				// Standard 4-arg is final public wrapper in Condition
				if (params.size() == 5
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("ConditionDesc;")
						&& params.get(3).descriptorString().endsWith("Entity;")
						&& params.get(4).descriptorString().endsWith("Entity;")) {
					yield false;
				}
				if (params.size() == 4 && (mm.flags().flagsMask() & ClassFile.ACC_FINAL) != 0) {
					yield false;
				}
				yield (params.size() > 5
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("ConditionDesc;")
						&& params.get(3).descriptorString().endsWith("Entity;")
						&& params.get(4).descriptorString().endsWith("Entity;"))
						|| (params.size() >= 4
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Player;")
						&& params.get(2).descriptorString().endsWith("Entity;")
						&& params.get(3).descriptorString().endsWith("Entity;"));
			}
			case CARD_SOURCE -> {
				if (!name.equals("match")
						|| !mm.methodTypeSymbol().returnType().descriptorString().endsWith("CardList;")) {
					yield false;
				}
				if (params.size() == 3
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Entity;")
						&& params.get(2).descriptorString().endsWith("Player;")) {
					yield false;
				}
				yield params.size() > 3
						&& params.get(0).descriptorString().endsWith("GameContext;")
						&& params.get(1).descriptorString().endsWith("Entity;")
						&& params.get(2).descriptorString().endsWith("Player;");
			}
			default -> false;
		};
	}

	private static void extractTypedParameters(MethodModel mm, ComponentKind kind, RawClassAnalysis out) {
		List<ClassDesc> paramTypes = mm.methodTypeSymbol().parameterList();
		int prefixLen = (kind == ComponentKind.CARD_SOURCE) ? 3 : (hasDescInPrefix(paramTypes) ? 5 : 4);
		Optional<MethodParametersAttribute> mpaOpt = mm.findAttribute(Attributes.methodParameters());
		if (mpaOpt.isEmpty()) {
			return;
		}
		List<MethodParameterInfo> paramInfos = mpaOpt.get().parameters();
		Optional<RuntimeVisibleParameterAnnotationsAttribute> rvpaOpt =
				mm.findAttribute(Attributes.runtimeVisibleParameterAnnotations());
		List<List<Annotation>> paramAnns = rvpaOpt.map(RuntimeVisibleParameterAnnotationsAttribute::parameterAnnotations)
				.orElse(Collections.emptyList());

		for (int i = prefixLen; i < paramTypes.size() && i < paramInfos.size(); i++) {
			String pName = paramInfos.get(i).name().map(u -> u.stringValue()).orElse("arg" + i);
			ClassDesc pType = paramTypes.get(i);
			String simpleType = simpleTypeName(pType);

			Integer defaultInt = null;
			String defaultStr = null;
			if (i < paramAnns.size()) {
				for (Annotation ann : paramAnns.get(i)) {
					if (ann.className().stringValue().endsWith("/Default;")) {
						for (AnnotationElement el : ann.elements()) {
							if (el.name().equalsString("value") && el.value() instanceof AnnotationValue.OfInt oi) {
								defaultInt = oi.intValue();
							} else if (el.name().equalsString("string") && el.value() instanceof AnnotationValue.OfString os) {
								defaultStr = os.stringValue();
							}
						}
					}
				}
			}

			if (kind == ComponentKind.SPELL && pName.equals("cards") && simpleType.equals("CardList")) {
				Set<String> mixinSet = Set.of("CARD", "CARDS", "CARD_SOURCE", "CARD_FILTER", "VALUE");
				out.helperMixinArgs.addAll(mixinSet);
				out.typedParameters.add(new TypedParamInfo(pName, simpleType, null, true, mixinSet, null, null));
			} else if (kind == ComponentKind.SPELL && pName.equals("card") && simpleType.equals("Card")) {
				Set<String> mixinSet = Set.of("CARD");
				out.helperMixinArgs.addAll(mixinSet);
				out.typedParameters.add(new TypedParamInfo(pName, simpleType, null, true, mixinSet, null, null));
			} else if (kind == ComponentKind.SPELL && pName.equals("boardPosition") && simpleType.equals("int")) {
				Set<String> mixinSet = Set.of("BOARD_POSITION_ABSOLUTE", "BOARD_POSITION_RELATIVE");
				out.helperMixinArgs.addAll(mixinSet);
				out.typedParameters.add(new TypedParamInfo(pName, simpleType, null, true, mixinSet, null, null));
			} else if (kind == ComponentKind.ENTITY_FILTER
					&& (pName.equals("entities") || pName.equals("targetedEntities"))
					&& simpleType.equals("List")) {
				Set<String> mixinSet = Set.of("TARGET");
				out.helperMixinArgs.addAll(mixinSet);
				out.typedParameters.add(new TypedParamInfo(pName, "List<Entity>", null, true, mixinSet, null, null));
			} else {
				String argKey = camelToUpperSnake(pName);
				if (argKey.equals("TARGET_REFERENCE")) {
					argKey = "TARGET";
				} else if (argKey.equals("SOURCE_REFERENCE")) {
					argKey = "SOURCE";
				}
				out.directArgs.add(argKey);
				out.inferredArgTypes.put(argKey, simpleType);
				if (defaultInt != null) {
					out.inferredDefaultInts.put(argKey, defaultInt);
				} else if (simpleType.equals("int")) {
					out.inferredDefaultInts.putIfAbsent(argKey, 0);
				}
				out.typedParameters.add(new TypedParamInfo(pName, simpleType, argKey, false, Collections.emptySet(), defaultInt, defaultStr));
			}
		}
	}

	public static boolean hasDescInPrefix(List<ClassDesc> paramTypes) {
		return paramTypes.size() >= 5 && paramTypes.get(2).descriptorString().endsWith("Desc;");
	}

	public static String camelToUpperSnake(String camel) {
		StringBuilder sb = new StringBuilder(camel.length() + 4);
		for (int i = 0; i < camel.length(); i++) {
			char c = camel.charAt(i);
			if (Character.isUpperCase(c)) {
				if (i > 0) {
					sb.append('_');
				}
				sb.append(c);
			} else {
				sb.append(Character.toUpperCase(c));
			}
		}
		return sb.toString();
	}

	private static String simpleTypeName(ClassDesc cd) {
		if (cd.isPrimitive()) {
			return cd.displayName();
		}
		if (cd.isArray()) {
			return simpleTypeName(cd.componentType()) + "[]";
		}
		String desc = cd.descriptorString();
		int lastSlash = desc.lastIndexOf('/');
		int semi = desc.indexOf(';');
		if (lastSlash >= 0 && semi > lastSlash) {
			return desc.substring(lastSlash + 1, semi);
		}
		return cd.displayName();
	}

	private static void scanMethodBytecode(
			String ownerInternalName,
			MethodModel mm,
			ComponentKind kind,
			RawClassAnalysis out,
			Map<String, ClassModel> allModels,
			Set<String> visitedHelperMethods,
			int depth) {
		if (depth > 3) {
			return;
		}
		Optional<CodeModel> codeOpt = mm.code();
		if (codeOpt.isEmpty()) {
			return;
		}

		List<Instruction> insns = new ArrayList<>();
		for (CodeElement ce : codeOpt.get()) {
			if (ce instanceof Instruction insn) {
				insns.add(insn);
			}
		}

		for (int i = 0; i < insns.size(); i++) {
			Instruction insn = insns.get(i);

			if (insn instanceof FieldInstruction fi && fi.opcode() == Opcode.GETSTATIC) {
				String fieldOwner = fi.owner().asInternalName();
				String fieldName = fi.name().stringValue();
				if (ALL_ARG_ENUM_INTERNAL_NAMES.contains(fieldOwner)) {
					// Look ahead up to 10 instructions to see how this Arg constant is consumed
					boolean isMutation = false;
					boolean isForbiddenGuard = false;
					String inferredType = null;
					Integer inferredDefaultInt = null;

					for (int k = i + 1; k < Math.min(insns.size(), i + 15); k++) {
						Instruction next = insns.get(k);
						if (next.opcode() == Opcode.ATHROW) {
							isForbiddenGuard = true;
							break;
						}
						if (next instanceof FieldInstruction nextFi
								&& nextFi.opcode() == Opcode.GETSTATIC
								&& ALL_ARG_ENUM_INTERNAL_NAMES.contains(nextFi.owner().asInternalName())) {
							break;
						}
						if (next instanceof InvokeInstruction inv) {
							String mName = inv.name().stringValue();
							if (mName.equals("put") || mName.equals("addArg") || mName.equals("removeArg") || mName.equals("remove")) {
								isMutation = true;
								break;
							}
							if (mName.equals("getValue")) {
								inferredType = "int";
								if (k > i) {
									inferredDefaultInt = extractIntConstant(insns.get(k - 1));
								}
								break;
							}
							if (mName.equals("getBool")) {
								inferredType = "boolean";
								break;
							}
							if (mName.equals("getInt")) {
								inferredType = "int";
								if (k > i && inv.typeSymbol().parameterCount() == 2) {
									inferredDefaultInt = extractIntConstant(insns.get(k - 1));
								} else {
									inferredDefaultInt = 0;
								}
								break;
							}
							if (mName.equals("getString")) {
								inferredType = "String";
								break;
							}
							if (mName.equals("get") || mName.equals("getOrDefault") || mName.equals("getArg")) {
								if (k + 1 < insns.size() && insns.get(k + 1) instanceof TypeCheckInstruction tci
										&& tci.opcode() == Opcode.CHECKCAST) {
									inferredType = simpleTypeName(tci.type().asSymbol());
								}
								break;
							}
						}
					}

					if (isForbiddenGuard) {
						continue;
					}

					String simpleEnumName = fieldOwner.substring(fieldOwner.lastIndexOf('/') + 1);
					if (isMutation) {
						out.staticMutations.add(simpleEnumName + "." + fieldName);
					} else if (fieldOwner.equals(kind.argEnumInternalName)) {
						out.directArgs.add(fieldName);
						if (inferredType != null) {
							out.inferredArgTypes.put(fieldName, inferredType);
						}
						if (inferredDefaultInt != null) {
							out.inferredDefaultInts.put(fieldName, inferredDefaultInt);
						}
					} else {
						out.crossComponentReads.add(simpleEnumName + "." + fieldName);
					}
				}
			} else if (insn instanceof InvokeInstruction inv) {
				String targetOwner = inv.owner().asInternalName();
				String targetName = inv.name().stringValue();

				// Check known helper/mixin methods on SpellUtils, SpellDesc, EntityFilter, EntityFilterDesc, AuraDesc
				if (targetOwner.equals("net/demilich/metastone/game/spells/SpellUtils")) {
					switch (targetName) {
						case "getCards" -> {
							if (kind == ComponentKind.SPELL) {
								out.helperMixinArgs.addAll(List.of("CARD", "CARDS", "CARD_SOURCE", "CARD_FILTER", "VALUE"));
							}
						}
						case "getCard" -> {
							if (kind == ComponentKind.SPELL) {
								out.helperMixinArgs.add("CARD");
							}
						}
						case "getBoardPosition" -> {
							if (kind == ComponentKind.SPELL) {
								out.helperMixinArgs.addAll(List.of("BOARD_POSITION_ABSOLUTE", "BOARD_POSITION_RELATIVE"));
							}
						}
						case "discoverCard" -> {
							if (kind == ComponentKind.SPELL) {
								out.helperMixinArgs.add("SPELL");
								out.staticMutations.add("SpellArg.CARD (injected into child SPELL via SpellUtils.discoverCard)");
							}
						}
						case "getSpellDiscover" -> {
							if (kind == ComponentKind.SPELL) {
								out.helperMixinArgs.addAll(List.of("MANA", "TARGET_SELECTION", "EXCLUSIVE"));
								out.crossComponentReads.add("ChildSpellDesc.{NAME,DESCRIPTION,TARGET_SELECTION}");
							}
						}
					}
				} else if (targetOwner.equals("net/demilich/metastone/game/spells/desc/SpellDesc") && kind == ComponentKind.SPELL) {
					switch (targetName) {
						case "getFilteredCards" -> out.helperMixinArgs.addAll(List.of("CARD_SOURCE", "CARD_FILTER"));
						case "getCardFilter" -> out.helperMixinArgs.add("CARD_FILTER");
						case "getCardSource" -> out.helperMixinArgs.add("CARD_SOURCE");
						case "getEntityFilter" -> out.helperMixinArgs.add("FILTER");
						case "getTarget", "hasPredefinedTarget" -> out.helperMixinArgs.add("TARGET");
						case "getTargetPlayer" -> out.helperMixinArgs.add("TARGET_PLAYER");
						case "getAttribute" -> out.helperMixinArgs.add("ATTRIBUTE");
						case "getSecondaryTarget" -> out.helperMixinArgs.add("SECONDARY_TARGET");
						case "getSpell" -> out.helperMixinArgs.add("SPELL");
						case "getCards" -> out.helperMixinArgs.add("CARDS");
						case "subSpells", "spellStream" -> out.helperMixinArgs.addAll(List.of("SPELLS", "SPELL", "SPELL1", "SPELL2"));
					}
				} else if (targetOwner.equals("net/demilich/metastone/game/spells/desc/filter/EntityFilter")
						&& kind == ComponentKind.ENTITY_FILTER
						&& targetName.equals("getTargetedEntities")) {
					out.helperMixinArgs.add("TARGET");
				} else if (targetOwner.equals("net/demilich/metastone/game/spells/desc/filter/EntityFilterDesc")
						&& kind == ComponentKind.ENTITY_FILTER
						&& (targetName.equals("getCardOrCards") || targetName.equals("hasCardOrCards"))) {
					out.helperMixinArgs.addAll(List.of("CARD", "CARDS"));
				} else if (targetOwner.equals("net/demilich/metastone/game/spells/desc/aura/AuraDesc")
						&& kind == ComponentKind.AURA) {
					switch (targetName) {
						case "getApplyEffect" -> out.helperMixinArgs.add("APPLY_EFFECT");
						case "getRemoveEffect" -> out.helperMixinArgs.add("REMOVE_EFFECT");
						case "getAttribute" -> out.helperMixinArgs.add("ATTRIBUTE");
						case "getFilter" -> out.helperMixinArgs.add("FILTER");
						case "getTarget" -> out.helperMixinArgs.add("TARGET");
						case "getCondition" -> out.helperMixinArgs.add("CONDITION");
						case "getSecondaryTrigger" -> out.helperMixinArgs.add("SECONDARY_TRIGGER");
						case "getRevertTrigger" -> out.helperMixinArgs.add("REVERT_TRIGGER");
						case "getAlwaysApply" -> out.helperMixinArgs.add("ALWAYS_APPLY");
						case "getZones" -> out.helperMixinArgs.add("ZONES");
					}
				} else if (!targetName.startsWith("create")
						&& !targetName.startsWith("build")
						&& !targetName.equals("<init>")
						&& !targetName.equals("onCast")
						&& !targetName.equals("cast")
						&& !targetName.equals("test")
						&& !targetName.equals("provideValue")
						&& !targetName.equals("isFulfilled")
						&& !targetOwner.equals(kind.baseInternalName)
						&& allModels.containsKey(targetOwner)) {
					// Follow intra-component or cross-spell helper calls (e.g., DrawCardSpell -> FromDeckToHandSpell.drawFromDeck)
					ComponentKind targetKind = resolveKind(targetOwner, allModels);
					if (targetKind == kind) {
						String key = targetOwner + "#" + targetName + inv.type().stringValue();
						if (visitedHelperMethods.add(key)) {
							ClassModel targetCm = allModels.get(targetOwner);
							for (MethodModel targetMm : targetCm.methods()) {
								if (targetMm.methodName().equalsString(targetName)
										&& targetMm.methodType().equalsString(inv.type().stringValue())) {
									scanMethodBytecode(targetOwner, targetMm, kind, out, allModels, visitedHelperMethods, depth + 1);
								}
							}
						}
					}
				}
			}
		}
	}

	private static Integer extractIntConstant(Instruction insn) {
		return switch (insn.opcode()) {
			case ICONST_M1 -> -1;
			case ICONST_0 -> 0;
			case ICONST_1 -> 1;
			case ICONST_2 -> 2;
			case ICONST_3 -> 3;
			case ICONST_4 -> 4;
			case ICONST_5 -> 5;
			case BIPUSH, SIPUSH, LDC, LDC_W -> {
				if (insn instanceof ConstantInstruction ci && ci.constantValue() instanceof Integer val) {
					yield val;
				}
				yield null;
			}
			default -> null;
		};
	}

	public static void writeManifestJson(Map<String, ComponentSignature> signatures, Path outputPath) throws IOException {
		Files.createDirectories(outputPath.getParent());
		StringBuilder sb = new StringBuilder(65536);
		sb.append("{\n");
		sb.append("  \"schemaVersion\": 1,\n");
		sb.append("  \"componentCount\": ").append(signatures.size()).append(",\n");
		sb.append("  \"components\": {\n");
		int idx = 0;
		for (Map.Entry<String, ComponentSignature> entry : signatures.entrySet()) {
			ComponentSignature sig = entry.getValue();
			sb.append("    \"").append(sig.className()).append("\": {\n");
			sb.append("      \"simpleName\": \"").append(sig.simpleName()).append("\",\n");
			sb.append("      \"kind\": \"").append(sig.kind().label).append("\",\n");
			sb.append("      \"abstract\": ").append(sig.isAbstract()).append(",\n");
			sb.append("      \"superClass\": \"").append(sig.superClassName()).append("\",\n");
			sb.append("      \"directArgs\": ").append(toJsonStringArray(sig.directArgs())).append(",\n");
			sb.append("      \"inheritedArgs\": ").append(toJsonStringArray(sig.inheritedArgs())).append(",\n");
			sb.append("      \"helperMixinArgs\": ").append(toJsonStringArray(sig.helperMixinArgs())).append(",\n");
			sb.append("      \"inferredArgTypes\": ").append(toJsonMap(sig.inferredArgTypes())).append(",\n");
			sb.append("      \"inferredDefaultInts\": ").append(toJsonIntMap(sig.inferredDefaultInts())).append(",\n");
			sb.append("      \"typedParameters\": [");
			if (!sig.typedParameters().isEmpty()) {
				sb.append("\n");
				for (int p = 0; p < sig.typedParameters().size(); p++) {
					TypedParamInfo tp = sig.typedParameters().get(p);
					sb.append("        {\"name\": \"").append(tp.name())
							.append("\", \"type\": \"").append(tp.javaType())
							.append("\", \"arg\": ").append(tp.mappedArg() != null ? "\"" + tp.mappedArg() + "\"" : "null")
							.append(", \"mixin\": ").append(tp.isMixin());
					if (tp.defaultInt() != null) {
						sb.append(", \"defaultInt\": ").append(tp.defaultInt());
					}
					sb.append("}").append(p + 1 < sig.typedParameters().size() ? "," : "").append("\n");
				}
				sb.append("      ");
			}
			sb.append("]\n");
			sb.append("    }").append(++idx < signatures.size() ? "," : "").append("\n");
		}
		sb.append("  }\n");
		sb.append("}\n");
		Files.writeString(outputPath, sb.toString(), StandardCharsets.UTF_8);
	}

	private static String toJsonStringArray(Set<String> set) {
		if (set.isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		int i = 0;
		for (String s : set) {
			if (i++ > 0) {
				sb.append(", ");
			}
			sb.append('"').append(escapeJson(s)).append('"');
		}
		sb.append(']');
		return sb.toString();
	}

	private static String toJsonMap(Map<String, String> map) {
		if (map.isEmpty()) {
			return "{}";
		}
		StringBuilder sb = new StringBuilder("{");
		int i = 0;
		for (Map.Entry<String, String> e : map.entrySet()) {
			if (i++ > 0) {
				sb.append(", ");
			}
			sb.append('"').append(escapeJson(e.getKey())).append("\": \"").append(escapeJson(e.getValue())).append('"');
		}
		sb.append('}');
		return sb.toString();
	}

	private static String toJsonIntMap(Map<String, Integer> map) {
		if (map.isEmpty()) {
			return "{}";
		}
		StringBuilder sb = new StringBuilder("{");
		int i = 0;
		for (Map.Entry<String, Integer> e : map.entrySet()) {
			if (i++ > 0) {
				sb.append(", ");
			}
			sb.append('"').append(escapeJson(e.getKey())).append("\": ").append(e.getValue());
		}
		sb.append('}');
		return sb.toString();
	}

	private static String escapeJson(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
