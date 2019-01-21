## spring-cache-enhancer
&emsp;&emsp;这是一个对Spring-Cache的一些增强实现，目前实现了@CacheRefresh注解

### 应用场景
1. 采用@CacheRefresh可以控制缓存在指定时间由一个线程更新，其他线程等待更新线程同步完成，等待超时则返回旧值
```
//等待5s后开始更新缓存
@Cacheable(key = "#p0")
@CacheRefresh(refreshAfterWrite = 5,timeWait = 1,unit = TimeUnit.SECONDS,async = false)
```

### 如何使用
1. 引入本项目的jar包（目前还没发布到仓库）
2. 用@EnableEnhancedCaching代替@EnableCaching

