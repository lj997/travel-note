package com.travelnote.controller;

import com.travelnote.dto.ApiResponse;
import com.travelnote.dto.UserDTO;
import com.travelnote.security.UserPrincipal;
import com.travelnote.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户资料管理接口")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            UserDTO userDTO = userService.getUserDTO(userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(userDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/me")
    @Operation(summary = "更新用户资料", description = "更新当前登录用户的个人资料")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UserDTO userDTO) {
        try {
            UserDTO updatedUser = userService.updateProfile(userPrincipal.getId(), userDTO);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/me/change-password")
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody Map<String, String> passwordMap) {
        try {
            String oldPassword = passwordMap.get("oldPassword");
            String newPassword = passwordMap.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("旧密码和新密码不能为空"));
            }
            
            userService.changePassword(userPrincipal.getId(), oldPassword, newPassword);
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
