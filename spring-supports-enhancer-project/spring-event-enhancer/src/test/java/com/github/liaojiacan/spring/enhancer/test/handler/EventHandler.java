package com.github.liaojiacan.spring.enhancer.test.handler;

import com.github.liaojiacan.spring.enhancer.test.event.DelayTestEvent;
import com.github.liaojiacan.spring.enhancer.test.event.TestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

/**
 * @author liaojiacan
 * @date 2019/1/7
 */
public class EventHandler {

	private final Logger logger  = LoggerFactory.getLogger(EventHandler.class);

	@EventListener(TestEvent.class)
	public void handleTestEvent(TestEvent event){
		logger.info("Receive TestEvent.id={}",event.getId());
	}

	@EventListener(DelayTestEvent.class)
	public void handleDelayTestEvent(DelayTestEvent event){
		logger.info("Receive DelayTestEvent success");
	}
}
