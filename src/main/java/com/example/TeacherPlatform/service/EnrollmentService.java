package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.EnrollmentRequest;
import com.example.TeacherPlatform.dataTransferObject.EnrollmentResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.repository.EnrollmentRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    // -------------------------------------------------------------------------
    // Mapări
    // -------------------------------------------------------------------------

    @Override
    protected Enrollment toEntity(EnrollmentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (enrollmentRepository.findByCourseIdAndTeacherId(course.getId(), teacher.getId()).isPresent()) {
            throw new RuntimeException("Already enrolled in this course");
        }
        if (course.getCurrentEnrolled() >= course.getMaxParticipants()) {
            throw new RuntimeException("Course is full");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setTeacher(teacher);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.PENDING);
        enrollment.setCertificateGenerated(false);
        return enrollment;
    }

    @Override
    protected EnrollmentResponse toResponse(Enrollment entity) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(entity.getId());
        response.setCourseId(entity.getCourse().getId());
        response.setCourseTitle(entity.getCourse().getTitle());
        response.setTeacherId(entity.getTeacher().getId());
        response.setTeacherFirstName(entity.getTeacher().getFirstName());
        response.setTeacherLastName(entity.getTeacher().getLastName());
        response.setStatus(entity.getStatus());
        response.setCertificateGenerated(entity.getCertificateGenerated());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    @Override
    protected void updateEntity(Enrollment entity, EnrollmentRequest request) {
        // Enrollment-urile nu se modifică prin PUT generic
    }

    // -------------------------------------------------------------------------
    // PROFESOR
    // -------------------------------------------------------------------------

    public List<EnrollmentResponse> getMyEnrollments() {
        User teacher = getCurrentUser();
        return enrollmentRepository.findByTeacherId(teacher.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<EnrollmentResponse> getMyActiveEnrollments() {
        User teacher = getCurrentUser();
        return enrollmentRepository.findConfirmedEnrollmentsByTeacher(teacher.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<EnrollmentResponse> getMyCompletedEnrollments() {
        User teacher = getCurrentUser();
        return enrollmentRepository.findByTeacherAndStatus(teacher.getId(), EnrollmentStatus.COMPLETED)
                .stream().map(this::toResponse).toList();
    }

    public boolean checkEnrollment(Long courseId) {
        User teacher = getCurrentUser();
        return enrollmentRepository.findByCourseIdAndTeacherId(courseId, teacher.getId()).isPresent();
    }

    @Transactional
    public void cancelEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        User teacher = getCurrentUser();
        if (!enrollment.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Not authorized to cancel this enrollment");
        }

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }

    // -------------------------------------------------------------------------
    // FORMATOR / ADMIN
    // -------------------------------------------------------------------------

    public List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId)
                .stream().map(this::toResponse).toList();
    }

    public List<EnrollmentResponse> getConfirmedEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findConfirmedEnrollmentsByCourse(courseId)
                .stream().map(this::toResponse).toList();
    }

    public List<EnrollmentResponse> getPendingEnrollments() {
        return enrollmentRepository.findPendingEnrollments()
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public EnrollmentResponse confirmEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new RuntimeException("Only PENDING enrollments can be confirmed");
        }

        enrollment.setStatus(EnrollmentStatus.CONFIRMED);

        Course course = enrollment.getCourse();
        course.setCurrentEnrolled(course.getCurrentEnrolled() + 1);

        enrollmentRepository.save(enrollment);
        return toResponse(enrollment);
    }

    @Transactional
    public EnrollmentResponse completeEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        if (enrollment.getStatus() != EnrollmentStatus.CONFIRMED) {
            throw new RuntimeException("Only CONFIRMED enrollments can be completed");
        }

        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollmentRepository.save(enrollment);
        return toResponse(enrollment);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}