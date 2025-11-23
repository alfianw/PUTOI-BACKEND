/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.dto.TrainingDto;

import java.time.LocalDateTime;
import java.util.List;
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
public class TrainingResponse {

    private String trainingTitle;

    private String description;

    private List<String> trainingMaterials;

    private String institutionName;

    private String duration;

    private String minimumParticipants;

    private List<String> facilities;

    private String implementationSchedule;

    private String competencyTestPlace;

    private List<String> certificate;

    private String trainingFee;

    private String author;

    private LocalDateTime createdAt;

    private LocalDateTime updateAt;

}
