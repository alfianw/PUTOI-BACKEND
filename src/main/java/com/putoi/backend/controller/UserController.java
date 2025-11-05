/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.controller;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.dto.LoginRequest;
import com.putoi.backend.dto.LoginResponse;
import com.putoi.backend.dto.UserRequest;
import com.putoi.backend.dto.UserResponse;
import com.putoi.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<ApiResponse<String>> sendOTP(@RequestBody LoginRequest req){
        return ResponseEntity.ok(userService.sendOTP(req));
    }

}
