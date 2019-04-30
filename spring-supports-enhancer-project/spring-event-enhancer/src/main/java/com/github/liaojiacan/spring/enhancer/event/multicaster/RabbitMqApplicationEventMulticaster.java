package com.github.liaojiacan.spring.enhancer.event.multicaster;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.liaojiacan.spring.enhancer.common.util.JsonUtil;
import com.github.liaojiacan.spring.enhancer.event.AmqpApplicationEvent;
import com.github.liaojiacan.spring.enhancer.event.AsyncEventWrapperMessage;
import com.github.liaojiacan.spring.enhancer.event.TransactionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.*;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于RabbitMQ的事件多播器
 *
 * @author liaojiacan
 * @see PublisherConfig 发布者配置类 applicationContext.publishEvent(..)
 * @see SubscriberConfig 订阅者配置类 @EventListener(...)
 * groupId 组ID是为了区分单播还是群播，不设置groupId或者设置一个相同的groupId则为单播. 相同的groupId的消费者都是绑定同一个队列，群播的话可以将groupId设置为机器的hostname
 */
public class RabbitMqApplicationEventMulticaster extends SimpleApplicationEventMulticaster implements SmartInitializingSingleton, DisposableBean, ApplicationContextAware, MessageListener {

    private static Logger logger = LoggerFactory.getLogger("EVENT_BUS_LOGGER");

    private final static String EXCHANGE_NAME_PREFIX = "event.bus.exchange";

    private final static String QUEUE_NAME_PREFIX = "event.bus.queue";

    private AmqpTemplate amqpTemplate;

    private ConfigurableApplicationContext applicationContext;

    private String publishExchangeName;

    private PublisherConfig publisherConfig;

    private SubscriberConfig[] subscriberConfigs;

    private List<SimpleMessageListenerContainer> listenerContainers;

    private TransactionResolver transactionResolver;

    private Map<String, Class> qualifiedClassNameMap = Collections.emptyMap();

    private Map<String, Set<Class>> nonQualifiedClassNameMap = Collections.emptyMap();

    private ScheduledExecutorService compensationTaskService = new ScheduledThreadPoolExecutor(4, new ThreadFactory() {

        private AtomicInteger threadId = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "AMQP-Multicaster-task-thread-" + threadId.incrementAndGet());
        }
    });

    private RabbitMqApplicationEventMulticaster() {

    }

    private RabbitMqApplicationEventMulticaster(BeanFactory beanFactory) {
        super(beanFactory);
    }

    public RabbitMqApplicationEventMulticaster(PublisherConfig publisherConfig, SubscriberConfig... subscriberConfig) {
        Assert.notNull(publisherConfig.exchangeName, "configuration[groupId] can not be null");
        this.publisherConfig = publisherConfig;
        this.subscriberConfigs = subscriberConfig;
        this.publishExchangeName = getPublishExchangeName();

    }

    /**
     * 延时消息时长阈值，当延时消息时长大于该值时，将以接力方式传递
     */
    private static final Long MAX_DELAY_MESSAGE_TIME = 1000L * 60L * 60L * 24L * 15L;


    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        if (supported(event) && ((AmqpApplicationEvent) event).isRemoteEvent()) {
            AmqpApplicationEvent amqpEvent = (AmqpApplicationEvent) event;
            //往MQ队列里面丢
            AsyncEventWrapperMessage<AmqpApplicationEvent> message = new AsyncEventWrapperMessage<>();
            message.setEvent(amqpEvent);
            message.setDelay(amqpEvent.getDelay());
            message.setEventClass(amqpEvent.getEventClass());
            message.setSendTime(new Date());
            message.setReceiveTime(new Date(System.currentTimeMillis() + amqpEvent.getDelay()));
            if (transactionResolver != null) {

                if (StringUtils.hasText(amqpEvent.getEventId())) {
                    message.setMessageId(amqpEvent.getEventId());
                } else {
                    message.setMessageId(UUID.randomUUID().toString());
                }
                transactionResolver.storeMessage(message);
            }
            sendMqMessage(message);
        } else {
            super.multicastEvent(event, eventType);
        }
    }

    protected boolean supported(ApplicationEvent event) {
        return event instanceof AmqpApplicationEvent;
    }

    private void sendMqMessage(AsyncEventWrapperMessage<? extends AmqpApplicationEvent> message) {

        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
        properties.setContentEncoding("UTF-8");
        properties.setMessageId(message.getMessageId());

        if (message.getDelay() > MAX_DELAY_MESSAGE_TIME) {
            properties.setDelay(MAX_DELAY_MESSAGE_TIME.intValue());
            message.setRelayed(true);
        } else {
            properties.setDelay((message.getDelay().intValue()));
            message.setRelayed(false);
        }

        byte[] messageByte = JsonUtil.toBytesJson(message);
        Message mqMessage = new Message(messageByte, properties);

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Schedule delay message [delay={}ms] for event center [eventClass={}] .", properties.getDelay(), message.getEventClass());
            }
            amqpTemplate.send(this.publishExchangeName, publisherConfig.getExchangeName() + "." + message.getEventClass(), mqMessage);
        } catch (Exception e) {
            logger.error("Error while scheduling delay message for event center [eventClass={}].", message.getEventClass(), e);
        }
    }

    private String getPublishExchangeName() {
        return EXCHANGE_NAME_PREFIX + "." + publisherConfig.getExchangeName();
    }


    @Override
    protected void invokeListener(ApplicationListener listener, ApplicationEvent event) {
        try {
            super.invokeListener(listener, event);
        } catch (Throwable ex) {
            String eventClass = event.getClass().toString();
            String detail = listener.toString();
            logger.error("Unexpected exception occurs when processing published application event, event class is {},detail is {} ", eventClass, detail);
            if (getErrorHandler() != null) {
                getErrorHandler().handleError(ex);
            }
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        initPublisher();
        initSubscriber();
        //Event的反序列化需要做的准备
        initEventRetriever();
        initCompensationTask();
    }

    private void initCompensationTask() {
        if (this.transactionResolver != null) {
            this.compensationTaskService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    transactionResolver.findUnConfirmMessages().forEach(message -> sendMqMessage(message));
                }
            }, 5, 60, TimeUnit.SECONDS);
        }
    }

    private void initSubscriber() {

        List<SimpleMessageListenerContainer> listenerContainers = new ArrayList<>(subscriberConfigs.length);
        Arrays.stream(subscriberConfigs).forEach(subscriberConfig -> {
            Assert.notNull(subscriberConfig.exchangeName, "SubscriberConfig[exchangeName] can not be null");
            Assert.notNull(subscriberConfig.connectionFactory, "SubscriberConfig[connectionFactory] can not be null");
            Assert.notEmpty(subscriberConfig.patterns, "SubscriberConfig[patterns] can not be empty");

            RabbitAdmin rabbitAdmin = new RabbitAdmin(subscriberConfig.getConnectionFactory());
            rabbitAdmin.afterPropertiesSet();

            TopicExchange exchange = new TopicExchange(EXCHANGE_NAME_PREFIX + "." + subscriberConfig.exchangeName, true, false);
            exchange.setAdminsThatShouldDeclare(true);
            exchange.setDelayed(true);
            Queue queue = new Queue(QUEUE_NAME_PREFIX + "." + subscriberConfig.getGroupId(), true);
            rabbitAdmin.declareExchange(exchange);
            rabbitAdmin.declareQueue(queue);
            subscriberConfig.getPatterns().forEach(pattern -> {
                rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(pattern));
            });

            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
            container.setConnectionFactory(subscriberConfig.getConnectionFactory());
            container.setMessageListener(this);
            container.setMaxConcurrentConsumers(subscriberConfig.getMaxConcurrentConsumers());
            container.setConcurrentConsumers(subscriberConfig.getConcurrentConsumers());
            container.addQueueNames(queue.getName());
            container.afterPropertiesSet();

            if (!container.isRunning()) {
                container.start();
            }
            listenerContainers.add(container);
        });

        this.listenerContainers = listenerContainers;
    }

    private void initPublisher() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(publisherConfig.getConnectionFactory());
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        rabbitTemplate.setRetryTemplate(retryTemplate);
        if (this.transactionResolver != null) {
            rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {

                @Override
                public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                    if (ack) {
                        transactionResolver.confirmMessage(correlationData.getId());
                    }
                }
            });
        }

        this.amqpTemplate = rabbitTemplate;
        RabbitAdmin rabbitAdmin = new RabbitAdmin(publisherConfig.getConnectionFactory());
        rabbitAdmin.afterPropertiesSet();

        //定义exchange
        TopicExchange exchange = new TopicExchange(this.publishExchangeName, true, false);
        exchange.setDelayed(true);
        rabbitAdmin.declareExchange(exchange);

    }

    private void initEventRetriever() {
        String[] beanNames = this.applicationContext.getBeanNamesForType(Object.class);
        Map<String, Class> qualifiedClassNameMap = new HashMap<>(beanNames.length);
        Map<String, Set<Class>> nonQualifiedClassNameMap = new HashMap<>(beanNames.length);
        for (String beanName : beanNames) {
            if (!ScopedProxyUtils.isScopedTarget(beanName)) {
                Class<?> type = null;
                try {
                    type = AutoProxyUtils.determineTargetClass(this.applicationContext.getBeanFactory(), beanName);
                } catch (Throwable ex) {
                    // An unresolvable bean type, probably from a lazy bean - let's ignore it.
                    if (logger.isDebugEnabled()) {
                        logger.debug("Could not resolve target class for bean with name '" + beanName + "'", ex);
                    }
                }
                if (type != null) {
                    if (ScopedObject.class.isAssignableFrom(type)) {
                        try {
                            type = AutoProxyUtils.determineTargetClass(this.applicationContext.getBeanFactory(),
                                    ScopedProxyUtils.getTargetBeanName(beanName));
                        } catch (Throwable ex) {
                            // An invalid scoped proxy arrangement - let's ignore it.
                            if (logger.isDebugEnabled()) {
                                logger.debug("Could not resolve target bean for scoped proxy '" + beanName + "'", ex);
                            }
                        }
                    }
                    try {

                        //scan @EventListener annotation methods
                        scanEventListerAnnotation(type, beanName, qualifiedClassNameMap, nonQualifiedClassNameMap);

                    } catch (Throwable ex) {
                        throw new BeanInitializationException("Failed to process @EventListener " +
                                "annotation on bean with name '" + beanName + "'", ex);
                    }
                }
            }
        }

        this.qualifiedClassNameMap = qualifiedClassNameMap;
        this.nonQualifiedClassNameMap = nonQualifiedClassNameMap;

    }

    private void scanEventListerAnnotation(Class<?> targetType, String beanName, Map<String, Class> qualifiedClassNameMap, Map<String, Set<Class>> nonQualifiedClassNameMap) {
        try {
            Map<Method, EventListener> annotatedMethods = MethodIntrospector.selectMethods(targetType,
                    new MethodIntrospector.MetadataLookup<EventListener>() {
                        @Override
                        public EventListener inspect(Method method) {
                            return AnnotatedElementUtils.findMergedAnnotation(method, EventListener.class);
                        }
                    });


            if (!CollectionUtils.isEmpty(annotatedMethods)) {

                annotatedMethods.forEach((method, eventListener) -> {
                    Class<?>[] classes = eventListener.value();
                    Arrays.stream(classes).forEach(clazz -> {
                        String simpleName = clazz.getSimpleName();
                        String qualifiedName = clazz.getCanonicalName();
                        if (!nonQualifiedClassNameMap.containsKey(simpleName)) {
                            nonQualifiedClassNameMap.put(simpleName, Stream.of(clazz).collect(Collectors.toSet()));
                        } else {
                            nonQualifiedClassNameMap.get(simpleName).add(clazz);
                        }
                        qualifiedClassNameMap.put(qualifiedName, clazz);
                    });

                });
            }
        } catch (Throwable ex) {
            // An unresolvable type in a method signature, probably from a lazy bean - let's ignore it.
            if (logger.isDebugEnabled()) {
                logger.debug("Could not resolve methods for bean with name '" + beanName + "'", ex);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void onMessage(Message rawMessage) {
        logger.info("Receive event bus message [{}].", rawMessage.toString());

        AsyncEventWrapperMessage<AmqpApplicationEvent> message = JsonUtil.fromJson(rawMessage.getBody(), new TypeReference<AsyncEventWrapperMessage<AmqpApplicationEvent>>() {
        });
        Class eventTargetType = this.qualifiedClassNameMap.get(message.getEventClass());
        if (eventTargetType == null) {
            String nonQualifiedClassName = getNonQualifiedClassName(message.getEventClass());
            Set<Class> classes = this.nonQualifiedClassNameMap.get(nonQualifiedClassName);
            if (!CollectionUtils.isEmpty(classes)) {
                if (classes.size() > 1) {
                    logger.error("Found duplicate EventName for eventClass=[{}], duplicate classes[{}]", nonQualifiedClassName, String.join(",", classes.stream().map(clazz -> clazz.getCanonicalName()).collect(Collectors.toList())));
                } else {
                    eventTargetType = classes.iterator().next();
                }
            }
        }

        if (eventTargetType == null) {
            logger.warn("Unsupported event,eventName={},message={}", message.getEventClass(), rawMessage.toString());
            return;
        }
        message = JsonUtil.fromJson(rawMessage.getBody(), AsyncEventWrapperMessage.class, eventTargetType);
        if (message.isRelayed()) {
            long delay = (message.getReceiveTime().getTime() - System.currentTimeMillis());
            message.setDelay(delay);
            sendMqMessage(message);
            return;
        }
        message.getEvent().setRemoteEvent(false);
        super.multicastEvent(message.getEvent());
    }

    private String getNonQualifiedClassName(String eventClass) {
        return eventClass.substring(eventClass.lastIndexOf(".") + 1);
    }

    @Override
    public void destroy() throws Exception {
        this.listenerContainers.forEach(listenerContainer -> {
            try {
                listenerContainer.destroy();
            } catch (Exception e) {
                logger.error("ListenerContainer close fail!", e);
            }
        });
        this.compensationTaskService.shutdown();
    }

    public void setTransactionResolver(TransactionResolver transactionResolver) {
        this.transactionResolver = transactionResolver;
    }


    /**
     * 发布者配置
     */
    public static class PublisherConfig {

        private String exchangeName;

        private ConnectionFactory connectionFactory;

        public String getExchangeName() {
            return exchangeName;
        }

        public void setExchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
        }

        public ConnectionFactory getConnectionFactory() {
            return connectionFactory;
        }

        public void setConnectionFactory(ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
        }
    }

    /**
     * 订阅配置
     */
    public static class SubscriberConfig {

        private String exchangeName;

        private ConnectionFactory connectionFactory;

        private Set<String> patterns;

        private int maxConcurrentConsumers = 2;

        private int concurrentConsumers = 1;

        /**
         * 同一个消息只能被同一组的消费者消费一次
         */
        private String groupId = "default";

        public String getExchangeName() {
            return exchangeName;
        }

        public void setExchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
        }

        public ConnectionFactory getConnectionFactory() {
            return connectionFactory;
        }

        public void setConnectionFactory(ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
        }

        public Set<String> getPatterns() {
            return patterns;
        }

        public void setPatterns(Set<String> patterns) {
            this.patterns = patterns;
        }

        public int getMaxConcurrentConsumers() {
            return maxConcurrentConsumers;
        }

        public void setMaxConcurrentConsumers(int maxConcurrentConsumers) {
            this.maxConcurrentConsumers = maxConcurrentConsumers;
        }

        public int getConcurrentConsumers() {
            return concurrentConsumers;
        }

        public void setConcurrentConsumers(int concurrentConsumers) {
            this.concurrentConsumers = concurrentConsumers;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }
    }


}