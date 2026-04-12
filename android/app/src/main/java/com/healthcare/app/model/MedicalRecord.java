package com.healthcare.app.model;

import com.google.firebase.firestore.DocumentId;

public class MedicalRecord {
    @DocumentId
    private String documentId;
    private String recordId;
    private String userId;
    private String date;
    private String type;
    private String title;
    private String doctor;
    private String hospital;
    private String details;
    private String attachments;

    public MedicalRecord() {}

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }
    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
}
