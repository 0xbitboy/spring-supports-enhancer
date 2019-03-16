package com.github.liaojiacan.spring.enhancer.i18n.sample.dto.benchmark;

import com.github.liaojiacan.spring.enhancer.i18n.annotation.I18n;
import com.github.liaojiacan.spring.enhancer.i18n.annotation.Translate;

/**
 * @author liaojiacan
 * @date 2019/3/16
 */
public class MultiLayerSample {

	public static class SecondLayerSample {

		@I18n
		private TopLayerSample topLayerSample;

		public TopLayerSample getTopLayerSample() {
			return topLayerSample;
		}

		public void setTopLayerSample(TopLayerSample topLayerSample) {
			this.topLayerSample = topLayerSample;
		}
	}

	@Translate(code = "benchmark.multi_layer.name")
	private String name;

	@I18n
	private TopLayerSample topLayerSample;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TopLayerSample getTopLayerSample() {
		return topLayerSample;
	}

	public void setTopLayerSample(TopLayerSample topLayerSample) {
		this.topLayerSample = topLayerSample;
	}
}
