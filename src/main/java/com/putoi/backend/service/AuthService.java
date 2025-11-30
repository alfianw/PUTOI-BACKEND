/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.service;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.models.PasswordResetToken;
import com.putoi.backend.models.User;
import com.putoi.backend.repository.PasswordResetTokenRepository;
import com.putoi.backend.repository.UserRepository;
import com.putoi.backend.service.email.EmailService;
import com.putoi.backend.service.email.TokenUtils;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author alfia
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.reset-password.url}")
    private String resetPasswordUrl;

    @Transactional
    public ApiResponse<String> sendResetPasswordEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        passwordResetTokenRepository.deleteByUser(user);

        String rawToken = TokenUtils.generateToken(32);
        String tokenHash = TokenUtils.sha256Hex(rawToken);

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        passwordResetTokenRepository.save(token);

        // kirim email
        String resetLink = resetPasswordUrl + "?token=" + rawToken;
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);

        return new ApiResponse<>("00", "Reset password email sent", null, null, null, null);
    }

    @Transactional
    public ApiResponse<String> resetPassword(String rawToken, String newPassword) {
        String tokenHash = TokenUtils.sha256Hex(rawToken);

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token is expired or already used");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        emailService.sendPasswordChangedNotification(user.getEmail(), user.getName());

        return new ApiResponse<>("00", "Password reset successful", null, null, null, null);
    }

}
