# UnsafeReflect

**UnsafeReflect**是一个通过Unsafe API来实现JAVA 反射功能的库，能够绕过Android P及以上系统对**隐藏API**的限制。



## 一、特征

- 支持Android API21-API35。
- 能够绕过Android P及以上系统对**隐藏API**的限制。
- 提供不调用构造函数创建对象的方法`allocateInstance()`。



## 二、实现原理

[Android Hook - 隐藏API拦截机制](https://juejin.cn/post/7440716088898060339)

[Android Hook - 隐藏API绕过实践](https://juejin.cn/post/7440700915140001843)





## 三、快速开始

你可以参考[app](https://github.com/835127729/UnsafeReflect/tree/main/app)中的示例。

### 1、在 build.gradle.kts 中增加依赖

```kotlin
dependencies {
    implementation("com.github.835127729:UnsafeReflect:1.0.0")
}
```



### 2、功能介绍

#### 2.1、Method

```kotlin
//1、获取View静态方法列表
val staticMethods: List<Method> = UnsafeReflect.getStaticMethods(View::class.java)

//2、获取View指定静态方法printFlags()
val staticMethod: Method? = UnsafeReflect.getStaticMethod(
                View::class.java,
                "printFlags",
                arrayOf(Int::class.java)
            )

//3、获取View非静态方法列表
val instanceMethods = UnsafeReflect.getInstanceMethods(View::class.java)

//4、获取View指定非静态方法getFinalAlpha()
val instanceMethod = UnsafeReflect.getInstanceMethod(View::class.java,"getFinalAlpha")

//5、调用View指定静态方法debugIndent()
UnsafeReflect.invoke(View::class.java, null, "debugIndent", arrayOf(Int::class.java), 1)
```



#### 2.2、Field

```kotlin
//1、获取LinearLayout静态成员列表
val staticFields: List<Field> = UnsafeReflect.getStaticFields(LinearLayout::class.java)

//2、获取View指定静态成员VISIBILITY_FLAGS
val staticField: Field? = UnsafeReflect.getStaticField(View::class.java, "VISIBILITY_FLAGS")

//3、获取LinearLayout非静态成员列表
val instanceFields: List<Field> = UnsafeReflect.getInstanceFields(LinearLayout::class.java)

//4、获取View指定非静态成员mContext
val instanceFields: Field? = UnsafeReflect.getInstanceField(View::class.java, "mContext")

//5、获取View指定静态成员VISIBILITY_FLAGS的值
UnsafeReflect.getValue(View::class.java, null, "VISIBILITY_FLAGS")

//6、修改View指定非静态成员mContext的值
UnsafeReflect.setValue(View::class.java, linearLayout, "mContext", null)
```



#### 2.3、构造方法

```kotlin
//1、获取View构造方法列表
val constructors:List<Constructor<*>> = UnsafeReflect.getConstructors(View::class.java)

//2、获取View指定构造方法
val constructor: Constructor<*>? = UnsafeReflect.getConstructor(View::class.java, arrayOf(Context::class.java))

//3、调用View指定构造方法
val view: View = UnsafeReflect.newInstance(View::class.java, arrayOf(Context::class.java),context)

//4、不调用构造方法创建View对象
val view: View = UnsafeReflect.allocateInstance(View::class.java)
```





## 四、许可证

MapsVisitor 使用 [MIT 许可证](https://github.com/bytedance/bhook/blob/main/LICENSE) 授权。