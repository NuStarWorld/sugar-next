# Context 容器

`context` 是 Sugar Next 的核心模块。它负责生命周期、父上下文、属性、BeanFactory、组件扫描、Bean 创建、依赖注入和资源加载。

## 它解决什么问题

当一个应用或插件需要统一管理启动、加载、启用、禁用、销毁流程时，散落在各处的手写初始化代码很快会变成垃圾。`Context` 把这些流程收拢到一个明确的生命周期里，并且提供 Bean 容器和资源加载能力。

它适合做轻量运行时容器，不适合伪装成完整应用框架。

## 创建 Context

入口方法是：

```java
Context context = Context.of(contextHolder, contextLifecycle, eventBus);
```

三个参数分别是：

- `ContextHolder`：宿主对象，提供名称、版本、数据目录等信息。
- `ContextLifecycle`：宿主生命周期回调。
- `EventBus`：事件总线。

创建之后按顺序调用：

```java
context.initialize();
context.load();
context.enable();

context.disable();
context.destroy();
```

生命周期顺序不要乱。跳过阶段或重复调用都会让状态机进入不可靠状态。

## 生命周期

生命周期顺序：

```text
destroyed -> initialized -> loaded -> enabled -> disabled -> destroyed
```

对应方法：

| 方法 | 作用 |
| --- | --- |
| `initialize()` | 初始化上下文，加载启动元数据，注册默认 BeanFactory |
| `load()` | 扫描组件并注册 Bean |
| `enable()` | 初始化单例 Bean，使上下文进入活动状态 |
| `disable()` | 禁用上下文 |
| `destroy()` | 销毁实例，清理父上下文、属性、Bean 和缓存 |

`destroy()` 会按实例创建的反序销毁实现了 `Destroyable` 的对象。这个顺序是对的，因为后创建的对象通常依赖先创建的对象。

## 属性管理

注册属性：

```java
context.registerProperty("profile", "dev");
```

读取属性：

```java
ContextProperty property = context.getProperty("profile");
```

默认会继承父上下文属性。如果只想查当前上下文：

```java
ContextProperty property = context.getProperty("profile", false);
```

属性适合表达环境和功能开关，不适合塞大量业务配置。业务配置应该用 `@Configuration` 和 `Codec`。

## 父上下文

`Context` 支持父上下文。Bean 和属性查询默认可以向父上下文继承。

这种设计适合插件依赖宿主、模块依赖公共上下文的场景。不要把父上下文当全局变量垃圾桶，否则依赖关系会变得不可读。

## Bean 查找

按名称查找：

```java
Bean<?> bean = context.getBean("userService");
```

按类型查找：

```java
Bean<UserService> bean = context.getBean(UserService.class);
```

查找多个：

```java
List<Bean<UserService>> beans = context.getBeans(UserService.class);
```

默认查找会继承父上下文。如果只查当前上下文：

```java
Bean<UserService> bean = context.getBean(UserService.class, false);
```

如果同类型存在多个 Bean，按类型查单个 Bean 会有歧义。不要让容器替你猜，应该用名称或 `@Qualifier` 明确指定。

## 组件扫描

组件扫描由启动元数据控制。常见方式是在宿主类或其注解上使用 `@Scan`。

扫描结果会通过字节码读取判断类是否可能是组件，之后再加载符合条件的类。这样做比无脑加载所有类更合理，因为类加载有副作用。

## 组件注解

常见组件注解：

| 注解 | 用途 |
| --- | --- |
| `@Component` | 通用组件 |
| `@Service` | 服务组件 |
| `@Repository` | 存储库组件 |
| `@Controller` | 控制器组件 |
| `@Configuration` | 配置对象 |
| `@Serialization` | Codec 组件 |
| `@Supplier` | 从配置类方法供应 Bean |

`@Service`、`@Repository`、`@Controller` 这类注解本质上是组件语义的细分。它们的价值在于表达意图，不是制造复杂继承体系。

## 依赖注入

构造器或方法可以使用 `@Autowired`：

```java
@Service
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
}
```

支持注入：

- 普通 Bean 实例
- `Bean<T>`
- `Lazy<T>`
- `List<T>`
- `Map<String, T>`
- `Context`
- `ContextHolder`

使用 `@Qualifier` 指定 Bean 名称：

```java
@Autowired
public UserController(@Qualifier("mainUserService") UserService userService) {
    this.userService = userService;
}
```

使用 `@Owned` 可以限制不从父上下文查找。

## 作用域

`@Scope` 控制 Bean 创建策略。

- 单例适合无状态服务、共享资源、管理器。
- 原型适合短生命周期、有独立状态的对象。

不要为了“灵活”到处用原型。那会增加对象分配，状态也更难追踪。

## 条件依赖

`@DependsOn` 可以声明 Bean 的启用条件，例如依赖某个 Bean、某个 class 或某个属性。

它适合表达可选功能：

```java
@DependsOn(classes = "com.example.OptionalDependency")
@Service
public class OptionalService {
}
```

不要用它掩盖糟糕的模块边界。如果某个服务必须存在，构造器依赖比条件注解更直白。

## 资源加载

`Context` 支持多种资源路径：

| 前缀 | 作用 |
| --- | --- |
| `classpath:` | 从 classpath 读取 |
| `context:` | 从当前上下文所属 jar 或 classloader 读取 |
| `embedded:` | 从指定 holder 所在 jar 读取 |
| 相对路径 | 从 data folder 读取 |
| 绝对 URI | 直接打开 URI |

`bundled:` 已废弃，应使用 `embedded:`。

## 常见错误

1. 生命周期顺序乱调用。
2. 没配置扫描包，却期待组件自动出现。
3. 同类型多个 Bean 却不写 `@Qualifier`。
4. 把父上下文当全局状态池。
5. 在普通业务对象里滥用 `Aware`。
6. 用 `@DependsOn` 修补本该由构造器表达的强依赖。

## 相关文档

- [核心概念](../02-core-concepts.md)
- [Component 注解](component-annotations.md)
- [Context 生命周期细节](../advanced/lifecycle.md)
- [自定义 BeanFactory](../advanced/bean-factory.md)
- [资源加载规则](../advanced/resource-loading.md)
