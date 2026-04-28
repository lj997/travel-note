package com.travelnote.dto;

import com.travelnote.entity.Trip;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripDTO {
    
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String destination;
    private String coverImage;
    private Trip.TripStatus status;
    private String shareToken;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer journalEntryCount;
    private Integer photoCount;
    private List<TagDTO> tags;
}
