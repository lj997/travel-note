package com.travelnote.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "trips")
public class Trip {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Column(length = 200)
    private String destination;
    
    @Column(length = 500)
    private String coverImage;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TripStatus status = TripStatus.PLANNING;
    
    @Column(length = 100)
    private String shareToken;
    
    @Column(nullable = false)
    private Boolean isPublic = false;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date ASC")
    private List<JournalEntry> journalEntries = new ArrayList<>();
    
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("visitDate ASC")
    private List<Location> locations = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "trip_tags",
        joinColumns = @JoinColumn(name = "trip_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum TripStatus {
        PLANNING,
        ONGOING,
        COMPLETED,
        ARCHIVED
    }
}
