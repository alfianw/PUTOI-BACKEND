/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.dto.TraningParticipanDto;

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
public class TrainingParticipanResponse {

    private Long id;

    private String name;

    private String participantType;

    private String identityNumber;

    private String gender;

    private String universityName;

    private String email;

    private String phoneNumber;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String training;

}
