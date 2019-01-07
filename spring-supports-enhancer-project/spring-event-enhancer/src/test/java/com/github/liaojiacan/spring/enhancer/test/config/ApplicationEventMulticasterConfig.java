package com.github.liaojiacan.spring.enhancer.test.config;

import com.github.liaojiacan.spring.enhancer.event.multicaster.RabbitMqApplicationEventMulticaster;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liaojiacan
 * @date 2019/1/7
 */
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
		subscriberConfig.setExchangeName("prj01");
		subscriberConfig.setConnectionFactory(connectionFactory());
		subscriberConfig.setPatterns(Stream.of("prj01.#").collect(Collectors.toSet()));

		RabbitMqApplicationEventMulticaster applicationEventMulticaster = new RabbitMqApplicationEventMulticaster(publisherConfig,subscriberConfig);
		return applicationEventMulticaster;
	}

}
