package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.CourseCategory;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.CourseStatus;
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

@Service
@RequiredArgsConstructor
public class CourseService extends GenericService<Course, CourseRequest, CourseResponse> {

    private final CourseRepository courseRepository;
    private final CourseCategoryRepository courseCategoryRepository;
    private final UserRepository userRepository;

    @Override
    protected BaseRepository<Course> getRepository() {
        return courseRepository;
    }

    @Override
    protected Course toEntity(CourseRequest request) {
        CourseCategory category = courseCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        User trainer = userRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + request.getTrainerId()));

        if (trainer.getRole() != UserRole.FORMATOR) {
            throw new RuntimeException("User with id " + request.getTrainerId() + " is not a FORMATOR");
        }

        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);

        Course course = new Course();
        mapFields(course, request, category);
        course.setTrainer(trainer);
        course.setCurrentEnrolled(0);
        course.setSessionCount(0); // Sincronizat cu CourseSessionService
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
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);
        mapFields(entity, request, category);
    }

    @Transactional
    public CourseResponse createCourseSecure(CourseRequest request, Authentication authentication) {
        User currentUser = getUserByEmail(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        User trainer = currentUser;

        if (isAdmin) {
            trainer = userRepository.findById(request.getTrainerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
            if (trainer.getRole() != UserRole.FORMATOR) {
                throw new RuntimeException("The selected trainer must have the FORMATOR role.");
            }
        }

        CourseCategory category = courseCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);

        Course course = new Course();
        mapFields(course, request, category);
        course.setTrainer(trainer);
        course.setCurrentEnrolled(0);
        course.setSessionCount(0); // Sincronizat cu CourseSessionService
        course.setStatus(isAdmin ? request.getStatus() : CourseStatus.DRAFT);

        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourseSecure(Long id, CourseRequest request, Authentication authentication) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        if (course.getStatus() == CourseStatus.COMPLETED || course.getStatus() == CourseStatus.CANCELLED) {
            throw new RuntimeException("Cannot edit a COMPLETED or CANCELLED course.");
        }

        User currentUser = getUserByEmail(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (!isAdmin && !course.getTrainer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access Denied. You can only edit your own courses.");
        }

        if (request.getTrainerId() != null && !course.getTrainer().getId().equals(request.getTrainerId())) {
            if (course.getStatus() == CourseStatus.ACTIVE) {
                throw new RuntimeException("Schimbarea formatorului este strict interzisă pentru cursurile cu status ACTIVE.");
            }

            User newTrainer = userRepository.findById(request.getTrainerId())
                    .orElseThrow(() -> new ResourceNotFoundException("New trainer not found"));
            if (newTrainer.getRole() != UserRole.FORMATOR) {
                throw new RuntimeException("The selected user is not a FORMATOR.");
            }
            course.setTrainer(newTrainer);
        }

        CourseCategory category = courseCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);
        mapFields(course, request, category);

        if (!isAdmin) {
            course.setStatus(CourseStatus.DRAFT);
        }

        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourseSecure(Long id, Authentication authentication) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (!isAdmin) {
            User currentUser = getUserByEmail(authentication.getName());
            if (!course.getTrainer().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Access Denied. You cannot delete this course.");
            }
            if (course.getStatus() != CourseStatus.DRAFT) {
                throw new RuntimeException("Formators can only delete courses that are in DRAFT status.");
            }
        } else {
            if (course.getCurrentEnrolled() > 0) {
                throw new RuntimeException("Cannot delete a course that currently has active student enrollments.");
            }
        }

        courseRepository.delete(course);
    }

    @Transactional
    public CourseResponse approveCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        course.setStatus(CourseStatus.ACTIVE);
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse rejectCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        // Resetăm starea cursului în DRAFT pentru a permite editarea lui de către formator
        course.setStatus(CourseStatus.DRAFT);
        return toResponse(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findByStatus(CourseStatus status) {
        return courseRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findByTrainerId(Long trainerId) {
        return courseRepository.findByTrainerId(trainerId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findByCategoryId(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findAvailableCourses() {
        return courseRepository.findAvailableCourses().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findUpcomingCourses(LocalDate date) {
        return courseRepository.findUpcomingCourses(date).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findPendingApprovalCourses() {
        return courseRepository.findPendingApprovalCourses().stream().map(this::toResponse).toList();
    }

    @Transactional
    public CourseResponse updateStatus(Long id, CourseStatus newStatus) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        course.setStatus(newStatus);
        return toResponse(courseRepository.save(course));
    }

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
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated identity not found for: " + email));
    }
}