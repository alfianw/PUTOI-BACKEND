/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.dto.UserDto;

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
public class UserUpdateRequest {

    private String email;
   
    private String name;
    
    private String phoneNumber;
    
    private String role;
    
    private String participantType;

    private String identityNumber;

    private String gender;

    private String universityName;

    private String lastEducationField;

    private String majorStudyProgram;

    private String cityOfResidence;
}
