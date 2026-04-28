package com.travelnote.controller;

import com.travelnote.dto.ApiResponse;
import com.travelnote.dto.TripCreateRequest;
import com.travelnote.dto.TripDTO;
import com.travelnote.dto.TripUpdateRequest;
import com.travelnote.security.UserPrincipal;
import com.travelnote.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@Tag(name = "旅程管理", description = "旅程增删改查接口")
@SecurityRequirement(name = "bearerAuth")
public class TripController {
    
    private final TripService tripService;
    
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }
    
    @GetMapping
    @Operation(summary = "获取用户旅程列表", description = "获取当前用户的所有旅程")
    public ResponseEntity<?> getUserTrips(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<TripDTO> trips = tripService.getUserTrips(userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(trips));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{tripId}")
    @Operation(summary = "获取旅程详情", description = "根据ID获取旅程详情")
    public ResponseEntity<?> getTripById(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            TripDTO trip = tripService.getTripById(tripId, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(trip));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/shared/{shareToken}")
    @Operation(summary = "获取分享的旅程", description = "通过分享令牌获取公开旅程")
    public ResponseEntity<?> getSharedTrip(@PathVariable String shareToken) {
        try {
            TripDTO trip = tripService.getTripByShareToken(shareToken);
            return ResponseEntity.ok(ApiResponse.success(trip));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping
    @Operation(summary = "创建旅程", description = "创建新的旅程")
    public ResponseEntity<?> createTrip(
            @Valid @RequestBody TripCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            TripDTO trip = tripService.createTrip(userPrincipal.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("创建成功", trip));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{tripId}")
    @Operation(summary = "更新旅程", description = "更新指定ID的旅程信息")
    public ResponseEntity<?> updateTrip(
            @PathVariable Long tripId,
            @RequestBody TripUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            TripDTO trip = tripService.updateTrip(tripId, userPrincipal.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", trip));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{tripId}")
    @Operation(summary = "删除旅程", description = "删除指定ID的旅程")
    public ResponseEntity<?> deleteTrip(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            tripService.deleteTrip(tripId, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/{tripId}/share")
    @Operation(summary = "生成分享链接", description = "为旅程生成分享令牌")
    public ResponseEntity<?> generateShareToken(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            String token = tripService.generateShareToken(tripId, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("分享链接已生成", Map.of("shareToken", token)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/{tripId}/unshare")
    @Operation(summary = "取消分享", description = "撤销旅程的分享令牌")
    public ResponseEntity<?> revokeShareToken(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            tripService.revokeShareToken(tripId, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("已取消分享", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
