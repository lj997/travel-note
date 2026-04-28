package com.travelnote.controller;

import com.travelnote.dto.ApiResponse;
import com.travelnote.dto.AuthResponse;
import com.travelnote.dto.LoginRequest;
import com.travelnote.dto.RegisterRequest;
import com.travelnote.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户登录和注册接口")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名或邮箱登录")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(ApiResponse.success(authResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账号")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse authResponse = authService.registerUser(registerRequest);
            return ResponseEntity.ok(ApiResponse.success("注册成功", authResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
