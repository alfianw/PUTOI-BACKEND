/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.service.email;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.exception.BadRequestException;
import com.putoi.backend.exception.DataNotFoundException;
import com.putoi.backend.models.EmailOTP;
import com.putoi.backend.repository.EmailOTPRepositorry;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * @author alfia
 */
@Service
@RequiredArgsConstructor
public class EmailVerify {
    
    private final EmailOTPRepositorry emailOTPRepositorry;
    private final EmailService emailService;
    
    @Transactional
    public ApiResponse<String> verifyEmail(String email, String otp){
        EmailOTP emailOTP = emailOTPRepositorry.findByEmail(email)
                .orElseThrow(()-> new DataNotFoundException("Email tidak ditemukan: " + email));
        
        if (emailOTP.isEmailVerified()){
            return new ApiResponse<>("00", "Email sudah diverifikasi", null, null, null, null);
        }
        
        if(!otp.equals(emailOTP.getVerificationCode())){
            throw new BadRequestException("Kode OTP Salah");
        }
        
        emailOTP.setEmailVerified(true);
        emailOTP.setVerificationCode(null);
        emailOTP.setVerificationExpiry(null);
        emailOTPRepositorry.save(emailOTP);
        
        return new ApiResponse<>("00", "Email berhasil diverifikasi", null, null, null, null);
    }
    
@Transactional
public ApiResponse<String> resendVerification(String email) {
    EmailOTP emailOTP = emailOTPRepositorry.findByEmail(email).orElse(null);

    if (emailOTP != null) {
        if (emailOTP.isEmailVerified()) {
            throw new BadRequestException("Email sudah diverifikasi");
        }
    } else {
        emailOTP = new EmailOTP();
        emailOTP.setEmail(email);
        emailOTP.setEmailVerified(false);
    }

    String otp = String.format("%06d", new Random().nextInt(999999));
    emailOTP.setVerificationCode(otp);
    emailOTP.setVerificationExpiry(LocalDateTime.now().plusMinutes(10));

    emailOTPRepositorry.save(emailOTP);

    emailService.sendVerificationEmail(emailOTP.getEmail(), otp);

    return new ApiResponse<>("00", "Kode verifikasi baru telah dikirim ke email Anda", null, null, null, null);
}

}
