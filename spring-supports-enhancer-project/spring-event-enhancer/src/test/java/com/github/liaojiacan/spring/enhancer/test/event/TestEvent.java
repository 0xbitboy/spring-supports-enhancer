package com.github.liaojiacan.spring.enhancer.test.event;

import com.github.liaojiacan.spring.enhancer.event.AmqpApplicationEvent;

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
