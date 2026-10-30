package com.hiddenswitch.diagnostics;

import java.io.IOException;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Label;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LineNumber;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Compile-time bytecode weaver for {@code spellsource-server} using the JDK {@link java.lang.classfile} API.
 * <p>
 * Performs two transformations:
 * <ol>
 *   <li>Rewrites {@code Vertx.eventBus()} and {@code EventBus.consumer(String, Handler)} call sites to
 *       {@link Tracing#eventBus(io.vertx.core.Vertx)} and
 *       {@link Tracing#consumer(io.vertx.core.eventbus.EventBus, String, io.vertx.core.Handler)} so that
 *       OpenTracing {@code SpanContext} propagates automatically across EventBus boundaries and onto virtual
 *       thread handlers without DTO serializer boilerplate.</li>
 *   <li>Weaves {@link Traced}-annotated methods and constructors with {@link Tracing#enterSpan} /
 *       {@code Scope.close()} so Java callstacks and distributed OpenTracing spans stay in 1:1 correspondence
 *       without manual {@code GlobalTracer} ceremony.</li>
 * </ol>
 */
public final class ServerClassfileWeaver {
	private static final String TRACED_DESC = "Lcom/hiddenswitch/diagnostics/Traced;";
	private static final ClassDesc CD_TRACING = ClassDesc.of("com.hiddenswitch.diagnostics.Tracing");
	private static final ClassDesc CD_VERTX = ClassDesc.of("io.vertx.core.Vertx");
	private static final ClassDesc CD_EVENT_BUS = ClassDesc.of("io.vertx.core.eventbus.EventBus");
	private static final ClassDesc CD_HANDLER = ClassDesc.of("io.vertx.core.Handler");
	private static final ClassDesc CD_MESSAGE_CONSUMER = ClassDesc.of("io.vertx.core.eventbus.MessageConsumer");
	private static final ClassDesc CD_SCOPE = ClassDesc.of("io.opentracing.Scope");
	private static final ClassDesc CD_SPAN_CONTEXT = ClassDesc.of("io.opentracing.SpanContext");
	private static final ClassDesc CD_GAME_CONTEXT = ClassDesc.of("net.demilich.metastone.game.GameContext");

	private static final MethodTypeDesc MTD_TRACING_EVENT_BUS = MethodTypeDesc.of(CD_EVENT_BUS, CD_VERTX);
	private static final MethodTypeDesc MTD_TRACING_CONSUMER = MethodTypeDesc.of(
			CD_MESSAGE_CONSUMER, CD_EVENT_BUS, ConstantDescs.CD_String, CD_HANDLER);
	private static final MethodTypeDesc MTD_TRACING_HANDLER = MethodTypeDesc.of(
			CD_MESSAGE_CONSUMER, CD_MESSAGE_CONSUMER, CD_HANDLER);
	private static final MethodTypeDesc MTD_ENTER_SPAN = MethodTypeDesc.of(
			CD_SCOPE, ConstantDescs.CD_String, CD_SPAN_CONTEXT);
	private static final MethodTypeDesc MTD_ACTIVE_SPAN_CTX = MethodTypeDesc.of(CD_SPAN_CONTEXT);
	private static final MethodTypeDesc MTD_GET_SPAN_CTX = MethodTypeDesc.of(CD_SPAN_CONTEXT);
	private static final MethodTypeDesc MTD_SET_SPAN_CTX = MethodTypeDesc.of(CD_GAME_CONTEXT, CD_SPAN_CONTEXT);

	private ServerClassfileWeaver() {
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			throw new IllegalArgumentException("Expected classes directory path as argument");
		}
		weaveAll(Path.of(args[0]));
	}

	public static void weaveAll(Path classesDir) throws IOException {
		if (!Files.isDirectory(classesDir)) {
			return;
		}
		ClassFile cf = ClassFile.of();
		Files.walkFileTree(classesDir, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				String rel = classesDir.relativize(dir).toString().replace('\\', '/');
				if (rel.startsWith("com/hiddenswitch/framework/schema")
						|| rel.startsWith("com/hiddenswitch/framework/graphql")
						|| rel.startsWith("com/hiddenswitch/framework/rpc")) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.getFileName().toString().endsWith(".class")) {
					weaveClassFile(cf, file);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void weaveClassFile(ClassFile cf, Path file) throws IOException {
		byte[] raw = Files.readAllBytes(file);
		ClassModel model = cf.parse(raw);
		String internalName = model.thisClass().asInternalName();
		if (!internalName.startsWith("com/hiddenswitch/framework/")) {
			return;
		}
		boolean isServerGameContext = internalName.equals("com/hiddenswitch/framework/impl/ServerGameContext");
		String simpleClassName = internalName.substring(internalName.lastIndexOf('/') + 1);

		boolean[] modified = {false};
		byte[] transformed = cf.transformClass(model, (clb, ce) -> {
			if (ce instanceof MethodModel mm && mm.code().isPresent()) {
				String tracedOp = getTracedOperationName(mm, simpleClassName);
				boolean hasEventBusCall = containsEventBusCall(mm.code().get());
				if (tracedOp != null || hasEventBusCall) {
					modified[0] = true;
					boolean isStatic = (mm.flags().flagsMask() & ClassFile.ACC_STATIC) != 0;
					boolean isConstructor = mm.methodName().equalsString(ConstantDescs.INIT_NAME);
					clb.transformMethod(mm, (mb, me) -> {
						if (me instanceof CodeModel cm) {
							mb.transformCode(cm, createCodeTransform(
									tracedOp, isStatic, isConstructor, isServerGameContext));
						} else {
							mb.with(me);
						}
					});
					return;
				}
			}
			clb.with(ce);
		});

		if (modified[0]) {
			Files.write(file, transformed);
		}
	}

	private static String getTracedOperationName(MethodModel mm, String simpleClassName) {
		for (var element : mm) {
			if (element instanceof RuntimeVisibleAnnotationsAttribute attrs) {
				for (Annotation ann : attrs.annotations()) {
					if (ann.className().equalsString(TRACED_DESC)) {
						for (AnnotationElement ae : ann.elements()) {
							if (ae.name().equalsString("value") && ae.value() instanceof AnnotationValue.OfString s) {
								String val = s.stringValue();
								if (!val.isEmpty()) {
									return val;
								}
							}
						}
						String methodName = mm.methodName().stringValue();
						return simpleClassName + "/" + (methodName.equals(ConstantDescs.INIT_NAME) ? "init" : methodName);
					}
				}
			}
		}
		return null;
	}

	private static boolean containsEventBusCall(CodeModel cm) {
		for (CodeElement ce : cm) {
			if (ce instanceof InvokeInstruction inv && inv.opcode() == Opcode.INVOKEINTERFACE) {
				String owner = inv.owner().asInternalName();
				String name = inv.name().stringValue();
				String desc = inv.type().stringValue();
				if (owner.equals("io/vertx/core/Vertx")
						&& name.equals("eventBus")
						&& desc.equals("()Lio/vertx/core/eventbus/EventBus;")) {
					return true;
				}
				if (owner.equals("io/vertx/core/eventbus/EventBus")
						&& name.equals("consumer")
						&& desc.equals("(Ljava/lang/String;Lio/vertx/core/Handler;)Lio/vertx/core/eventbus/MessageConsumer;")) {
					return true;
				}
				if (owner.equals("io/vertx/core/eventbus/MessageConsumer")
						&& name.equals("handler")
						&& desc.equals("(Lio/vertx/core/Handler;)Lio/vertx/core/eventbus/MessageConsumer;")) {
					return true;
				}
			}
		}
		return false;
	}

	private static CodeTransform createCodeTransform(
			String tracedOp,
			boolean isStatic,
			boolean isConstructor,
			boolean isServerGameContext) {
		return new CodeTransform() {
			private int scopeSlot = -1;
			private boolean spanEntered = false;
			private int uninitializedCount = isConstructor ? 1 : 0;
			private Label tryStart;
			private Label tryEnd;
			private int firstLine = -1;

			@Override
			public void atStart(CodeBuilder cob) {
				if (tracedOp != null && !isConstructor) {
					emitEnterSpan(cob);
				}
			}

			private void emitEnterSpan(CodeBuilder cob) {
				scopeSlot = cob.allocateLocal(TypeKind.REFERENCE);
				cob.ldc(tracedOp);
				if (!isStatic && isServerGameContext) {
					cob.aload(0)
							.invokevirtual(CD_GAME_CONTEXT, "getSpanContext", MTD_GET_SPAN_CTX);
				} else {
					cob.aconst_null();
				}
				cob.invokestatic(CD_TRACING, "enterSpan", MTD_ENTER_SPAN)
						.astore(scopeSlot);
				if (!isStatic && isServerGameContext && (isConstructor || tracedOp.endsWith("/play") || tracedOp.endsWith("/resume"))) {
					cob.aload(0)
							.invokestatic(CD_TRACING, "activeSpanContext", MTD_ACTIVE_SPAN_CTX)
							.invokevirtual(CD_GAME_CONTEXT, "setSpanContext", MTD_SET_SPAN_CTX)
							.pop();
				}
				tryStart = cob.newBoundLabel();
				spanEntered = true;
			}

			@Override
			public void accept(CodeBuilder cob, CodeElement coe) {
				if (coe instanceof LineNumber ln && firstLine == -1) {
					firstLine = ln.line();
				}
				if (tracedOp != null && isConstructor && !spanEntered) {
					if (coe instanceof NewObjectInstruction) {
						uninitializedCount++;
					} else if (coe instanceof InvokeInstruction inv
							&& inv.opcode() == Opcode.INVOKESPECIAL
							&& inv.name().equalsString(ConstantDescs.INIT_NAME)) {
						uninitializedCount--;
						if (uninitializedCount == 0) {
							cob.with(coe);
							emitEnterSpan(cob);
							return;
						}
					}
				}
				if (coe instanceof InvokeInstruction inv && inv.opcode() == Opcode.INVOKEINTERFACE) {
					String owner = inv.owner().asInternalName();
					String name = inv.name().stringValue();
					String desc = inv.type().stringValue();
					if (owner.equals("io/vertx/core/Vertx")
							&& name.equals("eventBus")
							&& desc.equals("()Lio/vertx/core/eventbus/EventBus;")) {
						cob.invokestatic(CD_TRACING, "eventBus", MTD_TRACING_EVENT_BUS);
						return;
					}
					if (owner.equals("io/vertx/core/eventbus/EventBus")
							&& name.equals("consumer")
							&& desc.equals("(Ljava/lang/String;Lio/vertx/core/Handler;)Lio/vertx/core/eventbus/MessageConsumer;")) {
						cob.invokestatic(CD_TRACING, "consumer", MTD_TRACING_CONSUMER);
						return;
					}
					if (owner.equals("io/vertx/core/eventbus/MessageConsumer")
							&& name.equals("handler")
							&& desc.equals("(Lio/vertx/core/Handler;)Lio/vertx/core/eventbus/MessageConsumer;")) {
						cob.invokestatic(CD_TRACING, "handler", MTD_TRACING_HANDLER);
						return;
					}
				}
				if (spanEntered && coe instanceof ReturnInstruction) {
					cob.aload(scopeSlot)
							.invokeinterface(CD_SCOPE, "close", ConstantDescs.MTD_void);
				}
				cob.with(coe);
			}

			@Override
			public void atEnd(CodeBuilder cob) {
				if (spanEntered && tryStart != null) {
					tryEnd = cob.newBoundLabel();
					Label catchHandler = cob.newBoundLabel();
					if (firstLine != -1) {
						cob.lineNumber(firstLine);
					}
					cob.aload(scopeSlot)
							.invokeinterface(CD_SCOPE, "close", ConstantDescs.MTD_void)
							.athrow();
					cob.exceptionCatchAll(tryStart, tryEnd, catchHandler);
				}
			}
		};
	}
}
