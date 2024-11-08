package com.muye.unsafe_reflect

import android.os.Build
import androidx.annotation.RequiresApi
import sun.misc.Unsafe
import java.lang.invoke.MethodHandles
import java.lang.reflect.Modifier

internal object UnsafeClass {
    val unsafe = lazy {
        Unsafe::class.java.getDeclaredMethod("getUnsafe").invoke(null) as Unsafe
    }

    val artFieldOrMethodOffset = lazy {
        unsafe.value.objectFieldOffset(ClassHelper.MethodHandle::class.java.getDeclaredField("artFieldOrMethod"))
    }

    val infoOffset = lazy {
        unsafe.value.objectFieldOffset(ClassHelper.MethodHandleImpl::class.java.getDeclaredField("info"))
    }

    val memberOffset = lazy {
        unsafe.value.objectFieldOffset(ClassHelper.HandleInfo::class.java.getDeclaredField("member"))
    }

    private val lookupClassOffset = lazy {
        unsafe.value.objectFieldOffset(ClassHelper.Lookup::class.java.getDeclaredField("lookupClass"))
    }

    private val allowedModesOffset = lazy {
        unsafe.value.objectFieldOffset(ClassHelper.Lookup::class.java.getDeclaredField("allowedModes"))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun revealDirect(targetClass: Class<*>, methodHandle: java.lang.invoke.MethodHandle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            MethodHandles.privateLookupIn(targetClass, MethodHandles.lookup())
                .revealDirect(methodHandle)
        } else {
            //Compatible with lower versions, self-test Android O is effective
            val lookup =
                unsafe.value.allocateInstance(MethodHandles.Lookup::class.java) as MethodHandles.Lookup
            unsafe.value.putObject(lookup, lookupClassOffset.value, targetClass)
            unsafe.value.putInt(
                lookup,
                allowedModesOffset.value,
                Modifier.PUBLIC or Modifier.PRIVATE or Modifier.PROTECTED or Modifier.STATIC
            )
            lookup.revealDirect(methodHandle)
        }
    }

    fun <T> allocateInstance(targetClass: Class<T>): T =
        unsafe.value.allocateInstance(targetClass) as T
}