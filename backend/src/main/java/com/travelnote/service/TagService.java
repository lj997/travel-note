package com.travelnote.service;

import com.travelnote.dto.TagDTO;
import com.travelnote.entity.Tag;
import com.travelnote.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {
    
    private final TagRepository tagRepository;
    
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
    
    public List<TagDTO> getAllTags() {
        List<Tag> tags = tagRepository.findAllOrderByUsageCount();
        return tags.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public List<TagDTO> searchTags(String keyword) {
        List<Tag> tags = tagRepository.searchByName(keyword);
        return tags.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public TagDTO getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        return convertToDTO(tag);
    }
    
    @Transactional
    public TagDTO createTag(String name, String color) {
        if (tagRepository.existsByName(name.trim())) {
            throw new RuntimeException("标签名称已存在");
        }
        
        Tag tag = new Tag();
        tag.setName(name.trim());
        if (color != null && !color.isEmpty()) {
            tag.setColor(color);
        }
        
        Tag savedTag = tagRepository.save(tag);
        return convertToDTO(savedTag);
    }
    
    @Transactional
    public TagDTO updateTag(Long id, String name, String color) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        if (name != null && !name.trim().isEmpty()) {
            String trimmedName = name.trim();
            if (!trimmedName.equals(tag.getName()) && tagRepository.existsByName(trimmedName)) {
                throw new RuntimeException("标签名称已存在");
            }
            tag.setName(trimmedName);
        }
        
        if (color != null) {
            tag.setColor(color);
        }
        
        Tag savedTag = tagRepository.save(tag);
        return convertToDTO(savedTag);
    }
    
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        if (tag.getTrips() != null && !tag.getTrips().isEmpty()) {
            throw new RuntimeException("标签已被使用，无法删除");
        }
        
        tagRepository.delete(tag);
    }
    
    private TagDTO convertToDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setColor(tag.getColor());
        dto.setCreatedAt(tag.getCreatedAt());
        dto.setUsageCount(tag.getTrips() != null ? tag.getTrips().size() : 0);
        return dto;
    }
}
