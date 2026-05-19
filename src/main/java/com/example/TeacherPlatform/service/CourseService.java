package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.*;
import com.example.TeacherPlatform.model.enums.CourseStatus;
import com.example.TeacherPlatform.repository.*;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.TeacherPlatform.model.enums.UserRole;

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

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        if (Boolean.TRUE.equals(request.getIsOnline()) && (request.getMeetingLink() == null || request.getMeetingLink().isBlank())) {
            throw new RuntimeException("Meeting link is required for online courses");
        }

        if (Boolean.FALSE.equals(request.getIsOnline()) && (request.getLocation() == null || request.getLocation().isBlank())) {
            throw new RuntimeException("Location is required for in-person courses");
        }

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCategory(category);
        course.setTrainer(trainer);
        course.setStartDate(request.getStartDate());
        course.setEndDate(request.getEndDate());
        course.setCreditHours(request.getCreditHours());
        course.setMaxParticipants(request.getMaxParticipants());
        course.setCurrentEnrolled(0);
        course.setSessionCount(0);
        course.setIsOnline(request.getIsOnline());
        course.setLocation(request.getLocation());
        course.setMeetingLink(request.getMeetingLink());
        course.setStatus(request.getStatus() != null ? request.getStatus() : CourseStatus.DRAFT);
        course.setThumbnailUrl(request.getThumbnailUrl());
        return course;
    }

    @Override
    protected CourseResponse toResponse(Course entity) {
        CourseResponse response = new CourseResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setCategoryId(entity.getCategory().getId());
        response.setCategoryName(entity.getCategory().getName());
        response.setTrainerId(entity.getTrainer().getId());
        response.setTrainerFirstName(entity.getTrainer().getFirstName());
        response.setTrainerLastName(entity.getTrainer().getLastName());
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
            throw new RuntimeException("Cannot edit a COMPLETED or CANCELLED course");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        CourseCategory category = courseCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setCategory(category);
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setCreditHours(request.getCreditHours());
        entity.setMaxParticipants(request.getMaxParticipants());
        entity.setIsOnline(request.getIsOnline());
        entity.setLocation(request.getLocation());
        entity.setMeetingLink(request.getMeetingLink());
        entity.setThumbnailUrl(request.getThumbnailUrl());
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
    public List<CourseResponse> findUpcomingCourses() {
        return courseRepository.findUpcomingCourses(LocalDate.now()).stream().map(this::toResponse).toList();
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
}

