package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends BaseRepository<Enrollment> {
    
    List<Enrollment> findByTeacherId(Long teacherId);
    
    List<Enrollment> findByCourseId(Long courseId);
    
    List<Enrollment> findByStatus(EnrollmentStatus status);
    
    Optional<Enrollment> findByCourseIdAndTeacherId(Long courseId, Long teacherId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.teacher.id = :teacherId AND e.status = 'CONFIRMED' ORDER BY e.course.startDate ASC")
    List<Enrollment> findConfirmedEnrollmentsByTeacher(@Param("teacherId") Long teacherId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'CONFIRMED'")
    List<Enrollment> findConfirmedEnrollmentsByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'CONFIRMED'")
    Long countConfirmedEnrollmentsByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<Enrollment> findPendingEnrollments();
    
    @Query("SELECT e FROM Enrollment e WHERE e.teacher.id = :teacherId AND e.status = :status ORDER BY e.course.startDate DESC")
    List<Enrollment> findByTeacherAndStatus(@Param("teacherId") Long teacherId, @Param("status") EnrollmentStatus status);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
}



