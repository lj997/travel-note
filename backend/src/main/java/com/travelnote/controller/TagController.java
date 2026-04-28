package com.travelnote.controller;

import com.travelnote.dto.ApiResponse;
import com.travelnote.dto.TagDTO;
import com.travelnote.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "标签管理", description = "标签分类管理接口")
@SecurityRequirement(name = "bearerAuth")
public class TagController {
    
    private final TagService tagService;
    
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }
    
    @GetMapping
    @Operation(summary = "获取所有标签", description = "获取系统中所有标签列表")
    public ResponseEntity<?> getAllTags() {
        try {
            List<TagDTO> tags = tagService.getAllTags();
            return ResponseEntity.ok(ApiResponse.success(tags));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索标签", description = "根据关键词搜索标签")
    public ResponseEntity<?> searchTags(@RequestParam String keyword) {
        try {
            List<TagDTO> tags = tagService.searchTags(keyword);
            return ResponseEntity.ok(ApiResponse.success(tags));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取标签详情", description = "根据ID获取标签详情")
    public ResponseEntity<?> getTagById(@PathVariable Long id) {
        try {
            TagDTO tag = tagService.getTagById(id);
            return ResponseEntity.ok(ApiResponse.success(tag));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping
    @Operation(summary = "创建标签", description = "创建新的标签")
    public ResponseEntity<?> createTag(@RequestBody Map<String, String> tagMap) {
        try {
            String name = tagMap.get("name");
            String color = tagMap.get("color");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("标签名称不能为空"));
            }
            
            TagDTO tag = tagService.createTag(name, color);
            return ResponseEntity.ok(ApiResponse.success("创建成功", tag));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新标签", description = "更新指定ID的标签")
    public ResponseEntity<?> updateTag(@PathVariable Long id, @RequestBody Map<String, String> tagMap) {
        try {
            String name = tagMap.get("name");
            String color = tagMap.get("color");
            
            TagDTO tag = tagService.updateTag(id, name, color);
            return ResponseEntity.ok(ApiResponse.success("更新成功", tag));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签", description = "删除指定ID的标签")
    public ResponseEntity<?> deleteTag(@PathVariable Long id) {
        try {
            tagService.deleteTag(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
