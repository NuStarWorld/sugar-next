# Component 注解

本文档专门说明 `team.idealstate.sugar.next.context.annotation.component` 包下的组件注解。它们是 Context 容器识别、注册和创建 Bean 的主要入口。

## 注解总览

| 注解 | 是否带 `@Component` | 主要用途 |
| --- | --- | --- |
| `@Component` | 是自身 | 通用组件 |
| `@Service` | 是 | 服务类 |
| `@Repository` | 是 | 存储库类 |
| `@Controller` | 是 | 控制器类 |
| `@Subscriber` | 是 | 事件订阅组件 |
| `@Supplier` | 是 | 供应 Bean 的方法或组件 |
| `@Configuration` | 否 | 从配置资源创建配置对象 |
| `@Serialization` | 否 | 声明 Codec 支持的序列化格式 |

多数注解都有 `name()`，用于指定 Bean 名称。为空时，容器会按自身规则生成默认名称。

## @Component

`@Component` 是最基础的组件注解，可以标在类型或方法上。

最小示例：

```java
import team.idealstate.sugar.next.context.annotation.component.Component;

@Component
public class UserService {
    public String name() {
        return "sugar";
    }
}
```

指定名称：

```java
@Component(name = "mainUserService")
public class UserService {
}
```

`@Component` 适合没有明确分层语义的普通组件。如果类明显是服务、仓储或控制器，使用更具体的注解会让文档和代码更清楚。

## @Service

`@Service` 用于服务类，本质上是带 `@Component` 的语义化注解。

```java
import team.idealstate.sugar.next.context.annotation.component.Service;

@Service
public class UserService {
    public String displayName(String username) {
        return username.trim();
    }
}
```

它适合放业务流程、领域服务、可复用应用服务。不要把所有类都标成 `@Service`，那只是把分层语义磨平。

## @Repository

`@Repository` 用于存储库或数据访问对象。

```java
import team.idealstate.sugar.next.context.annotation.component.Repository;

@Repository
public class UserRepository {
    public boolean exists(String username) {
        return false;
    }
}
```

如果项目接入了 `database` 模块，Repository 通常也会和事务抽象一起使用。

## @Controller

`@Controller` 用于入口控制类，例如命令控制器、请求控制器或适配层对象。

```java
import team.idealstate.sugar.next.context.annotation.component.Controller;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;

@Controller
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
}
```

控制器应该协调输入输出，不应该塞大量业务逻辑。业务逻辑放到 `@Service`。

## @Subscriber

`@Subscriber` 用于声明事件订阅组件。它带有 `event()`，可以指定订阅的事件类型。

```java
import team.idealstate.sugar.next.context.annotation.component.Subscriber;
import team.idealstate.sugar.next.eventbus.Event;
import team.idealstate.sugar.next.eventbus.EventSubscriber;

public final class UserCreatedEvent implements Event {
}

@Subscriber(event = UserCreatedEvent.class)
public class UserCreatedSubscriber implements EventSubscriber<UserCreatedEvent> {
    @Override
    public void onEvent(UserCreatedEvent event) {
        // handle event
    }
}
```

如果不指定 `event()`，默认是 `Event.class`。这通常太宽泛，除非你确实要处理所有事件，否则应该明确指定事件类型。

## @Supplier

`@Supplier` 用于供应 Bean，常见场景是在配置类方法上创建第三方对象或手动组装对象。

```java
import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.component.Supplier;

@Component
public class AppConfiguration {
    @Supplier(name = "userFormatter")
    public UserFormatter userFormatter() {
        return new UserFormatter();
    }
}
```

`@Supplier` 很适合创建无法直接由容器构造的对象，比如第三方库对象。

不要滥用 `@Supplier`。如果一个类可以用构造器注入正常创建，就直接让它成为组件。到处写供应方法，会让 Bean 来源变得混乱。

## @Configuration

`@Configuration` 用于声明一个配置对象。容器会从 `uri()` 指定的资源读取配置，并通过匹配的 Codec 反序列化成 Bean。

```java
import team.idealstate.sugar.next.context.annotation.component.Configuration;

@Configuration(
    uri = "config/app.json",
    release = "embedded:default-app.json"
)
public class AppConfig {
    private String name;

    public String getName() {
        return name;
    }
}
```

字段如何赋值取决于实际 Codec 实现。`@Configuration` 本身只负责说明配置资源位置。

关键规则：

- `uri()` 是最终配置文件位置。
- `release()` 是默认配置资源位置。
- 当 `uri()` 对应文件不存在时，容器可以把 `release()` 释放到 `uri()`。
- 文件扩展名必须有对应的 `@Serialization` Codec。

## @Serialization

`@Serialization` 用于声明 Codec 支持的格式。

```java
import java.io.InputStream;
import java.io.OutputStream;
import team.idealstate.sugar.next.context.annotation.component.Serialization;
import team.idealstate.sugar.next.databind.codec.Codec;

@Serialization("json")
public class JsonCodec implements Codec {
    @Override
    public void serialize(Object object, OutputStream outputStream) {
        // write json
    }

    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> type) {
        // read json
        return null;
    }
}
```

当配置文件是 `app.json` 时，容器会查找 `@Serialization("json")` 的 Codec。

如果没有匹配 Codec，`@Configuration` Bean 创建会失败。不要把这个错误吞掉，因为配置格式不匹配是明确的启动错误。

## 自定义组件注解

可以把 `@Component` 作为元注解使用：

```java
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import team.idealstate.sugar.next.context.annotation.component.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Job {
    String name() default "";
}
```

然后使用：

```java
@Job(name = "dailyCleanup")
public class DailyCleanupJob {
}
```

自定义组件注解应该提供 `name()`。源码注释里也明确说，三方组件注解通常应该实现这个成员。没有 `name()` 不是不能工作，但会降低和容器命名规则的兼容性。

## 和 BeanFactory 的关系

组件注解只是元数据，真正创建 Bean 的是 `BeanFactory`。

默认关系大致是：

| 注解 | 工厂 |
| --- | --- |
| `@Component` 及其派生注解 | `ComponentBeanFactory` |
| `@Configuration` | `ConfigurationBeanFactory` |
| `@Serialization` | `SerializationBeanFactory` |

`@Service`、`@Repository`、`@Controller`、`@Subscriber`、`@Supplier` 都带有 `@Component`，因此通常会委托给组件工厂处理。

## 最小组合示例

下面示例展示服务、控制器和依赖注入的组合：

```java
@Service
public class UserService {
    public String normalize(String username) {
        return username.trim().toLowerCase();
    }
}

@Controller
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    public String create(String username) {
        return userService.normalize(username);
    }
}
```

这类代码的依赖关系是清晰的：控制器依赖服务，服务不反向依赖控制器。

## 常见错误

1. 标了组件注解，但扫描包没有覆盖目标类。
2. 多个 Bean 同名。
3. 同类型多个 Bean，注入时不写 `@Qualifier`。
4. `@Configuration` 文件扩展名没有对应 Codec。
5. `@Subscriber` 不指定事件类型，导致订阅范围过大。
6. 能用构造器注入创建的对象，却绕到 `@Supplier` 里手动 new。
7. 自定义组件注解不保留运行时注解，即缺少 `@Retention(RetentionPolicy.RUNTIME)`。

## 相关文档

- [Context 容器](context.md)
- [Databind 与 Codec](databind-codec.md)
- [EventBus 事件总线](eventbus.md)
- [自定义 BeanFactory](../advanced/bean-factory.md)
- [注解参考](../reference/annotations.md)
