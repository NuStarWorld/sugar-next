# Sugar Next 开发者文档编写计划

本文档用于规划 Sugar Next 的开发者文档结构、编写顺序和每个章节的写法。目标不是把源码翻译成文字，而是让读者按“先会用、再理解机制、最后能扩展和维护”的顺序读下去。

## 总体原则

1. `README.md` 只做项目入口，不写成长篇百科。
2. 详细文档放到 `docs/` 目录下，按难度和使用频率组织。
3. 模块文档按使用场景写，不按源码文件逐个解释。
4. 每篇文档都要有最小示例、核心 API、常见用法、注意事项。
5. 高级机制和源码维护内容放到 `advanced/`，避免普通用户一上来就被实现细节淹没。
6. 查表类内容放到 `reference/`，例如注解表、异常表、包结构表。

## 推荐目录结构

```text
README.md
docs/
  00-overview.md
  01-getting-started.md
  02-core-concepts.md

  modules/
    context.md
    component-annotations.md
    eventbus.md
    command.md
    calculate.md
    databind-codec.md
    reflect.md
    bytecode.md
    database.md
    function.md
    utils.md

  advanced/
    lifecycle.md
    bean-factory.md
    resource-loading.md
    annotation-scanning.md
    extension-points.md
    performance-notes.md

  reference/
    annotations.md
    exceptions.md
    package-map.md
    migration-deprecated.md
```

## 推荐阅读路线

```text
README.md
  -> docs/00-overview.md
  -> docs/01-getting-started.md
  -> docs/02-core-concepts.md
  -> docs/modules/context.md
  -> docs/modules/eventbus.md
  -> docs/modules/command.md
  -> docs/modules/calculate.md
  -> 其他模块文档
  -> docs/advanced/*
  -> docs/reference/*
```

这个顺序的好处是：读者先知道项目能干什么，再跑通最小例子，然后理解核心概念，最后再深入模块和扩展点。

## 编写步骤

### 第 1 步：整理根目录 README.md

`README.md` 是门面，只回答四件事：

1. Sugar Next 是什么。
2. 它提供哪些核心能力。
3. 怎么引入依赖和快速开始。
4. 文档入口在哪里。

建议内容结构：

```md
# Sugar Next

Sugar Next 是一个面向 Java 应用和插件开发的轻量工具库，提供上下文容器、Bean 管理、事件总线、命令系统、表达式计算、反射代理、字节码元数据读取、配置编解码和事务抽象等能力。

## 特性

- 轻量 Context 生命周期与 Bean 容器
- 注解驱动的组件扫描和依赖注入
- 简单事件总线
- 命令注册、参数转换和补全
- 可预编译的数值表达式
- 注解驱动反射代理
- ASM 字节码结构读取
- 配置 Codec 扩展点
- 数据库事务抽象

## 快速开始

见 [快速开始](docs/01-getting-started.md)。

## 文档导航

- [项目概览](docs/00-overview.md)
- [快速开始](docs/01-getting-started.md)
- [核心概念](docs/02-core-concepts.md)
- [Context 容器](docs/modules/context.md)
- [EventBus 事件总线](docs/modules/eventbus.md)
- [Command 命令系统](docs/modules/command.md)
- [Calculate 表达式计算](docs/modules/calculate.md)
```

注意：不要把所有 API 都塞进 `README.md`。那样 README 会变成垃圾堆，后面没人愿意维护。

### 第 2 步：编写 docs/00-overview.md

这篇是项目概览，不写过多代码。

必须回答：

- Sugar Next 是完整框架还是工具库。
- 适合什么场景。
- 不适合什么场景。
- 各模块之间是什么关系。
- 新读者应该按什么顺序阅读。

建议结构：

```md
# 项目概览

## 项目定位

## 适用场景

## 不适用场景

## 模块关系

## 推荐阅读路线
```

写法重点：

- `context` 是中心模块，负责生命周期、组件扫描、Bean 创建、依赖注入和资源加载。
- `eventbus`、`command`、`calculate`、`reflect`、`bytecode` 等模块可以独立使用，也可以被上层集成。
- `database` 只是事务抽象，不是 ORM。

### 第 3 步：编写 docs/01-getting-started.md

这篇负责让读者跑通最小例子。

建议结构：

```md
# 快速开始

## 环境要求

## 引入依赖

## 创建最小 Context

## 定义一个组件

## 获取 Bean

## 下一步
```

注意：

- 示例必须尽量短。
- 不要一开始解释所有注解。
- 如果某些代码依赖宿主环境，需要明确说明。

### 第 4 步：编写 docs/02-core-concepts.md

这篇是模块文档的地基，先解释名词。

建议覆盖：

- `ContextHolder`：上下文宿主，提供名称、版本、数据目录等信息。
- `Context`：运行时上下文，管理生命周期、父上下文、属性、Bean 和资源。
- `Bean`：容器中的对象包装，包含名称、类型、作用域、元数据和实例。
- `BeanFactory`：Bean 创建策略。
- `Metadata`：Bean 的注解元数据。
- `Scope`：Bean 的作用域。
- `DependsOn`：条件依赖声明。
- `Aware`：Bean 接收上下文相关信息的回调接口。
- `EventBus`：发布订阅机制。
- `Codec`：配置序列化和反序列化扩展点。

写法重点：

- 每个概念先写一句话定义。
- 再写它和其他概念的关系。
- 最后放一个很短的代码或伪代码示例。

### 第 5 步：编写 docs/modules/context.md

`context` 是最核心的模块，优先级最高。这篇应该写得最完整。

建议结构：

```md
# Context 容器

## 它解决什么问题

## 创建 Context

## 生命周期

## 属性管理

## 父上下文

## Bean 查找

## 组件扫描

## 组件注解

## 依赖注入

## 作用域

## 条件依赖

## 资源加载

## 常见错误
```

必须讲清楚：

- 生命周期顺序：`initialize -> load -> enable -> disable -> destroy`。
- Bean 查找方式：按名称、按类型、是否继承父上下文。
- 组件注解：`@Component`、`@Service`、`@Repository`、`@Controller`、`@Configuration`、`@Serialization`、`@Supplier`。
- 注入注解：`@Autowired`、`@Qualifier`、`@Owned`。
- 可注入类型：普通 Bean、`Bean<T>`、`Lazy<T>`、`List<T>`、`Map<String, T>`、`Context`、`ContextHolder`。
- 资源路径：`classpath:`、`context:`、`embedded:`、文件路径、绝对 URI。

注意事项：

- 这里不要假装它是 Spring。它是轻量容器，不是全家桶。
- 如果某些行为依赖扫描 jar 或 classpath，要明确写出来。

### 第 6 步：编写 docs/modules/eventbus.md

事件总线相对独立，适合紧跟 `context` 之后写。

建议结构：

```md
# EventBus 事件总线

## 它解决什么问题

## 定义事件

## 订阅事件

## 发布事件

## 可取消事件

## 订阅顺序

## 错误处理
```

必须讲清楚：

- `Event` 是事件标记接口。
- `Cancelable` 支持取消事件。
- `EventSubscriber<T>` 处理事件。
- `EventBus.publish` 返回 `EventState`。
- 订阅者抛异常会导致发布失败。
- 同一个事件对象循环发布会被检测为失败。

### 第 7 步：编写 docs/modules/command.md

命令模块比较复杂，必须按场景写，不要按类堆 API。

建议结构：

```md
# Command 命令系统

## 它解决什么问题

## 创建命令行

## 定义命令处理方法

## 字面量参数

## 变量参数

## 参数转换

## 参数补全

## 权限控制

## 帮助树

## 执行结果

## 常见错误
```

必须讲清楚：

- 入口是 `CommandLine.of(name, command)`。
- 命令处理方法使用 `@CommandHandler`。
- 路径中的变量参数用 `{name}`。
- 变量参数必须在字面量参数之后。
- 方法参数要么是 `CommandContext`，要么标注 `@CommandArgument`。
- `CommandResult` 不能返回 `null`。
- 转换器和补全器可以用类，也可以用方法名绑定。

### 第 8 步：编写 docs/modules/calculate.md

表达式模块独立、容易上手，适合写成教程。

建议结构：

```md
# Calculate 表达式计算

## 它解决什么问题

## 最小示例

## 预编译表达式

## 变量上下文

## 布尔判断

## 标准运算符

## 自定义运算符

## 异常类型
```

必须讲清楚：

- `Expression` 可以先 `compile()`，再多次 `calculate()`。
- 变量通过 `Map<String, Number>` 传入。
- `isTrue()` 的规则是结果大于 `0`。
- 标准运算符来自 `StandardOperator.ALL`。
- 语法错误和计算错误是两类问题。

### 第 9 步：编写 docs/modules/databind-codec.md

这个模块分两块写。

建议结构：

```md
# Databind 与 Codec

## Pair

## Property

## Serializer

## Deserializer

## Codec

## 和 Configuration 的关系

## 自定义 Codec
```

写法重点：

- `Pair` 和 `Property` 是简单数据结构。
- `Codec` 同时继承 `Serializer` 和 `Deserializer`。
- `@Configuration` 会根据文件扩展名寻找匹配的 `@Serialization` Codec。

### 第 10 步：编写 docs/modules/reflect.md

反射代理是高级能力，文档要明确风险。

建议结构：

```md
# Reflect 反射代理

## 它解决什么问题

## 创建反射代理

## 访问构造器

## 访问字段

## 调用方法

## 静态成员

## 默认方法

## accessible 的风险
```

必须讲清楚：

- 入口是 `Reflection.reflect(...)`。
- 反射接口必须是 interface。
- `@Reflect` 可以指定默认目标类。
- `@ReflectConstructor`、`@ReflectField`、`@ReflectMethod` 分别绑定构造器、字段、方法。
- `accessible = true` 会突破可见性，使用者应该知道风险。

### 第 11 步：编写 docs/modules/bytecode.md

字节码模块用于不加载类就读取类结构。

建议结构：

```md
# Bytecode 字节码读取

## 它解决什么问题

## Java.typeof

## JavaCache

## JavaClass

## JavaField

## JavaMethod

## JavaConstructor

## JavaAnnotation

## 与反射的区别

## 使用限制
```

必须讲清楚：

- `Java.typeof(...)` 返回 `JavaClass`。
- `JavaCache` 避免重复解析 class。
- 这个模块基于 ASM 读取 class 文件。
- 它适合扫描和元数据读取，不适合替代所有反射场景。

### 第 12 步：编写 docs/modules/database.md

数据库模块只是抽象层，不是完整数据库框架。

建议结构：

```md
# Database 事务抽象

## 它解决什么问题

## DatabaseSession

## DatabaseSessionFactory

## TransactionManager

## TransactionSession

## @Transaction

## 嵌套事务

## 使用限制
```

必须讲清楚：

- `DatabaseSession` 负责获取 repository、提交、回滚、关闭。
- `TransactionSession` 使用引用计数管理嵌套事务。
- `TransactionManager` 通常交给自动化框架使用。
- 这里没有提供具体 ORM 实现。

### 第 13 步：编写 docs/modules/function.md

函数工具模块是辅助能力。

建议结构：

```md
# Function 工具

## Lazy

## CachedLazy

## Functional

## closure 接口

## 异常包装
```

必须讲清楚：

- `Lazy` 用于延迟初始化。
- `Functional.use` 会自动关闭 `Closeable`。
- `Action`、`Condition`、`Function`、`Provider` 是自定义函数式接口。
- 受检异常会包装为 `FunctionExecutionException`。

### 第 14 步：编写 docs/modules/utils.md

工具类放到最后，不要抢主线。

建议结构：

```md
# 工具类

## IOUtils

## HexUtils

## UUIDUtils

## WrappedType

## OrderComparator

## StackTraceUtils
```

写法重点：

- 每个工具类只写用途、示例、边界条件。
- 不要把每个方法都机械翻译一遍。

### 第 15 步：编写 docs/advanced/lifecycle.md

这篇给维护者看，解释 Context 生命周期内部顺序。

建议覆盖：

- 每个生命周期阶段做什么。
- 阶段状态如何变化。
- 什么时候注册属性、父上下文、BeanFactory。
- 什么时候扫描 Bean。
- 什么时候初始化单例 Bean。
- destroy 阶段如何逆序销毁实例。

### 第 16 步：编写 docs/advanced/bean-factory.md

这篇讲如何扩展 Bean 创建逻辑。

建议覆盖：

- `BeanFactory<M extends Annotation>` 的职责。
- `validate`、`create`、`proxy` 的调用顺序。
- `AbstractBeanFactory` 提供了什么默认行为。
- 如何写一个新的组件注解。
- 如何通过 `@RegisterFactory` 注册。

### 第 17 步：编写 docs/advanced/resource-loading.md

这篇专门讲资源路径。

建议覆盖：

- `classpath:` 从 classpath 读取。
- `context:` 从当前上下文 jar 或 classloader 读取。
- `embedded:` 从指定 holder 的 jar 读取。
- 普通相对路径从 data folder 读取。
- 绝对 URI 直接打开流。
- `bundled:` 已废弃，应使用 `embedded:`。

### 第 18 步：编写 docs/advanced/annotation-scanning.md

这篇讲扫描机制。

建议覆盖：

- `@Scan` 的作用。
- 扫描 jar/class 的大致流程。
- 如何通过字节码判断组件注解。
- 为什么不直接加载所有类。
- 扫描成本和缓存。

### 第 19 步：编写 docs/advanced/extension-points.md

这篇汇总所有扩展点。

建议覆盖：

- 自定义 `BeanFactory`
- 自定义 `Codec`
- 自定义 `Operator`
- 自定义命令参数 `Converter`
- 自定义命令参数 `Completer`
- 自定义 `EventSubscriber`
- 自定义 `DatabaseSessionFactory`

每个扩展点都按这个格式写：

```md
## 扩展点名称

### 适用场景

### 需要实现的接口

### 最小示例

### 注意事项
```

### 第 20 步：编写 docs/advanced/performance-notes.md

这篇专门写性能和内存，不要含糊。

建议覆盖：

- Context 扫描 jar 的成本。
- `JavaCache` 的缓存价值。
- 反射代理的 MethodHandle 缓存。
- 表达式 `compile()` 后复用的收益。
- `IOUtils.transferTo` 的缓冲区行为。
- `CopyOnWriteArraySet` 在 EventBus 中的读多写少假设。
- 大量 Bean、事件订阅者、命令节点时的潜在成本。

写法要求：

- 不要写“可以优化”这种废话。
- 要写清楚什么时候慢、为什么慢、怎么避免。

### 第 21 步：编写 docs/reference/annotations.md

注解参考文档适合最后写。

建议用表格：

```md
| 注解 | 所属模块 | 作用 | 常用位置 | 注意事项 |
| --- | --- | --- | --- | --- |
| @Component | context | 声明组件 Bean | class | 可作为元注解 |
```

应覆盖：

- `context.annotation.component`
- `context.annotation.feature`
- `command.annotation`
- `dispatch.annotation`
- `dispatch.argument.annotation`
- `database.annotation`
- `reflect.annotation`
- `sorting.annotation`

### 第 22 步：编写 docs/reference/exceptions.md

异常参考文档用于排错。

建议结构：

```md
# 异常参考

## SugarNextException

## ContextException

## CommandException

## ExpressionException

## BytecodeException

## ReflectionException

## DatabaseException

## CodecException
```

每个异常写：

- 所属模块。
- 常见触发场景。
- 排查步骤。

### 第 23 步：编写 docs/reference/package-map.md

这篇是包结构查表。

建议表格：

```md
| 包 | 作用 |
| --- | --- |
| team.idealstate.sugar.next.context | Context 容器、Bean、生命周期 |
| team.idealstate.sugar.next.eventbus | 事件总线 |
```

不要逐个类解释，包级别就够了。

### 第 24 步：编写 docs/reference/migration-deprecated.md

项目里已有一些 `@Deprecated` API，要给替代方案。

建议表格：

```md
| 已废弃 API | 替代 API | 原因 |
| --- | --- | --- |
| Context.RESOURCE_BUNDLED | Context.RESOURCE_EMBEDDED | 命名更准确 |
```

这个文档非常有价值，因为废弃 API 不写迁移说明就是给用户挖坑。

## 模块文档统一模板

所有 `docs/modules/*.md` 建议使用同一套结构：

```md
# 模块名

## 它解决什么问题

一句话说明模块边界。

## 什么时候使用

列出真实使用场景。

## 快速示例

给最小代码。

## 核心 API

解释主要类和接口。

## 常见用法

按场景写。

## 扩展方式

如果模块有扩展点就写，没有就省略。

## 注意事项

写限制、坑、性能问题。

## 相关文档

链接到其他章节。
```

## 示例代码要求

1. 示例必须能说明一个明确问题。
2. 示例不要堆无关类。
3. 示例优先使用项目现有 API，不要发明不存在的辅助工具。
4. 如果代码不能直接运行，要明确说明缺少什么宿主环境。
5. 示例中的中文注释必须使用 UTF-8 保存。

## 质量检查清单

每写完一篇文档，都按下面清单检查：

- 是否说明了这个模块解决什么问题。
- 是否说明了不该在什么场景使用。
- 是否有最小示例。
- 是否解释了核心 API 的职责边界。
- 是否写了常见错误。
- 是否写了性能或资源成本。
- 是否链接到相关文档。
- 是否避免了源码逐行翻译。
- 是否没有使用已经废弃的 API 作为推荐写法。

## 最终目标

文档最终应该形成三层：

1. **入门层**：`README.md`、`overview`、`getting-started`、`core-concepts`，让新用户知道怎么开始。
2. **使用层**：`modules/*`，让开发者能独立使用每个模块。
3. **维护层**：`advanced/*` 和 `reference/*`，让维护者知道机制、扩展点、性能成本和迁移规则。

这个结构比把所有内容塞进一个 README 更可靠。README 负责引路，模块文档负责教会使用，高级文档负责解释机制，参考文档负责查表。
