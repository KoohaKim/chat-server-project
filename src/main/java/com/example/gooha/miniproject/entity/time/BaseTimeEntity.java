package com.example.gooha.miniproject.entity.time;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@MappedSuperclass
@Setter
public abstract class BaseTimeEntity {
    @Column(name = "created_at")
    protected ZonedDateTime createdAt;

    @Column(name = "modified_at")
    protected ZonedDateTime modifiedAt;

    @Column(name = "deleted_at")
    protected ZonedDateTime deletedAt;


    @PrePersist
    protected void onCreate(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.modifiedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate(){
        this.modifiedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public void softDelete() {
        this.deletedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

}
