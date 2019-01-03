package com.github.liaojiacan.spring.enhancer.i18n.sample.response;


import com.github.liaojiacan.spring.enhancer.i18n.annotation.I18n;
import com.github.liaojiacan.spring.enhancer.i18n.sample.dto.ArticleDTO;

import java.util.List;

/**
 * @author liaojiacan
 * @date 2018/12/24
 */
public class ArticleResponse {

	@I18n
	private List<ArticleDTO> articles;

	public List<ArticleDTO> getArticles() {
		return articles;
	}

	public void setArticles(List<ArticleDTO> articles) {
		this.articles = articles;
	}
}
