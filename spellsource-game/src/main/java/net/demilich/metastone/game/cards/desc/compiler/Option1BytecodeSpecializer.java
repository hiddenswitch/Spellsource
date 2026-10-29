package net.demilich.metastone.game.cards.desc.compiler;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterArg;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ConstValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;

import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.Label;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.IncrementInstruction;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LabelTarget;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.LookupSwitchInstruction;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.lang.classfile.instruction.ThrowInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bytecode partial evaluator and dead-branch pruner for Option 1 component methods
 * ({@code Spell.onCast}, {@code EntityFilter.test}, {@code ValueProvider.provideValue}, {@code Condition.isFulfilled}).
 * <p>
 * Keeps expressive {@code .java} component classes 100% free of compiler concerns while specializing each Option 1
 * method's bytecode for a concrete read-only {@link Desc}:
 * <ul>
 *   <li>Substitutes known constant parameters ({@code null}, {@code int}, {@code boolean}, {@code Integer},
 *       {@code String}, {@code Enum}, {@code EntityReference}, {@code EntityFilter}, {@code Condition},
 *       {@code ValueProvider}, {@code SpellDesc}) directly into the instruction stream.</li>
 *   <li>Propagates single-store local variable constants (such as {@code int totalArmorBonus = armorBonus} in
 *       {@code BuffSpell}).</li>
 *   <li>Constant-folds conditional branches ({@code ifnull}, {@code ifnonnull}, {@code ifeq..ifle},
 *       {@code if_acmpeq}, {@code if_acmpne}, {@code if_icmpeq..if_icmple}, {@code Integer.intValue()},
 *       {@code String.equals}, {@code Desc.containsKey}) and strips all unreachable basic blocks.</li>
 *   <li>Inlines the resulting pruned bytecode directly into the hidden class method without a wide 10-to-15-argument
 *       {@code invokevirtual} call.</li>
 * </ul>
 */
final class Option1BytecodeSpecializer {

	private static final ConcurrentHashMap<Class<?>, ParsedMethod> PARSED_METHODS = new ConcurrentHashMap<>(64);

	private record ParsedMethod(
			int maxLocals,
			List<CodeElement> rawElements
	) {
	}

	private enum ValKind {
		NULL,
		INT,
		BOXED_INT,
		STRING,
		REF
	}

	private record KnownVal(ValKind kind, int intVal, Object refVal, ClassDesc refDesc) {
		static final KnownVal NULL = new KnownVal(ValKind.NULL, 0, null, null);

		static KnownVal ofInt(int v) {
			return new KnownVal(ValKind.INT, v, null, ConstantDescs.CD_int);
		}

		static KnownVal ofBoxedInt(int v) {
			return new KnownVal(ValKind.BOXED_INT, v, Integer.valueOf(v), ConstantDescs.CD_Integer);
		}

		static KnownVal ofString(String s) {
			return new KnownVal(ValKind.STRING, 0, s, ConstantDescs.CD_String);
		}

		static KnownVal ofRef(Object ref, ClassDesc desc) {
			return new KnownVal(ValKind.REF, 0, ref, desc);
		}

		boolean isNull() {
			return kind == ValKind.NULL;
		}

		boolean isNonNull() {
			return kind == ValKind.BOXED_INT || kind == ValKind.STRING || kind == ValKind.REF;
		}
	}

	private sealed interface IrNode {
		record LabelNode(Label label) implements IrNode {
		}

		record GotoNode(Label target) implements IrNode {
		}

		record ConstLoadNode(KnownVal val) implements IrNode {
		}

		record CompareOpNode(ComparisonOperation op) implements IrNode {
		}

		record StaticCallNode(ClassDesc owner, String name, MethodTypeDesc desc) implements IrNode {
		}

		record InlineVpNode(ValueProvider vp) implements IrNode {
		}

		record RawInsnNode(Instruction insn) implements IrNode {
		}
	}

	private Option1BytecodeSpecializer() {
	}

	private static final ConcurrentHashMap<String, int[]> SWITCH_MAP_CACHE = new ConcurrentHashMap<>();

	private static int[] getSwitchMap(String ownerInternalName, String fieldName) {
		String key = ownerInternalName + "." + fieldName;
		return SWITCH_MAP_CACHE.computeIfAbsent(key, k -> {
			try {
				Class<?> cls = Class.forName(ownerInternalName.replace('/', '.'));
				java.lang.reflect.Field f = cls.getDeclaredField(fieldName);
				f.setAccessible(true);
				return (int[]) f.get(null);
			} catch (Throwable t) {
				return new int[0];
			}
		});
	}

	private static ParsedMethod getParsedMethod(Class<?> clazz, DescBytecodeCompiler.Option1MethodSpec spec) {
		return PARSED_METHODS.computeIfAbsent(clazz, c -> parseMethod(c, spec));
	}

	private static ParsedMethod parseMethod(Class<?> clazz, DescBytecodeCompiler.Option1MethodSpec spec) {
		String resourcePath = clazz.getName().replace('.', '/') + ".class";
		try (InputStream is = clazz.getClassLoader().getResourceAsStream(resourcePath)) {
			if (is == null) {
				return new ParsedMethod(-1, List.of());
			}
			byte[] bytes = is.readAllBytes();
			ClassModel cm = ClassFile.of().parse(bytes);
			for (MethodModel mm : cm.methods()) {
				if (mm.methodName().equalsString(spec.methodName())
						&& mm.methodTypeSymbol().equals(spec.methodTypeDesc())) {
					CodeModel code = mm.code().orElse(null);
					if (code == null || !code.exceptionHandlers().isEmpty()) {
						return new ParsedMethod(-1, List.of());
					}
					boolean hasDesc = spec.hasDescInPrefix();
					int maxLocals = (hasDesc ? 6 : 5) + spec.params().size();
					List<CodeElement> filtered = new ArrayList<>();
					for (CodeElement ce : code.elementList()) {
						if (ce instanceof Instruction || ce instanceof LabelTarget) {
							filtered.add(ce);
							if (ce instanceof LoadInstruction li) {
								maxLocals = Math.max(maxLocals, li.slot() + li.typeKind().slotSize());
							} else if (ce instanceof StoreInstruction si) {
								maxLocals = Math.max(maxLocals, si.slot() + si.typeKind().slotSize());
							} else if (ce instanceof IncrementInstruction iinc) {
								maxLocals = Math.max(maxLocals, iinc.slot() + 1);
							}
						}
					}
					return new ParsedMethod(maxLocals, List.copyOf(filtered));
				}
			}
		} catch (Throwable ignored) {
		}
		return new ParsedMethod(-1, List.of());
	}

	static boolean tryEmitSpecialized(
			CodeBuilder cob,
			DescBytecodeCompiler.Option1MethodSpec spec,
			Object instance,
			Desc<?, ?> desc,
			boolean isTopLevelSpell,
			List<Object> classData,
			int playerSlot,
			int callerArgASlot,
			int callerArgBSlot,
			int vpTargetSlot,
			int vpHostSlot,
			int tempSlotBase,
			int depth) {
		Class<?> clazz = desc.getDescClass();
		if (clazz == null) {
			return false;
		}
		ParsedMethod parsed = getParsedMethod(clazz, spec);
		if (parsed.maxLocals() < 0 || parsed.rawElements().isEmpty()) {
			return false;
		}

		final int maxLocals = parsed.maxLocals();
		final boolean hasDesc = spec.hasDescInPrefix();
		final int origArgASlot = hasDesc ? 4 : 3;
		final int origArgBSlot = hasDesc ? 5 : 4;
		final int prefixSlots = hasDesc ? 6 : 5;
		final List<DescBytecodeCompiler.Option1ParamSpec> params = spec.params();
		final int numExtra = params.size();

		if (prefixSlots + numExtra > maxLocals) {
			return false;
		}

		// 1. Build initial KnownVal table for slots known at entry
		KnownVal[] entryKnownVal = new KnownVal[maxLocals];
		if (callerArgASlot == 0) {
			entryKnownVal[origArgASlot] = KnownVal.NULL;
		}
		if (callerArgBSlot == 0) {
			entryKnownVal[origArgBSlot] = KnownVal.NULL;
		}

		for (int i = 0; i < numExtra; i++) {
			int slot = prefixSlots + i;
			DescBytecodeCompiler.Option1ParamSpec p = params.get(i);
			switch (p.kind()) {
				case INT_VALUE -> {
					Object raw = desc.get(p.argKey());
					if (raw == null) {
						entryKnownVal[slot] = KnownVal.ofInt(p.defaultInt());
					} else if (raw instanceof Number num) {
						entryKnownVal[slot] = KnownVal.ofInt(num.intValue());
					} else if (!(raw instanceof ValueProvider)) {
						entryKnownVal[slot] = KnownVal.ofInt(p.defaultInt());
					}
				}
				case BOXED_INT_VALUE -> {
					Object raw = desc.get(p.argKey());
					if (raw == null) {
						entryKnownVal[slot] = KnownVal.NULL;
					} else if (raw instanceof Number num) {
						entryKnownVal[slot] = KnownVal.ofBoxedInt(num.intValue());
					} else if (!(raw instanceof ValueProvider)) {
						entryKnownVal[slot] = KnownVal.NULL;
					}
				}
				case BOOLEAN -> {
					Object raw = desc.get(p.argKey());
					boolean b = raw instanceof Boolean bv ? bv : p.defaultBool();
					entryKnownVal[slot] = KnownVal.ofInt(b ? 1 : 0);
				}
				case VALUE_PROVIDER -> {
					Object raw = desc.get(p.argKey());
					ValueProvider vp = ConstValueProvider.fromObject(raw);
					entryKnownVal[slot] = vp == null ? KnownVal.NULL : KnownVal.ofRef(vp, DescBytecodeCompiler.CD_VP);
				}
				case STRING -> {
					Object raw = desc.get(p.argKey());
					String str = raw instanceof String s
							? s
							: (p.hasDefault() ? (p.defaultStr() != null ? p.defaultStr() : "") : null);
					entryKnownVal[slot] = str == null ? KnownVal.NULL : KnownVal.ofString(str);
				}
				case ENUM_OR_REF -> {
					Object raw = desc.get(p.argKey());
					if (raw == null) {
						raw = p.defaultRefValue();
					}
					entryKnownVal[slot] = raw == null ? KnownVal.NULL : KnownVal.ofRef(raw, p.paramDesc());
				}
				case MIXIN_CARD -> {
					if (!desc.containsKey(SpellArg.CARD)) {
						entryKnownVal[slot] = KnownVal.NULL;
					}
				}
				default -> {
				}
			}
		}

		// 2. Convert raw elements to mutable IrNode list
		List<IrNode> nodes = new ArrayList<>(parsed.rawElements().size());
		for (CodeElement ce : parsed.rawElements()) {
			if (ce instanceof LabelTarget lt) {
				nodes.add(new IrNode.LabelNode(lt.label()));
			} else if (ce instanceof BranchInstruction bi
					&& (bi.opcode() == Opcode.GOTO || bi.opcode() == Opcode.GOTO_W)) {
				nodes.add(new IrNode.GotoNode(bi.target()));
			} else if (ce instanceof Instruction insn) {
				nodes.add(new IrNode.RawInsnNode(insn));
			}
		}

		// 3. Fixed-point partial evaluation, constant propagation, and dead-branch pruning loop
		boolean[] nonNullDynamicBoxedInt = new boolean[maxLocals];
		for (int i = 0; i < numExtra; i++) {
			DescBytecodeCompiler.Option1ParamSpec p = params.get(i);
			if (p.kind() == DescBytecodeCompiler.Option1ParamKind.BOXED_INT_VALUE
					&& desc.get(p.argKey()) instanceof ValueProvider) {
				nonNullDynamicBoxedInt[prefixSlots + i] = true;
			}
		}

		for (int iter = 0; iter < 8; iter++) {
			boolean changed = false;
			changed |= cleanupJumpChainsAndFallthroughs(nodes);
			changed |= pruneUnreachableNodes(nodes);
			changed |= propagateConstantsAndPruneDeadStores(nodes, entryKnownVal, maxLocals, prefixSlots, numExtra);
			changed |= foldConstantBranches(nodes, desc, hasDesc, spec.kind(), nonNullDynamicBoxedInt);
			if (!changed) {
				break;
			}
		}
		cleanupJumpChainsAndFallthroughs(nodes);
		pruneUnreachableNodes(nodes);

		if (nodes.isEmpty() || nodes.size() > 55 || hasBackwardBranch(nodes)) {
			return false;
		}

		// 4. Validate remaining instructions before touching CodeBuilder
		for (int i = 0, n = nodes.size(); i < n; i++) {
			IrNode node = nodes.get(i);
			if (node instanceof IrNode.RawInsnNode(Instruction insn)) {
				if (insn instanceof InvokeDynamicInstruction idi) {
					if (!idi.bootstrapMethod().owner().descriptorString().equals("Ljava/lang/invoke/StringConcatFactory;")) {
						return false;
					}
				} else if (insn instanceof InvokeInstruction ii && ii.opcode() == Opcode.INVOKESPECIAL) {
					String name = ii.name().stringValue();
					if (!name.equals("<init>")) {
						String ownerInt = ii.owner().asInternalName();
						boolean handledSuper = (ownerInt.equals("net/demilich/metastone/game/spells/desc/condition/CountCondition") && name.equals("isFulfilled"))
								|| ii.owner().asSymbol().equals(spec.ownerDesc());
						if (!handledSuper) {
							return false;
						}
					}
				}
			}
		}

		// 5. Analyze which local variable slots are still referenced or mutated in pruned nodes
		// First: propagate single-store local variable copies of unmutated parameter slots (e.g. `int targetValue = value;`)
		propagateParameterCopySlots(nodes, maxLocals, prefixSlots, numExtra);

		// Second: if a dynamic BOXED_INT_VALUE parameter slot s is never mutated and every remaining load of s
		// is immediately followed by `invokevirtual Integer.intValue()I`, unbox slot s so we store/load primitive `int`
		// directly without `Integer.valueOf` boxing or `Integer.intValue()` unboxing!
		boolean[] unboxedDynamicIntSlot = new boolean[maxLocals];
		for (int i = 0; i < numExtra; i++) {
			int origSlot = prefixSlots + i;
			DescBytecodeCompiler.Option1ParamSpec p = params.get(i);
			if (p.kind() == DescBytecodeCompiler.Option1ParamKind.BOXED_INT_VALUE
					&& entryKnownVal[origSlot] == null
					&& desc.get(p.argKey()) instanceof ValueProvider) {
				boolean canUnbox = true;
				int loadCount = 0;
				for (int j = 0; j < nodes.size(); j++) {
					IrNode n = nodes.get(j);
					if (n instanceof IrNode.RawInsnNode(StoreInstruction si) && si.slot() == origSlot) {
						canUnbox = false;
						break;
					}
					if (n instanceof IrNode.RawInsnNode(LoadInstruction li) && li.slot() == origSlot) {
						loadCount++;
						if (j + 1 >= nodes.size()
								|| !(nodes.get(j + 1) instanceof IrNode.RawInsnNode(InvokeInstruction ii))
								|| ii.opcode() != Opcode.INVOKEVIRTUAL
								|| !ii.owner().asInternalName().equals("java/lang/Integer")
								|| !ii.name().equalsString("intValue")
								|| !ii.type().equalsString("()I")) {
							canUnbox = false;
							break;
						}
					}
				}
				if (canUnbox && loadCount > 0) {
					unboxedDynamicIntSlot[origSlot] = true;
					for (int j = nodes.size() - 2; j >= 0; j--) {
						if (nodes.get(j) instanceof IrNode.RawInsnNode(LoadInstruction li) && li.slot() == origSlot) {
							nodes.remove(j + 1); // remove Integer.intValue()I
						}
					}
				}
			}
		}

		// Re-run parameter copy slot propagation in case unboxing `value.intValue()` unlocked `int targetValue = value;`
		propagateParameterCopySlots(nodes, maxLocals, prefixSlots, numExtra);

		boolean[] slotLoaded = new boolean[maxLocals];
		boolean[] slotMutated = new boolean[maxLocals];
		boolean[] slotWide = new boolean[maxLocals];

		for (int i = 0, n = nodes.size(); i < n; i++) {
			IrNode node = nodes.get(i);
			if (node instanceof IrNode.RawInsnNode(Instruction insn)) {
				if (insn instanceof LoadInstruction li) {
					int s = li.slot();
					slotLoaded[s] = true;
					if (li.typeKind().slotSize() == 2) {
						slotWide[s] = true;
					}
				} else if (insn instanceof StoreInstruction si) {
					int s = si.slot();
					slotMutated[s] = true;
					if (si.typeKind().slotSize() == 2) {
						slotWide[s] = true;
					}
				} else if (insn instanceof IncrementInstruction ii) {
					int s = ii.slot();
					slotLoaded[s] = true;
					slotMutated[s] = true;
				}
			}
		}

		// 6. Allocate caller local slots for dynamic/mutated parameters and local variables
		int nextCallerSlot = tempSlotBase + depth;
		int[] mappedSlot = new int[maxLocals];
		Arrays.fill(mappedSlot, -1);

		// Prefix slots (1=context, 2=player, 3=desc if hasDesc, origArgASlot, origArgBSlot)
		if (slotMutated[1]) {
			mappedSlot[1] = nextCallerSlot++;
			cob.aload(1).astore(mappedSlot[1]);
		} else {
			mappedSlot[1] = 1;
		}
		if (slotMutated[2]) {
			mappedSlot[2] = nextCallerSlot++;
			cob.aload(playerSlot).astore(mappedSlot[2]);
		} else {
			mappedSlot[2] = playerSlot;
		}
		if (hasDesc && slotMutated[3]) {
			mappedSlot[3] = nextCallerSlot++;
			emitLoadDesc(cob, spec.kind(), desc, isTopLevelSpell, classData);
			cob.astore(mappedSlot[3]);
		}
		if (slotMutated[origArgASlot]) {
			mappedSlot[origArgASlot] = nextCallerSlot++;
			DescBytecodeCompiler.loadSlotOrNull(cob, callerArgASlot);
			cob.astore(mappedSlot[origArgASlot]);
		} else {
			mappedSlot[origArgASlot] = callerArgASlot;
		}
		if (slotMutated[origArgBSlot]) {
			mappedSlot[origArgBSlot] = nextCallerSlot++;
			DescBytecodeCompiler.loadSlotOrNull(cob, callerArgBSlot);
			cob.astore(mappedSlot[origArgBSlot]);
		} else {
			mappedSlot[origArgBSlot] = callerArgBSlot;
		}

		// Extra parameter slots: evaluate only if referenced (loaded or mutated) in the pruned bytecode!
		for (int i = 0; i < numExtra; i++) {
			int origSlot = prefixSlots + i;
			if (!slotLoaded[origSlot] && !slotMutated[origSlot]) {
				continue;
			}
			int destSlot = nextCallerSlot++;
			mappedSlot[origSlot] = destSlot;
			DescBytecodeCompiler.Option1ParamSpec param = params.get(i);
			emitParamInitIntoSlot(
					cob,
					param,
					entryKnownVal[origSlot],
					unboxedDynamicIntSlot[origSlot],
					instance,
					desc,
					isTopLevelSpell,
					classData,
					playerSlot,
					callerArgASlot,
					callerArgBSlot,
					vpTargetSlot,
					vpHostSlot,
					nextCallerSlot,
					0,
					destSlot);
		}

		// Method body local variable slots
		for (int origSlot = prefixSlots + numExtra; origSlot < maxLocals; origSlot++) {
			if (slotLoaded[origSlot] || slotMutated[origSlot]) {
				mappedSlot[origSlot] = nextCallerSlot;
				nextCallerSlot += slotWide[origSlot] ? 2 : 1;
			}
		}

		// 7. Emit specialized instructions into CodeBuilder
		IdentityHashMap<Label, Label> labelMap = new IdentityHashMap<>();
		Label endLabel = cob.newLabel();
		int lastIdx = nodes.size() - 1;

		for (int i = 0; i <= lastIdx; i++) {
			IrNode node = nodes.get(i);
			switch (node) {
				case IrNode.LabelNode(Label lbl) -> cob.labelBinding(mapLabel(lbl, labelMap, cob));
				case IrNode.GotoNode(Label target) -> cob.goto_(mapLabel(target, labelMap, cob));
				case IrNode.ConstLoadNode(KnownVal kv) -> emitKnownVal(cob, kv, classData);
				case IrNode.CompareOpNode(ComparisonOperation op) -> emitCompareOp(cob, op);
				case IrNode.StaticCallNode(ClassDesc owner, String name, MethodTypeDesc mtd) -> cob.invokestatic(owner, name, mtd);
				case IrNode.InlineVpNode(ValueProvider vp) ->
						DescBytecodeCompiler.emitInlineVP(cob, vp, classData, playerSlot, vpTargetSlot, vpHostSlot, nextCallerSlot, 0);
				case IrNode.RawInsnNode(Instruction insn) -> {
					if (insn instanceof LoadInstruction li) {
						int s = li.slot();
						if (s == 0) {
							int instIdx = DescBytecodeCompiler.addConstant(classData, instance);
							DescBytecodeCompiler.loadClassData(cob, instIdx, spec.ownerDesc());
						} else if (hasDesc && s == 3 && !slotMutated[3]) {
							emitLoadDesc(cob, spec.kind(), desc, isTopLevelSpell, classData);
						} else if (s == origArgASlot && !slotMutated[origArgASlot]) {
							DescBytecodeCompiler.loadSlotOrNull(cob, callerArgASlot);
						} else if (s == origArgBSlot && !slotMutated[origArgBSlot]) {
							DescBytecodeCompiler.loadSlotOrNull(cob, callerArgBSlot);
						} else if (s < unboxedDynamicIntSlot.length && unboxedDynamicIntSlot[s]) {
							cob.iload(mappedSlot[s]);
						} else {
							cob.loadLocal(li.typeKind(), mappedSlot[s]);
						}
					} else if (insn instanceof StoreInstruction si) {
						cob.storeLocal(si.typeKind(), mappedSlot[si.slot()]);
					} else if (insn instanceof IncrementInstruction ii) {
						cob.iinc(mappedSlot[ii.slot()], ii.constant());
					} else if (insn instanceof BranchInstruction bi) {
						cob.branch(bi.opcode(), mapLabel(bi.target(), labelMap, cob));
					} else if (insn instanceof LookupSwitchInstruction lsi) {
						List<SwitchCase> remappedCases = new ArrayList<>(lsi.cases().size());
						for (SwitchCase sc : lsi.cases()) {
							remappedCases.add(SwitchCase.of(sc.caseValue(), mapLabel(sc.target(), labelMap, cob)));
						}
						cob.lookupswitch(mapLabel(lsi.defaultTarget(), labelMap, cob), remappedCases);
					} else if (insn instanceof TableSwitchInstruction tsi) {
						List<SwitchCase> remappedCases = new ArrayList<>(tsi.cases().size());
						for (SwitchCase sc : tsi.cases()) {
							remappedCases.add(SwitchCase.of(sc.caseValue(), mapLabel(sc.target(), labelMap, cob)));
						}
						cob.tableswitch(tsi.lowValue(), tsi.highValue(), mapLabel(tsi.defaultTarget(), labelMap, cob), remappedCases);
					} else if (insn instanceof ReturnInstruction) {
						if (i != lastIdx) {
							cob.goto_(endLabel);
						}
					} else if (insn instanceof InvokeInstruction ii && ii.opcode() == Opcode.INVOKESPECIAL && !ii.name().equalsString("<init>")) {
						String name = ii.name().stringValue();
						cob.invokevirtual(ii.owner().asSymbol(), name, ii.typeSymbol());
					} else {
						cob.with(insn);
					}
				}
			}
		}

		cob.labelBinding(endLabel);
		return true;
	}

	private static Label mapLabel(Label orig, IdentityHashMap<Label, Label> labelMap, CodeBuilder cob) {
		return labelMap.computeIfAbsent(orig, k -> cob.newLabel());
	}

	private static void emitLoadDesc(
			CodeBuilder cob,
			ComponentSignatureAnalyzer.ComponentKind kind,
			Desc<?, ?> desc,
			boolean isTopLevelSpell,
			List<Object> classData) {
		switch (kind) {
			case SPELL -> DescBytecodeCompiler.emitLoadSpellDesc(cob, (SpellDesc) desc, isTopLevelSpell, classData);
			case ENTITY_FILTER -> {
				int idx = DescBytecodeCompiler.addConstant(classData, desc);
				DescBytecodeCompiler.loadClassData(cob, idx, DescBytecodeCompiler.CD_EF_DESC);
			}
			case VALUE_PROVIDER -> {
				int idx = DescBytecodeCompiler.addConstant(classData, desc);
				DescBytecodeCompiler.loadClassData(cob, idx, DescBytecodeCompiler.CD_VP_DESC);
			}
			case CONDITION -> {
				int idx = DescBytecodeCompiler.addConstant(classData, desc);
				DescBytecodeCompiler.loadClassData(cob, idx, DescBytecodeCompiler.CD_COND_DESC);
			}
			default -> throw new IllegalStateException("Unexpected kind: " + kind);
		}
	}

	private static void emitKnownVal(CodeBuilder cob, KnownVal kv, List<Object> classData) {
		switch (kv.kind()) {
			case NULL -> cob.aconst_null();
			case INT -> cob.ldc(kv.intVal());
			case STRING -> cob.ldc((String) kv.refVal());
			case BOXED_INT, REF -> {
				if (kv.refVal() instanceof Enum<?> enumConst) {
					ClassDesc enumDesc = ClassDesc.ofDescriptor(enumConst.getDeclaringClass().descriptorString());
					cob.getstatic(enumDesc, enumConst.name(), enumDesc);
				} else {
					int idx = DescBytecodeCompiler.addConstant(classData, kv.refVal());
					DescBytecodeCompiler.loadClassData(cob, idx, kv.refDesc());
				}
			}
		}
	}

	private static void emitCompareOp(CodeBuilder cob, ComparisonOperation op) {
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

	private static boolean hasBackwardBranch(List<IrNode> nodes) {
		Map<Label, Integer> labelIdx = buildLabelIndexMap(nodes);
		for (int i = 0, n = nodes.size(); i < n; i++) {
			IrNode node = nodes.get(i);
			if (node instanceof IrNode.GotoNode(Label target)) {
				Integer tIdx = labelIdx.get(target);
				if (tIdx != null && tIdx <= i) {
					return true;
				}
			} else if (node instanceof IrNode.RawInsnNode(BranchInstruction bi)) {
				Integer tIdx = labelIdx.get(bi.target());
				if (tIdx != null && tIdx <= i) {
					return true;
				}
			}
		}
		return false;
	}

	private static void propagateParameterCopySlots(List<IrNode> nodes, int maxLocals, int prefixSlots, int numExtra) {
		int[] storeCount = new int[maxLocals];
		int[] incCount = new int[maxLocals];
		int[] singleStoreIdx = new int[maxLocals];
		Arrays.fill(singleStoreIdx, -1);

		for (int i = 0; i < nodes.size(); i++) {
			IrNode node = nodes.get(i);
			if (node instanceof IrNode.RawInsnNode(StoreInstruction si)) {
				int s = si.slot();
				storeCount[s]++;
				singleStoreIdx[s] = i;
			} else if (node instanceof IrNode.RawInsnNode(IncrementInstruction ii)) {
				incCount[ii.slot()]++;
			}
		}

		for (int s = prefixSlots + numExtra; s < maxLocals; s++) {
			if (storeCount[s] == 1 && incCount[s] == 0) {
				int sIdx = singleStoreIdx[s];
				if (sIdx > 0 && nodes.get(sIdx - 1) instanceof IrNode.RawInsnNode(LoadInstruction srcLoad)) {
					int srcSlot = srcLoad.slot();
					if (srcSlot >= prefixSlots && srcSlot < prefixSlots + numExtra
							&& storeCount[srcSlot] == 0 && incCount[srcSlot] == 0) {
						for (int j = sIdx + 1; j < nodes.size(); j++) {
							if (nodes.get(j) instanceof IrNode.RawInsnNode(LoadInstruction dstLoad) && dstLoad.slot() == s) {
								nodes.set(j, new IrNode.RawInsnNode(srcLoad));
							}
						}
						nodes.remove(sIdx);
						nodes.remove(sIdx - 1);
						return;
					}
				}
			}
		}
	}

	private static void emitParamInitIntoSlot(
			CodeBuilder cob,
			DescBytecodeCompiler.Option1ParamSpec param,
			KnownVal knownVal,
			boolean unboxedDynamicInt,
			Object instance,
			Desc<?, ?> desc,
			boolean isTopLevelSpell,
			List<Object> classData,
			int playerSlot,
			int callerArgASlot,
			int callerArgBSlot,
			int vpTargetSlot,
			int vpHostSlot,
			int tempSlotBase,
			int depth,
			int destSlot) {
		if (knownVal != null) {
			emitKnownVal(cob, knownVal, classData);
			if (knownVal.kind() == ValKind.INT) {
				cob.istore(destSlot);
			} else {
				cob.astore(destSlot);
			}
			return;
		}
		// Dynamic parameter evaluation:
		// Note: for SPELL/CONDITION, callerArgASlot is source, callerArgBSlot is target;
		// for ENTITY_FILTER/VALUE_PROVIDER, callerArgASlot is entity/target, callerArgBSlot is host.
		switch (param.kind()) {
			case MIXIN_CARDS -> {
				cob.aload(1)
						.aload(playerSlot);
				DescBytecodeCompiler.loadSlotOrNull(cob, callerArgBSlot); // target
				DescBytecodeCompiler.loadSlotOrNull(cob, callerArgASlot); // source
				DescBytecodeCompiler.emitLoadSpellDesc(cob, (SpellDesc) desc, isTopLevelSpell, classData);
				cob.invokestatic(DescBytecodeCompiler.CD_SPELL_UTILS, "getCards",
								MethodTypeDesc.of(DescBytecodeCompiler.CD_CARD_LIST,
										DescBytecodeCompiler.CD_GAME_CONTEXT,
										DescBytecodeCompiler.CD_PLAYER,
										DescBytecodeCompiler.CD_ENTITY,
										DescBytecodeCompiler.CD_ENTITY,
										DescBytecodeCompiler.CD_SPELL_DESC))
						.astore(destSlot);
			}
			case MIXIN_CARD -> {
				cob.aload(1);
				DescBytecodeCompiler.emitLoadSpellDesc(cob, (SpellDesc) desc, isTopLevelSpell, classData);
				cob.invokestatic(DescBytecodeCompiler.CD_SPELL_UTILS, "getCard",
								MethodTypeDesc.of(DescBytecodeCompiler.CD_CARD,
										DescBytecodeCompiler.CD_GAME_CONTEXT,
										DescBytecodeCompiler.CD_SPELL_DESC))
						.astore(destSlot);
			}
			case MIXIN_BOARD_POSITION -> {
				cob.aload(1)
						.aload(playerSlot);
				DescBytecodeCompiler.emitLoadSpellDesc(cob, (SpellDesc) desc, isTopLevelSpell, classData);
				DescBytecodeCompiler.loadSlotOrNull(cob, callerArgASlot); // source
				cob.invokestatic(DescBytecodeCompiler.CD_SPELL_UTILS, "getBoardPosition",
								MethodTypeDesc.of(ConstantDescs.CD_int,
										DescBytecodeCompiler.CD_GAME_CONTEXT,
										DescBytecodeCompiler.CD_PLAYER,
										DescBytecodeCompiler.CD_SPELL_DESC,
										DescBytecodeCompiler.CD_ENTITY))
						.istore(destSlot);
			}
			case MIXIN_TARGETED_ENTITIES -> {
				int efIdx = DescBytecodeCompiler.addConstant(classData, instance);
				DescBytecodeCompiler.loadClassData(cob, efIdx, DescBytecodeCompiler.CD_EF);
				cob.aload(1)
						.aload(playerSlot);
				DescBytecodeCompiler.loadSlotOrNull(cob, callerArgBSlot); // host
				cob.invokevirtual(DescBytecodeCompiler.CD_EF, "getTargetedEntities",
								MethodTypeDesc.of(DescBytecodeCompiler.CD_LIST,
										DescBytecodeCompiler.CD_GAME_CONTEXT,
										DescBytecodeCompiler.CD_PLAYER,
										DescBytecodeCompiler.CD_ENTITY))
						.astore(destSlot);
			}
			case INT_VALUE -> {
				ValueProvider vp = (ValueProvider) desc.get(param.argKey());
				DescBytecodeCompiler.emitInlineVP(cob, vp, classData, playerSlot, vpTargetSlot, vpHostSlot, tempSlotBase, depth);
				cob.istore(destSlot);
			}
			case BOXED_INT_VALUE -> {
				ValueProvider vp = (ValueProvider) desc.get(param.argKey());
				DescBytecodeCompiler.emitInlineVP(cob, vp, classData, playerSlot, vpTargetSlot, vpHostSlot, tempSlotBase, depth);
				if (unboxedDynamicInt) {
					cob.istore(destSlot);
				} else {
					cob.invokestatic(ConstantDescs.CD_Integer, "valueOf",
									MethodTypeDesc.of(ConstantDescs.CD_Integer, ConstantDescs.CD_int))
							.astore(destSlot);
				}
			}
			default -> throw new IllegalStateException("Unexpected dynamic param kind: " + param.kind());
		}
	}

	private static boolean isLoadOfSlot(IrNode node, int slot) {
		return node instanceof IrNode.RawInsnNode(LoadInstruction li) && li.slot() == slot;
	}

	private static boolean cleanupJumpChainsAndFallthroughs(List<IrNode> nodes) {
		boolean changed = false;
		Map<Label, Integer> labelIdx = buildLabelIndexMap(nodes);

		// Redirect goto/branch to label that immediately executes a GotoNode
		for (int i = 0, n = nodes.size(); i < n; i++) {
			IrNode node = nodes.get(i);
			if (node instanceof IrNode.GotoNode(Label target)) {
				Label resolved = resolveJumpTarget(target, nodes, labelIdx);
				if (resolved != target) {
					nodes.set(i, new IrNode.GotoNode(resolved));
					changed = true;
				}
			}
		}

		// Remove GotoNode(L) when L immediately follows (only LabelNodes between GotoNode and LabelNode(L))
		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (nodes.get(i) instanceof IrNode.GotoNode(Label target)) {
				boolean fallsThrough = false;
				for (int j = i + 1, n = nodes.size(); j < n; j++) {
					if (nodes.get(j) instanceof IrNode.LabelNode(Label lbl)) {
						if (lbl == target) {
							fallsThrough = true;
							break;
						}
					} else {
						break;
					}
				}
				if (fallsThrough) {
					nodes.remove(i);
					changed = true;
				}
			}
		}
		return changed;
	}

	private static Label resolveJumpTarget(Label target, List<IrNode> nodes, Map<Label, Integer> labelIdx) {
		Label curr = target;
		for (int hop = 0; hop < 6; hop++) {
			Integer idx = labelIdx.get(curr);
			if (idx == null) {
				break;
			}
			IrNode nextInsn = null;
			for (int j = idx + 1, n = nodes.size(); j < n; j++) {
				IrNode nj = nodes.get(j);
				if (!(nj instanceof IrNode.LabelNode)) {
					nextInsn = nj;
					break;
				}
			}
			if (nextInsn instanceof IrNode.GotoNode(Label nextTarget) && nextTarget != curr) {
				curr = nextTarget;
			} else {
				break;
			}
		}
		return curr;
	}

	private static Map<Label, Integer> buildLabelIndexMap(List<IrNode> nodes) {
		Map<Label, Integer> map = new IdentityHashMap<>();
		for (int i = 0, n = nodes.size(); i < n; i++) {
			if (nodes.get(i) instanceof IrNode.LabelNode(Label lbl)) {
				map.put(lbl, i);
			}
		}
		return map;
	}

	private static boolean pruneUnreachableNodes(List<IrNode> nodes) {
		int n = nodes.size();
		if (n == 0) {
			return false;
		}
		Map<Label, Integer> labelIdx = buildLabelIndexMap(nodes);
		boolean[] reachable = new boolean[n];
		ArrayDeque<Integer> queue = new ArrayDeque<>();
		reachable[0] = true;
		queue.add(0);

		while (!queue.isEmpty()) {
			int idx = queue.poll();
			IrNode node = nodes.get(idx);
			switch (node) {
				case IrNode.GotoNode(Label target) -> {
					Integer tIdx = labelIdx.get(target);
					if (tIdx != null && !reachable[tIdx]) {
						reachable[tIdx] = true;
						queue.add(tIdx);
					}
				}
				case IrNode.RawInsnNode(BranchInstruction bi) -> {
					if (idx + 1 < n && !reachable[idx + 1]) {
						reachable[idx + 1] = true;
						queue.add(idx + 1);
					}
					Integer tIdx = labelIdx.get(bi.target());
					if (tIdx != null && !reachable[tIdx]) {
						reachable[tIdx] = true;
						queue.add(tIdx);
					}
				}
				case IrNode.RawInsnNode(LookupSwitchInstruction lsi) -> {
					Integer dIdx = labelIdx.get(lsi.defaultTarget());
					if (dIdx != null && !reachable[dIdx]) {
						reachable[dIdx] = true;
						queue.add(dIdx);
					}
					for (SwitchCase sc : lsi.cases()) {
						Integer cIdx = labelIdx.get(sc.target());
						if (cIdx != null && !reachable[cIdx]) {
							reachable[cIdx] = true;
							queue.add(cIdx);
						}
					}
				}
				case IrNode.RawInsnNode(TableSwitchInstruction tsi) -> {
					Integer dIdx = labelIdx.get(tsi.defaultTarget());
					if (dIdx != null && !reachable[dIdx]) {
						reachable[dIdx] = true;
						queue.add(dIdx);
					}
					for (SwitchCase sc : tsi.cases()) {
						Integer cIdx = labelIdx.get(sc.target());
						if (cIdx != null && !reachable[cIdx]) {
							reachable[cIdx] = true;
							queue.add(cIdx);
						}
					}
				}
				case IrNode.RawInsnNode(ReturnInstruction ignored) -> {
				}
				case IrNode.RawInsnNode(ThrowInstruction ignored) -> {
				}
				default -> {
					if (idx + 1 < n && !reachable[idx + 1]) {
						reachable[idx + 1] = true;
						queue.add(idx + 1);
					}
				}
			}
		}

		// Collect live branch target labels from reachable instructions
		Set<Label> liveLabels = new HashSet<>();
		for (int i = 0; i < n; i++) {
			if (!reachable[i]) {
				continue;
			}
			IrNode node = nodes.get(i);
			if (node instanceof IrNode.GotoNode(Label target)) {
				liveLabels.add(target);
			} else if (node instanceof IrNode.RawInsnNode(BranchInstruction bi)) {
				liveLabels.add(bi.target());
			} else if (node instanceof IrNode.RawInsnNode(LookupSwitchInstruction lsi)) {
				liveLabels.add(lsi.defaultTarget());
				for (SwitchCase sc : lsi.cases()) {
					liveLabels.add(sc.target());
				}
			} else if (node instanceof IrNode.RawInsnNode(TableSwitchInstruction tsi)) {
				liveLabels.add(tsi.defaultTarget());
				for (SwitchCase sc : tsi.cases()) {
					liveLabels.add(sc.target());
				}
			}
		}

		List<IrNode> pruned = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			IrNode node = nodes.get(i);
			if (node instanceof IrNode.LabelNode(Label lbl)) {
				if (liveLabels.contains(lbl)) {
					pruned.add(node);
				}
			} else if (reachable[i]) {
				pruned.add(node);
			}
		}

		if (pruned.size() != n) {
			nodes.clear();
			nodes.addAll(pruned);
			return true;
		}
		return false;
	}

	private static boolean propagateConstantsAndPruneDeadStores(
			List<IrNode> nodes,
			KnownVal[] entryKnownVal,
			int maxLocals,
			int prefixSlots,
			int numExtra) {
		boolean changed = false;
		int n = nodes.size();
		Map<Label, Integer> labelIdx = buildLabelIndexMap(nodes);

		int[] storeCount = new int[maxLocals];
		int[] incCount = new int[maxLocals];
		int[] singleStoreIdx = new int[maxLocals];
		Arrays.fill(singleStoreIdx, -1);

		for (int i = 0; i < n; i++) {
			IrNode node = nodes.get(i);
			if (node instanceof IrNode.RawInsnNode(StoreInstruction si)) {
				int s = si.slot();
				storeCount[s]++;
				singleStoreIdx[s] = i;
			} else if (node instanceof IrNode.RawInsnNode(IncrementInstruction ii)) {
				incCount[ii.slot()]++;
			}
		}

		KnownVal[] slotVal = Arrays.copyOf(entryKnownVal, maxLocals);

		// Single-store local variable copy propagation (for local slots >= prefixSlots + numExtra)
		for (int s = prefixSlots + numExtra; s < maxLocals; s++) {
			if (storeCount[s] == 1 && incCount[s] == 0) {
				int sIdx = singleStoreIdx[s];
				if (sIdx > 0) {
					IrNode prev = nodes.get(sIdx - 1);
					if (prev instanceof IrNode.ConstLoadNode(KnownVal kv)) {
						slotVal[s] = kv;
					} else if (prev instanceof IrNode.RawInsnNode(ConstantInstruction ci)) {
						Object cv = ci.constantValue();
						if (cv instanceof Integer iv) {
							slotVal[s] = KnownVal.ofInt(iv);
						} else if (cv instanceof String sv) {
							slotVal[s] = KnownVal.ofString(sv);
						}
					}
				}
			}
		}

		// Replace loads of non-dirty slots with ConstLoadNode
		for (int s = 0; s < maxLocals; s++) {
			KnownVal kv = slotVal[s];
			if (kv == null) {
				continue;
			}
			boolean[] dirty = (s >= prefixSlots + numExtra)
					? new boolean[n] // single-store local variable is never dirty
					: computeDirtyPositions(nodes, labelIdx, s);

			for (int i = 0; i < n; i++) {
				if (!dirty[i]
						&& nodes.get(i) instanceof IrNode.RawInsnNode(LoadInstruction li)
						&& li.slot() == s) {
					nodes.set(i, new IrNode.ConstLoadNode(kv));
					changed = true;
				}
			}
		}

		// Prune dead stores to local variables (slots >= prefixSlots + numExtra) that have zero remaining loads/increments
		int[] loadCount = new int[maxLocals];
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i) instanceof IrNode.RawInsnNode(LoadInstruction li)) {
				loadCount[li.slot()]++;
			}
		}
		for (int i = nodes.size() - 1; i >= 1; i--) {
			if (nodes.get(i) instanceof IrNode.RawInsnNode(StoreInstruction si)) {
				int s = si.slot();
				if (s >= prefixSlots + numExtra && storeCount[s] == 1 && incCount[s] == 0 && loadCount[s] == 0) {
					IrNode prev = nodes.get(i - 1);
					if (prev instanceof IrNode.ConstLoadNode || prev instanceof IrNode.RawInsnNode(ConstantInstruction ignored)) {
						nodes.remove(i);
						nodes.remove(i - 1);
						i--;
						changed = true;
					}
				}
			}
		}

		return changed;
	}

	private static boolean[] computeDirtyPositions(List<IrNode> nodes, Map<Label, Integer> labelIdx, int slot) {
		int n = nodes.size();
		boolean[] dirty = new boolean[n];
		ArrayDeque<Integer> queue = new ArrayDeque<>();

		for (int i = 0; i < n; i++) {
			IrNode node = nodes.get(i);
			boolean writesSlot = (node instanceof IrNode.RawInsnNode(StoreInstruction si) && si.slot() == slot)
					|| (node instanceof IrNode.RawInsnNode(IncrementInstruction ii) && ii.slot() == slot);
			if (writesSlot && i + 1 < n && !dirty[i + 1]) {
				dirty[i + 1] = true;
				queue.add(i + 1);
			}
		}

		while (!queue.isEmpty()) {
			int idx = queue.poll();
			IrNode node = nodes.get(idx);
			switch (node) {
				case IrNode.GotoNode(Label target) -> {
					Integer tIdx = labelIdx.get(target);
					if (tIdx != null && !dirty[tIdx]) {
						dirty[tIdx] = true;
						queue.add(tIdx);
					}
				}
				case IrNode.RawInsnNode(BranchInstruction bi) -> {
					if (idx + 1 < n && !dirty[idx + 1]) {
						dirty[idx + 1] = true;
						queue.add(idx + 1);
					}
					Integer tIdx = labelIdx.get(bi.target());
					if (tIdx != null && !dirty[tIdx]) {
						dirty[tIdx] = true;
						queue.add(tIdx);
					}
				}
				case IrNode.RawInsnNode(LookupSwitchInstruction lsi) -> {
					Integer dIdx = labelIdx.get(lsi.defaultTarget());
					if (dIdx != null && !dirty[dIdx]) {
						dirty[dIdx] = true;
						queue.add(dIdx);
					}
					for (SwitchCase sc : lsi.cases()) {
						Integer cIdx = labelIdx.get(sc.target());
						if (cIdx != null && !dirty[cIdx]) {
							dirty[cIdx] = true;
							queue.add(cIdx);
						}
					}
				}
				case IrNode.RawInsnNode(TableSwitchInstruction tsi) -> {
					Integer dIdx = labelIdx.get(tsi.defaultTarget());
					if (dIdx != null && !dirty[dIdx]) {
						dirty[dIdx] = true;
						queue.add(dIdx);
					}
					for (SwitchCase sc : tsi.cases()) {
						Integer cIdx = labelIdx.get(sc.target());
						if (cIdx != null && !dirty[cIdx]) {
							dirty[cIdx] = true;
							queue.add(cIdx);
						}
					}
				}
				case IrNode.RawInsnNode(ReturnInstruction ignored) -> {
				}
				case IrNode.RawInsnNode(ThrowInstruction ignored) -> {
				}
				default -> {
					if (idx + 1 < n && !dirty[idx + 1]) {
						dirty[idx + 1] = true;
						queue.add(idx + 1);
					}
				}
			}
		}
		return dirty;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static boolean foldConstantBranches(
			List<IrNode> nodes,
			Desc<?, ?> desc,
			boolean hasDesc,
			ComponentSignatureAnalyzer.ComponentKind kind,
			boolean[] nonNullDynamicBoxedInt) {
		boolean changed = false;

		for (int i = 0; i < nodes.size(); i++) {
			IrNode n0 = nodes.get(i);

			// Pattern 0: Dynamic non-null BOXED_INT_VALUE (ValueProvider) null check: aload s + ifnull/ifnonnull
			if (n0 instanceof IrNode.RawInsnNode(LoadInstruction li)
					&& li.slot() < nonNullDynamicBoxedInt.length
					&& nonNullDynamicBoxedInt[li.slot()]
					&& i + 1 < nodes.size()
					&& nodes.get(i + 1) instanceof IrNode.RawInsnNode(BranchInstruction bi)
					&& (bi.opcode() == Opcode.IFNULL || bi.opcode() == Opcode.IFNONNULL)) {
				boolean taken = bi.opcode() == Opcode.IFNONNULL;
				Label target = bi.target();
				if (taken) {
					nodes.set(i, new IrNode.GotoNode(target));
					nodes.remove(i + 1);
				} else {
					nodes.remove(i + 1);
					nodes.remove(i);
					i--;
				}
				changed = true;
				continue;
			}

			// Pattern 0b: ConstLoadNode(ComparisonOperation op) + arg1 + arg2 + invokestatic SpellUtils.evaluateOperation -> arg1 + arg2 + CompareOpNode(op)
			if (n0 instanceof IrNode.ConstLoadNode(KnownVal kvOp)
					&& kvOp.kind() == ValKind.REF
					&& kvOp.refVal() instanceof ComparisonOperation compOp
					&& compOp != ComparisonOperation.HAS
					&& i + 3 < nodes.size()
					&& isSingleStackPushInsn(nodes.get(i + 1))
					&& isSingleStackPushInsn(nodes.get(i + 2))
					&& nodes.get(i + 3) instanceof IrNode.RawInsnNode(InvokeInstruction iiOp)
					&& iiOp.opcode() == Opcode.INVOKESTATIC
					&& iiOp.owner().asInternalName().equals("net/demilich/metastone/game/spells/SpellUtils")
					&& iiOp.name().equalsString("evaluateOperation")) {
				nodes.set(i + 3, new IrNode.CompareOpNode(compOp));
				nodes.remove(i);
				changed = true;
				i--;
				continue;
			}

			// Pattern 0c: ConstLoadNode(Attribute.ATTACK) + loadEntity + invokestatic AttributeValueProvider.provideValueForAttribute
			if (n0 instanceof IrNode.ConstLoadNode(KnownVal kvAttr)
					&& kvAttr.kind() == ValKind.REF
					&& kvAttr.refVal() == Attribute.ATTACK
					&& i + 2 < nodes.size()
					&& isSingleStackPushInsn(nodes.get(i + 1))
					&& nodes.get(i + 2) instanceof IrNode.RawInsnNode(InvokeInstruction iiAttr)
					&& iiAttr.opcode() == Opcode.INVOKESTATIC
					&& iiAttr.owner().asInternalName().equals("net/demilich/metastone/game/spells/desc/valueprovider/AttributeValueProvider")
					&& iiAttr.name().equalsString("provideValueForAttribute")) {
				nodes.set(i + 2, new IrNode.StaticCallNode(
						ClassDesc.of("net.demilich.metastone.game.cards.desc.compiler", "TargetLogicRuntime"),
						"provideValueForAttack",
						MethodTypeDesc.of(ConstantDescs.CD_int, DescBytecodeCompiler.CD_GAME_CONTEXT, DescBytecodeCompiler.CD_ENTITY)));
				nodes.remove(i);
				changed = true;
				i--;
				continue;
			}

			// Pattern 1: ConstLoadNode(BOXED_INT(v)) + invokevirtual Integer.intValue()I -> ConstLoadNode(INT(v))
			if (n0 instanceof IrNode.ConstLoadNode(KnownVal kv)
					&& kv.kind() == ValKind.BOXED_INT
					&& i + 1 < nodes.size()
					&& nodes.get(i + 1) instanceof IrNode.RawInsnNode(InvokeInstruction ii)
					&& ii.opcode() == Opcode.INVOKEVIRTUAL
					&& ii.owner().asInternalName().equals("java/lang/Integer")
					&& ii.name().equalsString("intValue")
					&& ii.type().equalsString("()I")) {
				nodes.set(i, new IrNode.ConstLoadNode(KnownVal.ofInt(kv.intVal())));
				nodes.remove(i + 1);
				changed = true;
				i--;
				continue;
			}

			// Pattern 1b: ConstLoadNode(REF(Enum<?> e)) + invokevirtual Enum.ordinal()I -> ConstLoadNode(INT(e.ordinal()))
			if (n0 instanceof IrNode.ConstLoadNode(KnownVal kv)
					&& kv.kind() == ValKind.REF
					&& kv.refVal() instanceof Enum<?> enumVal
					&& i + 1 < nodes.size()
					&& nodes.get(i + 1) instanceof IrNode.RawInsnNode(InvokeInstruction ii)
					&& ii.opcode() == Opcode.INVOKEVIRTUAL
					&& ii.name().equalsString("ordinal")
					&& ii.type().equalsString("()I")) {
				nodes.set(i, new IrNode.ConstLoadNode(KnownVal.ofInt(enumVal.ordinal())));
				nodes.remove(i + 1);
				changed = true;
				i--;
				continue;
			}

			// Pattern 1c: GETSTATIC $SwitchMap$... + ConstLoadNode(INT(ordinal)) + IALOAD -> ConstLoadNode(INT(caseIndex))
			if (n0 instanceof IrNode.RawInsnNode(FieldInstruction fi)
					&& fi.opcode() == Opcode.GETSTATIC
					&& fi.name().stringValue().startsWith("$SwitchMap$")
					&& fi.type().equalsString("[I")
					&& i + 2 < nodes.size()
					&& nodes.get(i + 2) instanceof IrNode.RawInsnNode(Instruction ali)
					&& ali.opcode() == Opcode.IALOAD) {
				Integer ordinal = extractConstInt(nodes.get(i + 1));
				if (ordinal != null) {
					int[] switchMap = getSwitchMap(fi.owner().asInternalName(), fi.name().stringValue());
					if (ordinal >= 0 && ordinal < switchMap.length) {
						int caseIndex = switchMap[ordinal];
						nodes.set(i, new IrNode.ConstLoadNode(KnownVal.ofInt(caseIndex)));
						nodes.remove(i + 2);
						nodes.remove(i + 1);
						changed = true;
						i--;
						continue;
					}
				}
			}

			// Pattern 1d: (ConstLoadNode(INT(key)) | ConstantInstruction(key)) + TableSwitchInstruction / LookupSwitchInstruction -> GotoNode(target)
			Integer switchKey = extractConstInt(n0);
			if (switchKey != null && i + 1 < nodes.size()) {
				IrNode n1 = nodes.get(i + 1);
				if (n1 instanceof IrNode.RawInsnNode(TableSwitchInstruction tsi)) {
					Label target = tsi.defaultTarget();
					for (SwitchCase sc : tsi.cases()) {
						if (sc.caseValue() == switchKey) {
							target = sc.target();
							break;
						}
					}
					nodes.set(i, new IrNode.GotoNode(target));
					nodes.remove(i + 1);
					changed = true;
					continue;
				} else if (n1 instanceof IrNode.RawInsnNode(LookupSwitchInstruction lsi)) {
					Label target = lsi.defaultTarget();
					for (SwitchCase sc : lsi.cases()) {
						if (sc.caseValue() == switchKey) {
							target = sc.target();
							break;
						}
					}
					nodes.set(i, new IrNode.GotoNode(target));
					nodes.remove(i + 1);
					changed = true;
					continue;
				}
			}

			// Pattern 1e: Constant integer arithmetic (v1, v2, IADD/ISUB/IMUL)
			if (switchKey != null && i + 2 < nodes.size()
					&& nodes.get(i + 2) instanceof IrNode.RawInsnNode(Instruction oi)) {
				Integer v2 = extractConstInt(nodes.get(i + 1));
				if (v2 != null) {
					Integer res = switch (oi.opcode()) {
						case IADD -> switchKey + v2;
						case ISUB -> switchKey - v2;
						case IMUL -> switchKey * v2;
						default -> null;
					};
					if (res != null) {
						nodes.set(i, new IrNode.ConstLoadNode(KnownVal.ofInt(res)));
						nodes.remove(i + 2);
						nodes.remove(i + 1);
						changed = true;
						i--;
						continue;
					}
				}
			}

			// Pattern 2: Single-operand branch: ConstLoadNode(kv) + BranchInstruction(op, target)
			if (n0 instanceof IrNode.ConstLoadNode(KnownVal kv)
					&& i + 1 < nodes.size()
					&& nodes.get(i + 1) instanceof IrNode.RawInsnNode(BranchInstruction bi)) {
				Opcode op = bi.opcode();
				Label target = bi.target();
				Boolean taken = evalSingleBranch(op, kv);
				if (taken != null) {
					if (taken) {
						nodes.set(i, new IrNode.GotoNode(target));
						nodes.remove(i + 1);
					} else {
						nodes.remove(i + 1);
						nodes.remove(i);
						i--;
					}
					changed = true;
					continue;
				}
			}

			// Pattern 3: Two-operand enum comparison: ConstLoadNode(kv) + getstatic Enum + if_acmpeq/if_acmpne
			if (n0 instanceof IrNode.ConstLoadNode(KnownVal kv)
					&& i + 2 < nodes.size()
					&& nodes.get(i + 1) instanceof IrNode.RawInsnNode(FieldInstruction fi)
					&& fi.opcode() == Opcode.GETSTATIC
					&& nodes.get(i + 2) instanceof IrNode.RawInsnNode(BranchInstruction bi)
					&& (bi.opcode() == Opcode.IF_ACMPEQ || bi.opcode() == Opcode.IF_ACMPNE)) {
				Boolean taken = evalEnumBranch(kv, fi, bi.opcode());
				if (taken != null) {
					Label target = bi.target();
					if (taken) {
						nodes.set(i, new IrNode.GotoNode(target));
						nodes.remove(i + 2);
						nodes.remove(i + 1);
					} else {
						nodes.remove(i + 2);
						nodes.remove(i + 1);
						nodes.remove(i);
						i--;
					}
					changed = true;
					continue;
				}
			}

			// Pattern 4: Two-operand int comparison: ConstLoadNode(INT(v1)) + (ConstLoadNode(INT(v2)) | ConstantInstruction(v2)) + if_icmp*
			if (n0 instanceof IrNode.ConstLoadNode(KnownVal kv1)
					&& kv1.kind() == ValKind.INT
					&& i + 2 < nodes.size()
					&& nodes.get(i + 2) instanceof IrNode.RawInsnNode(BranchInstruction bi)) {
				Integer v2 = extractConstInt(nodes.get(i + 1));
				if (v2 != null) {
					Boolean taken = evalIntCompareBranch(bi.opcode(), kv1.intVal(), v2);
					if (taken != null) {
						Label target = bi.target();
						if (taken) {
							nodes.set(i, new IrNode.GotoNode(target));
							nodes.remove(i + 2);
							nodes.remove(i + 1);
						} else {
							nodes.remove(i + 2);
							nodes.remove(i + 1);
							nodes.remove(i);
							i--;
						}
						changed = true;
						continue;
					}
				}
			}

			// Pattern 5: ConstLoadNode(STRING(s1)) + ConstantInstruction(String s2) + String.equals + ifeq/ifne
			if (n0 instanceof IrNode.ConstLoadNode(KnownVal kv)
					&& kv.kind() == ValKind.STRING
					&& i + 3 < nodes.size()
					&& nodes.get(i + 1) instanceof IrNode.RawInsnNode(ConstantInstruction ci)
					&& ci.constantValue() instanceof String s2
					&& nodes.get(i + 2) instanceof IrNode.RawInsnNode(InvokeInstruction ii)
					&& ii.opcode() == Opcode.INVOKEVIRTUAL
					&& ii.owner().asInternalName().equals("java/lang/String")
					&& ii.name().equalsString("equals")
					&& nodes.get(i + 3) instanceof IrNode.RawInsnNode(BranchInstruction bi)
					&& (bi.opcode() == Opcode.IFEQ || bi.opcode() == Opcode.IFNE)) {
				boolean eq = ((String) kv.refVal()).equals(s2);
				boolean taken = bi.opcode() == Opcode.IFNE ? eq : !eq;
				Label target = bi.target();
				if (taken) {
					nodes.set(i, new IrNode.GotoNode(target));
					nodes.remove(i + 3);
					nodes.remove(i + 2);
					nodes.remove(i + 1);
				} else {
					nodes.remove(i + 3);
					nodes.remove(i + 2);
					nodes.remove(i + 1);
					nodes.remove(i);
					i--;
				}
				changed = true;
				continue;
			}

			// Pattern 6: aload 3 (desc) + getstatic ArgEnum.KEY + invokevirtual Desc.containsKey(Object)Z + ifeq/ifne
			if (hasDesc
					&& isLoadOfSlot(n0, 3)
					&& i + 3 < nodes.size()
					&& nodes.get(i + 1) instanceof IrNode.RawInsnNode(FieldInstruction fi)
					&& fi.opcode() == Opcode.GETSTATIC
					&& nodes.get(i + 2) instanceof IrNode.RawInsnNode(InvokeInstruction ii)
					&& ii.opcode() == Opcode.INVOKEVIRTUAL
					&& ii.name().equalsString("containsKey")
					&& nodes.get(i + 3) instanceof IrNode.RawInsnNode(BranchInstruction bi)
					&& (bi.opcode() == Opcode.IFEQ || bi.opcode() == Opcode.IFNE)) {
				Class<? extends Enum> argEnumClass = switch (kind) {
					case SPELL -> SpellArg.class;
					case ENTITY_FILTER -> EntityFilterArg.class;
					case VALUE_PROVIDER -> ValueProviderArg.class;
					case CONDITION -> ConditionArg.class;
					default -> null;
				};
				if (argEnumClass != null) {
					try {
						Enum<?> key = Enum.valueOf(argEnumClass, fi.name().stringValue());
						boolean hasKey = desc.containsKey(key);
						boolean taken = bi.opcode() == Opcode.IFNE ? hasKey : !hasKey;
						Label target = bi.target();
						if (taken) {
							nodes.set(i, new IrNode.GotoNode(target));
							nodes.remove(i + 3);
							nodes.remove(i + 2);
							nodes.remove(i + 1);
						} else {
							nodes.remove(i + 3);
							nodes.remove(i + 2);
							nodes.remove(i + 1);
							nodes.remove(i);
							i--;
						}
						changed = true;
					} catch (IllegalArgumentException ignored) {
					}
				}
			}
		}
		return changed;
	}

	private static boolean isSingleStackPushInsn(IrNode node) {
		if (node instanceof IrNode.ConstLoadNode || node instanceof IrNode.InlineVpNode) {
			return true;
		}
		if (node instanceof IrNode.RawInsnNode(Instruction insn)) {
			return insn instanceof LoadInstruction || insn instanceof ConstantInstruction;
		}
		return false;
	}

	private static Integer extractConstInt(IrNode node) {
		if (node instanceof IrNode.ConstLoadNode(KnownVal kv) && kv.kind() == ValKind.INT) {
			return kv.intVal();
		}
		if (node instanceof IrNode.RawInsnNode(ConstantInstruction ci) && ci.constantValue() instanceof Integer iv) {
			return iv;
		}
		return null;
	}

	private static Boolean evalSingleBranch(Opcode op, KnownVal kv) {
		return switch (op) {
			case IFNULL -> kv.isNull() ? Boolean.TRUE : (kv.isNonNull() ? Boolean.FALSE : null);
			case IFNONNULL -> kv.isNonNull() ? Boolean.TRUE : (kv.isNull() ? Boolean.FALSE : null);
			case IFEQ -> kv.kind() == ValKind.INT ? (kv.intVal() == 0) : null;
			case IFNE -> kv.kind() == ValKind.INT ? (kv.intVal() != 0) : null;
			case IFLT -> kv.kind() == ValKind.INT ? (kv.intVal() < 0) : null;
			case IFGE -> kv.kind() == ValKind.INT ? (kv.intVal() >= 0) : null;
			case IFGT -> kv.kind() == ValKind.INT ? (kv.intVal() > 0) : null;
			case IFLE -> kv.kind() == ValKind.INT ? (kv.intVal() <= 0) : null;
			default -> null;
		};
	}

	private static Boolean evalEnumBranch(KnownVal kv, FieldInstruction fi, Opcode op) {
		if (kv.isNull()) {
			// GETSTATIC of an enum constant is never null
			return op == Opcode.IF_ACMPNE;
		}
		if (kv.kind() == ValKind.REF && kv.refVal() instanceof Enum<?> e) {
			if (e.getDeclaringClass().descriptorString().equals(fi.type().stringValue())) {
				boolean eq = e.name().equals(fi.name().stringValue());
				return op == Opcode.IF_ACMPEQ ? eq : !eq;
			}
		}
		return null;
	}

	private static Boolean evalIntCompareBranch(Opcode op, int v1, int v2) {
		return switch (op) {
			case IF_ICMPEQ -> v1 == v2;
			case IF_ICMPNE -> v1 != v2;
			case IF_ICMPLT -> v1 < v2;
			case IF_ICMPGE -> v1 >= v2;
			case IF_ICMPGT -> v1 > v2;
			case IF_ICMPLE -> v1 <= v2;
			default -> null;
		};
	}
}
