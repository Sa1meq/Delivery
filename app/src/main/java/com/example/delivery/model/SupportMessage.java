package com.example.delivery.model;

import com.google.firebase.Timestamp;

public class SupportMessage {
    private String id; // Уникальный идентификатор сообщения
    private String content; // Текст сообщения
    private String senderId; // ID отправителя
    private String chatId; // ID чата, к которому принадлежит сообщение
    private Timestamp createTime; // Время создания сообщения
    private boolean isAdmin; // True, если сообщение от администратора

    public SupportMessage(String id, String content, String senderId, String chatId, Timestamp createTime, boolean isAdmin) {
        this.id = id;
        this.content = content;
        this.senderId = senderId;
        this.chatId = chatId;
        this.createTime = createTime;
        this.isAdmin = isAdmin;
    }

    public SupportMessage() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
