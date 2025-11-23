/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.controller;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.dto.TrainingDto.TrainingDeleteRequest;
import com.putoi.backend.dto.TrainingDto.TrainingDetailRequest;
import com.putoi.backend.dto.TrainingDto.TrainingDetailResponse;
import com.putoi.backend.dto.TrainingDto.TrainingPaginationRequest;
import com.putoi.backend.dto.TrainingDto.TrainingPaginationResponse;
import com.putoi.backend.dto.TrainingDto.TrainingRequest;
import com.putoi.backend.dto.TrainingDto.TrainingResponse;
import com.putoi.backend.dto.TrainingDto.TrainingUpdateRequest;
import com.putoi.backend.dto.TrainingDto.TrainingUpdateResponse;
import com.putoi.backend.service.TrainingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author alfia
 */
@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @PreAuthorize("hasAuthority('superadmin')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TrainingResponse>> createTraining(@RequestBody TrainingRequest request, Authentication authentication) {
        return ResponseEntity.ok(trainingService.createTraining(request, authentication));
    }

    @PostMapping("/pagination")
    public ResponseEntity<ApiResponse<List<TrainingPaginationResponse>>> getTrainingPagination(@RequestBody TrainingPaginationRequest request) {
        return ResponseEntity.ok(trainingService.getTrainingPagination(request));
    }

    @PreAuthorize("hasAuthority('superadmin')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<TrainingUpdateResponse>> updateTraining(@RequestBody TrainingUpdateRequest request, Authentication authentication) {
        return ResponseEntity.ok(trainingService.updateTraining(request, authentication));
    }

    @PostMapping("/detail")
    public ResponseEntity<ApiResponse<TrainingDetailResponse>> detailTraining(@RequestBody TrainingDetailRequest request) {
        return ResponseEntity.ok(trainingService.detailTraining(request));
    }

    @PreAuthorize("hasAuthority('superadmin')")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteTraining(@RequestBody TrainingDeleteRequest request) {
        return ResponseEntity.ok(trainingService.deleteTraining(request));
    }
}
