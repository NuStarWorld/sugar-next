# 废弃 API 迁移

本页记录项目中已废弃 API 的替代方案。

| 已废弃 API | 替代 API | 原因 |
| --- | --- | --- |
| `Context.RESOURCE_BUNDLED` | `Context.RESOURCE_EMBEDDED` | `embedded` 更准确表达嵌入资源 |
| `Bean#getDependsOn()` | `Bean#getDependencies()` | 单个 `DependsOn` 不能完整表达依赖列表 |
| `Bean#getMetadataType()` | `Bean#getMetadata().getClass()` | 元数据类型命名不准确 |
| `Bean#getMarked()` | `Bean#getType()` | `marked` 语义不明确 |

## 迁移原则

1. 新文档和新示例不要继续使用废弃 API。
2. 如果废弃 API 仍保留兼容，文档应明确替代写法。
3. 删除废弃 API 前必须给出迁移路径。
4. 如果替代 API 语义不同，要写清楚行为差异。

## 示例

旧写法：

```java
Context.RESOURCE_BUNDLED
```

新写法：

```java
Context.RESOURCE_EMBEDDED
```

旧写法：

```java
DependsOn dependsOn = bean.getDependsOn();
```

新写法：

```java
List<DependsOn> dependencies = bean.getDependencies();
```

