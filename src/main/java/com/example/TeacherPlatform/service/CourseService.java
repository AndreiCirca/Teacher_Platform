package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.CourseCategory;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseCategoryRepository;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService extends GenericService<Course, CourseRequest, CourseResponse> {

    private final CourseRepository courseRepository;
    private final CourseCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    protected BaseRepository<Course> getRepository() {
        return courseRepository;
    }

    @Override
    protected Course toEntity(CourseRequest request) {
        Course course = new Course();
        mapFields(course, request);
        course.setCurrentEnrolled(0);
        course.setSessionCount(1); // Default initial configuration
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
            response.setTrainerFullName(entity.getTrainer().getFullName());
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
        mapFields(entity, request);
    }

    private void mapFields(Course entity, CourseRequest request) {
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setCreditHours(request.getCreditHours());
        entity.setMaxParticipants(request.getMaxParticipants());
        entity.setIsOnline(request.getIsOnline());
        entity.setLocation(request.getLocation());
        entity.setMeetingLink(request.getMeetingLink());
        entity.setStatus(request.getStatus());
        entity.setThumbnailUrl(request.getThumbnailUrl());

        CourseCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        entity.setCategory(category);

        User trainer = userRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + request.getTrainerId()));
        entity.setTrainer(trainer);
    }
}