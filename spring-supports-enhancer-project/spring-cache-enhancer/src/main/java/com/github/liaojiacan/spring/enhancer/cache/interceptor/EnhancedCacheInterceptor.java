package com.github.liaojiacan.spring.enhancer.cache.interceptor;


import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.*;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author liaojiacan
 * @date 2019/1/11
 */
public class EnhancedCacheInterceptor extends CacheInterceptor {

	private Logger logger = LoggerFactory.getLogger(EnhancedCacheInterceptor.class);

	private final Map<AnnotatedElementKey, CacheRefreshOperationContext> refreshOperationContextCache = new ConcurrentHashMap<>(1024);

	private boolean initialized = false;


	/**
	 * A helper for access {@link CacheOperationExpressionEvaluator}
	 */
	static class CacheOperationExpressionEvaluatorHelper {

		/**
		 * Indicate that there is no result variable.
		 */
		public static Object NO_RESULT;

		/**
		 * Indicate that the result variable cannot be used at all.
		 */
		public static Object RESULT_UNAVAILABLE;

		/**
		 * The name of the variable holding the result object.
		 */
		public static String RESULT_VARIABLE;

		static {

			try {

				Class<?> clazz = Class.forName("org.springframework.cache.interceptor.CacheOperationExpressionEvaluator");
				Field F_NO_RESULT = clazz.getField("NO_RESULT");
				Field F_RESULT_UNAVAILABLE = clazz.getField("RESULT_UNAVAILABLE");
				Field F_RESULT_VARIABLE = clazz.getField("RESULT_VARIABLE");
				// make fields be accessible;
				F_NO_RESULT.setAccessible(true);
				F_RESULT_UNAVAILABLE.setAccessible(true);
				F_RESULT_VARIABLE.setAccessible(true);

				NO_RESULT = F_NO_RESULT.get(null);
				RESULT_UNAVAILABLE = F_RESULT_UNAVAILABLE.get(null);
				RESULT_VARIABLE = (String) F_RESULT_VARIABLE.get(null);

			} catch (Throwable e) {
				new RuntimeException(e);
			}

		}

		private CacheOperationExpressionEvaluatorHelper() {

		}
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		logger.info("Intercept success!");
		return super.invoke(invocation);
	}

	@Override
	protected Object execute(CacheOperationInvoker invoker, Object target, Method method, Object[] args) {
		//获取 CacheRefreshOperation .
		//如果存在@CacheRefresh，则判断当前是否符合refresh的条件。
		//如果符合refresh条件，尝试获取锁，如果或者成功执行以下逻辑，如果获取失败自旋timeWait时间，失败则返回缓存值
		//执行原始的invocation,获取到结果 originResult;
		//更新缓存释放锁。
		if (this.initialized) {
			Class<?> targetClass = getTargetClass(target);
			Collection<CacheOperation> operations = getCacheOperationSource().getCacheOperations(method, targetClass);
			if (!CollectionUtils.isEmpty(operations)) {
				// 此处需要做一下优化
				Optional<CacheOperation> optional = operations.stream().filter(cacheOperation -> cacheOperation instanceof CacheRefreshOperation).findFirst();
				if (optional.isPresent()) {
					CacheRefreshOperation cacheRefreshOperation = (CacheRefreshOperation) optional.get();
					CacheOperationMetadata metadata = getCacheOperationMetadata(cacheRefreshOperation, method, targetClass);
					AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(method, targetClass);
					CacheRefreshOperationContext refreshContext = this.refreshOperationContextCache.get(annotatedElementKey);
					//说明是第一个请求
					if (refreshContext == null) {
						//这里是不是得加锁?
						refreshContext = new CacheRefreshOperationContext(metadata, args, target);
						this.refreshOperationContextCache.put(annotatedElementKey,refreshContext);
					}

					CacheRefreshOperationContext opContext = new CacheRefreshOperationContext(metadata, args, target);
					Object key = opContext.generateKey(CacheOperationExpressionEvaluatorHelper.NO_RESULT);

					CacheValueRefreshMetadata refreshMetadata = refreshContext.getRefreshMetadata(key);
					// 说明是第一次进入 double check
					if (refreshMetadata == null) {
						synchronized (refreshContext) {
							if (refreshMetadata == null) {
								refreshMetadata = new CacheValueRefreshMetadata(System.currentTimeMillis(), new ReentrantReadWriteLock());
								refreshContext.addMetadata(key, refreshMetadata);
							}
						}
					}
					//判断是否已经满足刷新条件了
					if (doesNeedRefresh(cacheRefreshOperation, refreshMetadata)) {
						ReentrantReadWriteLock.WriteLock writeLock = refreshMetadata.readWriteLock.writeLock();
						ReentrantReadWriteLock.ReadLock readLock = refreshMetadata.readWriteLock.readLock();
						try {
							//先获取读锁
							if(readLock.tryLock(cacheRefreshOperation.getTimeWait(),cacheRefreshOperation.getUnit())){
								//再判断一次是否需要刷新
								//如果需要刷新则尝试获取写锁
								//在限定时间内获取到读锁
								readLock.unlock();
								if(doesNeedRefresh(cacheRefreshOperation,refreshMetadata) && writeLock.tryLock()){
									//开始执行刷新缓存逻辑
									Object value = invoker.invoke();
									//TODO 这里可能要对 condition 和 less等进行判断
									opContext.getCaches().stream().forEach(cache->cache.put(key,value));
									refreshMetadata.lastWriteTime = System.currentTimeMillis();
									return value;
								}else {
									//获取不到锁，锁已经被其他线程获取，忽略更新逻辑，直接跳到下一层逻辑，返回缓存值
									return super.execute(invoker,target,method,args);
								}
							}else {
								//获取锁超时了，直接返回缓存值
								return super.execute(invoker,target,method,args);
							}

						} catch (InterruptedException e) {
							e.printStackTrace();
						}finally {
							if(writeLock.isHeldByCurrentThread()){
								writeLock.unlock();
							}
						}
					}
				}

			} else {
				return invoker.invoke();
			}
		}

		return super.execute(invoker, target, method, args);
	}

	private boolean doesNeedRefresh(CacheRefreshOperation cacheRefreshOperation, CacheValueRefreshMetadata refreshMetadata) {
		return System.currentTimeMillis() - refreshMetadata.lastWriteTime >= TimeUnit.MILLISECONDS.convert(cacheRefreshOperation.getRefreshAfterWrite(), cacheRefreshOperation.getUnit());
	}

	private Class<?> getTargetClass(Object target) {
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
		if (targetClass == null && target != null) {
			targetClass = target.getClass();
		}
		return targetClass;
	}

	private class CacheValueRefreshMetadata {
		protected volatile long lastWriteTime;
		protected ReentrantReadWriteLock readWriteLock;

		public CacheValueRefreshMetadata(long lastWriteTime, ReentrantReadWriteLock readWriteLock) {
			this.lastWriteTime = lastWriteTime;
			this.readWriteLock = readWriteLock;
		}
	}

	protected class CacheRefreshOperationContext extends CacheOperationContext {

		protected Map<Object, CacheValueRefreshMetadata> refreshMetadataCache = new ConcurrentHashMap<>(1024);

		public CacheRefreshOperationContext(CacheOperationMetadata metadata, Object[] args, Object target) {
			super(metadata, args, target);
		}

		@Override
		public boolean isConditionPassing(Object result) {
			return super.isConditionPassing(result);
		}

		@Override
		public boolean canPutToCache(Object value) {
			return super.canPutToCache(value);
		}

		@Override
		public Object generateKey(Object result) {
			return super.generateKey(result);
		}

		@Override
		public Collection<? extends Cache> getCaches() {
			return super.getCaches();
		}

		@Override
		public Collection<String> getCacheNames() {
			return super.getCacheNames();
		}

		protected void clearCache() {
			refreshMetadataCache.clear();
		}

		protected CacheValueRefreshMetadata getRefreshMetadata(Object key) {
			return refreshMetadataCache.get(key);
		}

		protected void addMetadata(Object key, CacheValueRefreshMetadata metadata) {
			this.refreshMetadataCache.put(key, metadata);
		}

	}

	@Override
	public void afterSingletonsInstantiated() {
		super.afterSingletonsInstantiated();
		this.initialized = true;
	}
}
