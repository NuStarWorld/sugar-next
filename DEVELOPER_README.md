# Sugar Next 开发者文档

这是 Sugar Next 的开发者文档入口。根目录已有 `README.md` 作为项目首页，本文档只负责引导开发者阅读更完整的模块文档。

Sugar Next 是一个面向 Java 应用和插件开发的轻量工具库。它提供上下文容器、Bean 管理、事件总线、命令系统、表达式计算、反射代理、字节码元数据读取、配置编解码和数据库事务抽象等能力。

## 你应该先读什么

如果你第一次接触这个项目，按下面顺序读：

1. [项目概览](docs/00-overview.md)
2. [快速开始](docs/01-getting-started.md)
3. [核心概念](docs/02-core-concepts.md)
4. [Context 容器](docs/modules/context.md)

如果你只想使用某个独立模块，可以直接跳到对应模块文档。

## 模块文档

- [Context 容器](docs/modules/context.md)：生命周期、组件扫描、Bean、依赖注入、资源加载。
- [Component 注解](docs/modules/component-annotations.md)：组件注解、最小示例和 BeanFactory 关系。
- [EventBus 事件总线](docs/modules/eventbus.md)：事件发布、订阅、取消和顺序。
- [Command 命令系统](docs/modules/command.md)：命令树、参数转换、补全、权限和帮助树。
- [Calculate 表达式计算](docs/modules/calculate.md)：数值表达式、变量、预编译和自定义运算符。
- [Databind 与 Codec](docs/modules/databind-codec.md)：`Pair`、`Property`、序列化和反序列化扩展。
- [Reflect 反射代理](docs/modules/reflect.md)：注解驱动的字段、方法、构造器访问。
- [Bytecode 字节码读取](docs/modules/bytecode.md)：不加载类读取 class 元数据。
- [Database 事务抽象](docs/modules/database.md)：数据库会话、事务管理和嵌套事务。
- [Function 工具](docs/modules/function.md)：`Lazy`、`Functional` 和闭包接口。
- [工具类](docs/modules/utils.md)：IO、十六进制、UUID、排序、包装类型和堆栈工具。

## 高级主题

- [Context 生命周期细节](docs/advanced/lifecycle.md)
- [自定义 BeanFactory](docs/advanced/bean-factory.md)
- [资源加载规则](docs/advanced/resource-loading.md)
- [注解扫描机制](docs/advanced/annotation-scanning.md)
- [扩展点汇总](docs/advanced/extension-points.md)
- [性能与内存说明](docs/advanced/performance-notes.md)

## 参考文档

- [注解参考](docs/reference/annotations.md)
- [异常参考](docs/reference/exceptions.md)
- [包结构参考](docs/reference/package-map.md)
- [废弃 API 迁移](docs/reference/migration-deprecated.md)

## 文档维护原则

文档应该优先解释“怎么用”和“为什么这样设计”。不要把源码逐行翻译成自然语言，那种文档维护成本高，而且读者看完也不会真正理解项目。

每篇模块文档至少应该包含：

- 这个模块解决什么问题。
- 什么时候应该使用它。
- 最小示例。
- 核心 API 的职责边界。
- 常见错误和性能注意事项。
