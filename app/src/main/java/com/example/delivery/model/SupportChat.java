package com.example.delivery.model;

import java.util.List;

public class SupportChat {
    private String chatId;
    private String userId;
    private String adminId;
    private List<SupportMessage> messages;
    private String status; // open/closed
    private long createdAt;

    public SupportChat() {
    }

    public SupportChat(String chatId, String userId, String adminId, List<SupportMessage> messages, long createdAt, String status) {
        this.chatId = chatId;
        this.userId = userId;
        this.adminId = adminId;
        this.messages = messages;
        this.createdAt = createdAt;
        this.status = status;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public List<SupportMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<SupportMessage> messages) {
        this.messages = messages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
