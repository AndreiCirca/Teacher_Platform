package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseCategoryRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseCategoryResponse;
import com.example.TeacherPlatform.model.CourseCategory;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseCategoryRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseCategoryService extends GenericService<CourseCategory, CourseCategoryRequest, CourseCategoryResponse> {

    private final CourseCategoryRepository courseCategoryRepository;

    @Override
    protected BaseRepository<CourseCategory> getRepository() {
        return courseCategoryRepository;
    }

    @Override
    protected CourseCategory toEntity(CourseCategoryRequest request) {
        CourseCategory category = new CourseCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColor(request.getColor());
        category.setActive(request.getActive());
        return category;
    }

    @Override
    protected CourseCategoryResponse toResponse(CourseCategory entity) {
        CourseCategoryResponse response = new CourseCategoryResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setColor(entity.getColor());
        response.setActive(entity.getActive());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(CourseCategory entity, CourseCategoryRequest request) {
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setColor(request.getColor());
        entity.setActive(request.getActive());
    }
}