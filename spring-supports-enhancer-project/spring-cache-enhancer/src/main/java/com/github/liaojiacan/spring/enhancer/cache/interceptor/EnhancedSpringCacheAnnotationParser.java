package com.github.liaojiacan.spring.enhancer.cache.interceptor;

import com.github.liaojiacan.spring.enhancer.cache.annotation.CacheRefresh;
import org.springframework.cache.annotation.*;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Strategy implementation for parsing an extend annotation {@link CacheRefresh}
 *
 * @author liaojiacan
 * @date 2019/1/14
 */
public class EnhancedSpringCacheAnnotationParser implements CacheAnnotationParser {


	@Override
	public Collection<CacheOperation> parseCacheAnnotations(Class<?> type) {
		DefaultCacheConfig defaultConfig = getDefaultCacheConfig(type);
		return parseCacheAnnotations(defaultConfig, type);
	}

	@Override
	public Collection<CacheOperation> parseCacheAnnotations(Method method) {
		DefaultCacheConfig defaultConfig = getDefaultCacheConfig(method.getDeclaringClass());
		return parseCacheAnnotations(defaultConfig, method);
	}

	protected Collection<CacheOperation> parseCacheAnnotations(DefaultCacheConfig cachingConfig, AnnotatedElement ae) {
		Collection<CacheOperation> ops = null;

		Collection<CacheRefresh> refreshes = AnnotatedElementUtils.getAllMergedAnnotations(ae, CacheRefresh.class);
		if (!refreshes.isEmpty()) {
			ops = lazyInit(ops);
			for (CacheRefresh refresh : refreshes) {
				ops.add(parseCacheRefreshAnnotation(ae, cachingConfig, refresh));
			}
		}

		return ops;
	}

	private CacheOperation parseCacheRefreshAnnotation(AnnotatedElement ae, DefaultCacheConfig defaultConfig, CacheRefresh refresh) {
		CacheRefreshOperation.Builder builder = new CacheRefreshOperation.Builder();
		builder.setName(ae.toString());
		builder.setRefreshAfterWrite(refresh.refreshAfterWrite());
		builder.setAsync(refresh.async());
		builder.setTimeWait(refresh.timeWait());
		builder.setUnit(refresh.unit());
		builder.setCacheNames(refresh.cacheNames());
		builder.setKey(refresh.key());
		builder.setKeyGenerator(refresh.keyGenerator());
		builder.setCacheManager(refresh.cacheManager());
		builder.setCacheResolver(refresh.cacheResolver());
		defaultConfig.applyDefault(builder);
		CacheOperation op = builder.build();
		validateCacheOperation(ae, op);
		return op;
	}


	/**
	 * @param ae        the annotated element of the cache operation
	 * @param operation the {@link CacheOperation} to validate
	 * @see org.springframework.cache.annotation.SpringCacheAnnotationParser#validateCacheOperation(AnnotatedElement, CacheOperation)
	 * <p>
	 * Validates the specified {@link CacheOperation}.
	 * <p>Throws an {@link IllegalStateException} if the state of the operation is
	 * invalid. As there might be multiple sources for default values, this ensure
	 * that the operation is in a proper state before being returned.
	 */
	private void validateCacheOperation(AnnotatedElement ae, CacheOperation operation) {
		if (StringUtils.hasText(operation.getKey()) && StringUtils.hasText(operation.getKeyGenerator())) {
			throw new IllegalStateException("Invalid cache annotation configuration on '" +
					ae.toString() + "'. Both 'key' and 'keyGenerator' attributes have been set. " +
					"These attributes are mutually exclusive: either set the SpEL expression used to" +
					"compute the key at runtime or set the name of the KeyGenerator bean to use.");
		}
		if (StringUtils.hasText(operation.getCacheManager()) && StringUtils.hasText(operation.getCacheResolver())) {
			throw new IllegalStateException("Invalid cache annotation configuration on '" +
					ae.toString() + "'. Both 'cacheManager' and 'cacheResolver' attributes have been set. " +
					"These attributes are mutually exclusive: the cache manager is used to configure a" +
					"default cache resolver if none is set. If a cache resolver is set, the cache manager" +
					"won't be used.");
		}
	}

	private <T extends Annotation> Collection<CacheOperation> lazyInit(Collection<CacheOperation> ops) {
		return (ops != null ? ops : new ArrayList<CacheOperation>(1));
	}

	/**
	 * Provides the {@link SpringCacheAnnotationParser.DefaultCacheConfig} instance for the specified {@link Class}.
	 *
	 * @param target the class-level to handle
	 * @return the default config (never {@code null})
	 */
	DefaultCacheConfig getDefaultCacheConfig(Class<?> target) {
		CacheConfig annotation = AnnotatedElementUtils.getMergedAnnotation(target, CacheConfig.class);
		if (annotation != null) {
			return new DefaultCacheConfig(annotation.cacheNames(), annotation.keyGenerator(),
					annotation.cacheManager(), annotation.cacheResolver());
		}
		return new DefaultCacheConfig();
	}

	/**
	 * Provides default settings for a given set of cache operations.
	 *
	 * @see org.springframework.cache.annotation.SpringCacheAnnotationParser.DefaultCacheConfig;
	 */
	static class DefaultCacheConfig {

		private final String[] cacheNames;

		private final String keyGenerator;

		private final String cacheManager;

		private final String cacheResolver;

		public DefaultCacheConfig() {
			this(null, null, null, null);
		}

		private DefaultCacheConfig(String[] cacheNames, String keyGenerator, String cacheManager, String cacheResolver) {
			this.cacheNames = cacheNames;
			this.keyGenerator = keyGenerator;
			this.cacheManager = cacheManager;
			this.cacheResolver = cacheResolver;
		}

		/**
		 * Apply the defaults to the specified {@link CacheOperation.Builder}.
		 *
		 * @param builder the operation builder to update
		 */
		public void applyDefault(CacheOperation.Builder builder) {
			if (builder.getCacheNames().isEmpty() && this.cacheNames != null) {
				builder.setCacheNames(this.cacheNames);
			}
			if (!StringUtils.hasText(builder.getKey()) && !StringUtils.hasText(builder.getKeyGenerator()) &&
					StringUtils.hasText(this.keyGenerator)) {
				builder.setKeyGenerator(this.keyGenerator);
			}

			if (StringUtils.hasText(builder.getCacheManager()) || StringUtils.hasText(builder.getCacheResolver())) {
				// One of these is set so we should not inherit anything
			} else if (StringUtils.hasText(this.cacheResolver)) {
				builder.setCacheResolver(this.cacheResolver);
			} else if (StringUtils.hasText(this.cacheManager)) {
				builder.setCacheManager(this.cacheManager);
			}
		}

	}
}
