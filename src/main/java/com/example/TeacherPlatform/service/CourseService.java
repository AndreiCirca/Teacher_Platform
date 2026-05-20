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
        response.setCurrentEnrolled(entity.getCurrentEnrolled());
        response.setSessionCount(entity.getSessionCount());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(Course entity, CourseRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);
        mapFields(entity, request);
    }

    // ----------------------------------------------------------------------------------
    // Funcționalități Publice & Profesor
    // ----------------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<CourseResponse> findUpcomingCourses() {
        return courseRepository.findUpcomingCourses(LocalDate.now()).stream().map(this::toResponse).toList();
    }

    // ----------------------------------------------------------------------------------
    // Funcționalități FORMATOR
    // ----------------------------------------------------------------------------------

    @Transactional
    public CourseResponse proposeCourse(CourseRequest request, Authentication authentication) {
        User trainer = getUserByEmail(authentication.getName());

        validateDates(request.getStartDate(), request.getEndDate());
        validateDeliveryFormat(request);

        Course course = new Course();
        mapFields(course, request);
        course.setTrainer(trainer);
        course.setCurrentEnrolled(0);
        course.setSessionCount(0);

        Course savedCourse = courseRepository.save(course);

        return toResponse(savedCourse);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findMyCoursesAsTrainer(Authentication authentication) {
        User trainer = getUserByEmail(authentication.getName());
        return courseRepository.findByTrainerId(trainer.getId()).stream().map(this::toResponse).toList();
    }

    // ----------------------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------------------

    private void mapFields(Course entity, CourseRequest request) {
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
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