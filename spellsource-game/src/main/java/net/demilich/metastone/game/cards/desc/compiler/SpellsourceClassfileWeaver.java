package net.demilich.metastone.game.cards.desc.compiler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.FieldModel;
import java.lang.classfile.Label;
import java.lang.classfile.MethodElement;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Compile-time bytecode weaver using the JDK {@link java.lang.classfile} API.
 * <p>
 * Keeps expressive {@code .java} source files in {@code spellsource-game} free of compiler/lowering/inline-storage
 * plumbing while weaving the optimized fields, dispatch hooks, inline attribute slots, O(1) target group lookup,
 * and zero-arg {@code create()} memoization directly into the compiled {@code .class} files.
 */
public final class SpellsourceClassfileWeaver {
	private static final String COMPILER_PKG = "net.demilich.metastone.game.cards.desc.compiler";

	private static final ClassDesc CD_COMPILER = ClassDesc.of(COMPILER_PKG, "DescBytecodeCompiler");
	private static final ClassDesc CD_COMPILED_VP = ClassDesc.of(COMPILER_PKG, "CompiledValueProvider");
	private static final ClassDesc CD_COMPILED_EF = ClassDesc.of(COMPILER_PKG, "CompiledEntityFilter");
	private static final ClassDesc CD_COMPILED_COND = ClassDesc.of(COMPILER_PKG, "CompiledCondition");
	private static final ClassDesc CD_COMPILED_SPELL = ClassDesc.of(COMPILER_PKG, "CompiledSpell");
	private static final ClassDesc CD_TARGET_RUNTIME = ClassDesc.of(COMPILER_PKG, "TargetLogicRuntime");

	private static final ClassDesc CD_GAME_CONTEXT = ClassDesc.of("net.demilich.metastone.game.GameContext");
	private static final ClassDesc CD_PLAYER = ClassDesc.of("net.demilich.metastone.game.Player");
	private static final ClassDesc CD_ENTITY = ClassDesc.of("net.demilich.metastone.game.entities.Entity");
	private static final ClassDesc CD_CARD = ClassDesc.of("net.demilich.metastone.game.cards.Card");
	private static final ClassDesc CD_ATTRIBUTE = ClassDesc.of("net.demilich.metastone.game.cards.Attribute");
	private static final ClassDesc CD_ATTR_MAP = ClassDesc.of("net.demilich.metastone.game.cards.AttributeMap");
	private static final ClassDesc CD_DESC = ClassDesc.of("net.demilich.metastone.game.cards.desc.Desc");
	private static final ClassDesc CD_VP = ClassDesc.of("net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider");
	private static final ClassDesc CD_EF = ClassDesc.of("net.demilich.metastone.game.spells.desc.filter.EntityFilter");
	private static final ClassDesc CD_COND = ClassDesc.of("net.demilich.metastone.game.spells.desc.condition.Condition");
	private static final ClassDesc CD_COND_DESC = ClassDesc.of("net.demilich.metastone.game.spells.desc.condition.ConditionDesc");
	private static final ClassDesc CD_SPELL = ClassDesc.of("net.demilich.metastone.game.spells.Spell");
	private static final ClassDesc CD_SPELL_DESC = ClassDesc.of("net.demilich.metastone.game.spells.desc.SpellDesc");
	private static final ClassDesc CD_EVENT_TRIGGER = ClassDesc.of("net.demilich.metastone.game.spells.trigger.EventTrigger");
	private static final ClassDesc CD_ENCHANTMENT = ClassDesc.of("net.demilich.metastone.game.spells.trigger.Enchantment");
	private static final ClassDesc CD_GAME_EVENT = ClassDesc.of("net.demilich.metastone.game.events.GameEvent");
	private static final ClassDesc CD_GAME_EVENT_TYPE = ClassDesc.of("com.hiddenswitch.spellsource.rpc.Spellsource$GameEventTypeMessage$GameEventType");
	private static final ClassDesc CD_TARGET_LOGIC = ClassDesc.of("net.demilich.metastone.game.logic.TargetLogic");
	private static final ClassDesc CD_ENTITY_REF = ClassDesc.of("net.demilich.metastone.game.targeting.EntityReference");
	private static final ClassDesc CD_LIST = ClassDesc.of("java.util.List");

	private static final MethodTypeDesc MTD_VP_COMPUTE = MethodTypeDesc.of(
			ConstantDescs.CD_int, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY);
	private static final MethodTypeDesc MTD_EF_MATCHES = MethodTypeDesc.of(
			ConstantDescs.CD_boolean, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY);
	private static final MethodTypeDesc MTD_COND_FULFILLED = MethodTypeDesc.of(
			ConstantDescs.CD_boolean, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY);
	private static final MethodTypeDesc MTD_COND_EVAL = MethodTypeDesc.of(
			ConstantDescs.CD_boolean, CD_GAME_CONTEXT, CD_PLAYER, CD_COND_DESC, CD_ENTITY, CD_ENTITY);
	private static final MethodTypeDesc MTD_SPELL_ONCAST = MethodTypeDesc.of(
			ConstantDescs.CD_void, CD_GAME_CONTEXT, CD_PLAYER, CD_SPELL_DESC, CD_ENTITY, CD_ENTITY);
	private static final MethodTypeDesc MTD_SPELL_CAST = MethodTypeDesc.of(
			ConstantDescs.CD_void, CD_GAME_CONTEXT, CD_PLAYER, CD_SPELL_DESC, CD_ENTITY, CD_LIST);
	private static final MethodTypeDesc MTD_INNER_QUEUES = MethodTypeDesc.of(
			ConstantDescs.CD_boolean, CD_GAME_EVENT, CD_ENCHANTMENT, CD_ENTITY);
	private static final MethodTypeDesc MTD_HOST_COND = MethodTypeDesc.of(
			ConstantDescs.CD_boolean, CD_GAME_EVENT, CD_ENTITY);
	private static final MethodTypeDesc MTD_TRIGGER_QUEUES = MethodTypeDesc.of(
			ConstantDescs.CD_boolean, CD_GAME_EVENT, CD_ENCHANTMENT, CD_ENTITY, ConstantDescs.CD_int);
	private static final MethodTypeDesc MTD_TRIGGER_FIRES = MethodTypeDesc.of(
			ConstantDescs.CD_boolean, CD_GAME_EVENT, CD_ENTITY, ConstantDescs.CD_int);

	private SpellsourceClassfileWeaver() {
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			throw new IllegalArgumentException("Expected classes directory path as argument");
		}
		Path classesDir = Path.of(args[0]);
		weaveAll(classesDir);
	}

	public static void weaveAll(Path classesDir) throws IOException {
		weaveAttributeMapAndCardAttributeMap(classesDir);
		weaveCompilerBridge(classesDir);
		weaveDesc(classesDir);
		weaveValueProvider(classesDir);
		weaveEntityFilter(classesDir);
		weaveCondition(classesDir);
		weaveSpellAndSpellDesc(classesDir);
		weaveEventTriggerAndEnchantment(classesDir);
		weaveEntityCardAndGameLogic(classesDir);
		weaveTargetLogic(classesDir);
		weaveZeroArgCreateMemoization(classesDir);
	}

	private static boolean hasField(ClassModel model, String name) {
		for (FieldModel f : model.fields()) {
			if (f.fieldName().equalsString(name)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasMethod(ClassModel model, String name) {
		for (MethodModel m : model.methods()) {
			if (m.methodName().equalsString(name)) {
				return true;
			}
		}
		return false;
	}

	// =========================================================================
	// 1. AttributeMap & CardAttributeMap Template Remapping
	// =========================================================================

	private static void weaveAttributeMapAndCardAttributeMap(Path classesDir) throws IOException {
		Path compilerDir = classesDir.resolve("net/demilich/metastone/game/cards/desc/compiler");
		Path cardsDir = classesDir.resolve("net/demilich/metastone/game/cards");

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(cardsDir, "AttributeMap$*.class")) {
			for (Path p : stream) {
				Files.deleteIfExists(p);
			}
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(cardsDir, "CardAttributeMap$*.class")) {
			for (Path p : stream) {
				Files.deleteIfExists(p);
			}
		}

		Map<String, String> attrMapReplacements = new LinkedHashMap<>();
		attrMapReplacements.put(
				"net/demilich/metastone/game/cards/desc/compiler/AttributeMapTemplate",
				"net/demilich/metastone/game/cards/AttributeMap");
		attrMapReplacements.put("AttributeMapTemplate.java", "AttributeMap.java");

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(compilerDir, "AttributeMapTemplate*.class")) {
			for (Path templatePath : stream) {
				String fileName = templatePath.getFileName().toString();
				String targetFileName = fileName.replace("AttributeMapTemplate", "AttributeMap");
				byte[] raw = Files.readAllBytes(templatePath);
				byte[] remapped = remapConstantPoolUtf8(raw, attrMapReplacements);
				// Validate with ClassFile API
				ClassFile.of().parse(remapped);
				Files.write(cardsDir.resolve(targetFileName), remapped);
			}
		}

		Map<String, String> cardAttrReplacements = new LinkedHashMap<>();
		cardAttrReplacements.put(
				"net/demilich/metastone/game/cards/desc/compiler/CardAttributeMapTemplate",
				"net/demilich/metastone/game/cards/CardAttributeMap");
		cardAttrReplacements.put(
				"net/demilich/metastone/game/cards/desc/compiler/AttributeMapTemplate",
				"net/demilich/metastone/game/cards/AttributeMap");
		cardAttrReplacements.put("CardAttributeMapTemplate.java", "CardAttributeMap.java");

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(compilerDir, "CardAttributeMapTemplate*.class")) {
			for (Path templatePath : stream) {
				String fileName = templatePath.getFileName().toString();
				String targetFileName = fileName.replace("CardAttributeMapTemplate", "CardAttributeMap");
				byte[] raw = Files.readAllBytes(templatePath);
				byte[] remapped = remapConstantPoolUtf8(raw, cardAttrReplacements);
				ClassFile.of().parse(remapped);
				Files.write(cardsDir.resolve(targetFileName), remapped);
			}
		}
	}

	private static byte[] remapConstantPoolUtf8(byte[] bytes, Map<String, String> replacements) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
		DataOutputStream out = new DataOutputStream(baos);

		// magic (4), minor (2), major (2), constant_pool_count (2)
		out.write(bytes, 0, 10);
		int cpCount = ((bytes[8] & 0xFF) << 8) | (bytes[9] & 0xFF);
		int pos = 10;

		for (int i = 1; i < cpCount; i++) {
			int tag = bytes[pos] & 0xFF;
			switch (tag) {
				case 1 -> { // CONSTANT_Utf8
					int len = ((bytes[pos + 1] & 0xFF) << 8) | (bytes[pos + 2] & 0xFF);
					String str = new String(bytes, pos + 3, len, StandardCharsets.UTF_8);
					for (Map.Entry<String, String> entry : replacements.entrySet()) {
						if (str.contains(entry.getKey())) {
							str = str.replace(entry.getKey(), entry.getValue());
						}
					}
					byte[] newUtf = str.getBytes(StandardCharsets.UTF_8);
					out.writeByte(1);
					out.writeShort(newUtf.length);
					out.write(newUtf);
					pos += 3 + len;
				}
				case 3, 4, 9, 10, 11, 12, 17, 18 -> {
					out.write(bytes, pos, 5);
					pos += 5;
				}
				case 5, 6 -> { // Long, Double take 2 CP slots
					out.write(bytes, pos, 9);
					pos += 9;
					i++;
				}
				case 7, 8, 16, 19, 20 -> {
					out.write(bytes, pos, 3);
					pos += 3;
				}
				case 15 -> {
					out.write(bytes, pos, 4);
					pos += 4;
				}
				default -> throw new IllegalStateException("Unknown CP tag " + tag + " at index " + i);
			}
		}

		out.write(bytes, pos, bytes.length - pos);
		out.flush();
		return baos.toByteArray();
	}

	// =========================================================================
	// 2. CompilerBridge Weaving
	// =========================================================================

	private static void weaveCompilerBridge(Path classesDir) throws IOException {
		Path path = classesDir.resolve("net/demilich/metastone/game/cards/desc/compiler/CompilerBridge.class");
		ClassFile cf = ClassFile.of();
		ClassModel model = cf.parse(path);

		byte[] woven = cf.build(model.thisClass().asSymbol(), clb -> {
			for (ClassElement ce : model) {
				if (ce instanceof MethodModel mm) {
					String name = mm.methodName().stringValue();
					if (!name.equals(ConstantDescs.INIT_NAME)) {
						clb.withMethodBody(name, mm.methodTypeSymbol(), mm.flags().flagsMask(), cob -> {
							switch (name) {
								case "getCompiled" -> cob.aload(0)
										.getfield(CD_DESC, "compiled", ConstantDescs.CD_Object)
										.areturn();
								case "setCompiled" -> cob.aload(0)
										.aload(1)
										.putfield(CD_DESC, "compiled", ConstantDescs.CD_Object)
										.return_();
								case "evaluateProvideValue" -> cob.aload(0).aload(1).aload(2).aload(3).aload(4)
										.invokevirtual(CD_VP, "evaluateProvideValue", MTD_VP_COMPUTE)
										.ireturn();
								case "evaluateFullValue" -> cob.aload(0).aload(1).aload(2).aload(3).aload(4)
										.invokevirtual(CD_VP, "evaluateFullValue", MTD_VP_COMPUTE)
										.ireturn();
								case "evaluateTest" -> cob.aload(0).aload(1).aload(2).aload(3).aload(4)
										.invokevirtual(CD_EF, "evaluateTest", MTD_EF_MATCHES)
										.ireturn();
								case "evaluateFullMatches" -> cob.aload(0).aload(1).aload(2).aload(3).aload(4)
										.invokevirtual(CD_EF, "evaluateFullMatches", MTD_EF_MATCHES)
										.ireturn();
								case "evaluateIsFulfilled" -> cob.aload(0).aload(1).aload(2).aload(3).aload(4).aload(5)
										.invokevirtual(CD_COND, "evaluateIsFulfilled", MTD_COND_EVAL)
										.ireturn();
								case "evaluateFullIsFulfilled" -> cob.aload(0).aload(1).aload(2).aload(3).aload(4)
										.invokevirtual(CD_COND, "evaluateFullIsFulfilled", MTD_COND_FULFILLED)
										.ireturn();
								case "isTargetOverridden" -> cob.aload(0)
										.invokevirtual(CD_COND, "isTargetOverridden", MethodTypeDesc.of(ConstantDescs.CD_boolean))
										.ireturn();
								case "isMultiTargetAnd" -> cob.aload(0)
										.invokevirtual(CD_COND, "isMultiTargetAnd", MethodTypeDesc.of(ConstantDescs.CD_boolean))
										.ireturn();
								case "isAtLeastOneTargetRequired" -> cob.aload(0)
										.invokevirtual(CD_COND, "isAtLeastOneTargetRequired", MethodTypeDesc.of(ConstantDescs.CD_boolean))
										.ireturn();
								case "isFilterUsed" -> cob.aload(0)
										.invokevirtual(CD_COND, "isFilterUsed", MethodTypeDesc.of(ConstantDescs.CD_boolean))
										.ireturn();
								case "evaluateOnCast" -> cob.aload(0).aload(1).aload(2).aload(3).aload(4).aload(5)
										.invokevirtual(CD_SPELL, "evaluateOnCast", MTD_SPELL_ONCAST)
										.return_();
								case "evaluateInnerQueues" -> cob.aload(0).aload(1).aload(2).aload(3)
										.invokevirtual(CD_EVENT_TRIGGER, "evaluateInnerQueues", MTD_INNER_QUEUES)
										.ireturn();
								case "evaluateHostConditionMet" -> cob.aload(0).aload(1).aload(2)
										.invokevirtual(CD_EVENT_TRIGGER, "evaluateHostConditionMet", MTD_HOST_COND)
										.ireturn();
								case "evaluateFullQueues" -> cob.aload(0).aload(1).aload(2).aload(3).iload(4)
										.invokevirtual(CD_EVENT_TRIGGER, "evaluateFullQueues", MTD_TRIGGER_QUEUES)
										.ireturn();
								case "evaluateFullFires" -> cob.aload(0).aload(1).aload(2).iload(3)
										.invokevirtual(CD_EVENT_TRIGGER, "evaluateFullFires", MTD_TRIGGER_FIRES)
										.ireturn();
								case "getEnchantmentMask" -> cob.aload(0)
										.getfield(CD_ENCHANTMENT, "interestedMask", ConstantDescs.CD_long)
										.lreturn();
								case "setEnchantmentMask" -> cob.aload(0)
										.lload(1)
										.putfield(CD_ENCHANTMENT, "interestedMask", ConstantDescs.CD_long)
										.return_();
								default -> throw new IllegalStateException("Unexpected CompilerBridge method: " + name);
							}
						});
						continue;
					}
				}
				clb.with(ce);
			}
		});
		Files.write(path, woven);
	}

	// =========================================================================
	// 3. Desc.class Weaving
	// =========================================================================

	private static void weaveDesc(Path classesDir) throws IOException {
		Path path = classesDir.resolve("net/demilich/metastone/game/cards/desc/Desc.class");
		ClassFile cf = ClassFile.of();
		ClassModel model = cf.parse(path);
		if (hasField(model, "compiled")) {
			return;
		}

		byte[] woven = cf.transformClass(model, (clb, ce) -> {
			if (ce instanceof MethodModel mm) {
				String name = mm.methodName().stringValue();
				String desc = mm.methodType().stringValue();
				if ((name.equals("put") && desc.equals("(Ljava/lang/Enum;Ljava/lang/Object;)Ljava/lang/Object;"))
						|| name.equals("remove")
						|| name.equals("clear")) {
					clb.transformMethod(mm, (mb, me) -> {
						if (me instanceof CodeModel cm) {
							mb.transformCode(cm, (cob, coe) -> {
								if (coe instanceof FieldInstruction fi
										&& fi.opcode() == Opcode.PUTFIELD
										&& fi.name().equalsString("cachedInstance")) {
									cob.aload(0)
											.aconst_null()
											.putfield(CD_DESC, "compiled", ConstantDescs.CD_Object);
								}
								cob.with(coe);
							});
						} else {
							mb.with(me);
						}
					});
					return;
				}
				if ((name.equals("clone") || name.equals("cloneAndUnfreeze"))
						&& desc.equals("()Lnet/demilich/metastone/game/cards/desc/Desc;")) {
					clb.transformMethod(mm, (mb, me) -> {
						if (me instanceof CodeModel cm) {
							mb.transformCode(cm, (cob, coe) -> {
								if (coe instanceof ReturnInstruction ri && ri.opcode() == Opcode.ARETURN) {
									cob.dup()
											.aconst_null()
											.putfield(CD_DESC, "compiled", ConstantDescs.CD_Object);
								}
								cob.with(coe);
							});
						} else {
							mb.with(me);
						}
					});
					return;
				}
			}
			clb.with(ce);
		});

		// Add public transient Object compiled field
		ClassModel intermediate = cf.parse(woven);
		byte[] finalBytes = cf.build(intermediate.thisClass().asSymbol(), clb -> {
			for (ClassElement ce : intermediate) {
				clb.with(ce);
			}
			clb.withField("compiled", ConstantDescs.CD_Object, ClassFile.ACC_PUBLIC | ClassFile.ACC_TRANSIENT);
		});
		Files.write(path, finalBytes);
	}

	// =========================================================================
	// 4. ValueProvider.class Weaving
	// =========================================================================

	private static void weaveValueProvider(Path classesDir) throws IOException {
		Path path = classesDir.resolve("net/demilich/metastone/game/spells/desc/valueprovider/ValueProvider.class");
		ClassFile cf = ClassFile.of();
		ClassModel model = cf.parse(path);
		if (hasMethod(model, "evaluateFullValue")) {
			return;
		}

		byte[] woven = cf.build(model.thisClass().asSymbol(), clb -> {
			for (ClassElement ce : model) {
				if (ce instanceof MethodModel mm && mm.methodName().equalsString("getValue")
						&& mm.methodTypeSymbol().equals(MTD_VP_COMPUTE)) {
					// Rename original getValue to evaluateFullValue
					clb.withMethod("evaluateFullValue", MTD_VP_COMPUTE, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, mb -> {
						for (MethodElement me : mm) {
							mb.with(me);
						}
					});
					continue;
				}
				clb.with(ce);
			}

			clb.withMethodBody("evaluateProvideValue", MTD_VP_COMPUTE, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
				cob.aload(0).aload(1).aload(2).aload(3).aload(4)
						.invokevirtual(CD_VP, "provideValue", MTD_VP_COMPUTE)
						.ireturn();
			});

			clb.withMethodBody("getCompiled", MethodTypeDesc.of(CD_COMPILED_VP), ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
				cob.aload(0)
						.invokestatic(CD_COMPILER, "getCompiled", MethodTypeDesc.of(CD_COMPILED_VP, CD_VP))
						.areturn();
			});

			clb.withMethodBody("getValue", MTD_VP_COMPUTE, ClassFile.ACC_PUBLIC, cob -> {
				cob.aload(0).aload(1).aload(2).aload(3).aload(4)
						.invokestatic(CD_COMPILER, "evalValueProvider",
								MethodTypeDesc.of(ConstantDescs.CD_int, CD_VP, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY))
						.ireturn();
			});
		});
		Files.write(path, woven);
	}

	// =========================================================================
	// 5. EntityFilter.class Weaving
	// =========================================================================

	private static void weaveEntityFilter(Path classesDir) throws IOException {
		Path path = classesDir.resolve("net/demilich/metastone/game/spells/desc/filter/EntityFilter.class");
		ClassFile cf = ClassFile.of();
		ClassModel model = cf.parse(path);
		if (hasMethod(model, "evaluateFullMatches")) {
			return;
		}

		byte[] woven = cf.build(model.thisClass().asSymbol(), clb -> {
			for (ClassElement ce : model) {
				if (ce instanceof MethodModel mm && mm.methodName().equalsString("matches")
						&& mm.methodTypeSymbol().equals(MTD_EF_MATCHES)) {
					clb.withMethod("evaluateFullMatches", MTD_EF_MATCHES, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, mb -> {
						for (MethodElement me : mm) {
							mb.with(me);
						}
					});
					continue;
				}
				clb.with(ce);
			}

			clb.withMethodBody("evaluateTest", MTD_EF_MATCHES, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
				cob.aload(0).aload(1).aload(2).aload(3).aload(4)
						.invokevirtual(CD_EF, "test", MTD_EF_MATCHES)
						.ireturn();
			});

			clb.withMethodBody("getCompiled", MethodTypeDesc.of(CD_COMPILED_EF), ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
				cob.aload(0)
						.invokestatic(CD_COMPILER, "getCompiled", MethodTypeDesc.of(CD_COMPILED_EF, CD_EF))
						.areturn();
			});

			clb.withMethodBody("matches", MTD_EF_MATCHES, ClassFile.ACC_PUBLIC, cob -> {
				cob.aload(0).aload(1).aload(2).aload(3).aload(4)
						.invokestatic(CD_COMPILER, "evalEntityFilter",
								MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_EF, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY))
						.ireturn();
			});
		});
		Files.write(path, woven);
	}

	// =========================================================================
	// 6. Condition.class Weaving
	// =========================================================================

	private static void weaveCondition(Path classesDir) throws IOException {
		Path path = classesDir.resolve("net/demilich/metastone/game/spells/desc/condition/Condition.class");
		ClassFile cf = ClassFile.of();
		ClassModel model = cf.parse(path);
		if (hasMethod(model, "evaluateFullIsFulfilled")) {
			return;
		}

		byte[] woven = cf.build(model.thisClass().asSymbol(), clb -> {
			for (ClassElement ce : model) {
				if (ce instanceof MethodModel mm && mm.methodName().equalsString("isFulfilled")
						&& mm.methodTypeSymbol().equals(MTD_COND_FULFILLED)) {
					clb.withMethod("evaluateFullIsFulfilled", MTD_COND_FULFILLED, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, mb -> {
						for (MethodElement me : mm) {
							mb.with(me);
						}
					});
					continue;
				}
				clb.with(ce);
			}

			clb.withMethodBody("evaluateIsFulfilled", MTD_COND_EVAL, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
				cob.aload(0).aload(1).aload(2).aload(3).aload(4).aload(5)
						.invokevirtual(CD_COND, "isFulfilled", MTD_COND_EVAL)
						.ireturn();
			});

			clb.withMethodBody("isTargetOverridden", MethodTypeDesc.of(ConstantDescs.CD_boolean), ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
				cob.aload(0)
						.invokevirtual(CD_COND, "targetConditionArgOverridesSuppliedTarget", MethodTypeDesc.of(ConstantDescs.CD_boolean))
						.ireturn();
			});

			clb.withMethodBody("isMultiTargetAnd", MethodTypeDesc.of(ConstantDescs.CD_boolean), ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
				cob.aload(0)
						.invokevirtual(CD_COND, "multipleTargetsEvaluatedAsAnd", MethodTypeDesc.of(ConstantDescs.CD_boolean))
						.ireturn();
			});

			clb.withMethodBody("isAtLeastOneTargetRequired", MethodTypeDesc.of(ConstantDescs.CD_boolean), ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
				cob.aload(0)
						.invokevirtual(CD_COND, "requiresAtLeastOneTarget", MethodTypeDesc.of(ConstantDescs.CD_boolean))
						.ireturn();
			});

			clb.withMethodBody("isFilterUsed", MethodTypeDesc.of(ConstantDescs.CD_boolean), ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
				cob.aload(0)
						.invokevirtual(CD_COND, "usesFilter", MethodTypeDesc.of(ConstantDescs.CD_boolean))
						.ireturn();
			});

			clb.withMethodBody("getCompiled", MethodTypeDesc.of(CD_COMPILED_COND), ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
				cob.aload(0)
						.invokestatic(CD_COMPILER, "getCompiled", MethodTypeDesc.of(CD_COMPILED_COND, CD_COND))
						.areturn();
			});

			clb.withMethodBody("isFulfilled", MTD_COND_FULFILLED, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
				cob.aload(0).aload(1).aload(2).aload(3).aload(4)
						.invokestatic(CD_COMPILER, "evalCondition",
								MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_COND, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY))
						.ireturn();
			});
		});
		Files.write(path, woven);
	}

	// =========================================================================
	// 7. Spell.class & SpellDesc.class Weaving
	// =========================================================================

	private static void weaveSpellAndSpellDesc(Path classesDir) throws IOException {
		Path spellPath = classesDir.resolve("net/demilich/metastone/game/spells/Spell.class");
		ClassFile cf = ClassFile.of();
		ClassModel spellModel = cf.parse(spellPath);
		if (!hasMethod(spellModel, "evaluateOnCast")) {
			byte[] wovenSpell = cf.build(spellModel.thisClass().asSymbol(), clb -> {
				for (ClassElement ce : spellModel) {
					clb.with(ce);
				}
				clb.withMethodBody("evaluateOnCast", MTD_SPELL_ONCAST, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
					cob.aload(0).aload(1).aload(2).aload(3).aload(4).aload(5)
							.invokevirtual(CD_SPELL, "onCast", MTD_SPELL_ONCAST)
							.return_();
				});
			});
			Files.write(spellPath, wovenSpell);
		}

		Path spellDescPath = classesDir.resolve("net/demilich/metastone/game/spells/desc/SpellDesc.class");
		ClassModel spellDescModel = cf.parse(spellDescPath);
		if (!hasMethod(spellDescModel, "getCompiledSpell")) {
			byte[] wovenSpellDesc = cf.build(spellDescModel.thisClass().asSymbol(), clb -> {
				for (ClassElement ce : spellDescModel) {
					clb.with(ce);
				}
				clb.withMethodBody("getCompiledSpell", MethodTypeDesc.of(CD_COMPILED_SPELL), ClassFile.ACC_PUBLIC, cob -> {
					cob.aload(0)
							.invokestatic(CD_COMPILER, "getCompiledSpell", MethodTypeDesc.of(CD_COMPILED_SPELL, CD_SPELL_DESC))
							.areturn();
				});
			});
			Files.write(spellDescPath, wovenSpellDesc);
		}
	}

	// =========================================================================
	// 8. EventTrigger.class & Enchantment.class Weaving
	// =========================================================================

	private static void weaveEventTriggerAndEnchantment(Path classesDir) throws IOException {
		ClassFile cf = ClassFile.of();
		Path triggerPath = classesDir.resolve("net/demilich/metastone/game/spells/trigger/EventTrigger.class");
		ClassModel triggerModel = cf.parse(triggerPath);
		if (!hasMethod(triggerModel, "evaluateFullQueues")) {
			byte[] wovenTrigger = cf.build(triggerModel.thisClass().asSymbol(), clb -> {
				for (ClassElement ce : triggerModel) {
					if (ce instanceof MethodModel mm && mm.methodName().equalsString("queues")
							&& mm.methodTypeSymbol().equals(MTD_TRIGGER_QUEUES)) {
						clb.withMethod("evaluateFullQueues", MTD_TRIGGER_QUEUES, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, mb -> {
							for (MethodElement me : mm) {
								mb.with(me);
							}
						});
						continue;
					}
					if (ce instanceof MethodModel mm && mm.methodName().equalsString("fires")
							&& mm.methodTypeSymbol().equals(MTD_TRIGGER_FIRES)) {
						clb.withMethod("evaluateFullFires", MTD_TRIGGER_FIRES, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, mb -> {
							for (MethodElement me : mm) {
								mb.with(me);
							}
						});
						continue;
					}
					clb.with(ce);
				}

				clb.withMethodBody("evaluateInnerQueues", MTD_INNER_QUEUES, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
					cob.aload(0).aload(1).aload(2).aload(3)
							.invokevirtual(CD_EVENT_TRIGGER, "innerQueues", MTD_INNER_QUEUES)
							.ireturn();
				});

				clb.withMethodBody("evaluateHostConditionMet", MTD_HOST_COND, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
					cob.aload(0).aload(1).aload(2)
							.invokevirtual(CD_EVENT_TRIGGER, "hostConditionMet", MTD_HOST_COND)
							.ireturn();
				});

				clb.withMethodBody("queues", MTD_TRIGGER_QUEUES, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL, cob -> {
					cob.aload(0).aload(1).aload(2).aload(3).iload(4)
							.invokestatic(CD_COMPILER, "evalTriggerQueues",
									MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_EVENT_TRIGGER, CD_GAME_EVENT, CD_ENCHANTMENT, CD_ENTITY, ConstantDescs.CD_int))
							.ireturn();
				});

				clb.withMethodBody("fires", MTD_TRIGGER_FIRES, ClassFile.ACC_PUBLIC, cob -> {
					cob.aload(0).aload(1).aload(2).iload(3)
							.invokestatic(CD_COMPILER, "evalTriggerFires",
									MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_EVENT_TRIGGER, CD_GAME_EVENT, CD_ENTITY, ConstantDescs.CD_int))
							.ireturn();
				});
			});
			Files.write(triggerPath, wovenTrigger);
		}

		Path enchPath = classesDir.resolve("net/demilich/metastone/game/spells/trigger/Enchantment.class");
		ClassModel enchModel = cf.parse(enchPath);
		if (!hasField(enchModel, "interestedMask")) {
			MethodTypeDesc mtdInterestedIn = MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_GAME_EVENT_TYPE);
			byte[] step1 = cf.transformClass(enchModel, (clb, ce) -> {
				if (ce instanceof MethodModel mm) {
					String name = mm.methodName().stringValue();
					if (name.equals("setTriggers") || name.equals("setExpirationTriggers") || name.equals("setActivationTriggers")) {
						clb.transformMethod(mm, (mb, me) -> {
							if (me instanceof CodeModel cm) {
								mb.transformCode(cm, new java.lang.classfile.CodeTransform() {
									private boolean inserted = false;

									@Override
									public void accept(java.lang.classfile.CodeBuilder cob, CodeElement coe) {
										if (!inserted) {
											inserted = true;
											cob.aload(0)
													.lconst_0()
													.putfield(CD_ENCHANTMENT, "interestedMask", ConstantDescs.CD_long);
										}
										cob.with(coe);
									}
								});
							} else {
								mb.with(me);
							}
						});
						return;
					}
					if (name.equals("interestedIn") && mm.methodTypeSymbol().equals(mtdInterestedIn)) {
						clb.withMethodBody("interestedIn", mtdInterestedIn, mm.flags().flagsMask(), cob -> {
							cob.aload(0)
									.aload(1)
									.invokestatic(CD_COMPILER, "evalEnchantmentInterestedIn",
											MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_ENCHANTMENT, CD_GAME_EVENT_TYPE))
									.ireturn();
						});
						return;
					}
				}
				clb.with(ce);
			});

			ClassModel intermediate = cf.parse(step1);
			byte[] finalEnch = cf.build(intermediate.thisClass().asSymbol(), clb -> {
				for (ClassElement ce : intermediate) {
					clb.with(ce);
				}
				clb.withField("interestedMask", ConstantDescs.CD_long, ClassFile.ACC_PUBLIC | ClassFile.ACC_TRANSIENT);
			});
			Files.write(enchPath, finalEnch);
		}
	}

	// =========================================================================
	// 9. Entity.class, Card.class & GameLogic.class Weaving
	// =========================================================================

	private static void weaveEntityCardAndGameLogic(Path classesDir) throws IOException {
		ClassFile cf = ClassFile.of();

		// Weave Entity.class
		Path entityPath = classesDir.resolve("net/demilich/metastone/game/entities/Entity.class");
		ClassModel entityModel = cf.parse(entityPath);
		byte[] wovenEntity = cf.build(entityModel.thisClass().asSymbol(), clb -> {
			for (ClassElement ce : entityModel) {
				if (ce instanceof MethodModel mm) {
					String name = mm.methodName().stringValue();
					String desc = mm.methodType().stringValue();
					if (name.equals("getAttributeValue") && desc.equals("(Lnet/demilich/metastone/game/cards/Attribute;)I")) {
						clb.withMethodBody(name, mm.methodTypeSymbol(), mm.flags().flagsMask(), cob -> {
							cob.aload(0)
									.invokevirtual(CD_ENTITY, "getAttributes", MethodTypeDesc.of(CD_ATTR_MAP))
									.aload(1)
									.iconst_0()
									.invokevirtual(CD_ATTR_MAP, "getInt", MethodTypeDesc.of(ConstantDescs.CD_int, CD_ATTRIBUTE, ConstantDescs.CD_int))
									.ireturn();
						});
						continue;
					}
					if (name.equals("getAttributeValue") && desc.equals("(Lnet/demilich/metastone/game/cards/Attribute;I)I")) {
						clb.withMethodBody(name, mm.methodTypeSymbol(), mm.flags().flagsMask(), cob -> {
							cob.aload(0)
									.invokevirtual(CD_ENTITY, "getAttributes", MethodTypeDesc.of(CD_ATTR_MAP))
									.aload(1)
									.iload(2)
									.invokevirtual(CD_ATTR_MAP, "getInt", MethodTypeDesc.of(ConstantDescs.CD_int, CD_ATTRIBUTE, ConstantDescs.CD_int))
									.ireturn();
						});
						continue;
					}
					if (name.equals("hasAttribute") && desc.equals("(Lnet/demilich/metastone/game/cards/Attribute;)Z")) {
						clb.withMethodBody(name, mm.methodTypeSymbol(), mm.flags().flagsMask(), cob -> {
							cob.aload(0)
									.invokevirtual(CD_ENTITY, "getAttributes", MethodTypeDesc.of(CD_ATTR_MAP))
									.aload(1)
									.invokevirtual(CD_ATTR_MAP, "hasAttributeValue", MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_ATTRIBUTE))
									.ireturn();
						});
						continue;
					}
					if (name.equals("modifyAttribute") && desc.equals("(Lnet/demilich/metastone/game/cards/Attribute;I)V")) {
						clb.withMethodBody(name, mm.methodTypeSymbol(), mm.flags().flagsMask(), cob -> {
							cob.aload(0)
									.aload(1)
									.invokevirtual(CD_ENTITY, "clearSilence", MethodTypeDesc.of(ConstantDescs.CD_void, CD_ATTRIBUTE))
									.aload(0)
									.invokevirtual(CD_ENTITY, "getAttributes", MethodTypeDesc.of(CD_ATTR_MAP))
									.astore(3)
									.aload(3)
									.aload(1)
									.aload(3)
									.aload(1)
									.iconst_0()
									.invokevirtual(CD_ATTR_MAP, "getInt", MethodTypeDesc.of(ConstantDescs.CD_int, CD_ATTRIBUTE, ConstantDescs.CD_int))
									.iload(2)
									.iadd()
									.invokevirtual(CD_ATTR_MAP, "putInt", MethodTypeDesc.of(ConstantDescs.CD_void, CD_ATTRIBUTE, ConstantDescs.CD_int))
									.return_();
						});
						continue;
					}
					if (name.equals("setAttribute") && desc.equals("(Lnet/demilich/metastone/game/cards/Attribute;)V")) {
						clb.withMethodBody(name, mm.methodTypeSymbol(), mm.flags().flagsMask(), cob -> {
							cob.aload(0)
									.aload(1)
									.invokevirtual(CD_ENTITY, "clearSilence", MethodTypeDesc.of(ConstantDescs.CD_void, CD_ATTRIBUTE))
									.aload(0)
									.invokevirtual(CD_ENTITY, "getAttributes", MethodTypeDesc.of(CD_ATTR_MAP))
									.aload(1)
									.iconst_1()
									.invokevirtual(CD_ATTR_MAP, "putBoolean", MethodTypeDesc.of(ConstantDescs.CD_void, CD_ATTRIBUTE, ConstantDescs.CD_boolean))
									.return_();
						});
						continue;
					}
					if (name.equals("clearSilence") && desc.equals("(Lnet/demilich/metastone/game/cards/Attribute;)V")) {
						clb.withMethodBody(name, mm.methodTypeSymbol(), mm.flags().flagsMask(), cob -> {
							Label done = cob.newLabel();
							cob.aload(0)
									.invokevirtual(CD_ENTITY, "getAttributes", MethodTypeDesc.of(CD_ATTR_MAP))
									.astore(2)
									.aload(2)
									.getstatic(CD_ATTRIBUTE, "SILENCED", CD_ATTRIBUTE)
									.invokevirtual(CD_ATTR_MAP, "hasAttributeValue", MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_ATTRIBUTE))
									.ifeq(done)
									.aload(1)
									.invokestatic(CD_ATTR_MAP, "isImmuneToSilence", MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_ATTRIBUTE))
									.ifne(done)
									.aload(2)
									.getstatic(CD_ATTRIBUTE, "SILENCED", CD_ATTRIBUTE)
									.invokevirtual(CD_ATTR_MAP, "remove", MethodTypeDesc.of(ConstantDescs.CD_Object, ConstantDescs.CD_Object))
									.pop()
									.labelBinding(done)
									.return_();
						});
						continue;
					}
					if (name.equals("setAttribute") && desc.equals("(Lnet/demilich/metastone/game/cards/Attribute;I)V")) {
						clb.withMethodBody(name, mm.methodTypeSymbol(), mm.flags().flagsMask(), cob -> {
							cob.aload(0)
									.aload(1)
									.invokevirtual(CD_ENTITY, "clearSilence", MethodTypeDesc.of(ConstantDescs.CD_void, CD_ATTRIBUTE))
									.aload(0)
									.invokevirtual(CD_ENTITY, "getAttributes", MethodTypeDesc.of(CD_ATTR_MAP))
									.aload(1)
									.iload(2)
									.invokevirtual(CD_ATTR_MAP, "putInt", MethodTypeDesc.of(ConstantDescs.CD_void, CD_ATTRIBUTE, ConstantDescs.CD_int))
									.return_();
						});
						continue;
					}
				}
				clb.with(ce);
			}
		});
		Files.write(entityPath, wovenEntity);

		// Weave Card.class
		Path cardPath = classesDir.resolve("net/demilich/metastone/game/cards/Card.class");
		ClassModel cardModel = cf.parse(cardPath);
		byte[] wovenCard = cf.build(cardModel.thisClass().asSymbol(), clb -> {
			for (ClassElement ce : cardModel) {
				if (ce instanceof MethodModel mm && mm.methodName().equalsString("hasHeroClass")
						&& mm.methodType().equalsString("(Ljava/lang/String;)Z")) {
					clb.withMethodBody("hasHeroClass", mm.methodTypeSymbol(), mm.flags().flagsMask(), cob -> {
						cob.aload(0)
								.aload(1)
								.invokestatic(CD_ATTR_MAP, "hasHeroClassFast",
										MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_CARD, ConstantDescs.CD_String))
								.ireturn();
					});
					continue;
				}
				clb.with(ce);
			}
		});
		Files.write(cardPath, wovenCard);

		// Weave GameLogic.class
		Path gameLogicPath = classesDir.resolve("net/demilich/metastone/game/logic/GameLogic.class");
		ClassModel gameLogicModel = cf.parse(gameLogicPath);
		MethodTypeDesc mtdCastSpellBridge = MethodTypeDesc.of(
				ConstantDescs.CD_void, CD_SPELL, CD_GAME_CONTEXT, CD_PLAYER, CD_SPELL_DESC, CD_ENTITY, CD_LIST);
		byte[] wovenGameLogic = cf.transformClass(gameLogicModel, (clb, ce) -> {
			if (ce instanceof MethodModel mm) {
				String name = mm.methodName().stringValue();
				String desc = mm.methodType().stringValue();
				if (name.equals("clearEndOfTurnAttributes")
						&& desc.equals("(Lnet/demilich/metastone/game/entities/Entity;)V")) {
					clb.withMethodBody(name, mm.methodTypeSymbol(), mm.flags().flagsMask(), cob -> {
						cob.aload(0)
								.invokevirtual(CD_ENTITY, "getAttributes", MethodTypeDesc.of(CD_ATTR_MAP))
								.invokevirtual(CD_ATTR_MAP, "clearEndOfTurnAttributes", ConstantDescs.MTD_void)
								.return_();
					});
					return;
				}
				if (name.equals("castSpell") || name.equals("castChooseOneSpell")) {
					clb.transformMethod(mm, (mb, me) -> {
						if (me instanceof CodeModel cm) {
							mb.transformCode(cm, (cob, coe) -> {
								if (coe instanceof InvokeInstruction ii
										&& ii.opcode() == Opcode.INVOKEVIRTUAL
										&& ii.owner().asSymbol().equals(CD_SPELL)
										&& ii.name().equalsString("cast")
										&& ii.typeSymbol().equals(MTD_SPELL_CAST)) {
									cob.invokestatic(CD_COMPILER, "castSpell", mtdCastSpellBridge);
									return;
								}
								cob.with(coe);
							});
						} else {
							mb.with(me);
						}
					});
					return;
				}
			}
			clb.with(ce);
		});
		Files.write(gameLogicPath, wovenGameLogic);
	}

	// =========================================================================
	// 10. TargetLogic.class Weaving
	// =========================================================================

	private static void weaveTargetLogic(Path classesDir) throws IOException {
		Path path = classesDir.resolve("net/demilich/metastone/game/logic/TargetLogic.class");
		ClassFile cf = ClassFile.of();
		ClassModel model = cf.parse(path);

		MethodTypeDesc mtdResolveTargetKey = MethodTypeDesc.of(
				CD_LIST, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY_REF);
		MethodTypeDesc mtdRuntimeResolve = MethodTypeDesc.of(
				CD_LIST, CD_TARGET_LOGIC, CD_GAME_CONTEXT, CD_PLAYER, CD_ENTITY, CD_ENTITY_REF);

		byte[] woven = cf.build(model.thisClass().asSymbol(), clb -> {
			for (ClassElement ce : model) {
				if (ce instanceof MethodModel mm && mm.methodName().equalsString("resolveTargetKey")
						&& mm.methodTypeSymbol().equals(mtdResolveTargetKey)) {
					clb.withMethodBody("resolveTargetKey", mtdResolveTargetKey, mm.flags().flagsMask(), cob -> {
						cob.aload(0).aload(1).aload(2).aload(3).aload(4)
								.invokestatic(CD_TARGET_RUNTIME, "resolveTargetKey", mtdRuntimeResolve)
								.areturn();
					});
					continue;
				}
				clb.with(ce);
			}
		});
		Files.write(path, woven);
	}

	// =========================================================================
	// 11. Zero-Arg create() Memoization Weaving
	// =========================================================================

	private static void weaveZeroArgCreateMemoization(Path classesDir) throws IOException {
		String[] targets = {
				"net/demilich/metastone/game/spells/desc/filter/AndFilter.class",
				"net/demilich/metastone/game/spells/DrawCardSpell.class",
				"net/demilich/metastone/game/spells/desc/source/DeckSource.class",
				"net/demilich/metastone/game/spells/desc/source/GraveyardActorsSource.class",
				"net/demilich/metastone/game/spells/desc/source/HandSource.class",
				"net/demilich/metastone/game/spells/desc/source/UnweightedCatalogueSource.class"
		};
		for (String relPath : targets) {
			weaveMemoizedCreate(classesDir.resolve(relPath));
		}
	}

	private static void weaveMemoizedCreate(Path classPath) throws IOException {
		ClassFile cf = ClassFile.of();
		ClassModel model = cf.parse(classPath);
		if (hasMethod(model, "create$orig")) {
			return;
		}

		MethodModel targetMethod = null;
		for (MethodModel mm : model.methods()) {
			if (mm.methodName().equalsString("create") && mm.methodTypeSymbol().parameterCount() == 0) {
				targetMethod = mm;
				break;
			}
		}
		if (targetMethod == null) {
			return;
		}

		final MethodModel origCreate = targetMethod;
		final ClassDesc ownerDesc = model.thisClass().asSymbol();
		final ClassDesc returnTypeDesc = origCreate.methodTypeSymbol().returnType();
		final MethodTypeDesc mtdNoArg = MethodTypeDesc.of(returnTypeDesc);

		byte[] woven = cf.build(ownerDesc, clb -> {
			for (ClassElement ce : model) {
				if (ce instanceof MethodModel mm && mm.methodName().equalsString("create")
						&& mm.methodTypeSymbol().parameterCount() == 0) {
					clb.withMethod("create$orig", mtdNoArg, ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC, mb -> {
						for (MethodElement me : mm) {
							mb.with(me);
						}
					});
					continue;
				}
				clb.with(ce);
			}

			clb.withField("MEMOIZED_INSTANCE", returnTypeDesc, ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC);

			clb.withMethodBody("create", mtdNoArg, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> {
				Label nonNull = cob.newLabel();
				cob.getstatic(ownerDesc, "MEMOIZED_INSTANCE", returnTypeDesc)
						.dup()
						.ifnonnull(nonNull)
						.pop()
						.invokestatic(ownerDesc, "create$orig", mtdNoArg)
						.invokestatic(CD_COMPILER, "freezeMemoized",
								MethodTypeDesc.of(ConstantDescs.CD_Object, ConstantDescs.CD_Object))
						.checkcast(returnTypeDesc)
						.dup()
						.putstatic(ownerDesc, "MEMOIZED_INSTANCE", returnTypeDesc)
						.labelBinding(nonNull)
						.areturn();
			});
		});
		Files.write(classPath, woven);
	}
}
