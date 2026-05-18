package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.CourseCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseCategoryRepository extends BaseRepository<CourseCategory> {
    
    Optional<CourseCategory> findByName(String name);
    
    @Query("SELECT c FROM CourseCategory c WHERE c.active = true ORDER BY c.name ASC")
    List<CourseCategory> findAllActive();
    
    @Query("SELECT COUNT(c) FROM CourseCategory c WHERE c.active = true")
    Long countActive();
}



