# 快速开始

这篇文档给出 Sugar Next 的最小使用路径。目标是先跑通基本概念，不在第一步就塞满所有注解和扩展点。

## 环境要求

项目使用 Gradle 构建，源码基于 Java 21 toolchain，同时通过构建配置面向 Java 8 发布。

当前项目依赖 `team.idealstate.glass` Gradle 插件和 `team.idealstate.sugar` 相关依赖。直接构建项目时使用：

```shell
./gradlew assemble
```

Windows PowerShell 下可以使用：

```powershell
.\gradlew.bat assemble
```

## 入口选择

Sugar Next 的模块可以分两类：

- 独立工具模块：例如 `calculate`、`hex`、`uuid`、`function`，可以直接使用。
- 运行时框架模块：例如 `context`、`command`、`eventbus`，通常会被宿主应用或插件系统集成。

如果你只想快速验证项目可用性，表达式模块最简单：

```java
import team.idealstate.sugar.next.calculate.Expression;

public class Example {
    public static void main(String[] args) {
        Expression expression = new Expression("(-5 + 3) * 2 - -1 / 4.0");
        Number result = expression.calculate();
        System.out.println(result.doubleValue());
    }
}
```

这个例子不依赖容器，也不需要组件扫描。

## 使用 Context 的最小思路

`Context` 是 Sugar Next 最核心的运行时入口。它需要三个对象：

- `ContextHolder`：提供名称、版本、数据目录等宿主信息。
- `ContextLifecycle`：宿主生命周期回调。
- `EventBus`：事件总线。

伪代码如下：

```java
Context context = Context.of(contextHolder, contextLifecycle, eventBus);

context.initialize();
context.load();
context.enable();

Bean<MyService> bean = context.getBean(MyService.class);
MyService service = bean.getInstance();

context.disable();
context.destroy();
```

这段代码展示的是调用顺序，不是完整可运行示例。实际项目里 `ContextHolder`、`ContextLifecycle` 和 `EventBus` 通常由宿主框架提供。

## 定义组件

一个典型组件会使用 `@Component` 或其派生注解：

```java
import team.idealstate.sugar.next.context.annotation.component.Service;

@Service
public class UserService {
    public String name() {
        return "Sugar";
    }
}
```

组件能否被发现取决于扫描配置。通常需要在宿主类上通过 `@Scan` 指定扫描包，或者让默认扫描覆盖宿主所在包。

## 依赖注入

构造器或方法可以使用 `@Autowired`：

```java
import team.idealstate.sugar.next.context.annotation.component.Service;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;

@Service
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
}
```

如果存在多个同类型 Bean，使用 `@Qualifier` 指定名称。不要靠碰运气让容器“猜对”，那是把不确定性推给运行时。

## 下一步

- 想理解容器：读 [Context 容器](modules/context.md)。
- 想写命令：读 [Command 命令系统](modules/command.md)。
- 想写事件：读 [EventBus 事件总线](modules/eventbus.md)。
- 想写配置 Codec：读 [Databind 与 Codec](modules/databind-codec.md)。

