package com.example.delivery.model;

public class SupportMessage {
    private String messageId;
    private String senderId; // user or admin
    private String messageContent;
    private long timestamp;

    public SupportMessage(String senderId, String messageContent, long timestamp) {
        this.senderId = senderId;
        this.messageContent = messageContent;
        this.timestamp = timestamp;
    }

    public SupportMessage() {
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
