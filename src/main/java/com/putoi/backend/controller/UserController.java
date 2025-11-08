/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.controller;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.dto.LoginRequest;
import com.putoi.backend.dto.LoginResponse;
import com.putoi.backend.dto.UserDeleteRequest;
import com.putoi.backend.dto.UserGetDetailByEmailRequest;
import com.putoi.backend.dto.UserGetDetailByEmailResponse;
import com.putoi.backend.dto.UserPaginationRequest;
import com.putoi.backend.dto.UserPaginationResponse;
import com.putoi.backend.dto.UserRequest;
import com.putoi.backend.dto.UserResponse;
import com.putoi.backend.dto.UserUpdatePassAndEmailRequest;
import com.putoi.backend.dto.UserUpdateRequest;
import com.putoi.backend.dto.UserUpdateResponse;
import com.putoi.backend.service.UserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/addUser")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@RequestBody UserRequest req) {
        return ResponseEntity.ok(userService.createUser(req));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(userService.login(req));
    }

    @PostMapping("/sendOTP")
    public ResponseEntity<ApiResponse<String>> sendOTP(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(userService.sendOTP(req));
    }

    @PostMapping("/user-pagination")
    public ResponseEntity<ApiResponse<List<UserPaginationResponse>>> getUsers(
            @RequestBody UserPaginationRequest request) {

        ApiResponse<List<UserPaginationResponse>> response = userService.getUsers(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-detail-by-email")
    public ResponseEntity<ApiResponse<UserGetDetailByEmailResponse>> getDetailByEmail(@RequestBody UserGetDetailByEmailRequest response) {
        return ResponseEntity.ok(userService.getDetailByEmail(response));
    }

    @PutMapping("/update-data-user")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateUser(@RequestBody UserUpdateRequest request, Authentication authentication) {
        return ResponseEntity.ok(userService.updateUser(request, authentication));
    }

    @PutMapping("/update-password-email")
    public ResponseEntity<ApiResponse<String>> updatePassword(@RequestBody UserUpdatePassAndEmailRequest request, Authentication authentication) {
        return ResponseEntity.ok(userService.updatePasswordAndEmail(request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete-user")
    public ResponseEntity<ApiResponse<String>> deleteUser(@RequestBody UserDeleteRequest request) {
        return ResponseEntity.ok(userService.deleteUser(request));
    }
}
