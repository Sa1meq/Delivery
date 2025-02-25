package com.example.delivery.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Date;

public class SupportChat implements Serializable {
    private String id;
    private String userId;
    private String topic;
    private String requestType;
    private String status;
    private Date createdAt;

    public SupportChat() {
    }

    public SupportChat(String id, String userId, String topic, String requestType, String status, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.topic = topic;
        this.requestType = requestType;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}