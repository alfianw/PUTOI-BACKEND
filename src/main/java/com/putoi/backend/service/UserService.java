/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.service;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.dto.AuthDto.LoginRequest;
import com.putoi.backend.dto.AuthDto.LoginResponse;
import com.putoi.backend.dto.UserDto.UserDeleteRequest;
import com.putoi.backend.dto.UserDto.UserGetDetailByEmailRequest;
import com.putoi.backend.dto.UserDto.UserGetDetailByEmailResponse;
import com.putoi.backend.dto.UserDto.UserMeResponse;
import com.putoi.backend.dto.UserDto.UserPaginationRequest;
import com.putoi.backend.dto.UserDto.UserPaginationResponse;
import com.putoi.backend.dto.UserDto.UserRequest;
import com.putoi.backend.dto.UserDto.UserResponse;
import com.putoi.backend.dto.UserDto.UserUpdatePassAndEmailRequest;
import com.putoi.backend.dto.UserDto.UserUpdateRequest;
import com.putoi.backend.dto.UserDto.UserUpdateResponse;
import com.putoi.backend.exception.BadRequestException;
import com.putoi.backend.exception.ConflictException;
import com.putoi.backend.exception.DataNotFoundException;
import com.putoi.backend.exception.UnauthorizedException;
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
import com.putoi.backend.repository.PasswordResetTokenRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;

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
    private final PasswordResetTokenRepository passwordResetTokenRepository;

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

        if (isBlank(req.getCityOfResidence())) {
            throw new BadRequestException("City of Residence is required");
        }

        if (isBlank(req.getGender())) {
            throw new BadRequestException("Gender is required");
        }

        if (isBlank(req.getIdentityNumber())) {
            throw new BadRequestException("Identity Number is required");
        }

        if (isBlank(req.getLastEducationField())) {
            throw new BadRequestException("Last Education Field is required");
        }

        if (isBlank(req.getMajorStudyProgram())) {
            throw new BadRequestException("Major Study Program is required");
        }

        if (isBlank(req.getParticipantType())) {
            throw new BadRequestException("Participant Type is required");
        }

        if (isBlank(req.getUniversityName())) {
            throw new BadRequestException("University Name is required");
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

        Role defaultRole = roleRepository.findByName("student")
                .orElseThrow(() -> new RuntimeException("student role not found"));
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(defaultRole);
        user.setRoles(roleSet);

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(user);

        UserResponse resp = modelMapper.map(saved, UserResponse.class);
        String firstRole = saved.getRoles() == null || saved.getRoles().isEmpty()
                ? null
                : saved.getRoles().iterator().next().getName();
        resp.setRole(firstRole);
        resp.setToken(token);

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
        response.setRoles(
                Optional.ofNullable(user.getRoles())
                        .filter(roles -> !roles.isEmpty())
                        .map(roles -> roles.iterator().next().getName())
                        .orElse(null)
        );
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
            // Reset emailVerified jika sebelumnya sudah true
            if (Boolean.TRUE.equals(emailOTP.isEmailVerified())) {
                emailOTP.setEmailVerified(false);
            }
        } else {
            emailOTP = new EmailOTP();
            emailOTP.setEmail(normalizedEmail);
        }

        // Generate OTP baru
        String otp = String.format("%06d", new Random().nextInt(999999));
        emailOTP.setEmail(normalizedEmail);
        emailOTP.setVerificationCode(otp);
        emailOTP.setVerificationExpiry(LocalDateTime.now().plusMinutes(10));
        emailOTPRepositorry.save(emailOTP);

        emailService.sendVerificationEmail(emailOTP.getEmail(), otp);

        return new ApiResponse<>("00", "Success send OTP", null, null, null, null);
    }

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
            throw new DataNotFoundException("Data Not Found");
        }

        List<UserPaginationResponse> items = pageResult.getContent().stream()
                .map(u -> UserPaginationResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .createdAt(u.getCreatedAt())
                .updateAt(u.getUpdateAt())
                .role(Optional.ofNullable(u.getRoles())
                        .filter(roles -> !roles.isEmpty())
                        .map(roles -> roles.iterator().next().getName())
                        .orElse(null))
                .cityOfResidence(u.getCityOfResidence())
                .gender(u.getGender())
                .identityNumber(u.getIdentityNumber())
                .lastEducationField(u.getLastEducationField())
                .majorStudyProgram(u.getMajorStudyProgram())
                .participantType(u.getParticipantType())
                .universityName(u.getUniversityName())
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

    public ApiResponse<UserGetDetailByEmailResponse> getDetailByEmail(UserGetDetailByEmailRequest request) {

        if (isBlank(request.getEmail())) {
            throw new BadRequestException("Email is required ");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        UserGetDetailByEmailResponse response = modelMapper.map(user, UserGetDetailByEmailResponse.class);

        response.setRole(
                Optional.ofNullable(user.getRoles())
                        .filter(roles -> !roles.isEmpty())
                        .map(roles -> roles.iterator().next().getName())
                        .orElse(null)
        );

        return ApiResponse.<UserGetDetailByEmailResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }

    @Transactional
    public ApiResponse<UserUpdateResponse> updateUser(UserUpdateRequest request, Authentication authentication) {

        String loginEmail = authentication.getName();

        User authUser = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new DataNotFoundException("Authenticated user not found"));

        boolean isAdmin = authUser.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("superadmin"));

        User userTarget;
        if (isAdmin && !isBlank(request.getEmail())) {

            userTarget = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new DataNotFoundException("Target user not found"));
        } else {

            userTarget = authUser;
        }

        if (!isBlank(request.getName())) {
            userTarget.setName(request.getName());
        }
        if (!isBlank(request.getPhoneNumber())) {
            userTarget.setPhoneNumber(request.getPhoneNumber());
        }

        if (!isBlank(request.getCityOfResidence())) {
            userTarget.setCityOfResidence(request.getCityOfResidence());
        }

        if (!isBlank(request.getGender())) {
            userTarget.setGender(request.getGender());
        }

        if (!isBlank(request.getIdentityNumber())) {
            userTarget.setIdentityNumber(request.getIdentityNumber());
        }

        if (!isBlank(request.getLastEducationField())) {
            userTarget.setLastEducationField(request.getLastEducationField());
        }

        if (!isBlank(request.getMajorStudyProgram())) {
            userTarget.setMajorStudyProgram(request.getMajorStudyProgram());
        }

        if (!isBlank(request.getParticipantType())) {
            userTarget.setParticipantType(request.getParticipantType());
        }

        if (!isBlank(request.getUniversityName())) {
            userTarget.setUniversityName(request.getUniversityName());
        }

        if (!isBlank(request.getRole())) {

            if (!isAdmin) {
                throw new UnauthorizedException("Only superadmin can update roles");
            }

            if (loginEmail.equalsIgnoreCase(userTarget.getEmail())) {
                throw new UnauthorizedException("superadmin cannot update their own role");
            }

            Role newRole = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new DataNotFoundException("Role not found: " + request.getRole()));

            userTarget.setRoles(new HashSet<>(Collections.singleton(newRole)));
        }

        userRepository.save(userTarget);

        UserUpdateResponse response = modelMapper.map(userTarget, UserUpdateResponse.class);
        response.setRole(
                Optional.ofNullable(userTarget.getRoles())
                        .filter(roles -> !roles.isEmpty())
                        .map(roles -> roles.iterator().next().getName())
                        .orElse(null)
        );

        return ApiResponse.<UserUpdateResponse>builder()
                .code("00")
                .message("Success Update")
                .data(response)
                .build();
    }

    @Transactional
    public ApiResponse<String> updatePasswordAndEmail(UserUpdatePassAndEmailRequest request, Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        if (!isBlank(request.getOldPassword())) {
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new BadRequestException("Old password is incorrect");
            }
        }

        if (!isBlank(request.getNewPassword())) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        if (!isBlank(request.getEmail())) {
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);

        return ApiResponse.<String>builder()
                .code("00")
                .message("Password or Email updated successfully, please login again")
                .data(null)
                .build();
    }

    @Transactional
    public ApiResponse<String> deleteUser(UserDeleteRequest request) {

        // Cari user berdasarkan email
        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        // Cek apakah target adalah superadmin
        boolean targetIsAdmin = targetUser.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("superadmin"));

        if (targetIsAdmin) {
            throw new UnauthorizedException("Cannot delete another superadmin account");
        }

        // Hapus semua password reset token terkait user
        passwordResetTokenRepository.deleteByUser(targetUser);

        // Hapus user (Hibernate akan otomatis hapus user_roles, news, products, trainings karena cascade + orphanRemoval)
        userRepository.delete(targetUser);

        return ApiResponse.<String>builder()
                .code("00")
                .message("User deleted successfully")
                .data(null)
                .build();
    }

    public ApiResponse<UserMeResponse> getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        UserMeResponse response = UserMeResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();

        response.setRoles(
                Optional.ofNullable(user.getRoles())
                        .filter(roles -> !roles.isEmpty())
                        .map(roles -> roles.iterator().next().getName())
                        .orElse(null)
        );

        return ApiResponse.<UserMeResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }

}
