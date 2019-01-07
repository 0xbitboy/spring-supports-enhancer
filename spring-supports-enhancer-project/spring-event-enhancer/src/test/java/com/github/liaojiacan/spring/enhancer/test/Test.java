package com.github.liaojiacan.spring.enhancer.test;

import com.github.liaojiacan.spring.enhancer.test.boot.ApplicationBoot;
import com.github.liaojiacan.spring.enhancer.test.config.ApplicationEventMulticasterConfig;
import com.github.liaojiacan.spring.enhancer.test.config.EventHandlerConfig;
import com.github.liaojiacan.spring.enhancer.test.event.DelayTestEvent;
import com.github.liaojiacan.spring.enhancer.test.event.TestEvent;
import com.github.liaojiacan.spring.enhancer.test.handler.EventHandler;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author liaojiacan
 * @date 2019/1/7
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@ContextConfiguration(classes = {
		EventHandlerConfig.class,
		ApplicationEventMulticasterConfig.class
})
@SpringBootTest(classes = ApplicationBoot.class)
@TestPropertySource(locations="classpath:application.properties")
public class Test {

	private final  Logger logger  = LoggerFactory.getLogger(Test.class);


	@Autowired
	private ApplicationContext applicationContext;


	@org.junit.Test
	public void testTestEventFires() throws InterruptedException {
		TestEvent testEvent = new TestEvent();
		testEvent.setId("001");
		applicationContext.publishEvent(testEvent);
		// verify(eventHandler).handleTestEvent(any(TestEvent.class));
		// block here waiting MQ consumer receive events.
		Thread.sleep(Integer.MAX_VALUE);
	}

	@org.junit.Test
	public void testDelayTestEventFires() throws InterruptedException {
		DelayTestEvent delayTestEvent = new DelayTestEvent("prj01");
		delayTestEvent.setDelay(TimeUnit.SECONDS.toMillis(5));
		applicationContext.publishEvent(delayTestEvent);
		// verify(eventHandler).handleTestEvent(any(TestEvent.class));
		// block here waiting MQ consumer receive events.
		Thread.sleep(Integer.MAX_VALUE);
	}

}
