# 异常参考

本页记录 Sugar Next 主要异常类型和常见触发场景。

## SugarNextException

基础异常类型，其他模块异常通常继承它。

## ContextException

所属模块：`context`

常见场景：

- 生命周期状态错误。
- Bean 创建失败。
- 资源加载失败。
- 组件扫描失败。
- 依赖注入失败。

排查建议：

- 先看生命周期是否按顺序调用。
- 检查扫描包是否覆盖目标类。
- 检查 BeanFactory 是否注册。
- 检查构造器参数是否能被注入。

## CommandException

所属模块：`command`

常见场景：

- 命令处理方法调用失败。
- 参数转换失败。
- 命令方法签名非法。

排查建议：

- 检查方法是否返回 `CommandResult`。
- 检查变量路径和 `@CommandArgument` 数量是否一致。
- 检查转换器和补全器方法签名。

## CommandArgumentConversionException

所属模块：`command`

常见场景：

- 输入参数无法转换成目标类型。
- 转换器内部抛错。

## ExpressionException

所属模块：`calculate`

表达式模块基础异常。

## ExpressionSyntaxException

表达式语法错误，例如非法字符、括号不匹配、运算符歧义。

## ExpressionCalculationException

计算阶段错误，例如变量缺失、操作数不足、表达式结果不唯一。

## ExpressionOperationException

运算符执行错误，例如操作数数量不匹配或运算逻辑失败。

## BytecodeException

所属模块：`bytecode`

字节码模块基础异常。

## BytecodeParsingException

常见场景：

- class 文件找不到。
- class 文件读取失败。
- ASM 解析失败。
- 注解或父类元数据解析失败。

## ReflectionException

所属模块：`reflect`

常见场景：

- 反射接口不是 interface。
- 目标类找不到。
- 字段、方法、构造器不存在。
- 返回类型或参数类型不兼容。
- 无权限访问目标成员。

## DatabaseException

所属模块：`database`

数据库抽象基础异常。

## TransactionException

常见场景：

- 当前线程没有开启事务却尝试获取 repository。
- 事务会话状态不合法。

## CodecException

所属模块：`databind.codec`

常见场景：

- 序列化失败。
- 反序列化失败。
- 输入格式和目标类型不匹配。

