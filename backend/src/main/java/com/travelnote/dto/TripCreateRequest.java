package com.travelnote.dto;

import com.travelnote.entity.Trip;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TripCreateRequest {
    
    @NotBlank(message = "旅程名称不能为空")
    private String title;
    
    private String description;
    
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    
    private String destination;
    
    private String coverImage;
    
    private Trip.TripStatus status = Trip.TripStatus.PLANNING;
    
    private Boolean isPublic = false;
    
    private List<String> tagNames;
}
