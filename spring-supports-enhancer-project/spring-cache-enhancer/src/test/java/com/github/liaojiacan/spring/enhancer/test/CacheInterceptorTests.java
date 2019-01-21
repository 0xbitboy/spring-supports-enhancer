package com.github.liaojiacan.spring.enhancer.test;

import com.github.liaojiacan.spring.enhancer.cache.annotation.CacheRefresh;
import com.github.liaojiacan.spring.enhancer.cache.annotation.EnableEnhancedCaching;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author liaojiacan
 * @date 2019/1/11
 */
public class CacheInterceptorTests {


	private ConfigurableApplicationContext context;

	private Cache cache;

	private SimpleService service;

	@Before
	public void setup() {
		this.context = new AnnotationConfigApplicationContext(Config.class);
		this.cache = this.context.getBean(CacheManager.class).getCache("test");
		this.service = this.context.getBean(SimpleService.class);
	}

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}


	@Configuration
	@EnableEnhancedCaching
	static class Config extends CachingConfigurerSupport {

		@Bean
		@Override
		public CacheManager cacheManager() {
			return new ConcurrentMapCacheManager();
		}

		@Bean
		public SimpleService simpleService() {
			return new SimpleService();
		}

	}

	@Test
	public void getAndPut() {
		this.cache.clear();

		long key = 1;
		Long value = this.service.getAndPut(key);

		assertEquals("Wrong value for @Cacheable key", value, this.cache.get(key).get());
		assertEquals("Wrong value for @CachePut key", value, this.cache.get(value + 100).get()); // See @CachePut

		// CachePut forced a method call
		Long anotherValue = this.service.getAndPut(key);
		assertNotSame(value, anotherValue);
		// NOTE: while you might expect the main key to have been updated, it hasn't. @Cacheable operations
		// are only processed in case of a cache miss. This is why combining @Cacheable with @CachePut
		// is a very bad idea. We could refine the condition now that we can figure out if we are going
		// to invoke the method anyway but that brings a whole new set of potential regressions.
		//assertEquals("Wrong value for @Cacheable key", anotherValue, cache.get(key).get());
		assertEquals("Wrong value for @CachePut key", anotherValue, this.cache.get(anotherValue + 100).get());
	}


	@Test
	public void testRefresh() throws InterruptedException {
		this.cache.clear();
		String initialValue = service.getTime("main");
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					System.out.println(Thread.currentThread().getName()+":"+service.getTime("thread"));
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					System.out.println(Thread.currentThread().getName()+":"+service.getTime("thread"));
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		Thread.sleep(100000);

		String refreshValue = service.getTime(Thread.currentThread().getName());
		assertNotSame(initialValue,refreshValue);
	}

	@CacheConfig(cacheNames = "test")
	public static class SimpleService {
		private AtomicLong counter = new AtomicLong();

		@Cacheable(key = "#p0")
		@CacheRefresh(refreshAfterWrite = 5,timeWait = 1,unit = TimeUnit.SECONDS,async = false)
		public String getTime(String id){
			return id+":"+System.currentTimeMillis();
		}

		/**
		 * Represent a mutual exclusion use case. The boolean flag exclude one of the two operation.
		 */
		@Cacheable(condition = "#p1", key = "#p0")
		@CachePut(condition = "!#p1", key = "#p0")
		public Long getOrPut(Object id, boolean flag) {
			return this.counter.getAndIncrement();
		}

		/**
		 * Represent an invalid use case. If the result of the operation is non null, then we put
		 * the value with a different key. This forces the method to be executed every time.
		 */
		@Cacheable
		@CachePut(key = "#result + 100", condition = "#result != null")
		public Long getAndPut(long id) {
			return this.counter.getAndIncrement();
		}
	}


}
