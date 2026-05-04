# Reflect 反射代理

`reflect` 模块通过接口和注解创建反射代理，用方法调用的形式访问构造器、字段和方法。

## 它解决什么问题

直接写反射代码很容易变成重复的字符串、类型转换和异常处理。`Reflection.reflect(...)` 把这些绑定写到接口上，让调用端更清晰。

它适合访问第三方库、兼容不同版本 API 或封装少量底层反射操作。它不适合替代正常的 public API 设计。

## 创建反射代理

```java
TargetAccess access = Reflection.reflect(classLoader, TargetAccess.class, target);
```

要求：

- 反射类型必须是接口。
- 目标对象可以是实例，也可以通过注解指定静态目标。
- 未实现绑定注解的方法会抛出反射异常。

## 指定默认目标类

接口可以使用 `@Reflect` 指定默认目标类：

```java
@Reflect(name = "com.example.Target")
public interface TargetAccess {
}
```

如果方法注解没有指定声明类，会使用这个默认目标。

## 访问构造器

使用 `@ReflectConstructor`：

```java
public interface TargetAccess {
    @ReflectConstructor(declaringClass = "com.example.Target")
    Object create(String name);
}
```

方法返回类型必须能接收目标类实例。

## 访问字段

使用 `@ReflectField`：

```java
public interface TargetAccess {
    @ReflectField(name = "value", accessible = true)
    String value();

    @ReflectField(name = "value", accessible = true)
    void value(String value);
}
```

无参数且有返回值时通常是 getter；有一个参数时是 setter。

## 调用方法

使用 `@ReflectMethod`：

```java
public interface TargetAccess {
    @ReflectMethod(name = "reload", accessible = true)
    void reload();
}
```

代理会按接口方法参数类型查找目标方法。

## 静态成员

字段和方法注解支持静态成员。静态成员不需要实例目标。

静态反射要特别克制。大量修改静态状态会让测试和运行时行为变得难以预测。

## 默认方法

反射接口可以定义 default 方法。默认方法会通过 `MethodHandle` 调用。

这适合在接口里组合多个底层反射调用。

## accessible 的风险

`accessible = true` 会突破 Java 可见性限制。

这不是“高级用法”，这是主动绕过封装。只有在明确知道目标库版本、字段语义和兼容风险时才应该使用。

## 常见错误

1. 反射接口不是 interface。
2. 字段 getter 返回类型和目标字段类型不兼容。
3. 字段 setter 参数数量不是 1。
4. 方法参数类型和目标方法不匹配。
5. 目标类名写错。
6. 依赖私有字段但目标库升级后字段消失。

