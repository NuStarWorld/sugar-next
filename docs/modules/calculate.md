# Calculate 表达式计算

`calculate` 模块提供轻量数值表达式解析和计算能力。核心类是 `Expression`。

## 它解决什么问题

当你需要让用户或配置写简单数值表达式时，不应该直接 `eval`，Java 也没有内建安全表达式求值。`Expression` 会把表达式词法解析成 token，再转换成逆波兰表达式执行。

它适合数值计算和简单条件判断，不适合执行脚本。

## 最小示例

```java
Expression expression = new Expression("(-5 + 3) * 2 - -1 / 4.0");
Number result = expression.calculate();

System.out.println(result.doubleValue());
```

## 预编译表达式

如果同一个表达式要反复计算，先编译：

```java
Expression expression = new Expression("score * 2 + bonus").compile();

Number first = expression.calculate(Map.of("score", 10, "bonus", 5));
Number second = expression.calculate(Map.of("score", 20, "bonus", 1));
```

不预编译也能用，因为 `calculate()` 会自动调用 `compile()`。但高频路径里反复创建表达式对象是蠢的，应该复用已编译表达式。

## 变量上下文

变量通过 `Map<String, Number>` 提供：

```java
Map<String, Number> context = new HashMap<>();
context.put("price", 100);
context.put("discount", 20);

Number result = new Expression("price - discount").calculate(context);
```

变量不存在时会抛出计算异常。不要把缺失变量默默当成 `0`，那会隐藏配置错误。

## 布尔判断

`isTrue()` 的规则是计算结果大于 `0`：

```java
boolean enabled = new Expression("level >= 10").isTrue(Map.of("level", 12));
```

`isFalse()` 是 `!isTrue()`。

## 标准运算符

默认使用 `StandardOperator.ALL`。

| 类型 | 运算符 |
| --- | --- |
| 一元 | `!`、`~`、`+`、`-` |
| 乘除 | `*`、`/`、`%`、`**` |
| 加减 | `+`、`-` |
| 位移 | `<<`、`>>`、`<<<`、`>>>` |
| 比较 | `>`、`<`、`>=`、`<=` |
| 相等 | `==`、`!=` |
| 位运算 | `&`、`^`、`|` |

注意：这里的逻辑结果也是数值，通常 `1` 表示 true，`0` 表示 false。

## 自定义运算符

`Expression` 构造器可以传入自定义 `Operator` 集合：

```java
Expression expression = new Expression("a + b", operators);
```

自定义运算符必须定义：

- 字面量
- 优先级
- 结合性
- 元数
- 实际计算逻辑

不要定义多个无法区分的相同字面量运算符，否则词法阶段会报歧义错误。

## 异常类型

常见异常：

| 异常 | 场景 |
| --- | --- |
| `ExpressionSyntaxException` | 表达式语法错误 |
| `ExpressionCalculationException` | 计算时变量缺失或表达式无效 |
| `ExpressionOperationException` | 运算符执行错误 |

## 性能注意事项

- 高频使用时复用 `Expression`。
- 变量上下文尽量复用结构，不要在热路径里频繁创建巨大 Map。
- 自定义运算符不要做阻塞 IO。
- 表达式语言不是脚本引擎，不要往里面塞复杂业务逻辑。

