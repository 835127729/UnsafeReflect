package com.muye.unsafe_reflect

import android.os.Build
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier


private interface UnsafeReflectApi {
    //Retrieve all constructor methods of the specified class, including private constructor methods
    fun getConstructors(targetClass: Class<*>): List<Constructor<*>>

    //Get the specified constructor method of the specified class, including private constructor methods
    fun getConstructor(
        targetClass: Class<*>,
        parameterTypes: Array<Class<*>>? = null
    ): Constructor<*>?

    //Retrieve all static methods of the specified class, including private static methods
    fun getStaticMethods(targetClass: Class<*>): List<Method>

    //Get the specified static methods of the specified class, including private static methods
    fun getStaticMethod(
        targetClass: Class<*>,
        methodName: String,
        parameterTypes: Array<Class<*>>? = null
    ): Method?

    //Retrieve all member methods of the specified class, including private member methods
    fun getInstanceMethods(targetClass: Class<*>): List<Method>

    //Get the specified member methods of the specified class, including private member methods
    fun getInstanceMethod(
        targetClass: Class<*>,
        methodName: String,
        parameterTypes: Array<Class<*>>? = null
    ): Method?

    //Reflection calls the specified method
    fun invoke(
        targetClass: Class<*>,
        instance: Any?,
        methodName: String,
        parameterTypes: Array<Class<*>>? = null,
        vararg args: Any?
    ): Any?

    //Reflection call constructor
    fun <T> newInstance(clazz: Class<T>, parameterTypes: Array<Class<*>>?, vararg args: Any?): T?

    fun <T> allocateInstance(clazz: Class<T>): T

    //Retrieve all static properties of the specified class, including private properties and excluding parent class properties
    fun getStaticFields(targetClass: Class<*>): List<Field>

    //Retrieve specified static properties of the specified class, including private properties but excluding parent class properties
    fun getStaticField(targetClass: Class<*>, fieldName: String): Field?

    //Retrieve all member properties of the specified class, including private properties and excluding parent class properties
    fun getInstanceFields(targetClass: Class<*>): List<Field>

    //Retrieve specified member properties of the specified class, including private properties but excluding parent class properties
    fun getInstanceField(targetClass: Class<*>, fieldName: String): Field?

    //Retrieve the specified attribute value
    fun getValue(targetClass: Class<*>, instance: Any?, fieldName: String): Any?

    //Set specified attribute values
    fun setValue(targetClass: Class<*>, instance: Any?, fieldName: String, value: Any?): Boolean
}

private object UnsafeReflectApi21 : UnsafeReflectApi {
    override fun getConstructors(targetClass: Class<*>) = targetClass.constructors.toList()

    override fun getConstructor(
        targetClass: Class<*>,
        parameterTypes: Array<Class<*>>?
    ): Constructor<*>? = kotlin.runCatching {
        if (parameterTypes == null) {
            targetClass.getConstructor()
        } else {
            targetClass.getConstructor(*parameterTypes)
        }
    }.getOrNull()

    override fun getStaticMethods(targetClass: Class<*>) =
        targetClass.declaredMethods.filter { Modifier.isStatic(it.modifiers) }.toList()

    override fun getStaticMethod(
        targetClass: Class<*>,
        methodName: String,
        parameterTypes: Array<Class<*>>?
    ): Method? = kotlin.runCatching {
        if (parameterTypes == null) {
            targetClass.getDeclaredMethod(methodName)
        } else {
            targetClass.getDeclaredMethod(methodName, *parameterTypes)
        }.takeIf { Modifier.isStatic(it.modifiers) }
    }.getOrNull()

    override fun getInstanceMethods(targetClass: Class<*>) =
        targetClass.declaredMethods.filter { !Modifier.isStatic(it.modifiers) }.toList()

    override fun getInstanceMethod(
        targetClass: Class<*>,
        methodName: String,
        parameterTypes: Array<Class<*>>?
    ): Method? = kotlin.runCatching {
        if (parameterTypes == null) {
            targetClass.getDeclaredMethod(methodName)
        } else {
            targetClass.getDeclaredMethod(methodName, *parameterTypes)
        }.takeIf { !Modifier.isStatic(it.modifiers) }
    }.getOrNull()

    override fun invoke(
        targetClass: Class<*>,
        instance: Any?,
        methodName: String,
        parameterTypes: Array<Class<*>>?,
        vararg args: Any?
    ): Any? = runCatching {
        if (parameterTypes == null) {
            targetClass.getDeclaredMethod(methodName)
        } else {
            targetClass.getDeclaredMethod(methodName, *parameterTypes)
        }.run {
            isAccessible = true
            invoke(instance, *args)
        }
    }.getOrNull()

    override fun <T> newInstance(
        clazz: Class<T>,
        parameterTypes: Array<Class<*>>?,
        vararg args: Any?
    ): T? = runCatching {
        if (parameterTypes == null) {
            clazz.getConstructor()
        } else {
            clazz.getConstructor(*parameterTypes)
        }.run {
            isAccessible = true
            newInstance(*args)
        }
    }.getOrNull()

    override fun <T> allocateInstance(clazz: Class<T>): T = UnsafeClass.allocateInstance(clazz)

    override fun getStaticFields(targetClass: Class<*>): List<Field> =
        targetClass.declaredFields.filter { Modifier.isStatic(it.modifiers) }.toList()

    override fun getStaticField(targetClass: Class<*>, fieldName: String): Field? =
        kotlin.runCatching {
            targetClass.getDeclaredField(fieldName).takeIf { Modifier.isStatic(it.modifiers) }
        }.getOrNull()

    override fun getInstanceFields(targetClass: Class<*>): List<Field> =
        targetClass.declaredFields.filter { !Modifier.isStatic(it.modifiers) }.toList()

    override fun getInstanceField(targetClass: Class<*>, fieldName: String): Field? =
        kotlin.runCatching {
            targetClass.getDeclaredField(fieldName).takeIf { !Modifier.isStatic(it.modifiers) }
        }.getOrNull()


    override fun getValue(targetClass: Class<*>, instance: Any?, fieldName: String) =
        runCatching {
            targetClass.getDeclaredField(fieldName).run {
                isAccessible = true
                get(instance)
            }
        }.getOrNull()

    override fun setValue(
        targetClass: Class<*>,
        instance: Any?,
        fieldName: String,
        value: Any?
    ): Boolean = runCatching {
        targetClass.getDeclaredField(fieldName).run {
            isAccessible = true
            set(instance, value)
        }
        true
    }.getOrDefault(false)
}

private object UnsafeReflectApi26 : UnsafeReflectApi {
    override fun getConstructors(targetClass: Class<*>) = UnsafeMethod.getConstructors(targetClass)

    override fun getConstructor(
        targetClass: Class<*>,
        parameterTypes: Array<Class<*>>?
    ) = UnsafeMethod.getConstructor(targetClass, parameterTypes)

    override fun getStaticMethods(targetClass: Class<*>) =
        UnsafeMethod.getStaticMethods(targetClass)

    override fun getStaticMethod(
        targetClass: Class<*>,
        methodName: String,
        parameterTypes: Array<Class<*>>?
    ) = UnsafeMethod.getStaticMethod(targetClass, methodName, parameterTypes)

    override fun getInstanceMethods(targetClass: Class<*>) =
        UnsafeMethod.getInstanceMethods(targetClass)

    override fun getInstanceMethod(
        targetClass: Class<*>,
        methodName: String,
        parameterTypes: Array<Class<*>>?
    ) = UnsafeMethod.getInstanceMethod(targetClass, methodName, parameterTypes)

    override fun invoke(
        targetClass: Class<*>,
        instance: Any?,
        methodName: String,
        parameterTypes: Array<Class<*>>?,
        vararg args: Any?
    ) = UnsafeMethod.invoke(targetClass, instance, methodName, parameterTypes, *args)

    override fun <T> newInstance(
        clazz: Class<T>,
        parameterTypes: Array<Class<*>>?,
        vararg args: Any?
    ) = UnsafeMethod.newInstance(clazz, parameterTypes, *args)

    override fun <T> allocateInstance(clazz: Class<T>): T = UnsafeClass.allocateInstance(clazz)

    override fun getStaticFields(targetClass: Class<*>) = UnsafeField.getStaticFields(targetClass)

    override fun getStaticField(targetClass: Class<*>, fieldName: String) =
        UnsafeField.getStaticField(targetClass, fieldName)

    override fun getInstanceFields(targetClass: Class<*>) =
        UnsafeField.getInstanceFields(targetClass)

    override fun getInstanceField(targetClass: Class<*>, fieldName: String) =
        UnsafeField.getInstanceField(targetClass, fieldName)

    override fun getValue(targetClass: Class<*>, instance: Any?, fieldName: String) =
        UnsafeField.getValue(targetClass, instance, fieldName)

    override fun setValue(targetClass: Class<*>, instance: Any?, fieldName: String, value: Any?) =
        UnsafeField.setValue(targetClass, instance, fieldName, value)
}

/**
 *Use unsafe for reflection calls
 *Android O (8.0) and above versions only use unsafe for reflection, otherwise use Java reflection.
 *The reason is that APIs such as Unsafe. getInt (long) and MethodProcesses require Android 0 or above to support.
 */
object UnsafeReflect : UnsafeReflectApi by if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    UnsafeReflectApi26
} else {
    UnsafeReflectApi21
}