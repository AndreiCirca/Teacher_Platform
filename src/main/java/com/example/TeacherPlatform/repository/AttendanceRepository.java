package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.Attendance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends BaseRepository<Attendance> {

    @Query("SELECT a FROM Attendance a WHERE a.session.id = :sessionId")
    List<Attendance> findBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT a FROM Attendance a WHERE a.enrollment.id = :enrollmentId ORDER BY a.session.sessionNumber ASC")
    List<Attendance> findAttendanceByEnrollmentOrdered(@Param("enrollmentId") Long enrollmentId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.enrollment.id = :enrollmentId AND a.status = 'PRESENT'")
    Long countPresentSessionsByEnrollment(@Param("enrollmentId") Long enrollmentId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.session.id = :sessionId AND a.status = 'PRESENT'")
    Long countPresentTeachersInSession(@Param("sessionId") Long sessionId);

    // ADĂUGATĂ: Pentru statistici
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.session.id = :sessionId AND a.status = 'ABSENT'")
    Long countAbsentTeachersInSession(@Param("sessionId") Long sessionId);

    Optional<Attendance> findBySessionIdAndEnrollmentId(Long sessionId, Long enrollmentId);
}