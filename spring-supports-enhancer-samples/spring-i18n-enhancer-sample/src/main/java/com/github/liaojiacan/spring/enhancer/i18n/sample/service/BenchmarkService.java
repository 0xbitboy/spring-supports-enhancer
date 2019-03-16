package com.github.liaojiacan.spring.enhancer.i18n.sample.service;

import com.github.liaojiacan.spring.enhancer.i18n.sample.dto.benchmark.MultiLayerSample;
import com.github.liaojiacan.spring.enhancer.i18n.sample.dto.benchmark.MultiLayerWithListSample;
import com.github.liaojiacan.spring.enhancer.i18n.sample.dto.benchmark.TopLayerSample;

import java.util.ArrayList;
import java.util.List;


/**
 * @author liaojiacan
 * @date 2019/3/16
 */
public class BenchmarkService {


	public static TopLayerSample makeTopLayerSample() {
		TopLayerSample sample = new TopLayerSample();
		sample.setDescription("description");
		sample.setName("name");
		sample.setId("id");
		return sample;
	}


	public static List<TopLayerSample> makeTopLayerSamples() {
		List<TopLayerSample> topLayerSamples = new ArrayList<>(10);
		for (int i = 0; i < 10; i++) {
			TopLayerSample sample = makeTopLayerSample();
			sample.setId(i + "");
			topLayerSamples.add(sample);
		}
		return topLayerSamples;
	}

	public static MultiLayerSample makeMultiLayerSample() {
		MultiLayerSample.SecondLayerSample secondLayerSample = new MultiLayerSample.SecondLayerSample();
		secondLayerSample.setTopLayerSample(makeTopLayerSample());
		MultiLayerSample multiLayerSample = new MultiLayerSample();
		multiLayerSample.setName("multiLayerName");
		multiLayerSample.setTopLayerSample(makeTopLayerSample());
		return multiLayerSample;
	}

	public static MultiLayerWithListSample makeMultiLayerWithListSample() {
		MultiLayerWithListSample multiLayerWithListSample = new MultiLayerWithListSample();
		multiLayerWithListSample.setName("multiLayerWithListName");
		multiLayerWithListSample.setTopLayerSamples(makeTopLayerSamples());
		return multiLayerWithListSample;
	}
}
