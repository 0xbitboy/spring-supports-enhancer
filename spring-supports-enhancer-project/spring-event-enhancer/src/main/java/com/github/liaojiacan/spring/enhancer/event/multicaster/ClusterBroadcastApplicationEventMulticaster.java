package com.github.liaojiacan.spring.enhancer.event.multicaster;


import com.github.liaojiacan.spring.enhancer.event.ClusterBroadcastApplicationEvent;
import com.github.liaojiacan.spring.enhancer.event.multicaster.RabbitMqApplicationEventMulticaster;
import org.springframework.context.ApplicationEvent;

/**
 * @author liaojiacan
 */
public class ClusterBroadcastApplicationEventMulticaster extends RabbitMqApplicationEventMulticaster {


	public ClusterBroadcastApplicationEventMulticaster(PublisherConfig publisherConfig, SubscriberConfig... subscriberConfig) {
		super(publisherConfig, subscriberConfig);
	}

	@Override
	protected boolean supported(ApplicationEvent event) {
		return event instanceof ClusterBroadcastApplicationEvent;
	}
}
