/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author alfia
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "TRAINING_PARTICIPANTS",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"TRAINING_ID", "EMAIL"})
        }
)
public class TrainingParticipants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "PARTICIPANT_TYPE")
    private String participantType;

    @Column(nullable = false, name = "IDENTITY_NUMBER")
    private String identityNumber;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false, name = "UNIVERSITY_NAME")
    private String universityName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRAINING_ID", nullable = false)
    private Training training;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
