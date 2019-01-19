package com.github.liaojiacan.spring.enhancer.cache.interceptor;

import org.springframework.cache.interceptor.CacheOperation;

import java.util.concurrent.TimeUnit;

/**
 * @author liaojiacan
 * @date 2019/1/14
 */
public class CacheRefreshOperation extends CacheOperation {

	private long refreshAfterWrite;
	private boolean async;
	private long timeWait;
	private TimeUnit unit;
	/**
	 * @param b
	 * @since 4.3
	 */
	protected CacheRefreshOperation(Builder b) {
		super(b);
		this.refreshAfterWrite = b.refreshAfterWrite;
		this.async = b.async;
		this.timeWait = b.timeWait;
		this.unit = b.unit;
	}

	public long getRefreshAfterWrite() {
		return refreshAfterWrite;
	}

	public boolean isAsync() {
		return async;
	}

	public long getTimeWait() {
		return timeWait;
	}

	public void setTimeWait(long timeWait) {
		this.timeWait = timeWait;
	}

	public TimeUnit getUnit() {
		return unit;
	}

	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}

	public static class Builder extends CacheOperation.Builder {

		private long refreshAfterWrite;
		private boolean async;
		private long timeWait;
		private TimeUnit unit;

		public Builder setRefreshAfterWrite(long refreshAfterWrite) {
			this.refreshAfterWrite = refreshAfterWrite;
			return this;
		}

		public Builder setAsync(boolean async) {
			this.async = async;
			return this;
		}


		@Override
		protected StringBuilder getOperationDescription() {
			StringBuilder sb = super.getOperationDescription();
			sb.append(",");
			sb.append(this.refreshAfterWrite);
			sb.append(",");
			sb.append(this.async);
			sb.append(",");
			sb.append(this.timeWait);
			sb.append(",");
			sb.append(this.unit);
			return sb;
		}

		@Override
		public CacheOperation build() {
			return new CacheRefreshOperation(this);
		}

		public Builder setTimeWait(long timeWait) {
			this.timeWait = timeWait;
			return this;
		}

		public Builder setUnit(TimeUnit unit) {
			this.unit = unit;
			return this;
		}
	}


}
