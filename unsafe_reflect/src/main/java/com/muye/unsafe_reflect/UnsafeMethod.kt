package com.muye.unsafe_reflect

import android.annotation.TargetApi
import android.os.Build
import android.util.Log
import com.muye.unsafe_reflect.UnsafeClass.artFieldOrMethodOffset
import com.muye.unsafe_reflect.UnsafeClass.infoOffset
import com.muye.unsafe_reflect.UnsafeClass.memberOffset
import com.muye.unsafe_reflect.UnsafeClass.unsafe
import java.lang.invoke.MethodHandleInfo
import java.lang.invoke.MethodHandles
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.lang.reflect.Modifier

@TargetApi(Build.VERSION_CODES.O)
internal object UnsafeMethod {
    private const val TAG = "UnsafeReflect"
    private val classOffset = lazy {
        unsafe.value.objectFieldOffset(ClassHelper.Executable::class.java.getDeclaredField("declaringClass"))
    }

    private val methodOffset = lazy {
        unsafe.value.objectFieldOffset(ClassHelper.Executable::class.java.getDeclaredField("artMethod"))
    }

    private val methodsOffset = lazy {
        unsafe.value.objectFieldOffset(ClassHelper.MyClass::class.java.getDeclaredField("methods"))
    }

    private val artMethodSize = lazy {
        val artMethodPtrS = ClassHelper.NeverCall::class.java.getDeclaredMethod("s").run {
            isAccessible = true
            unsafe.value.getLong(this, methodOffset.value)
        }
        val artMethodPtrT = ClassHelper.NeverCall::class.java.getDeclaredMethod("t").run {
            isAccessible = true
            unsafe.value.getLong(this, methodOffset.value)
        }
        //Methods s and t are adjacent, so the difference between them is the size of an ArtMethod
        artMethodPtrT - artMethodPtrS
    }

    private val artMethodBias = lazy {
        //1. Get the value of NeverCall. methods_, which is the Length RefixedArray*
        val neverCallMethods =
            unsafe.value.getLong(ClassHelper.NeverCall::class.java, methodsOffset.value)
        //2. Get the address of the constructor, which is the first method in LengthRefixedArray
        val firstMethodPtr = ClassHelper.NeverCall::class.java.getConstructor().run {
            isAccessible = true
            unsafe.value.getLong(this, methodOffset.value)
        }
        //3. The difference between the two is the fixed offset, which is related to byte alignment,
        // usually 8 bytes, but we still calculate it here
        val artMethodBias = firstMethodPtr - neverCallMethods
        artMethodBias
    }

    private interface Callback {
        fun onMatchName(name: String): Boolean
        fun onVisited(method: Executable): Boolean
    }

    private fun foreachMethods(targetClass: Class<*>, callback: Callback) =
        runCatching {
            //1. Get the value of the target class. methods_, which is LengthPrefixedArray*
            val targetMethods = unsafe.value.getLong(targetClass, methodsOffset.value)
            if (targetMethods == 0L) {
                Log.e(TAG, "exemptAllByUnsafe: methods is null")
                return@runCatching
            }

            //2. The first member of LengthPrefixedArray is the size of the ArtMethod array
            val numMethods = unsafe.value.getInt(targetMethods)
            if (numMethods == 0) {
                return@runCatching
            }

            val methodHandle = MethodHandles.lookup()
                .unreflect(ClassHelper.NeverCall::class.java.getDeclaredMethod("s").apply {
                    isAccessible = true
                })
            for (i in 0 until numMethods) {
                /**
                 * 3. TargetMethods plus artMethodBias, i.e. offset, to obtain the starting address of the first ArtMethod,
                 * Then add the size of i ArtMethods to find the address of the i-th ArtMethod
                 */
                val artMethod: Long = targetMethods + artMethodBias.value + i * artMethodSize.value
                //Replace MethodHandle.artFieldOrMethod
                unsafe.value.putLong(methodHandle, artFieldOrMethodOffset.value, artMethod)
                //Empty MethodHandleImpl.info
                unsafe.value.putObject(methodHandle, infoOffset.value, null)
                //Retrieve MethodHandleImpl.info again
                UnsafeClass.revealDirect(targetClass, methodHandle)
                val info =
                    unsafe.value.getObject(methodHandle, infoOffset.value) as MethodHandleInfo
                if (!callback.onMatchName(info.name)) {
                    continue
                }
                val member =
                    (unsafe.value.getObject(info, memberOffset.value) as Executable).apply {
                        isAccessible = true
                    }
                if (callback.onVisited(member)) {
                    return@runCatching
                }
            }
        }

    fun getConstructors(targetClass: Class<*>): List<Constructor<*>> {
        val list = mutableListOf<Constructor<*>>()
        foreachMethods(targetClass, object : Callback {
            override fun onMatchName(name: String): Boolean = name == "<init>"
            override fun onVisited(method: Executable): Boolean {
                if (method !is Constructor<*> || Modifier.isStatic(method.modifiers)) return false
                list.add(method)
                return false
            }
        })
        return list
    }

    fun getConstructor(
        targetClass: Class<*>,
        parameterTypes: Array<Class<*>>? = null
    ): Constructor<*>? {
        var targetMethod: Constructor<*>? = null
        foreachMethods(targetClass, object : Callback {
            override fun onMatchName(name: String): Boolean = name == "<init>"
            override fun onVisited(method: Executable): Boolean {
                if (method !is Constructor<*> || Modifier.isStatic(method.modifiers)) return false
                if (!isMethodParameterMatch(method.parameterTypes, parameterTypes)) return false
                targetMethod = method
                return true
            }
        })
        return targetMethod
    }

    private fun getMethods(targetClass: Class<*>, isStatic: Boolean): List<Method> {
        val list = mutableListOf<Method>()
        foreachMethods(targetClass, object : Callback {
            override fun onMatchName(name: String): Boolean = true
            override fun onVisited(method: Executable): Boolean {
                if (Modifier.isStatic(method.modifiers) != isStatic || method !is Method) return false
                list.add(method)
                return false
            }
        })
        return list
    }

    private fun getMethod(
        targetClass: Class<*>,
        methodName: String,
        parameterTypes: Array<Class<*>>? = null,
        isStatic: Boolean
    ): Method? {
        var targetMethod: Method? = null
        foreachMethods(targetClass, object : Callback {
            override fun onMatchName(name: String): Boolean = name == methodName
            override fun onVisited(method: Executable): Boolean {
                if (Modifier.isStatic(method.modifiers) != isStatic || method !is Method) return false
                if (!isMethodParameterMatch(method.parameterTypes, parameterTypes)) return false
                targetMethod = method
                return true
            }
        })
        return targetMethod
    }

    fun getStaticMethods(targetClass: Class<*>) = getMethods(targetClass, true)

    fun getStaticMethod(
        targetClass: Class<*>,
        methodName: String,
        parameterTypes: Array<Class<*>>? = null
    ) = getMethod(targetClass, methodName, parameterTypes, true)

    fun getInstanceMethods(targetClass: Class<*>) = getMethods(targetClass, false)

    fun getInstanceMethod(
        targetClass: Class<*>,
        methodName: String,
        parameterTypes: Array<Class<*>>? = null
    ) = getMethod(targetClass, methodName, parameterTypes, false)

    fun invoke(
        targetClass: Class<*>,
        instance: Any?,
        methodName: String,
        parameterTypes: Array<Class<*>>? = null,
        vararg args: Any?
    ): Any? = runCatching {
        val method = getMethod(targetClass, methodName, parameterTypes, instance == null)
        method?.invoke(instance, *args)
    }.getOrNull()


    fun <T> newInstance(
        targetClass: Class<T>,
        parameterTypes: Array<Class<*>>?,
        vararg args: Any?
    ): T? =
        kotlin.runCatching {
            getConstructor(targetClass, parameterTypes)?.newInstance(*args) as T
        }.getOrNull()

    private fun isMethodParameterMatch(
        parameterTypes: Array<Class<*>>,
        args: Array<Class<*>>?
    ): Boolean {
        if (args == null) {
            return parameterTypes.isEmpty()
        }
        if (parameterTypes.size != args.size) {
            return false
        }
        if (!parameterTypes.zip(args).all {
                return@all it.first == it.second
            }) {
            return false
        }
        return true
    }
}
