# spring-event-enhancer
&emsp;&emsp; 本项目是对spring中的事件驱动编程进行的拓展，从ApplicationEventMultiCaster下手，将ApplicationEvent与RabbitMQ结合，实现可靠的事件传递与延时事件的目的。
当然也可以使用其他消息队列去承载。

## 更新记录
1. 2019-04-30 加入可靠消息的实现。

## 应用场景

1. 你的项目中大量使用的ApplicationEvent ，但是在做服务拆分的时候想做最小的改动。
2. 想引入异步延时消息，但是又不想在业务代码中直接使用@RabbitListener等（要配置各种Key，exchange，queue等）。
3. 主要还是第一点，如果一开始就有考虑到分布式之类的可以直接使用[JMS(Java Message Service)](https://zh.wikipedia.org/wiki/Java%E6%B6%88%E6%81%AF%E6%9C%8D%E5%8A%A1)

## 如何使用

- 配置ApplicationEventMultiCaster
```
@Configuration
public class ApplicationEventMulticasterConfig {

	@Value("${spring.rabbitmq.host}")
	private String rabbitHost ;

	@Value("${spring.rabbitmq.port}")
	private Integer rabbitPort;

	@Value("${spring.rabbitmq.username}")
	private String username;

	@Value("${spring.rabbitmq.password}")
	private String password;

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitHost, rabbitPort);
		connectionFactory.setVirtualHost("test");
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		connectionFactory.setPublisherConfirms(true);
		return connectionFactory;
	}
	@Bean
	public ApplicationEventMulticaster applicationEventMulticaster(){
		RabbitMqApplicationEventMulticaster.PublisherConfig publisherConfig = new RabbitMqApplicationEventMulticaster.PublisherConfig();
		publisherConfig.setConnectionFactory(connectionFactory());
		publisherConfig.setExchangeName("prj01");

		RabbitMqApplicationEventMulticaster.SubscriberConfig subscriberConfig = new RabbitMqApplicationEventMulticaster.SubscriberConfig();
		// 这个ExchageName为你订阅的MQ队列绑定的Exchage
		subscriberConfig.setExchangeName("prj01");
		subscriberConfig.setConnectionFactory(connectionFactory());
		// 这个pattern 为topicExchage的pattern，消息的rotueKey为${exchangeName}.${eventClassName}的组合
		subscriberConfig.setPatterns(Stream.of("prj01.#").collect(Collectors.toSet()));

		RabbitMqApplicationEventMulticaster applicationEventMulticaster = new RabbitMqApplicationEventMulticaster(publisherConfig,subscriberConfig);
		return applicationEventMulticaster;
	}

}

```
- 派生AmqpApplicationEvent
 
 > 因为使用的是Json的序列化方式，所以得有默认的构造方法，目前还没找到更加友好的方式

```
public class TestEvent extends AmqpApplicationEvent {

	private String id;

	public TestEvent() {
		super("prj01","prj01");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}

```
- 业务中还是使用@EventListener

```
@EventListener(TestEvent.class)
public void handleTestEvent(TestEvent event){
    logger.info("Receive TestEvent.id={}",event.getId());
}

@EventListener(DelayTestEvent.class)
public void handleDelayTestEvent(DelayTestEvent event){
    logger.info("Receive DelayTestEvent success");
}

```

## publisherConfig 和 subscriberConfig

- publisher 永远只push到自己的Queue, 即publisherConfig中的exchangeName最好就是项目的名称
- 支持多个subscriber，也就是可以订阅其他项目消息事件。
- 如果要实现一个消息集群广播，需要在subscriber设置groupId，设置为当前节点的hostName，或者instanceId。默认情况下groupId为default只能被消费一次。（类似Kafka的groupId）
