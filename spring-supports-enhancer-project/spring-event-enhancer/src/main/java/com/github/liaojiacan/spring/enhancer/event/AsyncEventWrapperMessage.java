package com.github.liaojiacan.spring.enhancer.event;

import java.util.Date;

/**
 * @author liaojiacan
 * @date 2019/4/30
 */
public class AsyncEventWrapperMessage<T extends AmqpApplicationEvent> {

    private String messageId;
    private String eventClass;
    private Long delay;
    private boolean relayed = false;
    private Date sendTime;
    private Date receiveTime;
    private T event;

    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }

    public boolean isRelayed() {
        return relayed;
    }

    public void setRelayed(boolean relayed) {
        this.relayed = relayed;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public T getEvent() {
        return event;
    }

    public void setEvent(T event) {
        this.event = event;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
