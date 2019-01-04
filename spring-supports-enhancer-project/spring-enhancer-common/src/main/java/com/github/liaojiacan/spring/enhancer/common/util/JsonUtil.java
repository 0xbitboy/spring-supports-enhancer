package com.github.liaojiacan.spring.enhancer.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * json工具类
 * 
 * @author liaojiacan
 */
public class JsonUtil {

	private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

	private static ObjectMapper objectMapper = null;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.registerModule(new JavaTimeModule());
	}

	/**
	 * 将实体序列化为json字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			logger.error("Error while serializing object to json.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将实体序列化为格式化的json字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String toPrettyJson(Object obj) {
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (Exception e) {
			logger.error("Error while serializing object to pretty json.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将实体序列化为字节json
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] toBytesJson(Object obj) {
		try {
			return objectMapper.writeValueAsBytes(obj);
		} catch (Exception e) {
			logger.error("Error while serializing object to bytes json.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将实体序列化为json,并写入文件
	 * 
	 * @param file
	 * @param obj
	 */
	public static void toJsonFile(File file, Object obj) {
		try {
			objectMapper.writeValue(file, obj);
		} catch (Exception e) {
			logger.error("Error while serializing object to json file.", e);
			throw new RuntimeException(e);
		}
	}

	public static <T> T fromJson(String json, Class<T> valueClass) {
		try {
			return objectMapper.readValue(json, valueClass);
		} catch (Exception e) {
			logger.error("Error while deserializing json to object.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将json字节数组反序列化为实体 Note:此方法只支持简单实体类型的反序列化,如果需要反序列化为集合类型实体,使用
	 * {@link #fromJson(byte[], Class, Class...)} }
	 * 
	 * @param json
	 * @param valueClass 实体类型
	 * @return
	 * @see JsonUtil#fromJson(byte[], Class, Class...)
	 */
	public static <T> T fromJson(byte[] json, Class<T> valueClass) {
		try {
			return objectMapper.readValue(json, valueClass);
		} catch (Exception e) {
			logger.error("Error while deserializing json to object.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将json字符串反序列化为泛型类型实体
	 * 
	 * @param json
	 * @param parametersFor 泛型实体类类型
	 * @param parameterClasses 泛型参数类型
	 * @return
	 */
	public static <T> T fromJson(String json, Class<?> parametersFor, Class<?>... parameterClasses) {
		try {
			JavaType javaType = objectMapper.getTypeFactory().constructParametrizedType(parametersFor, parametersFor, parameterClasses);
			return objectMapper.readValue(json, javaType);
		} catch (Exception e) {
			logger.error("Error while deserializing json to collection object.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将json字符串反序列化为实体
	 * 
	 * @param json
	 * @param typeReference 实体对应的类型引用
	 * @return
	 */
	public static <T> T fromJson(String json, TypeReference<T> typeReference) {
		try {
			return objectMapper.readValue(json, typeReference);
		} catch (Exception e) {
			logger.error("Error while deserializing json to object.", e);
			throw new RuntimeException(e);
		}
	}

	public static <T> T fromJson(byte[] json, TypeReference<T> typeReference) {
		try {
			return objectMapper.readValue(json, typeReference);
		} catch (Exception e) {
			logger.error("Error while deserializing json to object.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将json字节数组反序列化为泛型类型实体
	 * 
	 * @param json
	 * @param parametersFor 泛型实体类类型
	 * @param parameterClasses 泛型参数类型
	 * @return
	 */
	public static <T> T fromJson(byte[] json, Class<?> parametersFor, Class<?>... parameterClasses) {
		try {
			JavaType javaType = objectMapper.getTypeFactory().constructParametrizedType(parametersFor, parametersFor, parameterClasses);
			return objectMapper.readValue(json, javaType);
		} catch (Exception e) {
			logger.error("Error while deserializing json to collection object.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 从指定的文件读取json信息,并反序列化为实体
	 * 
	 * @param file
	 * @param valueClass
	 * @return
	 */
	public static <T> T fromJson(File file, Class<T> valueClass) {
		try {
			return objectMapper.readValue(file, valueClass);
		} catch (Exception e) {
			logger.error("Error while deserializing json to object from file.", e);
			throw new RuntimeException(e);
		}
	}
}
