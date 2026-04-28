package com.travelnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LocationCreateRequest {
    
    @NotBlank(message = "地点名称不能为空")
    private String name;
    
    private String description;
    
    @NotNull(message = "纬度不能为空")
    private Double latitude;
    
    @NotNull(message = "经度不能为空")
    private Double longitude;
    
    private LocalDate visitDate;
    private Integer displayOrder;
    private String photoUrl;
}
