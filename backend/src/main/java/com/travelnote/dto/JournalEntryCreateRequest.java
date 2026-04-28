package com.travelnote.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class JournalEntryCreateRequest {
    
    @NotNull(message = "日期不能为空")
    private LocalDate date;
    
    private String title;
    private String content;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private List<Long> photoIds;
}
