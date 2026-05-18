package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.CourseSession;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseSessionRepository extends BaseRepository<CourseSession> {
    
    List<CourseSession> findByCourseId(Long courseId);
    
    @Query("SELECT cs FROM CourseSession cs WHERE cs.course.id = :courseId ORDER BY cs.sessionNumber ASC")
    List<CourseSession> findCourseSessionsOrdered(@Param("courseId") Long courseId);
    
    @Query("SELECT cs FROM CourseSession cs WHERE cs.startTime >= :startTime AND cs.startTime <= :endTime ORDER BY cs.startTime ASC")
    List<CourseSession> findSessionsByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT cs FROM CourseSession cs WHERE cs.attendanceMarked = false ORDER BY cs.startTime ASC")
    List<CourseSession> findUnmarkedAttendanceSessions();
    
    @Query("SELECT COUNT(cs) FROM CourseSession cs WHERE cs.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
}



