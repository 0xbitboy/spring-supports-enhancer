package com.github.liaojiacan.spring.enhancer.cache.annotation;

import com.github.liaojiacan.spring.enhancer.cache.config.SpringCacheEnhancerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liaojiacan
 * @date 2019/1/11
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SpringCacheEnhancerConfiguration.class)
public @interface EnableEnhancedCaching {

}
