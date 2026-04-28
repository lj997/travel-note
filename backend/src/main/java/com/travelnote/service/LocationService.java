package com.travelnote.service;

import com.travelnote.dto.LocationCreateRequest;
import com.travelnote.dto.LocationDTO;
import com.travelnote.entity.Location;
import com.travelnote.entity.Trip;
import com.travelnote.entity.User;
import com.travelnote.repository.LocationRepository;
import com.travelnote.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {
    
    private final LocationRepository locationRepository;
    private final TripRepository tripRepository;
    
    public LocationService(LocationRepository locationRepository, TripRepository tripRepository) {
        this.locationRepository = locationRepository;
        this.tripRepository = tripRepository;
    }
    
    public List<LocationDTO> getLocationsByTrip(Long tripId, Long userId) {
        Trip trip = tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new RuntimeException("旅程不存在"));
        
        List<Location> locations = locationRepository.findByTripIdOrderByVisitDateAndDisplayOrder(tripId);
        return locations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public LocationDTO getLocationById(Long locationId, Long userId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("地点不存在"));
        
        if (!location.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权访问此地点");
        }
        
        return convertToDTO(location);
    }
    
    @Transactional
    public LocationDTO createLocation(Long tripId, Long userId, LocationCreateRequest request) {
        Trip trip = tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new RuntimeException("旅程不存在"));
        
        User user = trip.getUser();
        
        Location location = new Location();
        location.setTrip(trip);
        location.setUser(user);
        location.setName(request.getName());
        location.setDescription(request.getDescription());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setVisitDate(request.getVisitDate());
        location.setPhotoUrl(request.getPhotoUrl());
        
        if (request.getDisplayOrder() != null) {
            location.setDisplayOrder(request.getDisplayOrder());
        } else {
            Integer maxOrder = locationRepository.findMaxDisplayOrderByTripId(tripId).orElse(0);
            location.setDisplayOrder(maxOrder + 1);
        }
        
        Location savedLocation = locationRepository.save(location);
        return convertToDTO(savedLocation);
    }
    
    @Transactional
    public LocationDTO updateLocation(Long locationId, Long userId, LocationCreateRequest request) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("地点不存在"));
        
        if (!location.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权修改此地点");
        }
        
        if (request.getName() != null) location.setName(request.getName());
        if (request.getDescription() != null) location.setDescription(request.getDescription());
        if (request.getLatitude() != null) location.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) location.setLongitude(request.getLongitude());
        if (request.getVisitDate() != null) location.setVisitDate(request.getVisitDate());
        if (request.getDisplayOrder() != null) location.setDisplayOrder(request.getDisplayOrder());
        if (request.getPhotoUrl() != null) location.setPhotoUrl(request.getPhotoUrl());
        
        Location savedLocation = locationRepository.save(location);
        return convertToDTO(savedLocation);
    }
    
    @Transactional
    public void deleteLocation(Long locationId, Long userId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("地点不存在"));
        
        if (!location.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权删除此地点");
        }
        
        locationRepository.delete(location);
    }
    
    @Transactional
    public List<LocationDTO> reorderLocations(Long tripId, Long userId, List<Long> locationIds) {
        Trip trip = tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new RuntimeException("旅程不存在"));
        
        for (int i = 0; i < locationIds.size(); i++) {
            Long locationId = locationIds.get(i);
            Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new RuntimeException("地点不存在: " + locationId));
            
            if (!location.getTrip().getId().equals(tripId)) {
                throw new RuntimeException("地点不属于此旅程: " + locationId);
            }
            
            location.setDisplayOrder(i);
            locationRepository.save(location);
        }
        
        return getLocationsByTrip(tripId, userId);
    }
    
    private LocationDTO convertToDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setTripId(location.getTrip().getId());
        dto.setName(location.getName());
        dto.setDescription(location.getDescription());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setVisitDate(location.getVisitDate());
        dto.setDisplayOrder(location.getDisplayOrder());
        dto.setPhotoUrl(location.getPhotoUrl());
        dto.setCreatedAt(location.getCreatedAt());
        return dto;
    }
}
