/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.controller;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.dto.TraningParticipanDto.TrainingParticipanRequest;
import com.putoi.backend.dto.TraningParticipanDto.TrainingParticipanResponse;
import com.putoi.backend.dto.TraningParticipanDto.TrainingParticipantsCheckRequest;
import com.putoi.backend.dto.TraningParticipanDto.TrainingParticipantsCheckResponse;
import com.putoi.backend.dto.TraningParticipanDto.TrainingParticipantsDeleteRequest;
import com.putoi.backend.dto.TraningParticipanDto.TrainingParticipantsDetailRequest;
import com.putoi.backend.dto.TraningParticipanDto.TrainingParticipantsDetailResponse;
import com.putoi.backend.dto.TraningParticipanDto.TrainingParticipantsEditRequest;
import com.putoi.backend.dto.TraningParticipanDto.TrainingParticipantsEditResponse;
import com.putoi.backend.dto.TraningParticipanDto.TrainingParticipantsPaginationRequest;
import com.putoi.backend.dto.TraningParticipanDto.TrainingParticipantsPaginationResponse;
import com.putoi.backend.service.TrainingParticipantsService;
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
@RequestMapping("/api/training-participants")
@RequiredArgsConstructor
@RestController
public class TrainingParticipantsController {

    private final TrainingParticipantsService trainingPatricipantsService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TrainingParticipanResponse>> createTrainingParticipants(@RequestBody TrainingParticipanRequest request, Authentication authentication) {

        return ResponseEntity.ok(trainingPatricipantsService.createParticipan(request, authentication));
    }

    @PreAuthorize("hasAuthority('superadmin')")
    @PostMapping("/pagination")
    public ResponseEntity<ApiResponse<List<TrainingParticipantsPaginationResponse>>> getTrainingParticimantsPagination(@RequestBody TrainingParticipantsPaginationRequest request) {
        return ResponseEntity.ok(trainingPatricipantsService.getTrainingParticipantsPagination(request));
    }

    @PreAuthorize("hasAuthority('superadmin')")
    @PostMapping("/detail")
    public ResponseEntity<ApiResponse<TrainingParticipantsDetailResponse>> detailTrainingParticipants(@RequestBody TrainingParticipantsDetailRequest request) {
        return ResponseEntity.ok(trainingPatricipantsService.detailTrainingParticipants(request));
    }

    @PreAuthorize("hasAuthority('superadmin')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<TrainingParticipantsEditResponse>> editTrainingParticipants(@RequestBody TrainingParticipantsEditRequest request) {
        return ResponseEntity.ok(trainingPatricipantsService.editTrainingParticipants(request));
    }

    @PreAuthorize("hasAuthority('superadmin')")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteTrainingParticipants(@RequestBody TrainingParticipantsDeleteRequest request) {
        return ResponseEntity.ok(trainingPatricipantsService.deleteTrainingParticipants(request));
    }
    
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<TrainingParticipantsCheckResponse>> check(@RequestBody TrainingParticipantsCheckRequest request,  Authentication authentication){
        return ResponseEntity.ok(trainingPatricipantsService.checkRegistered(request, authentication));
    }
}
