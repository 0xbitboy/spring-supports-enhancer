package com.github.liaojiacan.spring.enhancer.i18n.sample.dto.benchmark;

import com.github.liaojiacan.spring.enhancer.i18n.annotation.Translate;

/**
 * @author liaojiacan
 * @date 2019/3/16
 */
public class TopLayerSample {

	private String id;
	@Translate(code = "benchmark.${id}.name")
	private String name;
	@Translate(code = "benchmark.${id}.description")
	private String description;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
