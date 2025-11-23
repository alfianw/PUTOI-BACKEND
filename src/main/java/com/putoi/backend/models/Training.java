/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.models;

import com.putoi.backend.config.ListToJsonConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author alfia
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "TRAINING")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_title", nullable = false)
    private String trainingTitle;

    @Column(columnDefinition = "text")
    private String description;

    @Convert(converter = ListToJsonConverter.class)
    @Column(name = "training_materials", columnDefinition = "text", nullable = false)
    private List<String> trainingMaterials;

    @Column(name = "institution_name")
    private String institutionName;

    @Column(nullable = false)
    private String duration;

    @Column(name = "minimum_participants", nullable = false)
    private String minimumParticipants;

    @Convert(converter = ListToJsonConverter.class)
    @Column(name = "facilities", columnDefinition = "text", nullable = false)
    private List<String> facilities;

    @Column(name = "implementation_schedule", nullable = false)
    private String implementationSchedule;

    @Column(name = "competency_test_place", nullable = false)
    private String competencyTestPlace;

    @Convert(converter = ListToJsonConverter.class)
    @Column(name = "certificate", columnDefinition = "text", nullable = false)
    private List<String> certificate;

    @Column(name = "training_fee")
    private String trainingFee;
    
    @Column(name = "AUTHOR", nullable = false)
    private String author;

    @Column(nullable = false, name = "CREATE_AT")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "UPDATE_AT")
    private LocalDateTime updateAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updateAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "training", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TrainingParticipants> trainingParticipantses;
}
