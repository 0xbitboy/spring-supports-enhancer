package com.github.liaojiacan.spring.enhancer.i18n;


import java.util.List;
import java.util.Locale;

public interface MessageSourceProvider {

	String getName();

	List<MessageEntry> load();

	int addMessage(Locale locale, String code, String type, String message);

	int updateMessage(Locale locale, String code, String type, String message);

	int deleteMessage(Locale locale, String code);

}
