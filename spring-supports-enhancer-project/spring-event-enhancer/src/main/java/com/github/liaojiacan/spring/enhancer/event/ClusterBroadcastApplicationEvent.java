package com.github.liaojiacan.spring.enhancer.event;


/**
 * @author liaojiacan
 */
public class ClusterBroadcastApplicationEvent extends AmqpApplicationEvent{

	public ClusterBroadcastApplicationEvent(String from) {
		super(from);
	}

	public ClusterBroadcastApplicationEvent(Object source, String from) {
		super(source, from);
	}
}
