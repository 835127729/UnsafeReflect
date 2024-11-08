package com.muye.unsafe_reflect

import android.annotation.TargetApi
import android.os.Build
import com.muye.unsafe_reflect.UnsafeClass.artFieldOrMethodOffset
import com.muye.unsafe_reflect.UnsafeClass.infoOffset
import com.muye.unsafe_reflect.UnsafeClass.memberOffset
import com.muye.unsafe_reflect.UnsafeClass.unsafe
import java.lang.invoke.MethodHandleInfo
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field

@TargetApi(Build.VERSION_CODES.O)
internal object UnsafeField {
    private val iFieldsOffset = lazy {
        unsafe.value.objectFieldOffset(ClassHelper.MyClass::class.java.getDeclaredField("iFields"))
    }

    private val sFieldsOffset = lazy {
        unsafe.value.objectFieldOffset(ClassHelper.MyClass::class.java.getDeclaredField("sFields"))
    }

    private val staticField = lazy {
        ClassHelper.NeverCall::class.java.getDeclaredField("a").apply {
            isAccessible = true
        }
    }
    private val instanceField = lazy {
        ClassHelper.NeverCall::class.java.getDeclaredField("i").apply {
            isAccessible = true
        }
    }

    private val artFieldPtrA = lazy {
        unsafe.value.getLong(
            MethodHandles.lookup().unreflectGetter(staticField.value),
            artFieldOrMethodOffset.value
        )
    }

    private val artFieldSize = lazy {
        val artFieldPtrB = ClassHelper.NeverCall::class.java.getDeclaredField("b").run {
            isAccessible = true
            val methodHandleB = MethodHandles.lookup().unreflectGetter(this)
            unsafe.value.getLong(methodHandleB, artFieldOrMethodOffset.value)
        }
        artFieldPtrB - artFieldPtrA.value
    }

    private val artFieldBias = lazy {
        val isFields =
            unsafe.value.getLong(ClassHelper.NeverCall::class.java, sFieldsOffset.value)
        val fieldBias = artFieldPtrA.value - isFields
        fieldBias
    }

    private interface Callback {
        fun onMatchName(name: String): Boolean
        fun onVisited(field: Field): Boolean
    }

    private fun foreachFields(targetClass: Class<*>, static: Boolean, callback: Callback) =
        runCatching {
            val targetFields = if (static) {
                unsafe.value.getLong(targetClass, sFieldsOffset.value)
            } else {
                unsafe.value.getLong(targetClass, iFieldsOffset.value)
            }
            if (targetFields == 0L) {
                return@runCatching
            }
            val numFields = unsafe.value.getInt(targetFields)
            if (numFields == 0) {
                return@runCatching
            }
            val methodHandle = if (static) {
                MethodHandles.lookup().unreflectGetter(staticField.value)
            } else {
                MethodHandles.lookup().unreflectGetter(instanceField.value)
            }
            for (i in 0 until numFields) {
                val field: Long = targetFields + artFieldBias.value + i * artFieldSize.value
                //Replace MethodHandle.artFieldOrMethod
                unsafe.value.putLong(methodHandle, artFieldOrMethodOffset.value, field)
                //Empty MethodHandleImpl.info
                unsafe.value.putObject(methodHandle, infoOffset.value, null)
                //Retrieve MethodHandleImpl.info again
                UnsafeClass.revealDirect(targetClass, methodHandle)
                val info =
                    unsafe.value.getObject(methodHandle, infoOffset.value) as MethodHandleInfo
                if (callback.onMatchName(info.name)) {
                    val member = (unsafe.value.getObject(info, memberOffset.value) as Field).apply {
                        isAccessible = true
                    }
                    if (callback.onVisited(member)) {
                        return@runCatching
                    }
                }
            }
        }

    private fun getFieldInner(targetClass: Class<*>, static: Boolean, fieldName: String): Field? =
        runCatching {
            var targetField: Field? = null
            foreachFields(targetClass, static, object : Callback {
                override fun onMatchName(name: String): Boolean = name == fieldName

                override fun onVisited(field: Field): Boolean {
                    targetField = field
                    return true
                }
            })
            targetField
        }.getOrNull()

    private fun getFieldsInner(targetClass: Class<*>, static: Boolean): List<Field> = runCatching {
        val list = mutableListOf<Field>()
        foreachFields(targetClass, static, object : Callback {
            override fun onMatchName(name: String): Boolean = true
            override fun onVisited(field: Field): Boolean {
                list.add(field)
                return false
            }
        })
        list
    }.getOrDefault(emptyList())

    fun getStaticFields(targetClass: Class<*>): List<Field> = getFieldsInner(targetClass, true)

    fun getInstanceFields(targetClass: Class<*>): List<Field> = getFieldsInner(targetClass, false)

    fun getStaticField(targetClass: Class<*>, fieldName: String): Field? =
        getFieldInner(targetClass, true, fieldName)

    fun getInstanceField(targetClass: Class<*>, fieldName: String): Field? =
        getFieldInner(targetClass, false, fieldName)

    fun getValue(
        targetClass: Class<*>,
        instance: Any?,
        fieldName: String
    ): Any? = runCatching {
        getFieldInner(targetClass, instance == null, fieldName)?.run {
            isAccessible = true
            get(instance)
        }
    }.getOrNull()

    fun setValue(
        targetClass: Class<*>,
        instance: Any?,
        fieldName: String, value: Any?
    ): Boolean = runCatching {
        getFieldInner(targetClass, instance == null, fieldName)?.run {
            isAccessible = true
            set(instance, value)
            true
        } ?: false
    }.getOrDefault(false)
}
