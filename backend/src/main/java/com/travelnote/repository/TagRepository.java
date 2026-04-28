package com.travelnote.repository;

import com.travelnote.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findByName(String name);
    
    Boolean existsByName(String name);
    
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Tag> searchByName(@Param("keyword") String keyword);
    
    @Query("SELECT t FROM Tag t ORDER BY SIZE(t.trips) DESC")
    List<Tag> findAllOrderByUsageCount();
    
    @Query("SELECT t FROM Tag t JOIN t.users u WHERE u.id = :userId")
    List<Tag> findFavoriteTagsByUserId(@Param("userId") Long userId);
}
