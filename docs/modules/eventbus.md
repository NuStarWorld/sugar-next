# EventBus 事件总线

`eventbus` 模块提供简单的发布订阅机制。

## 它解决什么问题

当模块之间需要通知，但不应该互相直接依赖时，可以使用事件总线。事件总线适合“发生了什么”的广播，不适合替代明确的业务调用。

如果调用方必须拿到一个确定返回值，直接调用方法更好。拿事件总线绕一圈只会让控制流变脏。

## 定义事件

事件实现 `Event`：

```java
public final class UserCreatedEvent implements Event {
    private final String username;

    public UserCreatedEvent(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
```

## 订阅事件

订阅者实现 `EventSubscriber<T>`：

```java
EventSubscriber<UserCreatedEvent> subscriber = event -> {
    System.out.println(event.getUsername());
};

eventBus.subscribe(UserCreatedEvent.class, subscriber);
```

取消订阅：

```java
eventBus.unsubscribe(subscriber);
```

## 发布事件

```java
EventState state = eventBus.publish(new UserCreatedEvent("sugar"));
```

返回状态：

| 状态 | 含义 |
| --- | --- |
| `SUCCESS` | 发布成功 |
| `FAILURE` | 订阅者抛异常或检测到循环发布 |
| `CANCELLED` | 可取消事件被取消 |

## 可取消事件

事件实现 `Cancelable` 后，订阅者可以取消事件。事件在发布前已经取消，或发布过程中被取消，都会返回 `CANCELLED`。

```java
public final class BeforeSaveEvent implements Event, Cancelable {
    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }
}
```

## 订阅顺序

订阅者如果实现 `Order`，会按 `Order.COMPARATOR` 排序。没有实现 `Order` 的订阅者使用默认顺序。

这适合处理明确的前后顺序，例如校验先于写入。但不要把大量业务顺序藏在事件优先级里，那会让行为很难追踪。

## 错误处理

订阅者抛出异常时，事件总线会记录错误并返回 `FAILURE`。后续订阅者不会继续执行。

同一个事件对象正在发布时再次发布，会被视为循环发布并返回 `FAILURE`。

## 性能注意事项

订阅者集合使用 Copy-On-Write 风格的数据结构，适合读多写少。频繁订阅和取消订阅不是它的强项。如果你每秒动态注册大量订阅者，这个用法很蠢，应该改成长期订阅、内部判断条件。
