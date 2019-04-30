package com.github.liaojiacan.spring.enhancer.event;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 本地内存的实现
 * @author liaojiacan
 * @date 2019/4/30
 */
public class MemoryTransactionResolver implements TransactionResolver {


    private Map<String, AsyncEventWrapperMessage<? extends AmqpApplicationEvent>> localStorage = new ConcurrentHashMap<>(1024);


    @Override
    public void storeMessage(AsyncEventWrapperMessage<? extends AmqpApplicationEvent> message) {
        localStorage.put(message.getMessageId(), message);
    }

    @Override
    public void confirmMessage(String messageId) {
        localStorage.remove(messageId);
    }

    @Override
    public List<AsyncEventWrapperMessage<? extends AmqpApplicationEvent>> findUnConfirmMessages() {
        return localStorage.values().stream().collect(Collectors.toList());
    }
}
