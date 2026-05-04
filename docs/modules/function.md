# Function 工具

`function` 模块提供延迟加载、函数式包装和一组自定义闭包接口。

## Lazy

`Lazy<V>` 表示延迟获取的值：

```java
Lazy<UserService> lazy = Lazy.of(() -> createUserService());
UserService service = lazy.get();
```

`isInitialized()` 可以判断值是否已经初始化。

`Context` 注入 `Lazy<T>` 时，会在真正调用 `get()` 时获取目标 Bean 实例。

## CachedLazy

`CachedLazy` 是 `Lazy` 的缓存实现。第一次获取后缓存结果，后续返回同一个值。

适合昂贵初始化、单例延迟加载等场景。

## Functional

`Functional<T>` 是一个轻量包装：

```java
Functional.functional(value)
    .apply(it -> doSomething(it))
    .convert(it -> convert(it));
```

它还提供：

- `optional`
- `lazy`
- `pair`
- `property`
- `use`

`use` 会在目标对象实现 `Closeable` 时自动关闭它。

## closure 接口

自定义闭包接口包括：

| 接口 | 作用 |
| --- | --- |
| `Action<T>` | 接收值，无返回 |
| `Condition<T>` | 接收值，返回 boolean |
| `Function<T, R>` | 接收值，返回结果 |
| `Provider<T>` | 无参数提供值 |
| `Runnable` | 无参数无返回 |

这些接口允许抛出受检异常，调用处会包装成运行时异常。

## 异常包装

`Functional` 中执行闭包时：

- `RuntimeException` 原样抛出。
- 其他 `Throwable` 包装成 `FunctionExecutionException`。

这能减少样板代码，但也意味着异常边界要写清楚。不要让业务异常在包装后没人知道原始含义。

## 注意事项

- `Lazy` 适合延迟，不适合隐藏复杂生命周期。
- `Functional` 链式调用不要滥用。链太长会降低可读性。
- `use` 会关闭 `Closeable`，不要把还要继续使用的对象交给它。

