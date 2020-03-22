/*
 * Copyright (c) 2013-2015, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *  
 *   or (per the licensee's choosing)
 *  
 * under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
package co.paralleluniverse.common.util;

import co.paralleluniverse.common.reflection.ASMUtil;
import java.lang.reflect.Constructor;
//import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author pron
 */
public class ExtendedStackTrace implements Iterable<ExtendedStackTraceElement> {
    public static ExtendedStackTrace of(Throwable t) {
        try {
            return new ExtendedStackTraceHotSpot(t);
        } catch (Throwable e) {
            return new ExtendedStackTrace(t);
        }
    }

    public static ExtendedStackTrace here() {
        try {
            return new ExtendedStackTraceHotSpot(new Throwable());
        } catch (Throwable e) {
            return new ExtendedStackTraceClassContext();
        }
    }

    protected final Throwable t;
    private ExtendedStackTraceElement[] est;
//    private transient Map<Class<?>, Member[]> methods; // cache

    protected ExtendedStackTrace(Throwable t) {
        this.t = t;
    }

    @Override
    public Iterator<ExtendedStackTraceElement> iterator() {
        return Arrays.asList(get()).iterator();
    }

    public ExtendedStackTraceElement[] get() {
        synchronized (this) {
            if (est == null) {
                StackTraceElement[] st = t.getStackTrace();
                if (st != null) {
                    est = new ExtendedStackTraceElement[st.length];
                    for (int i = 0; i < st.length; i++)
                        est[i] = new BasicExtendedStackTraceElement(st[i]);
                }
            }
            return est;
        }
    }

    protected /*Executable*/ Member getMethod(final ExtendedStackTraceElement este) {
        if (este.getDeclaringClass() == null)
            return null;
        Member[] ms = getMethods(este.getDeclaringClass());
        Member method = null;

        for (Member m : ms) {
            if (este.getMethodName().equals(m.getName())) {
                if (method == null)
                    method = m;
                else {
                    method = null; // more than one match
                    break;
                }
            }
        }
        if (method == null && este.getLineNumber() >= 0) {
            try {
                final AtomicReference<String> descriptor = new AtomicReference<>();
                ASMUtil.accept(este.getDeclaringClass(), ClassReader.SKIP_FRAMES, new ClassVisitor(Opcodes.ASM5) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, final String desc, String signature, String[] exceptions) {
                        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                        if (descriptor.get() == null && este.getMethodName().equals(name)) {
                            mv = new MethodVisitor(api, mv) {
                                int minLine = Integer.MAX_VALUE, maxLine = Integer.MIN_VALUE;

                                @Override
                                public void visitLineNumber(int line, Label start) {
                                    if (line < minLine)
                                        minLine = line;
                                    if (line > maxLine)
                                        maxLine = line;
                                }

                                @Override
                                public void visitEnd() {
                                    if (minLine <= este.getLineNumber() && maxLine >= este.getLineNumber())
                                        descriptor.set(desc);
                                    super.visitEnd();
                                }
                            };
                        }
                        return mv;
                    }
                });

                if (descriptor.get() != null) {
                    final String desc = descriptor.get();
                    for (Member m : ms) {
                        if (este.getMethodName().equals(getName(m)) && desc.equals(getDescriptor(m))) {
                            method = m;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                if (!(e instanceof UnsupportedOperationException))
                    e.printStackTrace();
            }
        }

        return method;
    }

    protected static final String getName(Member m) {
        if (m instanceof Constructor)
            return "<init>";
        return ((Method)m).getName();
    }

    protected static final String getDescriptor(Member m) {
        if (m instanceof Constructor)
            return Type.getConstructorDescriptor((Constructor) m);
        return Type.getMethodDescriptor((Method) m);
    }

    protected final Member[] getMethods(Class<?> clazz) {
//        synchronized (this) {
        Member[] es;
//            if (methods == null)
//                methods = new HashMap<>();
//            es = methods.get(clazz);
//            if (es == null) {
        Method[] ms = clazz.getDeclaredMethods();
        Constructor[] cs = clazz.getDeclaredConstructors();
        es = new Member[ms.length + cs.length];
        System.arraycopy(cs, 0, es, 0, cs.length);
        System.arraycopy(ms, 0, es, cs.length, ms.length);

//                methods.put(clazz, es);
//            }
        return es;
//        }
    }

    protected class BasicExtendedStackTraceElement extends ExtendedStackTraceElement {
        protected BasicExtendedStackTraceElement(StackTraceElement ste, Class<?> clazz, Method method, int bci) {
            super(ste, clazz, method, bci);
        }

        protected BasicExtendedStackTraceElement(StackTraceElement ste, Class<?> clazz) {
            super(ste, clazz, null, -1);
        }

        protected BasicExtendedStackTraceElement(StackTraceElement ste) {
            super(ste, null, null, -1);
        }

        @Override
        public Member getMethod() {
            if (method == null) {
                method = ExtendedStackTrace.this.getMethod(this);
                if (method != null && !getMethodName().equals(getName(method))) {
                    throw new IllegalStateException("Method name mismatch: " + getMethodName() + ", " + method.getName());
                    // method = null;
                }
            }
            return method;
        }

        @Override
        public Class<?> getDeclaringClass() {
            if (clazz == null) {
                try {
                    clazz = Class.forName(getClassName());
                } catch (ClassNotFoundException e) {
                }
            }
            return clazz;
        }
    }
}
