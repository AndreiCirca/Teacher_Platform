package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseMaterialRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseMaterialResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.CourseMaterial;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseMaterialRepository;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.repository.EnrollmentRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseMaterialService extends GenericService<CourseMaterial, CourseMaterialRequest, CourseMaterialResponse> {

    private final CourseMaterialRepository courseMaterialRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Override
    protected BaseRepository<CourseMaterial> getRepository() {
        return courseMaterialRepository;
    }

    @Override
    protected CourseMaterial toEntity(CourseMaterialRequest request) {
        CourseMaterial material = new CourseMaterial();
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

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
            throw new ResourceNotFoundException("Course not found");
        }

        String username = authentication.getName();
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(auth -> "PROFESOR".equals(auth.getAuthority()));

        if (isTeacher) {
            boolean hasAccess = enrollmentRepository.hasConfirmedOrCompletedEnrollment(courseId, username);
            if (!hasAccess) {
                throw new RuntimeException("Acces interzis. Trebuie să ai o înscriere confirmată.");
            }
        }

        return courseMaterialRepository.findCourseMaterialsOrdered(courseId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public CourseMaterialResponse incrementDownloadCount(Long id, Authentication authentication) {
        CourseMaterial material = courseMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));

        String username = authentication.getName();
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(auth -> "PROFESOR".equals(auth.getAuthority()));

        if (isTeacher && material.getCourse() != null) {
            boolean hasAccess = enrollmentRepository.hasConfirmedOrCompletedEnrollment(material.getCourse().getId(), username);
            if (!hasAccess) {
                throw new RuntimeException("Acces interzis.");
            }
        }

        material.setDownloadCount(material.getDownloadCount() + 1);
        return toResponse(courseMaterialRepository.save(material));
    }

    @Transactional(readOnly = true)
    public Map<String, List<CourseMaterialResponse>> findMyGroupedMaterials(Authentication authentication) {
        Long teacherId = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")).getId();

        List<Enrollment> myActiveEnrollments = enrollmentRepository.findConfirmedEnrollmentsByTeacher(teacherId);

        Map<String, List<CourseMaterialResponse>> groupedMaterials = new HashMap<>();

        for (Enrollment e : myActiveEnrollments) {
            List<CourseMaterialResponse> materialsForCourse = courseMaterialRepository.findCourseMaterialsOrdered(e.getCourse().getId())
                    .stream().map(this::toResponse).toList();
            if (!materialsForCourse.isEmpty()) {
                groupedMaterials.put(e.getCourse().getTitle(), materialsForCourse);
            }
        }
        return groupedMaterials;
    }
}