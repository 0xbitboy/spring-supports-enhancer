package com.github.liaojiacan.spring.enhancer.cache.config;

import com.github.liaojiacan.spring.enhancer.cache.interceptor.EnhancedCacheInterceptor;
import com.github.liaojiacan.spring.enhancer.cache.interceptor.EnhancedSpringCacheAnnotationParser;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.SpringCacheAnnotationParser;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.annotation.Bean;

/**
 * @author liaojiacan
 * @date 2019/1/11
 */
@EnableCaching
public class SpringCacheEnhancerConfiguration {


	@Bean
	public CacheOperationSource cacheOperationSource() {
		return new AnnotationCacheOperationSource(new SpringCacheAnnotationParser(),new EnhancedSpringCacheAnnotationParser());
	}

	@Bean
	public CacheInterceptor cacheInterceptor() {
		CacheInterceptor interceptor = new EnhancedCacheInterceptor();
		interceptor.setCacheOperationSources(cacheOperationSource());
		return interceptor;
	}


}
