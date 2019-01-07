package com.github.liaojiacan.spring.enhancer.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author liaojiacan
 * @date 2019/1/4
 */
public class HostNameUtil {

	private final static Logger logger = LoggerFactory.getLogger(HostNameUtil.class);

	public static String localHostName ;

	static {
		try {
			localHostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("Error while parsing localHostName.", e);
			throw  new RuntimeException(e);
		}
	}
}
