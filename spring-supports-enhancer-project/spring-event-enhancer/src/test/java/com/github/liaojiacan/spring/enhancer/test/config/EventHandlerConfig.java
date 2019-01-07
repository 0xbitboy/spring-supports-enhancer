package com.github.liaojiacan.spring.enhancer.test.config;

import com.github.liaojiacan.spring.enhancer.test.handler.EventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liaojiacan
 * @date 2019/1/7
 */
@Configuration
public class EventHandlerConfig {

	@Bean
	public EventHandler eventHandler(){
		return  new EventHandler();
	}

}
