package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CourseCategoryRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseCategoryResponse;
import com.example.TeacherPlatform.model.CourseCategory;
import com.example.TeacherPlatform.service.CourseCategoryService;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CourseCategoryController extends GenericController<CourseCategory, CourseCategoryRequest, CourseCategoryResponse> {

    private final CourseCategoryService courseCategoryService;

    @Override
    protected GenericService<CourseCategory, CourseCategoryRequest, CourseCategoryResponse> getService() {
        return courseCategoryService;
    }
}