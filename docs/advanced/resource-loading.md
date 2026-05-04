# 资源加载规则

`Context#getResource` 支持多种资源路径。资源加载的关键是明确资源归属：它到底来自 classpath、当前上下文、嵌入 jar、数据目录，还是外部 URI。

## classpath:

```text
classpath:config/default.yml
```

从指定 `ClassLoader` 读取资源。

适合读取普通 classpath 资源。

## context:

```text
context:config/default.yml
```

从当前上下文宿主所在 jar 中读取资源。如果 jar 不存在，会回退到当前上下文 classloader。

适合读取当前插件或当前模块自己的内置资源。

## embedded:

```text
embedded:config/default.yml
```

从传入 holder 所在 jar 中读取资源。

适合读取某个具体类所在包或 jar 中的嵌入资源。

## bundled:

`bundled:` 已废弃。新代码应该使用 `embedded:`。

继续使用 `bundled:` 只会留下迁移债务。

## 相对路径

没有 URI scheme 的路径会从 `Context#getDataFolder()` 下读取：

```text
config/app.yml
```

适合用户可编辑配置。

## 绝对 URI

绝对 URI 会直接打开流：

```text
file:/data/app/config.yml
https://example.com/config.yml
```

使用远程 URI 要谨慎。网络失败、超时和安全问题都不是容器能替你解决的。

## 注意事项

- 资源流由调用方负责正确关闭，除非交给会关闭流的工具方法。
- 配置文件释放到 data folder 时，要处理父目录不存在的情况。
- 不要混淆 `context:` 和 `classpath:`，它们表达的资源归属不同。

