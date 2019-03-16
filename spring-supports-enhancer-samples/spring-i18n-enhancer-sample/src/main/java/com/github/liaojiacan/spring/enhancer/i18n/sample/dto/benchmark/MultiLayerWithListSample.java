package com.github.liaojiacan.spring.enhancer.i18n.sample.dto.benchmark;

import com.github.liaojiacan.spring.enhancer.i18n.annotation.I18n;
import com.github.liaojiacan.spring.enhancer.i18n.annotation.Translate;

import java.util.List;

/**
 * @author liaojiacan
 * @date 2019/3/16
 */
public class MultiLayerWithListSample {


	@Translate(code = "benchmark.multi_layer_list.name")
	private String name;

	@I18n
	private List<TopLayerSample> topLayerSamples;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TopLayerSample> getTopLayerSamples() {
		return topLayerSamples;
	}

	public void setTopLayerSamples(List<TopLayerSample> topLayerSamples) {
		this.topLayerSamples = topLayerSamples;
	}
}
