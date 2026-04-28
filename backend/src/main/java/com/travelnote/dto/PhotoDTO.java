package com.travelnote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDTO {
    
    private Long id;
    private String url;
    private String thumbnailUrl;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private String caption;
    private Integer displayOrder;
    private LocalDateTime takenAt;
    private LocalDateTime uploadedAt;
}
