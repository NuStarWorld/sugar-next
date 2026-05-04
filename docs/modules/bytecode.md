# Bytecode 字节码读取

`bytecode` 模块用于读取 class 文件中的结构信息。它基于 ASM，不需要像普通反射那样先初始化目标类。

## 它解决什么问题

组件扫描时，如果直接加载每一个 class，可能触发静态初始化或类加载副作用。字节码读取可以先判断 class 是否有目标注解，再决定是否加载。

这就是 `context` 扫描组件时使用字节码模块的原因。

## Java.typeof

入口方法：

```java
JavaClass javaClass = Java.typeof("com.example.UserService");
```

也可以传入 `Class<?>` 或对象：

```java
JavaClass javaClass = Java.typeof(UserService.class);
```

## JavaCache

`JavaCache` 缓存 class 解析结果：

```java
JavaCache cache = new JavaCache();
JavaClass first = Java.typeof("com.example.UserService", cache);
JavaClass second = Java.typeof("com.example.UserService", cache);
```

扫描大量 class 时必须复用缓存。每次都新建缓存就是浪费 CPU 和 IO。

## JavaClass

`JavaClass` 表示一个类的字节码结构，可以读取：

- 类名
- 包名
- 父类
- 接口
- 内部类
- 构造器
- 字段
- 方法
- 注解
- class 文件版本
- access flags

## JavaField、JavaMethod、JavaConstructor

这些接口分别表示字段、方法和构造器。它们提供结构信息，而不是直接执行成员调用。

如果你要调用方法或访问字段，用 `reflect` 模块或 Java 反射。别把字节码模块当执行引擎。

## JavaAnnotation

`JavaAnnotation` 表示字节码里的注解结构。扫描组件时可以通过它判断某个类是否带有组件元数据。

这比加载类后调用 `Class#getAnnotations()` 更轻，因为它可以避免类初始化。

## 与反射的区别

| 能力 | bytecode | reflection |
| --- | --- | --- |
| 是否需要加载类 | 不一定 | 需要 |
| 是否能调用方法 | 不能 | 能 |
| 是否适合扫描 | 适合 | 扫描大量类时成本更高 |
| 是否读取运行时实例 | 不能 | 能 |

## 使用限制

- class 文件必须能从 classpath、jar 或指定输入流读取。
- 字节码结构不等于运行时行为。
- 泛型、桥接方法、注解默认值等复杂情况需要谨慎验证。
- 解析大量 class 时要复用 `JavaCache`。

