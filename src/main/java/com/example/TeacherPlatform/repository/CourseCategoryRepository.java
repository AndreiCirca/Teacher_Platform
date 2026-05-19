package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.CourseCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseCategoryRepository extends BaseRepository<CourseCategory> {

    List<CourseCategory> findByActiveTrue();

    List<CourseCategory> findByNameContainingIgnoreCase(String name);

    @Query("SELECT COUNT(c) > 0 FROM Course c WHERE c.category.id = :categoryId")
    boolean hasCourses(@Param("categoryId") Long categoryId);
}