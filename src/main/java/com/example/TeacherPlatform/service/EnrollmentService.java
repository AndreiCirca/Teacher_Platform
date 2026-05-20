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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

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
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.PENDING);
        enrollment.setCertificateGenerated(false);
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
            response.setTeacherFirstName(entity.getTeacher().getFirstName());
            response.setTeacherLastName(entity.getTeacher().getLastName());
        }

        response.setStatus(entity.getStatus());
        response.setCertificateGenerated(entity.getCertificateGenerated());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    @Override
    protected void updateEntity(Enrollment entity, EnrollmentRequest request) {}

    // ----------------------------------------------------------------------------------
    // Funcționalități PROFESOR
    // ----------------------------------------------------------------------------------

    @Transactional
    public EnrollmentResponse createEnrollment(EnrollmentRequest request, Authentication authentication) {
        User teacher = getUserByEmail(authentication.getName());
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        if (enrollmentRepository.findByCourseIdAndTeacherId(course.getId(), teacher.getId()).isPresent()) {
            throw new RuntimeException("Ești deja înscris la acest curs.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setTeacher(teacher);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.PENDING);
        enrollment.setCertificateGenerated(false);

        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(Authentication authentication) {
        User teacher = getUserByEmail(authentication.getName());
        return enrollmentRepository.findByTeacherId(teacher.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyActiveEnrollments(Authentication authentication) {
        User teacher = getUserByEmail(authentication.getName());
        return enrollmentRepository.findConfirmedEnrollmentsByTeacher(teacher.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyCompletedEnrollments(Authentication authentication) {
        User teacher = getUserByEmail(authentication.getName());
        return enrollmentRepository.findByTeacherAndStatus(teacher.getId(), EnrollmentStatus.COMPLETED).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public boolean checkEnrollment(Long courseId, Authentication authentication) {
        User teacher = getUserByEmail(authentication.getName());
        return enrollmentRepository.findByCourseIdAndTeacherId(courseId, teacher.getId()).isPresent();
    }

    @Transactional
    public void cancelEnrollment(Long enrollmentId, Authentication authentication) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        User teacher = getUserByEmail(authentication.getName());
        if (!enrollment.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Access Denied.");
        }
        processCancellation(enrollment);
    }

    // ----------------------------------------------------------------------------------
    // Funcționalități FORMATOR
    // ----------------------------------------------------------------------------------

    @Transactional
    public void cancelEnrollmentAsTrainer(Long enrollmentId, Authentication authentication) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        User trainer = getUserByEmail(authentication.getName());
        if (!enrollment.getCourse().getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("Access Denied. You are not the trainer for this course.");
        }
        processCancellation(enrollment);
    }

    @Transactional
    public EnrollmentResponse confirmEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new RuntimeException("Doar înscrierile PENDING pot fi confirmate.");
        }

        Course course = enrollment.getCourse();

        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        course.setCurrentEnrolled(course.getCurrentEnrolled() + 1);
        courseRepository.save(course);

        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCourseEnrollmentStats(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        long total = enrollments.size();
        long confirmed = enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.CONFIRMED).count();
        long pending = enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.PENDING).count();
        long cancelled = enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.CANCELLED).count();

        return Map.of("total", total, "confirmed", confirmed, "pending", pending, "cancelled", cancelled);
    }

    // ----------------------------------------------------------------------------------
    // Funcționalități ADMIN
    // ----------------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsThisMonth() {
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        return enrollmentRepository.findAll().stream()
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().isAfter(startOfMonth))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EnrollmentResponse completeEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        if (enrollment.getStatus() != EnrollmentStatus.CONFIRMED) {
            throw new RuntimeException("Doar înscrierile CONFIRMED pot fi finalizate.");
        }

        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getPendingEnrollments() {
        return enrollmentRepository.findPendingEnrollments().stream().map(this::toResponse).toList();
    }

    // ----------------------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------------------

    private void processCancellation(Enrollment enrollment) {
        if (enrollment.getStatus() == EnrollmentStatus.CONFIRMED) {
            Course course = enrollment.getCourse();
            if (course.getCurrentEnrolled() > 0) {
                course.setCurrentEnrolled(course.getCurrentEnrolled() - 1);
                courseRepository.save(course);
            }
        }
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}