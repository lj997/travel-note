package com.travelnote.service;

import com.travelnote.dto.JournalEntryCreateRequest;
import com.travelnote.dto.JournalEntryDTO;
import com.travelnote.dto.PhotoDTO;
import com.travelnote.entity.JournalEntry;
import com.travelnote.entity.Photo;
import com.travelnote.entity.Trip;
import com.travelnote.entity.User;
import com.travelnote.repository.JournalEntryRepository;
import com.travelnote.repository.PhotoRepository;
import com.travelnote.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JournalEntryService {
    
    private final JournalEntryRepository journalEntryRepository;
    private final TripRepository tripRepository;
    private final PhotoRepository photoRepository;
    
    public JournalEntryService(JournalEntryRepository journalEntryRepository,
                               TripRepository tripRepository,
                               PhotoRepository photoRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.tripRepository = tripRepository;
        this.photoRepository = photoRepository;
    }
    
    public List<JournalEntryDTO> getJournalEntriesByTrip(Long tripId, Long userId) {
        Trip trip = tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new RuntimeException("旅程不存在"));
        
        List<JournalEntry> entries = journalEntryRepository.findByTripIdOrderByDateAsc(tripId);
        return entries.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public JournalEntryDTO getJournalEntryById(Long entryId, Long userId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("日记不存在"));
        
        if (!entry.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权访问此日记");
        }
        
        return convertToDTO(entry);
    }
    
    @Transactional
    public JournalEntryDTO createJournalEntry(Long tripId, Long userId, JournalEntryCreateRequest request) {
        Trip trip = tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new RuntimeException("旅程不存在"));
        
        User user = trip.getUser();
        
        JournalEntry entry = new JournalEntry();
        entry.setTrip(trip);
        entry.setUser(user);
        entry.setDate(request.getDate());
        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        entry.setLocationName(request.getLocationName());
        entry.setLatitude(request.getLatitude());
        entry.setLongitude(request.getLongitude());
        
        if (request.getPhotoIds() != null && !request.getPhotoIds().isEmpty()) {
            List<Photo> photos = new ArrayList<>();
            for (int i = 0; i < request.getPhotoIds().size(); i++) {
                Long photoId = request.getPhotoIds().get(i);
                Photo photo = photoRepository.findById(photoId)
                        .orElseThrow(() -> new RuntimeException("照片不存在: " + photoId));
                
                if (!photo.getUser().getId().equals(userId)) {
                    throw new RuntimeException("无权使用此照片: " + photoId);
                }
                
                photo.setJournalEntry(entry);
                photo.setDisplayOrder(i);
                photos.add(photo);
            }
            entry.setPhotos(photos);
        }
        
        JournalEntry savedEntry = journalEntryRepository.save(entry);
        return convertToDTO(savedEntry);
    }
    
    @Transactional
    public JournalEntryDTO updateJournalEntry(Long entryId, Long userId, JournalEntryCreateRequest request) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("日记不存在"));
        
        if (!entry.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权修改此日记");
        }
        
        if (request.getDate() != null) entry.setDate(request.getDate());
        if (request.getTitle() != null) entry.setTitle(request.getTitle());
        if (request.getContent() != null) entry.setContent(request.getContent());
        if (request.getLocationName() != null) entry.setLocationName(request.getLocationName());
        if (request.getLatitude() != null) entry.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) entry.setLongitude(request.getLongitude());
        
        if (request.getPhotoIds() != null) {
            if (entry.getPhotos() != null) {
                for (Photo photo : entry.getPhotos()) {
                    photo.setJournalEntry(null);
                    photo.setDisplayOrder(0);
                }
            }
            
            List<Photo> photos = new ArrayList<>();
            for (int i = 0; i < request.getPhotoIds().size(); i++) {
                Long photoId = request.getPhotoIds().get(i);
                Photo photo = photoRepository.findById(photoId)
                        .orElseThrow(() -> new RuntimeException("照片不存在: " + photoId));
                
                if (!photo.getUser().getId().equals(userId)) {
                    throw new RuntimeException("无权使用此照片: " + photoId);
                }
                
                photo.setJournalEntry(entry);
                photo.setDisplayOrder(i);
                photos.add(photo);
            }
            entry.setPhotos(photos);
        }
        
        JournalEntry savedEntry = journalEntryRepository.save(entry);
        return convertToDTO(savedEntry);
    }
    
    @Transactional
    public void deleteJournalEntry(Long entryId, Long userId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("日记不存在"));
        
        if (!entry.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权删除此日记");
        }
        
        journalEntryRepository.delete(entry);
    }
    
    private JournalEntryDTO convertToDTO(JournalEntry entry) {
        JournalEntryDTO dto = new JournalEntryDTO();
        dto.setId(entry.getId());
        dto.setTripId(entry.getTrip().getId());
        dto.setDate(entry.getDate());
        dto.setTitle(entry.getTitle());
        dto.setContent(entry.getContent());
        dto.setLocationName(entry.getLocationName());
        dto.setLatitude(entry.getLatitude());
        dto.setLongitude(entry.getLongitude());
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setUpdatedAt(entry.getUpdatedAt());
        
        if (entry.getPhotos() != null) {
            List<PhotoDTO> photoDTOs = entry.getPhotos().stream()
                    .map(this::convertPhotoToDTO)
                    .collect(Collectors.toList());
            dto.setPhotos(photoDTOs);
        }
        
        return dto;
    }
    
    private PhotoDTO convertPhotoToDTO(Photo photo) {
        PhotoDTO dto = new PhotoDTO();
        dto.setId(photo.getId());
        dto.setUrl(photo.getUrl());
        dto.setThumbnailUrl(photo.getThumbnailUrl());
        dto.setOriginalName(photo.getOriginalName());
        dto.setContentType(photo.getContentType());
        dto.setFileSize(photo.getFileSize());
        dto.setWidth(photo.getWidth());
        dto.setHeight(photo.getHeight());
        dto.setCaption(photo.getCaption());
        dto.setDisplayOrder(photo.getDisplayOrder());
        dto.setTakenAt(photo.getTakenAt());
        dto.setUploadedAt(photo.getUploadedAt());
        return dto;
    }
}
