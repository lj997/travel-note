package com.travelnote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    
    private Long id;
    private Long tripId;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDate visitDate;
    private Integer displayOrder;
    private String photoUrl;
    private LocalDateTime createdAt;
}
