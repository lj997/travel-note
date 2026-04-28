package com.travelnote.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tags")
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(length = 20)
    private String color = "#3b82f6";
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToMany(mappedBy = "tags")
    private List<Trip> trips = new ArrayList<>();
    
    @ManyToMany(mappedBy = "favoriteTags")
    private List<User> users = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
