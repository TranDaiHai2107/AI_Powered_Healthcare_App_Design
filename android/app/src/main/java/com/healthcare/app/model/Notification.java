package com.healthcare.app.model;

import com.google.firebase.firestore.DocumentId;

public class Notification {
    @DocumentId
    private String documentId;
    private String notificationId;
    private String userId;
    private String type;
    private String title;
    private String message;
    private String time;
    private Boolean isRead;

    public Notification() {}

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
}
