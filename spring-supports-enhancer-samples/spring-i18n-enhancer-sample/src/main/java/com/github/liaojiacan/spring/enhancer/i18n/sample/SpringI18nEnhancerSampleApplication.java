package com.github.liaojiacan.spring.enhancer.i18n.sample;

import com.github.liaojiacan.spring.enhancer.i18n.sample.config.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import(AppConfig.class)
@SpringBootApplication
public class SpringI18nEnhancerSampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringI18nEnhancerSampleApplication.class, args);
	}

}

