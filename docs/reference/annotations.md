# 注解参考

本页按模块列出 Sugar Next 的主要注解。

## context.annotation.component

| 注解 | 作用 | 常用位置 |
| --- | --- | --- |
| `@Component` | 声明通用组件 | class、annotation |
| `@Service` | 声明服务组件 | class |
| `@Repository` | 声明存储库组件 | class |
| `@Controller` | 声明控制器组件 | class |
| `@Configuration` | 声明配置对象 | class |
| `@Serialization` | 声明 Codec 支持的格式 | class |
| `@Supplier` | 从配置类方法供应 Bean | method |
| `@Subscriber` | 声明事件订阅相关组件 | class 或 method |

## context.annotation.feature

| 注解 | 作用 |
| --- | --- |
| `@Autowired` | 标记构造器或方法需要依赖注入 |
| `@Qualifier` | 按名称指定注入 Bean |
| `@Owned` | 限制只从当前上下文查找依赖 |
| `@Scope` | 指定 Bean 作用域 |
| `@DependsOn` | 声明依赖条件 |
| `@Scan` | 指定扫描包 |
| `@Named` | 指定 Bean 名称 |
| `@Environment` | 声明环境条件 |
| `@EnableSugar` | 启用 Sugar 相关能力 |
| `@RegisterFactory` | 注册 BeanFactory |
| `@RegisterFactories` | 批量注册 BeanFactory |
| `@RegisterProperty` | 注册上下文属性 |
| `@RegisterProperties` | 批量注册上下文属性 |
| `@RegisterParent` | 注册父上下文 |
| `@RegisterParents` | 批量注册父上下文 |

## command.annotation

| 注解 | 作用 |
| --- | --- |
| `@CommandHandler` | 标记命令处理方法 |
| `@CommandArgument` | 标记命令参数 |

## database.annotation

| 注解 | 作用 |
| --- | --- |
| `@Transaction` | 标记事务方法或类型 |

## reflect.annotation

| 注解 | 作用 |
| --- | --- |
| `@Reflect` | 指定反射代理默认目标 |
| `@ReflectConstructor` | 绑定构造器 |
| `@ReflectField` | 绑定字段 |
| `@ReflectMethod` | 绑定方法 |

## dispatch.annotation

| 注解 | 作用 |
| --- | --- |
| `@Dispatch` | 声明调度入口或调度方法 |

## dispatch.argument.annotation

| 注解 | 作用 |
| --- | --- |
| `@Argument` | 声明调度参数 |

## sorting.annotation

| 注解 | 作用 |
| --- | --- |
| `@Order` | 声明排序顺序 |

## 注意事项

- 组件注解可以作为元注解使用，但不要堆太深。元注解层级太深会让扫描和排错都变差。
- `@Transaction` 只是元数据，必须有外部框架处理才会生效。
- 反射注解的 `accessible = true` 要谨慎使用。

