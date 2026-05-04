# 扩展点汇总

本页汇总 Sugar Next 的主要扩展点。

## BeanFactory

适用场景：支持新的组件注解或新的 Bean 创建方式。

需要实现：

```java
BeanFactory<MyAnnotation>
```

常见用途：

- 自定义组件类型。
- 配置对象加载。
- 自动代理。

## Codec

适用场景：支持新的配置或数据格式。

需要实现：

```java
Codec
```

通常配合：

```java
@Serialization("json")
```

## Operator

适用场景：给表达式引擎增加运算符。

需要实现：

```java
Operator
```

必须定义字面量、优先级、结合性、元数和计算逻辑。

## CommandArgument.Converter

适用场景：把命令输入字符串转换成业务类型。

转换器要区分严格模式和非严格模式。非严格模式可能被频繁调用，不能做重活。

## CommandArgument.Completer

适用场景：给命令参数提供补全结果。

补全器应该快。补全时查数据库、扫文件系统或访问网络，都是糟糕用法。

## EventSubscriber

适用场景：订阅事件。

订阅者应当短小明确。事件处理里做长时间阻塞操作，会拖慢整个发布流程。

## DatabaseSessionFactory

适用场景：把具体数据库框架接入 Sugar Next 的事务抽象。

实现方负责：

- 打开数据库会话。
- 创建 repository。
- 提交和回滚。
- 关闭资源。

