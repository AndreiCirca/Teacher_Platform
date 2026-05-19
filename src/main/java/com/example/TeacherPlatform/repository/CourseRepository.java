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
    
    List<Course> findByStatus(CourseStatus status);
    
    List<Course> findByTrainerId(Long trainerId);
    
    List<Course> findByCategoryId(Long categoryId);
    
    @Query("SELECT c FROM Course c WHERE c.isOnline = true AND c.status = :status ORDER BY c.startDate ASC")
    List<Course> findOnlineCourses(@Param("status") CourseStatus status);

    @Query("SELECT c FROM Course c WHERE c.startDate >= :startDate AND c.status = :status ORDER BY c.startDate ASC")
    List<Course> findUpcomingCourses(@Param("startDate") LocalDate startDate, @Param("status") CourseStatus status);

    @Query("SELECT c FROM Course c WHERE c.startDate <= :endDate AND c.endDate >= :startDate AND c.status = :status")
    List<Course> findCoursesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("status") CourseStatus status);

    @Query("SELECT c FROM Course c WHERE c.status = :status ORDER BY c.createdAt ASC")
    List<Course> findPendingApprovalCourses(@Param("status") CourseStatus status);

    @Query("SELECT c FROM Course c WHERE c.currentEnrolled < c.maxParticipants AND c.status = :status ORDER BY c.startDate ASC")
    List<Course> findAvailableCourses(@Param("status") CourseStatus status);

    @Query("SELECT c FROM Course c WHERE c.trainer.id = :trainerId AND c.status = :status ORDER BY c.startDate DESC")
    List<Course> findByTrainerAndStatus(@Param("trainerId") Long trainerId, @Param("status") CourseStatus status);
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.status = :status")
    Long countByStatus(@Param("status") CourseStatus status);
}



