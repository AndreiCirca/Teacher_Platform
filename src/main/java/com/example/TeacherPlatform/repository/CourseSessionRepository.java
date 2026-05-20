package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.CourseSession;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseSessionRepository extends BaseRepository<CourseSession> {

    @Query("SELECT s FROM CourseSession s WHERE s.course.id = :courseId ORDER BY s.sessionNumber ASC")
    List<CourseSession> findByCourseIdOrdered(@Param("courseId") Long courseId);

    @Query("SELECT s FROM CourseSession s WHERE s.startTime >= :from AND s.endTime <= :to ORDER BY s.startTime ASC")
    List<CourseSession> findSessionsByTimeRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT s FROM CourseSession s WHERE s.attendanceMarked = false ORDER BY s.startTime ASC")
    List<CourseSession> findUnmarkedAttendanceSessions();
}