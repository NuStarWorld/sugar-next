# 自定义 BeanFactory

`BeanFactory<M extends Annotation>` 是 Context 容器的核心扩展点。它决定某种注解标记的类型如何变成 Bean 实例。

## 接口职责

```java
public interface BeanFactory<M extends Annotation> {
    Class<M> getMetadataType();

    boolean validate(Context context, String beanName, M metadata, Class<?> beanType);

    <T> T create(Context context, String beanName, M metadata, Class<T> beanType);

    <T> T proxy(Context context, String beanName, M metadata, T instance, Class<T> beanType);
}
```

## validate

`validate` 判断目标类型是否可以注册为 Bean。

适合检查：

- 类型是否合法。
- 构造器是否满足要求。
- 依赖 Codec 或其他扩展是否存在。
- 元数据配置是否有效。

不要在 `validate` 里创建重对象。这个方法应该判断规则，不应该偷偷做初始化。

## create

`create` 创建原始实例。

常见策略：

- 无参构造器创建。
- `@Autowired` 构造器创建。
- 从配置文件反序列化。
- 从供应方法返回。

## proxy

`proxy` 可以包装实例。

适合做：

- Aware 回调。
- 事务代理。
- 事件订阅注册。
- 其他轻量增强。

不要在 `proxy` 里藏复杂业务逻辑。代理应该是基础设施，不是业务规则垃圾桶。

## 注册方式

通过启动元数据注册：

```java
@RegisterFactory(
    metadata = MyComponent.class,
    beanFactory = MyBeanFactory.class
)
```

也可以在 `initialize` 阶段调用：

```java
context.registerBeanFactory(MyComponent.class, new MyBeanFactory());
```

## 实现建议

- 注解类型和 `getMetadataType()` 必须一致。
- 失败要抛清楚异常，不要返回 `null`。
- 创建实例时优先使用已有工具，例如 `AutowiredUtils`。
- 如果支持代理，要保证代理类型仍然能赋值给 Bean 类型。
- 不要制造“工厂的工厂”。如果一个 BeanFactory 不能用简单逻辑解释，它大概率设计错了。

