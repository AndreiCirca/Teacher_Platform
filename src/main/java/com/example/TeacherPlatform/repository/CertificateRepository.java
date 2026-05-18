package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.Certificate;
import com.example.TeacherPlatform.model.enums.CertificateStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends BaseRepository<Certificate> {
    
    Optional<Certificate> findByCertificateCode(String certificateCode);
    
    List<Certificate> findByStatus(CertificateStatus status);
    
    @Query("SELECT c FROM Certificate c WHERE c.enrollment.teacher.id = :teacherId ORDER BY c.issuedDate DESC")
    List<Certificate> findCertificatesByTeacher(@Param("teacherId") Long teacherId);
    
    @Query("SELECT c FROM Certificate c WHERE c.enrollment.course.id = :courseId")
    List<Certificate> findCertificatesByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.status = 'ACTIVE' AND c.enrollment.teacher.id = :teacherId")
    Long countActiveCertificatesByTeacher(@Param("teacherId") Long teacherId);
    
    @Query("SELECT c FROM Certificate c WHERE c.issuedDate >= :startDate AND c.issuedDate <= :endDate ORDER BY c.issuedDate DESC")
    List<Certificate> findCertificatesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT c FROM Certificate c WHERE c.status = 'PENDING' ORDER BY c.createdAt ASC")
    List<Certificate> findPendingCertificates();
    
    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.status = :status")
    Long countByStatus(@Param("status") CertificateStatus status);
}



