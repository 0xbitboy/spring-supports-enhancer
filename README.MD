# spring-supports-enhancer
&emsp;&emsp; 这是一个可以扩展/增强Spring 组件功能的项目，是对spring-context-support的拓展

## 组成部分

- spring-i18n-enhancer 对spring-i18n的拓展，支持ResponseBody的国际化等......
- spring-cache-enhancer 对spring-cache的拓展，如让@Cacheable 支持refreshAfterWrite等......
- spring-event-enhancer 对ApplicationEvent的增强，如让ApplicationEvent支持分布式(集群模式)和延时消息等......
- spring-restful-enhancer 对restful的增强，如支持requestBody，responseBody的加解密等......

## 代码与示例

| 模块 | 进度     | 核心代码    | 示例工程 |
| -- | ----- | ----- | ----------- |
| spring-i18n-enhancer  | 开发完成 | [spring-i18n-enhancer](spring-supports-enhancer-project/spring-i18n-enhancer) | [spring-i18n-enhancer-sample](spring-supports-enhancer-samples/spring-i18n-enhancer-sample) |
| spring-cache-enhancer  | 实现了@CacheRefresh | [spring-cache-enhancer](spring-supports-enhancer-project/spring-cache-enhancer) |  无|
| spring-event-enhancer  | 实现了RabbitMqApplicationEventMulticaster | [spring-event-enhancer](spring-supports-enhancer-project/spring-event-enhancer) |  无|
| spring-restful-enhancer  | 开发中 | [spring-restful-enhancer](spring-supports-enhancer-project/spring-restful-enhancer) |  无|
