package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseMaterialRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseMaterialResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.CourseMaterial;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseMaterialRepository;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseMaterialService extends GenericService<CourseMaterial, CourseMaterialRequest, CourseMaterialResponse> {

    private final CourseMaterialRepository courseMaterialRepository;
    private final CourseRepository courseRepository;

    @Override
    protected BaseRepository<CourseMaterial> getRepository() {
        return courseMaterialRepository;
    }

    @Override
    protected CourseMaterial toEntity(CourseMaterialRequest request) {
        CourseMaterial material = new CourseMaterial();
        mapFields(material, request);
        return material;
    }

    @Override
    protected CourseMaterialResponse toResponse(CourseMaterial entity) {
        CourseMaterialResponse response = new CourseMaterialResponse();
        response.setId(entity.getId());

        if (entity.getCourse() != null) {
            response.setCourseId(entity.getCourse().getId());
            response.setCourseTitle(entity.getCourse().getTitle());
        }

        response.setFileName(entity.getFileName());
        response.setFileType(entity.getFileType());
        response.setFileSize(entity.getFileSize());
        response.setFileUrl(entity.getFileUrl());
        response.setDescription(entity.getDescription());
        response.setDownloadCount(entity.getDownloadCount());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(CourseMaterial entity, CourseMaterialRequest request) {
        mapFields(entity, request);
    }

    private void mapFields(CourseMaterial entity, CourseMaterialRequest request) {
        entity.setFileName(request.getFileName());
        entity.setFileType(request.getFileType());
        entity.setFileSize(request.getFileSize());
        entity.setFileUrl(request.getFileUrl());
        entity.setDescription(request.getDescription());
        entity.setDownloadCount(request.getDownloadCount());

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));
        entity.setCourse(course);
    }
}