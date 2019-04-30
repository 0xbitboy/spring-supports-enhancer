package com.github.liaojiacan.spring.enhancer.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.liaojiacan.spring.enhancer.common.util.HostNameUtil;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.Assert;


/**
 * AMQP队列事件,子类必须有一个默认构造函数
 * 
 * @author liaojiacan
 */
public class AmqpApplicationEvent extends ApplicationEvent {

	private static final long serialVersionUID = -3051024683057439677L;

	/**
	 * 事件Id
	 */
	private String eventId;

	/**
	 * 发起事件主机名
	 */
	private final String fromHost = HostNameUtil.localHostName;

	/**
	 * 发起事件地方
	 */
	private final String from;

	/**
	 * 事件类名
	 */
	private final String eventClass;

	/**
	 * 延时消息 毫秒
	 */
	private long delay;

	/**
	 * 是否是远程事件
	 */
	private boolean remoteEvent = true;

	@JsonCreator
	public AmqpApplicationEvent(@JsonProperty("from") String from) {
		this(from, from);
	}

	public AmqpApplicationEvent(Object source, String from) {
		super(source);
		this.from = from;
		this.eventClass = getClass().getCanonicalName();
		Assert.hasText(eventClass, "Can't get the event's canonical name,make sure it is not a local or anonymous class");
	}

	public String getFromHost() {
		return fromHost;
	}


	public String getEventClass() {
		return eventClass;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public boolean isRemoteEvent() {
		return remoteEvent;
	}

	public void setRemoteEvent(boolean remoteEvent) {
		this.remoteEvent = remoteEvent;
	}

	public String getFrom() {
		return from;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
}
