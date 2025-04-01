package com.mingle.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
@Data
public class Audit {
    @CreationTimestamp
    LocalDateTime createdDttm;
    @UpdateTimestamp
    LocalDateTime updatedDttm;
    String  createdBy;
    String updatedBy;
}
