# Databind 与 Codec

`databind` 模块提供简单数据结构，`databind.codec` 提供序列化和反序列化接口。

## Pair

`Pair<F, S>` 表示二元组：

```java
Pair<String, Integer> pair = Pair.of("level", 10);
String key = pair.getFirst();
Integer value = pair.getSecond();
```

它适合在局部代码里返回两个相关值。不要把它当正式领域模型。只要字段开始有业务含义，就应该写一个明确命名的类。

## Property

`Property<V>` 表示一个 key-value 属性：

```java
Property<String> property = Property.of("profile", "dev");
```

`ContextProperty` 内部也会包装属性数据。

## Serializer

`Serializer` 负责把对象写出到输出流。

典型用途是把配置对象、状态对象或数据传输对象序列化到文件或网络流。

## Deserializer

`Deserializer` 负责从输入流读取对象。

反序列化时必须明确目标类型。不要依赖运行时猜类型，否则错误会变得很隐蔽。

## Codec

`Codec` 同时继承 `Serializer` 和 `Deserializer`：

```java
public interface Codec extends Serializer, Deserializer {
}
```

它代表一种完整的编解码能力，例如 json、yaml、toml 或自定义格式。

## 和 Configuration 的关系

`context` 模块中的 `@Configuration` Bean 会根据配置文件扩展名寻找匹配的 Codec。

Codec 通常会注册为带 `@Serialization` 元数据的 Bean：

```java
@Serialization("json")
public final class JsonCodec implements Codec {
}
```

当配置路径是 `config.json` 时，容器会寻找支持 `json` 的 Codec。

## 自定义 Codec

自定义 Codec 的基本步骤：

1. 实现 `Codec`。
2. 使用 `@Serialization("ext")` 标注支持的扩展名。
3. 确保它能被 Context 扫描并注册成 Bean。
4. 在 `@Configuration(uri = "...")` 中使用对应扩展名。

## 注意事项

- Codec 不应该吞异常。格式错误就应该让调用方知道。
- 不要在 Codec 里硬编码具体业务路径。
- 大文件反序列化要考虑内存占用。
- 配置格式和扩展名必须一致，否则 `ConfigurationBeanFactory` 找不到正确 Codec。

