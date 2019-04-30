package com.github.liaojiacan.spring.enhancer.event;


import java.util.List;

/**
 * @author liaojiacan
 * @date 2019/4/30
 */
public interface TransactionResolver {

    /**
     * 存储消息
     *
     * @param message
     */
    void storeMessage(AsyncEventWrapperMessage<? extends AmqpApplicationEvent> message);

    /**
     * 确认ID
     *
     * @param messageId
     */
    void confirmMessage(String messageId);

    /**
     * 获取未确认的消息列表
     *
     * @return
     */
    List<AsyncEventWrapperMessage<? extends AmqpApplicationEvent>> findUnConfirmMessages();
}
