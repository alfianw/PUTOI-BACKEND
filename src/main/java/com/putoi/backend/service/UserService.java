/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.service;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.dto.LoginRequest;
import com.putoi.backend.dto.LoginResponse;
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
import java.util.Optional;

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

        return new ApiResponse<>("00", "Success send OTP", null);
    }

}
