package com.github.liaojiacan.spring.enhancer.test.event;

import com.github.liaojiacan.spring.enhancer.event.AmqpApplicationEvent;

/**
 * @author liaojiacan
 * @date 2019/1/7
 */
public class DelayTestEvent extends AmqpApplicationEvent {

	public DelayTestEvent() {
		super("prj01");
	}

	public DelayTestEvent(String from) {
		super(from);
	}
}
