package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.CourseMaterial;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseMaterialRepository extends BaseRepository<CourseMaterial> {

    List<CourseMaterial> findByCourseId(Long courseId);

    @Query("SELECT cm FROM CourseMaterial cm WHERE cm.course.id = :courseId ORDER BY cm.createdAt DESC")
    List<CourseMaterial> findCourseMaterialsOrdered(@Param("courseId") Long courseId);

    @Query("SELECT cm FROM CourseMaterial cm WHERE cm.fileType = :fileType")
    List<CourseMaterial> findByFileType(@Param("fileType") String fileType);

    @Query("SELECT COUNT(cm) FROM CourseMaterial cm WHERE cm.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
}