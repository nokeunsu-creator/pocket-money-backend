package com.pocketmoney.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_photos")
public class ProfilePhoto {

    @Id
    @Column(name = "user_name", length = 20)
    private String userName;

    @Column(name = "photo_data", columnDefinition = "TEXT")
    private String photoData;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    protected void onSave() { updatedAt = LocalDateTime.now(); }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPhotoData() { return photoData; }
    public void setPhotoData(String photoData) { this.photoData = photoData; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
