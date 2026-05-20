package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.CourseCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseCategoryRepository extends BaseRepository<CourseCategory> {

    List<CourseCategory> findByActiveTrue();

    // Folosită pentru căutări rapide de tip auto-complete în UI
    List<CourseCategory> findByNameContainingIgnoreCase(String name);

    // CRUCIAL: Adăugată pentru verificarea exactă a unicității numelui în Service
    Optional<CourseCategory> findByNameIgnoreCase(String name);

    @Query("SELECT COUNT(c) > 0 FROM Course c WHERE c.category.id = :categoryId")
    boolean hasCourses(@Param("categoryId") Long categoryId);
}