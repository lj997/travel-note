package com.travelnote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryDTO {
    
    private Long id;
    private Long tripId;
    private LocalDate date;
    private String title;
    private String content;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PhotoDTO> photos;
}
