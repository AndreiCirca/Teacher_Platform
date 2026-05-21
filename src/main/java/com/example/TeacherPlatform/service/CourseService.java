package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.CourseStatus;
import com.example.TeacherPlatform.model.enums.NotificationType;
import com.example.TeacherPlatform.model.enums.UserRole;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService extends GenericService<Course, CourseRequest, CourseResponse> {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    protected BaseRepository<Course> getRepository() {
        return courseRepository;
    }

    @Override
    protected Course toEntity(CourseRequest request) {
        User trainer = userRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        if (trainer.getRole() != UserRole.FORMATOR) {
            throw new RuntimeException("User must have the FORMATOR role.");
        }

        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);

        Course course = new Course();
        mapFields(course, request);
        course.setTrainer(trainer);
        course.setCurrentEnrolled(0);
        course.setSessionCount(0);
        course.setStatus(request.getStatus() != null ? request.getStatus() : CourseStatus.DRAFT);
        return course;
    }

    @Override
    protected CourseResponse toResponse(Course entity) {
        CourseResponse response = new CourseResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        if (entity.getTrainer() != null) {
            response.setTrainerId(entity.getTrainer().getId());
            response.setTrainerFirstName(entity.getTrainer().getFirstName());
            response.setTrainerLastName(entity.getTrainer().getLastName());
        }
        response.setStartDate(entity.getStartDate());
        response.setEndDate(entity.getEndDate());
        response.setCreditHours(entity.getCreditHours());
        response.setMaxParticipants(entity.getMaxParticipants());
        response.setCurrentEnrolled(entity.getCurrentEnrolled());
        response.setSessionCount(entity.getSessionCount());
        response.setIsOnline(entity.getIsOnline());
        response.setLocation(entity.getLocation());
        response.setMeetingLink(entity.getMeetingLink());
        response.setStatus(entity.getStatus());
        response.setThumbnailUrl(entity.getThumbnailUrl());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(Course entity, CourseRequest request) {
        if (entity.getStatus() == CourseStatus.COMPLETED || entity.getStatus() == CourseStatus.CANCELLED) {
            throw new RuntimeException("Cannot edit a COMPLETED or CANCELLED course.");
        }
        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);
        mapFields(entity, request);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findUpcomingCourses() {
        return courseRepository.findUpcomingCourses(LocalDate.now(), CourseStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findAvailableCourses() {
        return courseRepository.findAvailableCourses(CourseStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findPopularCourses() {
        return courseRepository.findByStatus(CourseStatus.ACTIVE)
                .stream()
                .sorted((c1, c2) -> Integer.compare(c2.getCurrentEnrolled(), c1.getCurrentEnrolled()))
                .limit(6)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findByProfesorEmail(String email) {
        return courseRepository.findByTrainerEmail(email)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findMyCoursesAsTrainer(Authentication authentication) {
        User trainer = getUserByEmail(authentication.getName());
        return courseRepository.findByTrainerId(trainer.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void cancelCourseAsTrainer(Long id, Authentication authentication) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        User trainer = getUserByEmail(authentication.getName());
        if (!course.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("Access Denied.");
        }
        course.setStatus(CourseStatus.CANCELLED);
        courseRepository.save(course);
    }

    @Transactional
    public void deleteOwnCourse(Long id, Authentication authentication) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        User trainer = getUserByEmail(authentication.getName());
        if (!course.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("Access Denied.");
        }
        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.CANCELLED) {
            throw new RuntimeException("Only DRAFT or CANCELLED courses can be deleted.");
        }
        courseRepository.delete(course);
    }

    @Transactional
    public CourseResponse approveCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setStatus(CourseStatus.ACTIVE);
        Course saved = courseRepository.save(course);

        notificationService.sendNotification(
                course.getTrainer().getId(),
                "Curs Aprobat!",
                "Cursul tău '" + course.getTitle() + "' a fost aprobat și este acum activ.",
                NotificationType.SUCCESS,
                "/courses/" + course.getId()
        );

        return toResponse(saved);
    }

    @Transactional
    public CourseResponse rejectCourse(Long id, String reason) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setStatus(CourseStatus.DRAFT);
        Course saved = courseRepository.save(course);

        notificationService.sendNotification(
                course.getTrainer().getId(),
                "Curs Respins",
                "Cursul '" + course.getTitle() + "' a fost respins. Motiv: " + (reason != null ? reason : "Necunoscut"),
                NotificationType.WARNING,
                "/courses/" + course.getId()
        );

        return toResponse(saved);
    }

    @Transactional
    public CourseResponse markAsCompleted(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setStatus(CourseStatus.COMPLETED);
        return toResponse(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findPendingApprovalCourses() {
        return courseRepository.findPendingApprovalCourses(CourseStatus.PENDING_APPROVAL)
                .stream().map(this::toResponse).toList();
    }

    private void mapFields(Course entity, CourseRequest request) {
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setCreditHours(request.getCreditHours());
        entity.setMaxParticipants(request.getMaxParticipants());
        entity.setIsOnline(request.getIsOnline());
        entity.setThumbnailUrl(request.getThumbnailUrl() != null ? request.getThumbnailUrl() : "");
        if (Boolean.TRUE.equals(request.getIsOnline())) {
            entity.setMeetingLink(request.getMeetingLink());
            entity.setLocation(null);
        } else {
            entity.setLocation(request.getLocation());
            entity.setMeetingLink(null);
        }
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new RuntimeException("The course end date must be set after the start date.");
        }
    }

    private void validateDeliveryFormat(CourseRequest request) {
        if (Boolean.TRUE.equals(request.getIsOnline()) &&
                (request.getMeetingLink() == null || request.getMeetingLink().isBlank())) {
            throw new RuntimeException("A valid meeting link is strictly required for online courses.");
        }
        if (Boolean.FALSE.equals(request.getIsOnline()) &&
                (request.getLocation() == null || request.getLocation().isBlank())) {
            throw new RuntimeException("A physical location address is strictly required for classroom courses.");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}