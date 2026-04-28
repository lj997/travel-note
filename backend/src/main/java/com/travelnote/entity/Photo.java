package com.travelnote.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "photos")
public class Photo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id")
    private JournalEntry journalEntry;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 500)
    private String url;
    
    @Column(length = 500)
    private String thumbnailUrl;
    
    @Column(length = 200)
    private String originalName;
    
    @Column(length = 50)
    private String contentType;
    
    private Long fileSize;
    
    private Integer width;
    
    private Integer height;
    
    @Column(length = 500)
    private String caption;
    
    @Column(nullable = false)
    private Integer displayOrder = 0;
    
    private LocalDateTime takenAt;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
