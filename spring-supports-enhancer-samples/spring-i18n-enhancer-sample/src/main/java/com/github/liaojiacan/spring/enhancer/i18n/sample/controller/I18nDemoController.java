package com.github.liaojiacan.spring.enhancer.i18n.sample.controller;

import com.github.liaojiacan.spring.enhancer.i18n.annotation.I18n;
import com.github.liaojiacan.spring.enhancer.i18n.sample.dto.ArticleDTO;
import com.github.liaojiacan.spring.enhancer.i18n.sample.response.ArticleResponse;
import com.github.liaojiacan.spring.enhancer.i18n.sample.response.I18nSPELResponse;
import com.github.liaojiacan.spring.enhancer.i18n.sample.response.I18nSimpleResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class I18nDemoController {

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello World!";
	}

	@RequestMapping("/i18n/simple")
	@ResponseBody
	@I18n
	public I18nSimpleResponse i18nSimpleResponse() {
		I18nSimpleResponse response = new I18nSimpleResponse();
		response.setId(1);
		response.setMessage("default");
		return response;
	}

	/**
	 * SPEL的用法示例 key 是根据请求参数动态构造的
	 * @param id
	 * @return
	 */
	@RequestMapping("/i18n/spel/{id}")
	@ResponseBody
	@I18n
	public I18nSPELResponse i18nSpelResponse(@PathVariable("id") Integer id) {
		I18nSPELResponse response = new I18nSPELResponse();
		response.setId(id);
		response.setMessage("default");
		return response;
	}


	/**
	 * 返回的内容是一个列表的处理
	 * 注意 如果I18nSPELResponse 中有更深层次的结构需要翻译 需要用@I18n来标记
	 * @param id
	 * @return
	 */
	@RequestMapping("/i18n/spel/{id}/list")
	@ResponseBody
	@I18n
	public List<I18nSPELResponse> i18nSpelResponses(@PathVariable("id") Integer id) {
		List<I18nSPELResponse> list = new ArrayList<>();
		I18nSPELResponse response = new I18nSPELResponse();
		response.setId(id);
		response.setMessage("default");
		list.add(response);
		return list;
	}

	/**
	 * 自定义数据源的测试
	 * @see com.github.liaojiacan.spring.enhancer.i18n.provider.CustomMessageSourceProvider
	 * @return
	 */
	@RequestMapping("/i18n/article/list")
	@ResponseBody
	@I18n
	public ArticleResponse listArticles(){
		ArticleResponse articleResponse = new ArticleResponse();
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setId(1);
		articleResponse.setArticles(Arrays.asList(articleDTO));
		return articleResponse;
	}

}
