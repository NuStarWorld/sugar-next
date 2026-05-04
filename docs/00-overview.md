# 项目概览

Sugar Next 不是一个完整应用框架，而是一组可以组合使用的 Java 运行时工具。它的核心是 `context` 容器，其他模块围绕组件管理、事件、命令、表达式、反射、字节码、配置和事务提供能力。

## 项目定位

Sugar Next 适合给插件系统、小型服务、工具型应用或框架扩展层提供基础设施。它的设计重点是轻量、可组合和低接入成本。

它不是 Spring Boot 的替代品，也不应该被包装成一个臃肿的“企业级全家桶”。如果你的项目需要完整 Web 栈、自动配置生态、大量第三方集成，那应该选择成熟框架。Sugar Next 更适合做明确、边界较窄的运行时基础能力。

## 适用场景

- 插件或模块需要统一生命周期。
- 需要简单 Bean 管理和依赖注入。
- 需要在运行时发布和订阅事件。
- 需要构建命令行、游戏命令或控制台命令。
- 需要安全地计算简单数值表达式。
- 需要用注解代理反射访问私有字段或方法。
- 需要扫描 class 文件，但不想直接加载类。
- 需要通过 Codec 加载配置对象。
- 需要定义数据库事务抽象，具体实现交给上层框架。

## 不适用场景

- 你需要完整 Web 框架。
- 你需要成熟 ORM。
- 你需要复杂的 AOP、条件装配和自动配置体系。
- 你只需要几个静态工具方法，却引入整个容器生命周期。
- 你希望文档替你隐藏项目复杂度。复杂度客观存在，文档只能把它讲清楚，不能把糟糕用法变好。

## 模块关系

`context` 是中心模块。它负责：

- 生命周期：`initialize -> load -> enable -> disable -> destroy`
- 父上下文
- 属性注册和查询
- BeanFactory 注册
- 组件扫描
- Bean 创建
- 依赖注入
- 资源加载

其他模块可以独立使用，也可以被 `context` 集成：

| 模块 | 作用 |
| --- | --- |
| `eventbus` | 发布和订阅事件 |
| `command` | 构建命令树、执行命令、参数转换和补全 |
| `calculate` | 预编译和计算数值表达式 |
| `databind.codec` | 序列化和反序列化扩展 |
| `reflect` | 注解驱动的反射代理 |
| `bytecode` | 基于 ASM 读取 class 元数据 |
| `database` | 数据库会话和事务抽象 |
| `function` | 延迟加载和函数式辅助接口 |
| `io`、`hex`、`uuid`、`sorting`、`lang`、`stacktrace` | 小型工具模块 |

## 推荐阅读路线

第一次阅读建议按这个顺序：

1. [快速开始](01-getting-started.md)
2. [核心概念](02-core-concepts.md)
3. [Context 容器](modules/context.md)
4. [Component 注解](modules/component-annotations.md)
5. [EventBus 事件总线](modules/eventbus.md)
6. [Command 命令系统](modules/command.md)
7. [Calculate 表达式计算](modules/calculate.md)
7. 其他模块文档
8. 高级主题和参考文档

如果你只维护源码，可以直接读 [包结构参考](reference/package-map.md) 和 `advanced/` 下的文档。
