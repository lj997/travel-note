package com.travelnote.controller;

import com.travelnote.dto.ApiResponse;
import com.travelnote.dto.LocationCreateRequest;
import com.travelnote.dto.LocationDTO;
import com.travelnote.security.UserPrincipal;
import com.travelnote.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/locations")
@Tag(name = "地点管理", description = "旅程地点标记和地图轨迹接口")
@SecurityRequirement(name = "bearerAuth")
public class LocationController {
    
    private final LocationService locationService;
    
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }
    
    @GetMapping
    @Operation(summary = "获取旅程地点列表", description = "获取指定旅程下的所有标记地点")
    public ResponseEntity<?> getLocations(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<LocationDTO> locations = locationService.getLocationsByTrip(tripId, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(locations));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{locationId}")
    @Operation(summary = "获取地点详情", description = "根据ID获取地点详情")
    public ResponseEntity<?> getLocation(
            @PathVariable Long tripId,
            @PathVariable Long locationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            LocationDTO location = locationService.getLocationById(locationId, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(location));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping
    @Operation(summary = "标记地点", description = "在旅程下添加新的标记地点")
    public ResponseEntity<?> createLocation(
            @PathVariable Long tripId,
            @Valid @RequestBody LocationCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            LocationDTO location = locationService.createLocation(tripId, userPrincipal.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("标记成功", location));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{locationId}")
    @Operation(summary = "更新地点", description = "更新指定ID的地点信息")
    public ResponseEntity<?> updateLocation(
            @PathVariable Long tripId,
            @PathVariable Long locationId,
            @RequestBody LocationCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            LocationDTO location = locationService.updateLocation(locationId, userPrincipal.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", location));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{locationId}")
    @Operation(summary = "删除地点", description = "删除指定ID的标记地点")
    public ResponseEntity<?> deleteLocation(
            @PathVariable Long tripId,
            @PathVariable Long locationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            locationService.deleteLocation(locationId, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/reorder")
    @Operation(summary = "重新排序地点", description = "调整地点的显示顺序")
    public ResponseEntity<?> reorderLocations(
            @PathVariable Long tripId,
            @RequestBody List<Long> locationIds,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<LocationDTO> locations = locationService.reorderLocations(tripId, userPrincipal.getId(), locationIds);
            return ResponseEntity.ok(ApiResponse.success("排序成功", locations));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
