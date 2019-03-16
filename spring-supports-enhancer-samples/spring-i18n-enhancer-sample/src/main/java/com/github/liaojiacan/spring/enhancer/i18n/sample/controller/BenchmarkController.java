package com.github.liaojiacan.spring.enhancer.i18n.sample.controller;

import com.github.liaojiacan.spring.enhancer.i18n.annotation.I18n;
import com.github.liaojiacan.spring.enhancer.i18n.sample.dto.benchmark.MultiLayerSample;
import com.github.liaojiacan.spring.enhancer.i18n.sample.dto.benchmark.MultiLayerWithListSample;
import com.github.liaojiacan.spring.enhancer.i18n.sample.dto.benchmark.TopLayerSample;
import com.github.liaojiacan.spring.enhancer.i18n.sample.service.BenchmarkService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liaojiacan
 * @date 2019/3/16
 */
@RestController
@RequestMapping("/benchmark")
public class BenchmarkController {

	/**
	 * 没有使用多语言的单层结构
	 *
	 * @return
	 */
	@GetMapping("/just_one_layer")
	@ResponseBody
	public TopLayerSample testWithoutI18n() {
		return BenchmarkService.makeTopLayerSample();
	}

	/**
	 * 使用多语言的单层结构
	 *
	 * @return
	 */
	@GetMapping("/just_one_layer/i18n")
	@I18n
	public TopLayerSample testWithI18n() {
		return BenchmarkService.makeTopLayerSample();
	}

	/**
	 * 没有使用多语言的多层结构
	 *
	 * @return
	 */
	@GetMapping("/multi_layer")
	public MultiLayerSample testMultiSampleWithoutI18n() {
		return BenchmarkService.makeMultiLayerSample();
	}

	/**
	 * 使用多语言的多层结构
	 *
	 * @return
	 */
	@GetMapping("/multi_layer/i18n")
	@I18n
	public MultiLayerSample testMultiSampleWithI18n() {
		return BenchmarkService.makeMultiLayerSample();
	}

	/**
	 * 没有使用多语言的带列表多层结构
	 *
	 * @return
	 */
	@GetMapping("/multi_layer_list")
	public MultiLayerWithListSample testMultiLayerWithListSampleWithoutI18n() {
		return BenchmarkService.makeMultiLayerWithListSample();
	}

	/**
	 * 使用多语言的带列表多层结构
	 *
	 * @return
	 */
	@GetMapping("/multi_layer_list/i18n")
	@I18n
	public MultiLayerWithListSample testMultiLayerWithListSampleWithI18n() {
		return BenchmarkService.makeMultiLayerWithListSample();
	}


}
