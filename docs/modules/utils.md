# 工具类

本页记录 Sugar Next 中零散但常用的工具模块。

## IOUtils

`IOUtils` 提供基础 IO 操作：

- `readAllBytes(InputStream)`
- `transferTo(InputStream, OutputStream)`
- `readAllLines(InputStream)`
- `readAllLines(Reader)`

`readAllBytes` 会读取整个输入流到内存。大文件这么用很蠢，应该流式处理。

`transferTo` 使用默认 4096 字节缓冲区，并在传输后关闭输入流和输出流。

## HexUtils

`HexUtils` 提供二进制和十六进制字符串转换：

```java
String hex = HexUtils.binaryToHex(bytes);
byte[] bytes = HexUtils.hexToBinary(hex);
```

当前实现使用大写十六进制字符。输入非法字符时会触发运行时错误，调用方应先校验外部输入。

## UUIDUtils

`UUIDUtils` 提供 UUID 和 16 字节数组之间的转换：

```java
byte[] binary = UUIDUtils.uuidToBinary(uuid);
UUID uuid = UUIDUtils.binaryToUUID(binary);
```

支持 `swapFlag`，用于处理特定存储顺序。

## WrappedType

`WrappedType` 处理 Java 基本类型和包装类型映射：

```java
WrappedType<Integer> type = WrappedType.ofPrimitiveType(int.class);
Class<Integer> wrapped = type.getWrappedType();
```

支持 `byte`、`short`、`int`、`long`、`float`、`double`、`boolean`、`char`、`void`。

## OrderComparator

`OrderComparator` 根据以下来源计算顺序：

- 对象实现 `sorting.annotation.Order`
- 对象实现 `Orderable`
- 类上标注 `@Order`
- 默认顺序

适合排序插件、处理器、订阅者等。

## StackTraceUtils

`StackTraceUtils.makeThrowableDetails(Throwable)` 会生成异常消息和完整堆栈字符串。

它适合日志输出，不适合在高频路径中反复构造大字符串。

