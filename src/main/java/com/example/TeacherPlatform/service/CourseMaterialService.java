package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseMaterialRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseMaterialResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.CourseMaterial;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseMaterialRepository;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.repository.EnrollmentRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseMaterialService extends GenericService<CourseMaterial, CourseMaterialRequest, CourseMaterialResponse> {

    private final CourseMaterialRepository courseMaterialRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    protected BaseRepository<CourseMaterial> getRepository() {
        return courseMaterialRepository;
    }

    @Override
    protected CourseMaterial toEntity(CourseMaterialRequest request) {
        CourseMaterial material = new CourseMaterial();
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        material.setCourse(course);
        material.setFileName(request.getFileName());
        material.setFileType(request.getFileType());
        material.setFileSize(request.getFileSize());
        material.setFileUrl(request.getFileUrl());
        material.setDescription(request.getDescription());
        material.setDownloadCount(request.getDownloadCount() != null ? request.getDownloadCount() : 0);
        return material;
    }

    @Override
    protected CourseMaterialResponse toResponse(CourseMaterial entity) {
        CourseMaterialResponse response = new CourseMaterialResponse();
        response.setId(entity.getId());

        if (entity.getCourse() != null) {
            response.setCourseId(entity.getCourse().getId());
            response.setCourseTitle(entity.getCourse().getTitle());
        }

        response.setFileName(entity.getFileName());
        response.setFileType(entity.getFileType());
        response.setFileSize(entity.getFileSize());
        response.setFileUrl(entity.getFileUrl());
        response.setDescription(entity.getDescription());
        response.setDownloadCount(entity.getDownloadCount());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(CourseMaterial entity, CourseMaterialRequest request) {
        entity.setFileName(request.getFileName());
        entity.setFileType(request.getFileType());
        entity.setFileSize(request.getFileSize());
        entity.setFileUrl(request.getFileUrl());
        entity.setDescription(request.getDescription());
    }

    @Transactional(readOnly = true)
    public List<CourseMaterialResponse> findByCourseId(Long courseId, Authentication authentication) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        String username = authentication.getName();
        boolean isTeacher = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "PROFESOR".equals(auth));

        // Securitate dinamică direct din baza de date bazată pe user-ul logat prin JWT
        if (isTeacher) {
            boolean hasAccess = enrollmentRepository.hasConfirmedOrCompletedEnrollment(courseId, username);
            if (!hasAccess) {
                throw new RuntimeException("Access denied. You must have a confirmed enrollment to view materials.");
            }
        }

        return courseMaterialRepository.findCourseMaterialsOrdered(courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CourseMaterialResponse incrementDownloadCount(Long id, Authentication authentication) {
        CourseMaterial material = courseMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));

        String username = authentication.getName();
        boolean isTeacher = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "PROFESOR".equals(auth));

        if (isTeacher && material.getCourse() != null) {
            boolean hasAccess = enrollmentRepository.hasConfirmedOrCompletedEnrollment(material.getCourse().getId(), username);
            if (!hasAccess) {
                throw new RuntimeException("Access denied. You cannot download materials from this course.");
            }
        }

        material.setDownloadCount(material.getDownloadCount() + 1);
        return toResponse(courseMaterialRepository.save(material));
    }
}