/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.service;

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
import com.putoi.backend.exception.BadRequestException;
import com.putoi.backend.exception.ConflictException;
import com.putoi.backend.exception.DataNotFoundException;
import com.putoi.backend.models.Training;
import com.putoi.backend.models.TrainingParticipants;
import com.putoi.backend.models.User;
import com.putoi.backend.repository.TrainingPatrticipantsRepository;
import com.putoi.backend.repository.TrainingRepository;
import com.putoi.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 *
 * @author alfia
 */
@Service
@RequiredArgsConstructor
public class TrainingParticipantsService {

    private final TrainingPatrticipantsRepository trainingPatrticipantsRepository;
    private final ModelMapper modelMapper;
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String normalize(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t.toLowerCase();
    }

    @Transactional
    public ApiResponse<TrainingParticipanResponse> createParticipan(TrainingParticipanRequest request, Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        if (request.getTrainingId() == null) {
            throw new BadRequestException("Training Id is required");
        }

        Training training = trainingRepository.findById(request.getTrainingId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        boolean alreadyRegistered = trainingPatrticipantsRepository
                .existsByEmailAndTraining_Id(user.getEmail(), training.getId());
        if (alreadyRegistered) {
            throw new ConflictException("You have already registered for this training");
        }

        TrainingParticipants trainingParticipants = modelMapper.map(request, TrainingParticipants.class);

        trainingParticipants.setName(user.getName());
        trainingParticipants.setParticipantType(user.getParticipantType());
        trainingParticipants.setIdentityNumber(user.getIdentityNumber());
        trainingParticipants.setGender(user.getGender());
        trainingParticipants.setUniversityName(user.getUniversityName());
        trainingParticipants.setEmail(user.getEmail());
        trainingParticipants.setPhoneNumber(user.getPhoneNumber());
        trainingParticipants.setStatus("REGISTERED");
        trainingParticipants.setTraining(training);

        try {
            trainingPatrticipantsRepository.save(trainingParticipants);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("You have already registered for this training");
        }

        TrainingParticipanResponse response = TrainingParticipanResponse.builder()
                .id(trainingParticipants.getId())
                .name(trainingParticipants.getName())
                .email(trainingParticipants.getEmail())
                .gender(trainingParticipants.getGender())
                .identityNumber(trainingParticipants.getIdentityNumber())
                .participantType(trainingParticipants.getParticipantType())
                .phoneNumber(trainingParticipants.getPhoneNumber())
                .status(trainingParticipants.getStatus())
                .universityName(trainingParticipants.getUniversityName())
                .createdAt(trainingParticipants.getCreatedAt())
                .updatedAt(trainingParticipants.getUpdatedAt())
                .training(training.getTrainingTitle())
                .build();

        return ApiResponse.<TrainingParticipanResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }

    public ApiResponse<List<TrainingParticipantsPaginationResponse>> getTrainingParticipantsPagination(TrainingParticipantsPaginationRequest request) {

        int limit;
        try {
            limit = Integer.parseInt(request.getLimit());
        } catch (Exception e) {
            limit = 10;
        }
        if (limit <= 0) {
            limit = 10;
        }

        int pageClient;
        try {
            pageClient = Integer.parseInt(request.getPage());
        } catch (Exception e) {
            pageClient = 1;
        }
        if (pageClient <= 0) {
            pageClient = 1;
        }
        int page = pageClient - 1;

        String sortBy = (request.getSortBy() == null || request.getSortBy().isBlank()) ? "id" : request.getSortBy();
        String sortOrder = (request.getSortOrder() == null || request.getSortOrder().isBlank()) ? "desc" : request.getSortOrder();
        Sort.Direction dir = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(page, limit, Sort.by(dir, sortBy));

        Map<String, String> f = Optional.ofNullable(request.getFilters()).orElse(Collections.emptyMap());
        String name = normalize(f.get("name"));
        String email = normalize(f.get("author"));

        Specification<TrainingParticipants> spec = Specification.where(null);
        if (name != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("name")), "%" + name + "%"));
        }
        if (email != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("email")), "%" + email + "%"));
        }
        Page<TrainingParticipants> pageResult = trainingPatrticipantsRepository.findAll(spec, pageable);

        if (pageResult.isEmpty()) {
            throw new DataNotFoundException("Data Not Found");
        }

        List<TrainingParticipantsPaginationResponse> items = pageResult.getContent().stream()
                .map(entity -> {
                    TrainingParticipantsPaginationResponse dto
                            = modelMapper.map(entity, TrainingParticipantsPaginationResponse.class);
                    if (entity.getTraining() != null) {
                        dto.setTraining(entity.getTraining().getTrainingTitle());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        ApiResponse<List<TrainingParticipantsPaginationResponse>> resp = new ApiResponse<>();
        resp.setCode("00");
        resp.setMessage("Success");
        resp.setData(items);
        resp.setPage(pageResult.getNumber() + 1);
        resp.setTotalPages(pageResult.getTotalPages());
        resp.setCountData((int) pageResult.getTotalElements());
        return resp;
    }

    public ApiResponse<TrainingParticipantsDetailResponse> detailTrainingParticipants(TrainingParticipantsDetailRequest request) {

        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }

        TrainingParticipants trainingParticipants = trainingPatrticipantsRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        TrainingParticipantsDetailResponse response = modelMapper.map(trainingParticipants, TrainingParticipantsDetailResponse.class);

        return ApiResponse.<TrainingParticipantsDetailResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();

    }

    @Transactional
    public ApiResponse<TrainingParticipantsEditResponse> editTrainingParticipants(TrainingParticipantsEditRequest request) {

        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }

        TrainingParticipants trainingParticipants = trainingPatrticipantsRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        if (!isBlank(request.getStatus())) {
            trainingParticipants.setStatus(request.getStatus());
        }

        trainingPatrticipantsRepository.save(trainingParticipants);

        TrainingParticipantsEditResponse response = modelMapper.map(trainingParticipants, TrainingParticipantsEditResponse.class);

        return ApiResponse.<TrainingParticipantsEditResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }

    public ApiResponse<String> deleteTrainingParticipants(TrainingParticipantsDeleteRequest request) {

        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }

        TrainingParticipants trainingParticipants = trainingPatrticipantsRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        trainingPatrticipantsRepository.delete(trainingParticipants);

        return ApiResponse.<String>builder()
                .code("00")
                .message("Success")
                .data(null)
                .build();
    }

    public ApiResponse<TrainingParticipantsCheckResponse> checkRegistered(
            TrainingParticipantsCheckRequest request,
            Authentication authentication
    ) {

        if (request.getTrainingId() == null) {
            throw new BadRequestException("Training Id is required");
        }

        String email = authentication.getName();

        boolean registered = trainingPatrticipantsRepository
                .existsByEmailAndTraining_Id(email, request.getTrainingId());

        // bungkus ke DTO response
        TrainingParticipantsCheckResponse response = new TrainingParticipantsCheckResponse();
        response.setRegistered(registered);

        return ApiResponse.<TrainingParticipantsCheckResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }

}
