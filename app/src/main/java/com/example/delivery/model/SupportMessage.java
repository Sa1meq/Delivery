package com.example.delivery.model;

import com.google.firebase.Timestamp;

import java.util.Date;

public class SupportMessage {
    private String text;
    private String userId;
    private String chatId;
    private Date timestamp;
    private boolean isSupport;


    public SupportMessage() {
    }

    public SupportMessage(String text, String userId, String chatId, Date timestamp, boolean isSupport) {
        this.text = text;
        this.userId = userId;
        this.chatId = chatId;
        this.timestamp = timestamp;
        this.isSupport = isSupport;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean isSupport() {
        return isSupport;
    }

    public void setSupport(boolean support) {
        isSupport = support;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
