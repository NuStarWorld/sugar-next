# Command 命令系统

`command` 模块用于把一个对象上的注解方法组装成命令树，支持参数转换、补全、权限和帮助树。

## 它解决什么问题

命令系统适合游戏插件、控制台工具、管理命令等场景。它把命令路径、参数、权限和执行方法绑定在一起，避免手写一堆字符串分支。

手写几十层 `if-else` 解析命令是很愚蠢的做法：逻辑重复、补全难写、权限散落、错误提示不一致。命令树就是用结构消灭这些边界情况。

## 创建命令行

入口：

```java
CommandLine commandLine = CommandLine.of("user", new UserCommand());
```

执行：

```java
CommandResult result = commandLine.execute(context, "create", "sugar");
```

补全：

```java
List<String> completed = commandLine.complete(context, "cr");
```

## 定义命令处理方法

命令方法必须：

- 是实例方法。
- 标注 `@CommandHandler`。
- 返回 `CommandResult`。

```java
public final class UserCommand {
    @CommandHandler("create {name}")
    public CommandResult create(CommandContext context, @CommandArgument String name) {
        context.getSender().sendMessage("created: " + name);
        return CommandResult.success();
    }
}
```

返回 `null` 是错误。命令执行必须给出明确结果。

## 字面量参数

命令路径里的普通单词是字面量参数：

```java
@CommandHandler("reload")
public CommandResult reload(CommandContext context) {
    return CommandResult.success();
}
```

字面量参数用于固定命令结构，补全时会按前缀匹配。

## 变量参数

变量参数用 `{name}`：

```java
@CommandHandler("delete {name}")
public CommandResult delete(CommandContext context, @CommandArgument String name) {
    return CommandResult.success();
}
```

硬规则：变量参数必须出现在字面量参数之后。下面这种写法不允许：

```java
@CommandHandler("{name} delete")
```

这不是限制创造力，而是避免命令树匹配变成歧义垃圾。

## 方法参数

方法参数只能是两类：

- `CommandContext`
- 标注 `@CommandArgument` 的参数

如果参数标注了 `@CommandArgument`，默认使用 Java 参数名作为变量名。也可以显式指定：

```java
@CommandHandler("create {name}")
public CommandResult create(@CommandArgument("name") String username) {
    return CommandResult.success();
}
```

如果编译时没有保留参数名，应该显式写 `@CommandArgument("name")`，不要赌运行时能猜出来。

## 参数转换

参数转换器负责把字符串转换成目标类型。

可以通过 `converterType` 指定转换器类，也可以通过 `converter` 指定当前命令对象上的方法。

转换器应该区分两种场景：

- `strict = false`：用于匹配和预检查。
- `strict = true`：用于真正执行前转换。

不要在非严格模式里做昂贵操作，例如数据库查询。补全和匹配可能频繁调用它。

## 参数补全

补全器可以通过 `completerType` 指定类，也可以通过 `completer` 指定当前命令对象上的方法。

补全方法签名通常接收：

```java
List<String> completeName(CommandContext context, String argument)
```

补全结果会去重，并且会过滤权限不可见的路径。

## 权限控制

`@CommandHandler` 可以配置权限。没有配置时，默认权限由根命令名和路径节点组成。

如果命令是开放的，或发送者是管理员，则直接允许执行。

权限不要写得太细碎。权限节点碎成粉末，只会让配置和排错都痛苦。

## 帮助树

当没有传入参数时，命令系统会构建帮助树并发送给 `CommandSender`。帮助树会考虑权限，只展示当前发送者可见的命令。

命令描述来自 `@CommandHandler(description = "...")`。

## 常见错误

1. 命令方法返回类型不是 `CommandResult`。
2. 方法参数既不是 `CommandContext`，也没有 `@CommandArgument`。
3. 路径变量数量和 `@CommandArgument` 参数数量不一致。
4. 变量参数写在字面量参数前面。
5. 转换器方法签名不匹配。
6. 补全器里做昂贵查询。

## 相关文档

- [Context 容器](context.md)
- [注解参考](../reference/annotations.md)

