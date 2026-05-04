# 核心概念

这篇文档解释 Sugar Next 的基础概念。先理解这些名词，再看模块文档会轻松很多。

## ContextHolder

`ContextHolder` 是上下文宿主。它代表一个应用、插件或模块，负责提供名称、版本和数据目录等基础信息。

`Context` 不应该凭空存在，它必须挂在某个宿主上。这样资源加载、数据目录和生命周期才有明确归属。

## Context

`Context` 是运行时上下文，负责管理：

- 生命周期
- 父上下文
- 属性
- BeanFactory
- Bean 查找
- 资源加载

它的生命周期顺序是：

```text
initialize -> load -> enable -> disable -> destroy
```

这个顺序不要乱。生命周期乱了，组件扫描、单例初始化和销毁顺序都会变得不可预测。

## Bean

`Bean<T>` 是容器中对象的包装。它不只是实例本身，还包含：

- 所属 `Context`
- Bean 名称
- 作用域
- 依赖信息
- 元数据注解
- 实际类型
- 实例获取方式

不要把 `Bean<T>` 和 `T` 混为一谈。前者是容器视角的对象描述，后者才是真正业务实例。

## BeanFactory

`BeanFactory<M extends Annotation>` 负责把某种注解标记的类型创建成 Bean 实例。

它有三个关键动作：

- `validate`：判断这个类型能不能注册成 Bean。
- `create`：创建原始实例。
- `proxy`：必要时包装或代理实例。

如果你要支持新的组件注解或新的创建策略，扩展点通常就是 `BeanFactory`。

## Metadata

Metadata 指 Bean 的注解元数据。例如：

- `@Component`
- `@Service`
- `@Repository`
- `@Configuration`
- `@Serialization`

有些注解可能是 `@Component` 的派生注解。容器会根据元数据决定使用哪个 `BeanFactory` 创建 Bean。

## Scope

`@Scope` 控制 Bean 的实例策略。

常见作用域：

- `singleton`：单例，容器中只有一个实例。
- `prototype`：原型，每次获取可能创建新实例。

需要长期复用的服务一般用单例。带状态、临时计算、不可复用的对象才考虑原型。滥用原型会制造不必要的对象和内存压力。

## DependsOn

`@DependsOn` 声明 Bean 或上下文启动的条件。它可以依赖：

- 其他 Bean
- 某些 class 是否存在
- 某些上下文属性是否存在或匹配指定值

它适合处理可选功能，不适合补救混乱的依赖关系。如果一个 Bean 必须依赖另一个 Bean，优先用构造器注入表达依赖。

## Aware

`Aware` 系列接口让 Bean 在创建后接收容器信息，例如：

- Bean 名称
- Bean 类型
- Context
- ContextHolder
- EventBus
- 元数据
- 自身代理对象

这类接口适合基础设施类使用。普通业务代码大量实现 `Aware`，通常说明对象边界设计得不干净。

## EventBus

`EventBus` 是发布订阅机制。它负责：

- 注册事件订阅者
- 发布事件
- 处理可取消事件
- 返回事件发布状态

事件适合解耦模块，不适合替代清晰的函数调用。一个动作如果必须同步得到结果，用普通方法调用更直接。

## Codec

`Codec` 同时是 `Serializer` 和 `Deserializer`。它负责把对象写出到流，或从流中读回对象。

`@Configuration` Bean 会根据文件扩展名寻找匹配的 `@Serialization` Codec。也就是说，配置加载能力不是魔法，它依赖你注册了能处理对应格式的 Codec。

## JavaCache 和 JavaClass

`bytecode` 模块使用 `JavaCache` 缓存 class 解析结果，用 `JavaClass` 表示 class 文件中的结构信息。

它读取的是字节码元数据，不等于 Java 反射。反射通常要求类已经可加载，字节码读取可以在不初始化类的情况下完成扫描。

## Reflection Proxy

`reflect` 模块通过接口和注解生成反射代理：

- `@ReflectConstructor` 访问构造器。
- `@ReflectField` 访问字段。
- `@ReflectMethod` 调用方法。

这个能力强，但也危险。突破可见性访问私有成员意味着你主动绕开了封装，文档和代码都必须把这种风险写清楚。

