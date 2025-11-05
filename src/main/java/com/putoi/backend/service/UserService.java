/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.service;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.dto.LoginRequest;
import com.putoi.backend.dto.LoginResponse;
import com.putoi.backend.dto.UserPaginationRequest;
import com.putoi.backend.dto.UserPaginationResponse;
import com.putoi.backend.dto.UserRequest;
import com.putoi.backend.dto.UserResponse;
import com.putoi.backend.exception.BadRequestException;
import com.putoi.backend.exception.ConflictException;
import com.putoi.backend.exception.DataNotFoundException;
import com.putoi.backend.models.Role;
import com.putoi.backend.models.User;
import com.putoi.backend.models.EmailOTP;
import com.putoi.backend.repository.RoleRepository;
import com.putoi.backend.repository.UserRepository;
import com.putoi.backend.security.JwtUtil;
import com.putoi.backend.service.email.EmailService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.putoi.backend.repository.EmailOTPRepositorry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author alfia
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final EmailOTPRepositorry emailOTPRepositorry;

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
    public ApiResponse<UserResponse> createUser(UserRequest req) {
        if (req == null) {
            throw new BadRequestException("User payload is empty");
        }
        if (isBlank(req.getName())) {
            throw new BadRequestException("Name is required");
        }
        if (isBlank(req.getEmail())) {
            throw new BadRequestException("Email is required");
        }
        if (isBlank(req.getPassword())) {
            throw new BadRequestException("Password is required");
        }

        String normalizedEmail = req.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already in use: " + normalizedEmail);
        }

        User user = modelMapper.map(req, User.class);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        if (user.getPhoneNumber() == null) {
            user.setPhoneNumber("");
        }

        Role defaultRole = roleRepository.findByName("MEMBER")
                .orElseThrow(() -> new RuntimeException("MEMBER role not found"));
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(defaultRole);
        user.setRoles(roleSet);

        User saved = userRepository.save(user);

        UserResponse resp = modelMapper.map(saved, UserResponse.class);
        String firstRole = saved.getRoles() == null || saved.getRoles().isEmpty()
                ? null
                : saved.getRoles().iterator().next().getName();
        resp.setRole(firstRole);

        return ApiResponse.<UserResponse>builder()
                .code("00")
                .message("User success created")
                .data(resp)
                .build();
    }

    public ApiResponse<LoginResponse> login(LoginRequest request) {

        if (isBlank(request.getEmail())) {
            throw new BadRequestException("Email is required");
        }

        if (isBlank(request.getPassword())) {
            throw new BadRequestException("Password is required");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new DataNotFoundException("Email not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Password is incorrect");
        }

        String token = jwtUtil.generateToken(user);

        LoginResponse response = modelMapper.map(user, LoginResponse.class);
        response.setToken(token);
        response.setDateTime(LocalDateTime.now());

        return ApiResponse.<LoginResponse>builder()
                .code("00")
                .message("Success Login")
                .data(response)
                .build();
    }

    public ApiResponse<String> sendOTP(LoginRequest request) {

        if (isBlank(request.getEmail())) {
            throw new BadRequestException("Email is required");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        Optional<EmailOTP> maybeOtp = emailOTPRepositorry.findByEmail(normalizedEmail);

        EmailOTP emailOTP;

        if (maybeOtp.isPresent()) {
            emailOTP = maybeOtp.get();
            if (Boolean.TRUE.equals(emailOTP.isEmailVerified())) {
                throw new BadRequestException("Email already exists");
            }
        } else {
            emailOTP = new EmailOTP();
            emailOTP.setEmail(normalizedEmail);
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        emailOTP.setEmail(normalizedEmail);
        emailOTP.setEmailVerified(false);
        emailOTP.setVerificationCode(otp);
        emailOTP.setVerificationExpiry(LocalDateTime.now().plusMinutes(10));
        emailOTPRepositorry.save(emailOTP);

        emailService.sendVerificationEmail(emailOTP.getEmail(), otp);

        return new ApiResponse<>("00", "Success send OTP", null, null, null, null);
    }

//User pagination 
    public ApiResponse<List<UserPaginationResponse>> getUsers(UserPaginationRequest req) {

        int limit;
        try {
            limit = Integer.parseInt(req.getLimit());
        } catch (Exception e) {
            limit = 10;
        } 
        if (limit <= 0) {
            limit = 10;
        }

        int pageClient;
        try {
            pageClient = Integer.parseInt(req.getPage());
        } catch (Exception e) {
            pageClient = 1;
        } 
        if (pageClient <= 0) {
            pageClient = 1;
        }
        int page = pageClient - 1; 

        String sortBy = (req.getSortBy() == null || req.getSortBy().isBlank()) ? "id" : req.getSortBy();
        String sortOrder = (req.getSortOrder() == null || req.getSortOrder().isBlank()) ? "desc" : req.getSortOrder();
        Sort.Direction dir = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(page, limit, Sort.by(dir, sortBy));

        Map<String, String> f = Optional.ofNullable(req.getFilters()).orElse(Collections.emptyMap());
        String email = normalize(f.get("email"));
        String noHp = normalize(f.get("noHp"));
        String nama = normalize(f.get("nama"));

        Specification<User> spec = Specification.where(null);
        if (email != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("email")), "%" + email + "%"));
        }
        if (noHp != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("phoneNumber")), "%" + noHp + "%"));
        }
        if (nama != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("name")), "%" + nama + "%"));
        }

        Page<User> pageResult = userRepository.findAll(spec, pageable);

        if (pageResult.isEmpty()) {
            throw new DataNotFoundException("data notfound");
        }

        List<UserPaginationResponse> items = pageResult.getContent().stream()
                .map(u -> UserPaginationResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .creatAt(u.getCreatAt())
                .updateAt(u.getUpdateAt())
                .roles(Optional.ofNullable(u.getRoles()).orElse(Collections.emptySet())
                        .stream().map(r -> r.getName()).collect(Collectors.toList()))
                .build())
                .collect(Collectors.toList());

        ApiResponse<List<UserPaginationResponse>> resp = new ApiResponse<>();
        resp.setCode("00");
        resp.setMessage("Success");
        resp.setData(items);
        resp.setPage(pageResult.getNumber() + 1); 
        resp.setTotalPages(pageResult.getTotalPages());
        resp.setCountData((int) pageResult.getTotalElements());
        return resp;
    }

}
