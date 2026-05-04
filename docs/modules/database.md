# Database 事务抽象

`database` 模块提供数据库会话和事务抽象。它不是 ORM，也不负责连接池、SQL 映射或实体管理。

## 它解决什么问题

这个模块定义了一组接口，让上层框架可以用统一方式开启事务、获取 repository、提交、回滚和关闭会话。

具体数据库实现由外部提供。

## DatabaseSession

`DatabaseSession` 表示一次数据库会话，核心能力：

- 获取 repository
- 提交
- 回滚
- 关闭

```java
try (DatabaseSession session = factory.openSession(mode, isolation)) {
    UserRepository repository = session.getRepository(UserRepository.class);
    session.commit();
}
```

## DatabaseSessionFactory

`DatabaseSessionFactory` 负责创建 `DatabaseSession`。

它定义默认执行模式和隔离级别。具体含义取决于实现方，接口本身不绑定某个数据库框架。

## TransactionManager

`TransactionManager` 通常交给自动化框架使用，不建议业务代码到处手动调用。

它负责：

- 打开当前线程绑定的事务会话。
- 如果已有事务，则复用当前事务。
- 从当前事务会话获取 repository。

## TransactionSession

`TransactionSession` 是事务会话包装器，内部使用引用计数管理嵌套调用：

- `open()` 增加引用计数。
- `close()` 减少引用计数。
- 最外层关闭时提交或回滚，并关闭底层会话。

这种设计适合事务方法嵌套调用。

## @Transaction

`@Transaction` 用于标记需要事务的方法或类型。注解本身只表达元数据，真正拦截和执行事务需要外部自动化框架配合。

不要以为加了注解事务就自动生效。没有代理、拦截器或框架集成，它只是一个注解。

## 嵌套事务

`TransactionSession` 的引用计数避免内层方法提前关闭事务。

如果内层调用 `rollback()`，会标记当前事务需要回滚。最外层关闭时会按状态处理底层会话。

## 使用限制

- 这个模块不是 ORM。
- 事务绑定策略需要外部实现。
- repository 的创建和生命周期由具体 `DatabaseSession` 实现决定。
- 不要在业务代码里手写复杂事务栈，应该由框架集成 `TransactionManager`。

