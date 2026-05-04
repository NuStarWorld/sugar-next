# 包结构参考

本页按包说明 Sugar Next 的源码结构。

| 包 | 作用 |
| --- | --- |
| `team.idealstate.sugar.next.bytecode` | 字节码读取实现和入口 |
| `team.idealstate.sugar.next.bytecode.api` | 字节码 API 基础类型 |
| `team.idealstate.sugar.next.bytecode.api.member` | class、field、method、constructor、package 等成员结构 |
| `team.idealstate.sugar.next.bytecode.api.struct` | 注解、枚举等结构 |
| `team.idealstate.sugar.next.bytecode.exception` | 字节码异常 |
| `team.idealstate.sugar.next.calculate` | 表达式、词法器、token |
| `team.idealstate.sugar.next.calculate.operation` | 运算符抽象 |
| `team.idealstate.sugar.next.calculate.operation.standard` | 标准运算符实现 |
| `team.idealstate.sugar.next.calculate.exception` | 表达式异常 |
| `team.idealstate.sugar.next.command` | 命令树、上下文、执行器、结果 |
| `team.idealstate.sugar.next.command.annotation` | 命令注解 |
| `team.idealstate.sugar.next.command.exception` | 命令异常 |
| `team.idealstate.sugar.next.context` | Context、Bean、生命周期、属性 |
| `team.idealstate.sugar.next.context.annotation.component` | 组件类注解 |
| `team.idealstate.sugar.next.context.annotation.feature` | 容器功能注解 |
| `team.idealstate.sugar.next.context.aware` | Aware 回调接口 |
| `team.idealstate.sugar.next.context.factory` | BeanFactory 实现 |
| `team.idealstate.sugar.next.context.lifecycle` | 初始化和销毁生命周期接口 |
| `team.idealstate.sugar.next.context.util` | 自动注入工具 |
| `team.idealstate.sugar.next.database` | 数据库会话和事务抽象 |
| `team.idealstate.sugar.next.database.annotation` | 事务注解 |
| `team.idealstate.sugar.next.database.exception` | 数据库异常 |
| `team.idealstate.sugar.next.databind` | `Pair`、`Property` |
| `team.idealstate.sugar.next.databind.codec` | 序列化和反序列化接口 |
| `team.idealstate.sugar.next.dispatch` | 通用调度抽象 |
| `team.idealstate.sugar.next.dispatch.argument` | 调度参数转换、补全、接收 |
| `team.idealstate.sugar.next.eventbus` | 事件总线 |
| `team.idealstate.sugar.next.exception` | 基础异常 |
| `team.idealstate.sugar.next.function` | `Lazy`、`Functional` |
| `team.idealstate.sugar.next.function.closure` | 自定义闭包接口 |
| `team.idealstate.sugar.next.hex` | 十六进制工具 |
| `team.idealstate.sugar.next.io` | IO 工具 |
| `team.idealstate.sugar.next.lang` | 基本类型包装映射 |
| `team.idealstate.sugar.next.name` | 命名接口 |
| `team.idealstate.sugar.next.reflect` | 反射代理 |
| `team.idealstate.sugar.next.reflect.annotation` | 反射注解 |
| `team.idealstate.sugar.next.sorting` | 排序接口和比较器 |
| `team.idealstate.sugar.next.stacktrace` | 异常堆栈工具 |
| `team.idealstate.sugar.next.uuid` | UUID 工具 |

## 维护建议

新增包时要先判断它属于核心模块、扩展模块还是工具模块。不要为了一个类新建一个含义模糊的包。

