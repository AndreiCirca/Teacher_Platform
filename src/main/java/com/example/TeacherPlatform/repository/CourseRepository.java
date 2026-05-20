package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.enums.CourseStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CourseRepository extends BaseRepository<Course> {
    
    List<Course> findByTrainerId(Long trainerId);

    @Query("SELECT c FROM Course c WHERE c.startDate >= :startDate ORDER BY c.startDate ASC")
    List<Course> findUpcomingCourses(@Param("startDate") LocalDate startDate);

    @Query("SELECT c FROM Course c WHERE c.startDate <= :endDate AND c.endDate >= :startDate")
    List<Course> findCoursesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT c FROM Course c WHERE c.trainer.id = :trainerId ORDER BY c.startDate DESC")
    List<Course> findByTrainer(@Param("trainerId") Long trainerId);
}



