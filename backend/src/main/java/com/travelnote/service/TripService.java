package com.travelnote.service;

import com.travelnote.dto.TagDTO;
import com.travelnote.dto.TripCreateRequest;
import com.travelnote.dto.TripDTO;
import com.travelnote.dto.TripUpdateRequest;
import com.travelnote.entity.Tag;
import com.travelnote.entity.Trip;
import com.travelnote.entity.User;
import com.travelnote.repository.TagRepository;
import com.travelnote.repository.TripRepository;
import com.travelnote.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TripService {
    
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    
    public TripService(TripRepository tripRepository,
                       UserRepository userRepository,
                       TagRepository tagRepository) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }
    
    public List<TripDTO> getUserTrips(Long userId) {
        List<Trip> trips = tripRepository.findByUserIdOrderByStartDateDesc(userId);
        return trips.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public TripDTO getTripById(Long tripId, Long userId) {
        Trip trip = tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new RuntimeException("旅程不存在"));
        return convertToDTO(trip);
    }
    
    public TripDTO getTripByShareToken(String shareToken) {
        Trip trip = tripRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new RuntimeException("分享链接无效或已过期"));
        
        if (!trip.getIsPublic() && trip.getShareToken() == null) {
            throw new RuntimeException("此旅程未公开分享");
        }
        
        return convertToDTO(trip);
    }
    
    @Transactional
    public TripDTO createTrip(Long userId, TripCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Trip trip = new Trip();
        trip.setUser(user);
        trip.setTitle(request.getTitle());
        trip.setDescription(request.getDescription());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setDestination(request.getDestination());
        trip.setCoverImage(request.getCoverImage());
        trip.setStatus(request.getStatus());
        trip.setIsPublic(request.getIsPublic());
        
        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            List<Tag> tags = getOrCreateTags(request.getTagNames());
            trip.setTags(tags);
        }
        
        Trip savedTrip = tripRepository.save(trip);
        return convertToDTO(savedTrip);
    }
    
    @Transactional
    public TripDTO updateTrip(Long tripId, Long userId, TripUpdateRequest request) {
        Trip trip = tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new RuntimeException("旅程不存在"));
        
        if (request.getTitle() != null) trip.setTitle(request.getTitle());
        if (request.getDescription() != null) trip.setDescription(request.getDescription());
        if (request.getStartDate() != null) trip.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) trip.setEndDate(request.getEndDate());
        if (request.getDestination() != null) trip.setDestination(request.getDestination());
        if (request.getCoverImage() != null) trip.setCoverImage(request.getCoverImage());
        if (request.getStatus() != null) trip.setStatus(request.getStatus());
        if (request.getIsPublic() != null) trip.setIsPublic(request.getIsPublic());
        
        if (request.getTagNames() != null) {
            List<Tag> tags = getOrCreateTags(request.getTagNames());
            trip.setTags(tags);
        }
        
        Trip savedTrip = tripRepository.save(trip);
        return convertToDTO(savedTrip);
    }
    
    @Transactional
    public void deleteTrip(Long tripId, Long userId) {
        Trip trip = tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new RuntimeException("旅程不存在"));
        tripRepository.delete(trip);
    }
    
    @Transactional
    public String generateShareToken(Long tripId, Long userId) {
        Trip trip = tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new RuntimeException("旅程不存在"));
        
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        trip.setShareToken(token);
        trip.setIsPublic(true);
        
        tripRepository.save(trip);
        return token;
    }
    
    @Transactional
    public void revokeShareToken(Long tripId, Long userId) {
        Trip trip = tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new RuntimeException("旅程不存在"));
        
        trip.setShareToken(null);
        trip.setIsPublic(false);
        tripRepository.save(trip);
    }
    
    private List<Tag> getOrCreateTags(List<String> tagNames) {
        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            String trimmedName = tagName.trim();
            if (trimmedName.isEmpty()) continue;
            
            Optional<Tag> existingTag = tagRepository.findByName(trimmedName);
            if (existingTag.isPresent()) {
                tags.add(existingTag.get());
            } else {
                Tag newTag = new Tag();
                newTag.setName(trimmedName);
                tags.add(tagRepository.save(newTag));
            }
        }
        return tags;
    }
    
    private TripDTO convertToDTO(Trip trip) {
        TripDTO dto = new TripDTO();
        dto.setId(trip.getId());
        dto.setTitle(trip.getTitle());
        dto.setDescription(trip.getDescription());
        dto.setStartDate(trip.getStartDate());
        dto.setEndDate(trip.getEndDate());
        dto.setDestination(trip.getDestination());
        dto.setCoverImage(trip.getCoverImage());
        dto.setStatus(trip.getStatus());
        dto.setShareToken(trip.getShareToken());
        dto.setIsPublic(trip.getIsPublic());
        dto.setCreatedAt(trip.getCreatedAt());
        dto.setUpdatedAt(trip.getUpdatedAt());
        
        int journalCount = trip.getJournalEntries() != null ? trip.getJournalEntries().size() : 0;
        dto.setJournalEntryCount(journalCount);
        
        int photoCount = 0;
        if (trip.getJournalEntries() != null) {
            photoCount = trip.getJournalEntries().stream()
                    .mapToInt(je -> je.getPhotos() != null ? je.getPhotos().size() : 0)
                    .sum();
        }
        dto.setPhotoCount(photoCount);
        
        if (trip.getTags() != null) {
            List<TagDTO> tagDTOs = trip.getTags().stream()
                    .map(this::convertTagToDTO)
                    .collect(Collectors.toList());
            dto.setTags(tagDTOs);
        }
        
        return dto;
    }
    
    private TagDTO convertTagToDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setColor(tag.getColor());
        dto.setCreatedAt(tag.getCreatedAt());
        dto.setUsageCount(tag.getTrips() != null ? tag.getTrips().size() : 0);
        return dto;
    }
}
