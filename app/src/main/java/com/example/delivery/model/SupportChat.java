package com.example.delivery.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class SupportChat implements Serializable {
    private String id; // Уникальный идентификатор чата
    private String topic; // Тема обращения
    private String requestType; // Тип обращения (например: технический вопрос, жалоба, запрос на информацию)
    private String userId; // ID пользователя, создавшего обращение
    private Timestamp startTime; // Время создания чата
    private int messageCount; // Количество сообщений в чате
    private String status; // Статус чата (например: открытый, в процессе, завершён)

    public SupportChat(String id, String topic, String requestType, String userId, Timestamp startTime, int messageCount, String status) {
        this.id = id;
        this.topic = topic;
        this.requestType = requestType;
        this.userId = userId;
        this.startTime = startTime;
        this.messageCount = messageCount;
        this.status = status;
    }

    public SupportChat() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
