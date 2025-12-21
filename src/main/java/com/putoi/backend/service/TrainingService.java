/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.service;

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
import com.putoi.backend.exception.BadRequestException;
import com.putoi.backend.exception.ConflictException;
import com.putoi.backend.exception.DataNotFoundException;
import com.putoi.backend.models.Training;
import com.putoi.backend.models.User;
import com.putoi.backend.repository.TrainingRepository;
import com.putoi.backend.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
public class TrainingService {
    
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
    
    public ApiResponse<TrainingResponse> createTraining(TrainingRequest request, Authentication authentication) {
        
        String mail = authentication.getName();
        
        User user = userRepository.findByEmail(mail)
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));
        
        if (isBlank(request.getTrainingTitle())) {
            throw new BadRequestException("Title is required");
        }
        
        if (isBlank(request.getDescription())) {
            throw new BadRequestException("Description is required");
        }
        
        if (isBlank(request.getTrainingFee())) {
            throw new BadRequestException("Fee is required");
        }
        
        if (isBlank(request.getCompetencyTestPlace())) {
            throw new BadRequestException("Conmpetency Test Place is required");
        }
        
        if (isBlank(request.getDuration())) {
            throw new BadRequestException("Duration is required");
        }
        
        if (isBlank(request.getImplementationSchedule())) {
            throw new BadRequestException("Implementation Schedule is required");
        }
        
        if (isBlank(request.getInstitutionName())) {
            throw new BadRequestException("Institution Name is required");
        }
        
        if (isBlank(request.getMinimumParticipants())) {
            throw new BadRequestException("Minimum Participan is required");
        }
        
        if (request.getCertificate() == null) {
            throw new BadRequestException("Certificate is required");
        }
        
        if (request.getFacilities() == null) {
            throw new BadRequestException("Facilities is required");
        }
        
        if (request.getTrainingMaterials() == null) {
            throw new BadRequestException("Training Materials is required");
        }
        
        if (trainingRepository.existsByTrainingTitle(request.getTrainingTitle())) {
            throw new ConflictException("Title is already in use:" + request.getTrainingTitle());
        }

        Training training = modelMapper.map(request, Training.class);
        training.setAuthor(user.getName());
        training.setUser(user);
        
        trainingRepository.save(training);
        
        TrainingResponse response = modelMapper.map(training, TrainingResponse.class);
        
        return ApiResponse.<TrainingResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }
    
    public ApiResponse<List<TrainingPaginationResponse>> getTrainingPagination(TrainingPaginationRequest request) {
        
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
        String trainingTitle = normalize(f.get("trainingTitle"));
        String author = normalize(f.get("author"));
        
        Specification<Training> spec = Specification.where(null);
        if (trainingTitle != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("trainingTitle")), "%" + trainingTitle + "%"));
        }
        if (author != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("author")), "%" + author + "%"));
        }
        Page<Training> pageResult = trainingRepository.findAll(spec, pageable);
        
        if (pageResult.isEmpty()) {
            throw new DataNotFoundException("Data Not Found");
        }
        
        List<TrainingPaginationResponse> items = pageResult.getContent().stream()
                .map(u -> TrainingPaginationResponse.builder()
                .id(u.getId())
                .trainingTitle(u.getTrainingTitle())
                .description(u.getDescription())
                .author(u.getAuthor())
                .certificate(u.getCertificate())
                .competencyTestPlace(u.getCompetencyTestPlace())
                .createdAt(u.getCreatedAt())
                .duration(u.getDuration())
                .facilities(u.getFacilities())
                .implementationSchedule(u.getImplementationSchedule())
                .institutionName(u.getInstitutionName())
                .minimumParticipants(u.getMinimumParticipants())
                .trainingFee(u.getTrainingFee())
                .trainingMaterials(u.getTrainingMaterials())
                .updateAt(u.getUpdateAt())
                .totalParticipants(u.getTotalParticipants())
                .build())
                .collect(Collectors.toList());
        
        ApiResponse<List<TrainingPaginationResponse>> resp = new ApiResponse<>();
        resp.setCode("00");
        resp.setMessage("Success");
        resp.setData(items);
        resp.setPage(pageResult.getNumber() + 1);
        resp.setTotalPages(pageResult.getTotalPages());
        resp.setCountData((int) pageResult.getTotalElements());
        return resp;
    }
    
    public ApiResponse<TrainingUpdateResponse> updateTraining(TrainingUpdateRequest request, Authentication authentication) {
        
        String email = authentication.getName();
        
        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));
        
        Training training = trainingRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));
        
        if (!isBlank(request.getCompetencyTestPlace())) {
            training.setCompetencyTestPlace(request.getCompetencyTestPlace());
        }
        
        if (!isBlank(request.getDescription())) {
            training.setDescription(request.getDescription());
        }
        
        if (!isBlank(request.getDuration())) {
            training.setDuration(request.getDuration());
        }
        
        if (!isBlank(request.getImplementationSchedule())) {
            training.setImplementationSchedule(request.getImplementationSchedule());
        }
        
        if (!isBlank(request.getInstitutionName())) {
            training.setInstitutionName(request.getInstitutionName());
        }
        
        if (!isBlank(request.getMinimumParticipants())) {
            training.setMinimumParticipants(request.getMinimumParticipants());
        }
        
        if (!isBlank(request.getTrainingFee())) {
            training.setTrainingFee(request.getTrainingFee());
        }
        
        if (!isBlank(request.getTrainingTitle())) {
            training.setTrainingTitle(request.getTrainingTitle());
        }
        
        if (request.getCertificate() != null) {
            training.setCertificate(request.getCertificate());
        }
        
        if (request.getFacilities() != null) {
            training.setFacilities(request.getFacilities());
        }
        
        if (request.getTrainingMaterials() != null) {
            training.setTrainingMaterials(request.getTrainingMaterials());
        }
        
        training.setAuthor(user.getName());
        
        trainingRepository.save(training);
        
        TrainingUpdateResponse response = modelMapper.map(training, TrainingUpdateResponse.class);
        
        return ApiResponse.<TrainingUpdateResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }
    
    public ApiResponse<TrainingDetailResponse> detailTraining(TrainingDetailRequest request) {
        
        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }
        
        Training training = trainingRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));
        
        TrainingDetailResponse response = modelMapper.map(training, TrainingDetailResponse.class);
        
        return ApiResponse.<TrainingDetailResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }
    
    public ApiResponse<String> deleteTraining(TrainingDeleteRequest request) {
        
        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }
        
        Training training = trainingRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));
        
        trainingRepository.delete(training);
        
        return ApiResponse.<String>builder()
                .code("00")
                .message("Success")
                .data(null)
                .build();
    }
    
}
