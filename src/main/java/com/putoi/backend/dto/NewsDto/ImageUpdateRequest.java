/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.dto.NewsDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author alfia
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ImageUpdateRequest {

    private Long imageId;
    private MultipartFile file;
    private Boolean hasFile;
}
