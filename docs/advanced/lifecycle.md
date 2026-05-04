# Context 生命周期细节

`Context` 生命周期是 Sugar Next 容器行为的主轴。理解它，才能判断 Bean 什么时候注册、什么时候实例化、什么时候销毁。

## 状态流转

```text
destroyed -> initialized -> loaded -> enabled -> disabled -> destroyed
```

对应公开方法：

| 方法 | 前置状态 | 结果状态 |
| --- | --- | --- |
| `initialize()` | destroyed | initialized |
| `load()` | initialized | loaded |
| `enable()` | loaded | enabled |
| `disable()` | enabled | disabled |
| `destroy()` | disabled | destroyed |

## initialize 阶段

这个阶段主要做启动准备：

- 调用宿主生命周期回调。
- 读取宿主类上的启动元数据。
- 注册属性。
- 注册父上下文。
- 注册 BeanFactory。
- 计算扫描包。

这个阶段适合做“容器规则”的准备，不适合初始化大量业务对象。

## load 阶段

这个阶段主要扫描和注册 Bean：

- 根据扫描包读取 class。
- 使用字节码判断可能的组件。
- 加载符合条件的类。
- 根据组件注解选择 BeanFactory。
- 注册 Bean 元数据和实例 Provider。
- 处理 `@Supplier` 方法。
- 解析 `@DependsOn`。

注意：注册 Bean 不等于所有 Bean 都已经实例化。

## enable 阶段

这个阶段会让上下文进入可用状态。

单例 Bean 会在这里初始化。原型 Bean 通常在获取时创建。

如果单例初始化失败，说明问题应该尽早暴露。不要吞掉初始化错误，否则运行时只会更难排查。

## disable 阶段

这个阶段表示上下文停止对外提供能力。

当前实现里这个阶段较轻，主要留给宿主生命周期扩展。

## destroy 阶段

销毁阶段会清理：

- 已创建实例
- 父上下文集合
- 属性
- BeanFactory
- Bean 映射
- 初始化进度集合
- 全局上下文注册表中的当前上下文

已创建实例会反向遍历，调用实现了 `Destroyable` 的对象。

## 常见问题

- 在 `initialize` 前注册 BeanFactory 之外的东西，通常时机不对。
- 在 `load` 前获取 Bean，容器里可能还没有 Bean。
- 在 `enable` 前使用单例 Bean，可能绕开生命周期。
- 在 `destroy` 后继续持有 Bean 实例，是悬挂引用，容易造成内存泄漏。

