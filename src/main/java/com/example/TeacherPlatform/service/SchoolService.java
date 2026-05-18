package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.SchoolRequest;
import com.example.TeacherPlatform.dataTransferObject.SchoolResponse;
import com.example.TeacherPlatform.model.School;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.SchoolRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolService extends GenericService<School, SchoolRequest, SchoolResponse> {

    private final SchoolRepository schoolRepository;

    @Override
    protected BaseRepository<School> getRepository() {
        return schoolRepository;
    }

    // Request → Entity
    @Override
    protected School toEntity(SchoolRequest request) {
        School school = new School();
        school.setName(request.getName());
        school.setCounty(request.getCounty());
        school.setAddress(request.getAddress());
        school.setTaxId(request.getTaxId());
        school.setDirectorEmail(request.getDirectorEmail());
        school.setTeacherCount(request.getTeacherCount());
        return school;
    }

    // Entity → Response
    @Override
    protected SchoolResponse toResponse(School school) {
        SchoolResponse response = new SchoolResponse();
        response.setId(school.getId());
        response.setName(school.getName());
        response.setCounty(school.getCounty());
        response.setAddress(school.getAddress());
        response.setTaxId(school.getTaxId());
        response.setDirectorEmail(school.getDirectorEmail());
        response.setTeacherCount(school.getTeacherCount());
        response.setCreatedAt(school.getCreatedAt());
        response.setUpdatedAt(school.getUpdatedAt());
        return response;
    }

    // Update existing fields
    @Override
    protected void updateEntity(School school, SchoolRequest request) {
        school.setName(request.getName());
        school.setCounty(request.getCounty());
        school.setAddress(request.getAddress());
        school.setTaxId(request.getTaxId());
        school.setDirectorEmail(request.getDirectorEmail());
        school.setTeacherCount(request.getTeacherCount());
    }
}