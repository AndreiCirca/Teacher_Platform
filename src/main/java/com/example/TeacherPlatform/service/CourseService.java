package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.CourseCategory;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.CourseStatus;
import com.example.TeacherPlatform.model.enums.NotificationType;
import com.example.TeacherPlatform.model.enums.UserRole;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseCategoryRepository;
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
    private final CourseCategoryRepository courseCategoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // Adăugat pentru notificări automate

    @Override
    protected BaseRepository<Course> getRepository() {
        return courseRepository;
    }

    @Override
    protected Course toEntity(CourseRequest request) {
        CourseCategory category = courseCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        User trainer = userRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        if (trainer.getRole() != UserRole.FORMATOR) {
            throw new RuntimeException("User must have the FORMATOR role.");
        }

        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);

        Course course = new Course();
        mapFields(course, request, category);
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

        if (entity.getCategory() != null) {
            response.setCategoryId(entity.getCategory().getId());
            response.setCategoryName(entity.getCategory().getName());
        }
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
        CourseCategory category = courseCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);
        mapFields(entity, request, category);
    }

    // ----------------------------------------------------------------------------------
    // Funcționalități Publice & Profesor
    // ----------------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<CourseResponse> findAvailableCourses() {
        return courseRepository.findAvailableCourses().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findUpcomingCourses() {
        return courseRepository.findUpcomingCourses(LocalDate.now()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findPopularCourses() {
        // Returnează top 6 cursuri ordonate după currentEnrolled (logica se poate adăuga în repository sau aici)
        return courseRepository.findByStatus(CourseStatus.ACTIVE)
                .stream()
                .sorted((c1, c2) -> Integer.compare(c2.getCurrentEnrolled(), c1.getCurrentEnrolled()))
                .limit(6)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findByCategoryId(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId).stream().map(this::toResponse).toList();
    }

    // ----------------------------------------------------------------------------------
    // Funcționalități FORMATOR
    // ----------------------------------------------------------------------------------

    @Transactional
    public CourseResponse proposeCourse(CourseRequest request, Authentication authentication) {
        User trainer = getUserByEmail(authentication.getName());

        CourseCategory category = courseCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);

        Course course = new Course();
        mapFields(course, request, category);
        course.setTrainer(trainer);
        course.setCurrentEnrolled(0);
        course.setSessionCount(0);
        // Formatorul trimite automat cursul spre aprobare
        course.setStatus(CourseStatus.PENDING_APPROVAL);

        Course savedCourse = courseRepository.save(course);

        // Notificăm adminii (presupunând că găsim adminii, aici poți extinde logica)
        // notificationService.sendNotificationToAdmins(...)

        return toResponse(savedCourse);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findMyCoursesAsTrainer(Authentication authentication) {
        User trainer = getUserByEmail(authentication.getName());
        return courseRepository.findByTrainerId(trainer.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public CourseResponse updateCourseAsTrainer(Long id, CourseRequest request, Authentication authentication) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        User trainer = getUserByEmail(authentication.getName());
        if (!course.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("Access Denied. You can only edit your own courses.");
        }

        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.PENDING_APPROVAL) {
            throw new RuntimeException("You can only edit courses that are in DRAFT or PENDING_APPROVAL status.");
        }

        CourseCategory category = courseCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);
        mapFields(course, request, category);

        course.setStatus(CourseStatus.PENDING_APPROVAL); // Odată editat, reintră în aprobare

        return toResponse(courseRepository.save(course));
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

        // Specificații: La anulare se trimit notificări participanților
        // Vei putea apela un notificationService.sendToAllEnrolled(...) aici
    }

    // ----------------------------------------------------------------------------------
    // Funcționalități ADMIN
    // ----------------------------------------------------------------------------------

    @Transactional
    public CourseResponse approveCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setStatus(CourseStatus.ACTIVE);
        Course savedCourse = courseRepository.save(course);

        notificationService.sendNotification(
                course.getTrainer().getId(),
                "Curs Aprobat!",
                "Cursul tău '" + course.getTitle() + "' a fost aprobat și este acum activ.",
                NotificationType.SUCCESS,
                "/courses/" + course.getId()
        );

        return toResponse(savedCourse);
    }

    @Transactional
    public CourseResponse rejectCourse(Long id, String reason) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setStatus(CourseStatus.DRAFT);
        Course savedCourse = courseRepository.save(course);

        notificationService.sendNotification(
                course.getTrainer().getId(),
                "Curs Respins",
                "Cursul '" + course.getTitle() + "' a fost respins. Motiv: " + (reason != null ? reason : "Necunoscut"),
                NotificationType.WARNING,
                "/courses/" + course.getId()
        );

        return toResponse(savedCourse);
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
        return courseRepository.findPendingApprovalCourses().stream().map(this::toResponse).toList();
    }

    // ----------------------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------------------

    private void mapFields(Course entity, CourseRequest request, CourseCategory category) {
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setCategory(category);
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setCreditHours(request.getCreditHours());
        entity.setMaxParticipants(request.getMaxParticipants());
        entity.setIsOnline(request.getIsOnline());
        entity.setThumbnailUrl(request.getThumbnailUrl());

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
        if (Boolean.TRUE.equals(request.getIsOnline()) && (request.getMeetingLink() == null || request.getMeetingLink().isBlank())) {
            throw new RuntimeException("A valid meeting link is strictly required for online courses.");
        }
        if (Boolean.FALSE.equals(request.getIsOnline()) && (request.getLocation() == null || request.getLocation().isBlank())) {
            throw new RuntimeException("A physical location address is strictly required for classroom courses.");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}