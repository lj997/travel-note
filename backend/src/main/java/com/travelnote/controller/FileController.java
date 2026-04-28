package com.travelnote.controller;

import com.travelnote.dto.ApiResponse;
import com.travelnote.dto.PhotoDTO;
import com.travelnote.entity.Photo;
import com.travelnote.security.UserPrincipal;
import com.travelnote.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "图片上传和文件管理接口")
@SecurityRequirement(name = "bearerAuth")
public class FileController {
    
    private final FileStorageService fileStorageService;
    
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    
    @PostMapping("/upload")
    @Operation(summary = "上传单张图片", description = "上传一张图片文件")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Photo photo = fileStorageService.storeFile(file, userPrincipal.getId());
            PhotoDTO photoDTO = convertToDTO(photo);
            return ResponseEntity.ok(ApiResponse.success("上传成功", photoDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("上传失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/upload-multiple")
    @Operation(summary = "批量上传图片", description = "同时上传多张图片文件")
    public ResponseEntity<?> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<PhotoDTO> uploadedPhotos = new ArrayList<>();
            List<String> failedFiles = new ArrayList<>();
            
            for (MultipartFile file : files) {
                try {
                    Photo photo = fileStorageService.storeFile(file, userPrincipal.getId());
                    uploadedPhotos.add(convertToDTO(photo));
                } catch (Exception e) {
                    failedFiles.add(file.getOriginalFilename() + ": " + e.getMessage());
                }
            }
            
            if (uploadedPhotos.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("所有文件上传失败: " + String.join(", ", failedFiles)));
            }
            
            String message = uploadedPhotos.size() + " 张图片上传成功";
            if (!failedFiles.isEmpty()) {
                message += ", " + failedFiles.size() + " 张上传失败";
            }
            
            return ResponseEntity.ok(ApiResponse.success(message, uploadedPhotos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("上传失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/photos/{photoId}")
    @Operation(summary = "删除图片", description = "删除指定ID的图片")
    public ResponseEntity<?> deletePhoto(
            @PathVariable Long photoId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            fileStorageService.deletePhoto(photoId, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    private PhotoDTO convertToDTO(Photo photo) {
        PhotoDTO dto = new PhotoDTO();
        dto.setId(photo.getId());
        dto.setUrl(photo.getUrl());
        dto.setThumbnailUrl(photo.getThumbnailUrl());
        dto.setOriginalName(photo.getOriginalName());
        dto.setContentType(photo.getContentType());
        dto.setFileSize(photo.getFileSize());
        dto.setWidth(photo.getWidth());
        dto.setHeight(photo.getHeight());
        dto.setCaption(photo.getCaption());
        dto.setDisplayOrder(photo.getDisplayOrder());
        dto.setTakenAt(photo.getTakenAt());
        dto.setUploadedAt(photo.getUploadedAt());
        return dto;
    }
}
