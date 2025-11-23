/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.dto.UserDto;

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
public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private String phoneNumber;

    private String role;

    private String participantType;

    private String identityNumber;

    private String gender;

    private String universityName;

    private String lastEducationField;

    private String majorStudyProgram;

    private String cityOfResidence;

    private LocalDateTime createdAt;

    private LocalDateTime updateAt;

    private String token;
    
}
