package com.travelnote.dto;

import com.travelnote.entity.Trip;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TripUpdateRequest {
    
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String destination;
    private String coverImage;
    private Trip.TripStatus status;
    private Boolean isPublic;
    private List<String> tagNames;
}
