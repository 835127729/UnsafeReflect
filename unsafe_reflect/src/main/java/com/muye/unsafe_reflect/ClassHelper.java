package com.muye.unsafe_reflect;

import androidx.annotation.Keep;

import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

@Keep
class ClassHelper {
    @Keep
    static final public class MyClass {
        private transient ClassLoader classLoader;
        private transient Class<?> componentType;
        private transient Object dexCache;
        private transient Object extData;
        private transient Object[] ifTable;
        private transient String name;
        private transient Class<?> superClass;
        private transient Object vtable;
        private transient long iFields;
        private transient long methods;
        private transient long sFields;
        private transient int accessFlags;
        private transient int classFlags;
        private transient int classSize;
        private transient int clinitThreadId;
        private transient int dexClassDefIndex;
        private transient volatile int dexTypeIndex;
        private transient int numReferenceInstanceFields;
        private transient int numReferenceStaticFields;
        private transient int objectSize;
        private transient int objectSizeAllocFastPath;
        private transient int primitiveType;
        private transient int referenceInstanceOffsets;
        private transient int status;
        private transient short copiedMethodsOffset;
        private transient short virtualMethodsOffset;
    }

    @Keep
    static public class AccessibleObject {
        private boolean override;
    }

    @Keep
    static final public class Executable extends AccessibleObject {
        private int accessFlags;
        private long artMethod;
        private Class declaringClass;
        private Class declaringClassOfOverriddenMethod;
        private Object[] parameters;
    }

    @Keep
    static public class MethodHandle {
        private final MethodType type = null;
        private MethodType nominalType;
        private MethodHandle cachedSpreadInvoker;
        protected final int handleKind = 0;

        // The ArtMethod* or ArtField* associated with this method handle (used by the runtime).
        protected final long artFieldOrMethod = 0;
    }

    @Keep
    static final public class MethodHandleImpl extends MethodHandle {
        private final MethodHandleInfo info = null;
    }

    @Keep
    static final public class HandleInfo {
        private final Member member = null;
        private final MethodHandle handle = null;
    }

    @Keep
    public static final class Lookup {
        private final Class<?> lookupClass = null;
        private final int allowedModes = 0;
    }

    @Keep
    static final class NeverCall {
        private static int a;
        private static int b;
        private int i;

        public NeverCall() {
        }

        public static void s() {
        }

        public static void t() {
        }
    }
}
