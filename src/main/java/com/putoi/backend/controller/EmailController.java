/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.controller;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.dto.EmailVerifyRequest;
import com.putoi.backend.service.email.EmailVerify;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author alfia
 */
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailVerify emailVerify;

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestBody EmailVerifyRequest request) {
        return ResponseEntity.ok(emailVerify.verifyEmail(request.getEmail(), request.getOtp()));
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerifyEmail(@RequestBody EmailVerifyRequest request){;
        return ResponseEntity.ok(emailVerify.resendVerification(request.getEmail()));
    }
}
