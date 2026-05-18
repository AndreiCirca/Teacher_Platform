package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.EnrollmentRequest;
import com.example.TeacherPlatform.dataTransferObject.EnrollmentResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.repository.EnrollmentRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnrollmentService extends GenericService<Enrollment, EnrollmentRequest, EnrollmentResponse> {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    protected BaseRepository<Enrollment> getRepository() {
        return enrollmentRepository;
    }

    @Override
    protected Enrollment toEntity(EnrollmentRequest request) {
        Enrollment enrollment = new Enrollment();
        mapFields(enrollment, request);
        return enrollment;
    }

    @Override
    protected EnrollmentResponse toResponse(Enrollment entity) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(entity.getId());

        if (entity.getCourse() != null) {
            response.setCourseId(entity.getCourse().getId());
            response.setCourseTitle(entity.getCourse().getTitle());
        }

        if (entity.getTeacher() != null) {
            response.setTeacherId(entity.getTeacher().getId());
            response.setTeacherFullName(entity.getTeacher().getFullName());
        }

        response.setStatus(entity.getStatus());
        response.setCertificateGenerated(entity.getCertificateGenerated());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(Enrollment entity, EnrollmentRequest request) {
        mapFields(entity, request);
    }

    private void mapFields(Enrollment entity, EnrollmentRequest request) {
        entity.setStatus(request.getStatus());
        entity.setCertificateGenerated(request.getCertificateGenerated());

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));
        entity.setCourse(course);

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("User/Teacher not found with id: " + request.getTeacherId()));
        entity.setTeacher(teacher);
    }
}