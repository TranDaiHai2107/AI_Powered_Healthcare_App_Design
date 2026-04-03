package com.healthcare.app.model;

import com.google.firebase.firestore.DocumentId;

public class Doctor {
    @DocumentId
    private String documentId;
    private String name;
    private String specialization;
    private String hospitalId;
    private String hospitalName;
    private Double rating;
    private Integer reviewCount;
    private Integer experience;
    private Double consultationFee;
    private String image;
    private String nextAvailable;
    private String bio;
    private String availableSlots;

    public Doctor() {}

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }
    public Double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getNextAvailable() { return nextAvailable; }
    public void setNextAvailable(String nextAvailable) { this.nextAvailable = nextAvailable; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(String availableSlots) { this.availableSlots = availableSlots; }

    public String[] getAvailableSlotsArray() {
        return availableSlots != null ? availableSlots.split(",") : new String[0];
    }
}
