package com.example.delivery.model;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String messageId;
    private String orderId;
    private String senderId;
    private String text;
    private Timestamp timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String orderId, String senderId, String text, Timestamp timestamp) {
        this.orderId = orderId;
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}