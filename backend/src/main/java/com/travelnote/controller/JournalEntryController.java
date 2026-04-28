package com.travelnote.controller;

import com.travelnote.dto.ApiResponse;
import com.travelnote.dto.JournalEntryCreateRequest;
import com.travelnote.dto.JournalEntryDTO;
import com.travelnote.security.UserPrincipal;
import com.travelnote.service.JournalEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/journals")
@Tag(name = "日记管理", description = "旅程日记增删改查接口")
@SecurityRequirement(name = "bearerAuth")
public class JournalEntryController {
    
    private final JournalEntryService journalEntryService;
    
    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }
    
    @GetMapping
    @Operation(summary = "获取旅程日记列表", description = "获取指定旅程下的所有日记记录")
    public ResponseEntity<?> getJournalEntries(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<JournalEntryDTO> entries = journalEntryService.getJournalEntriesByTrip(tripId, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(entries));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{entryId}")
    @Operation(summary = "获取日记详情", description = "根据ID获取日记详情")
    public ResponseEntity<?> getJournalEntry(
            @PathVariable Long tripId,
            @PathVariable Long entryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            JournalEntryDTO entry = journalEntryService.getJournalEntryById(entryId, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(entry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping
    @Operation(summary = "创建日记", description = "在旅程下创建新的日记记录")
    public ResponseEntity<?> createJournalEntry(
            @PathVariable Long tripId,
            @Valid @RequestBody JournalEntryCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            JournalEntryDTO entry = journalEntryService.createJournalEntry(tripId, userPrincipal.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("创建成功", entry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{entryId}")
    @Operation(summary = "更新日记", description = "更新指定ID的日记信息")
    public ResponseEntity<?> updateJournalEntry(
            @PathVariable Long tripId,
            @PathVariable Long entryId,
            @RequestBody JournalEntryCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            JournalEntryDTO entry = journalEntryService.updateJournalEntry(entryId, userPrincipal.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", entry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{entryId}")
    @Operation(summary = "删除日记", description = "删除指定ID的日记记录")
    public ResponseEntity<?> deleteJournalEntry(
            @PathVariable Long tripId,
            @PathVariable Long entryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            journalEntryService.deleteJournalEntry(entryId, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
