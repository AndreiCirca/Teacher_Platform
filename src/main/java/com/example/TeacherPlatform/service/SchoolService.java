package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.SchoolRequest;
import com.example.TeacherPlatform.dataTransferObject.SchoolResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.School;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.SchoolRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolService extends GenericService<School, SchoolRequest, SchoolResponse> {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    @Override
    protected BaseRepository<School> getRepository() {
        return schoolRepository;
    }

    @Override
    protected School toEntity(SchoolRequest request) {
        School school = new School();
        school.setName(request.getName().trim());
        school.setCounty(request.getCounty().trim());
        school.setAddress(request.getAddress().trim());
        school.setTaxId(request.getTaxId() != null ? request.getTaxId().trim() : null);
        school.setDirectorEmail(request.getDirectorEmail() != null ? request.getDirectorEmail().trim() : null);
        school.setTeacherCount(0);
        return school;
    }

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

    @Override
    protected void updateEntity(School school, SchoolRequest request) {
        school.setName(request.getName().trim());
        school.setCounty(request.getCounty().trim());
        school.setAddress(request.getAddress().trim());
        school.setTaxId(request.getTaxId() != null ? request.getTaxId().trim() : null);
        school.setDirectorEmail(request.getDirectorEmail() != null ? request.getDirectorEmail().trim() : null);
    }

    @Override
    @Transactional
    public SchoolResponse create(SchoolRequest request) {
        if (schoolRepository.findByName(request.getName().trim()).isPresent()) {
            throw new RuntimeException("A school with this name already exists");
        }
        if (request.getTaxId() != null && schoolRepository.findByTaxId(request.getTaxId().trim()).isPresent()) {
            throw new RuntimeException("The Tax ID is already registered");
        }
        return super.create(request);
    }

    @Override
    @Transactional
    public SchoolResponse update(Long id, SchoolRequest request) {
        schoolRepository.findByName(request.getName().trim())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("A school with this name already exists");
                    }
                });

        if (request.getTaxId() != null) {
            schoolRepository.findByTaxId(request.getTaxId().trim())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new RuntimeException("The Tax ID is already used by another school");
                        }
                    });
        }
        return super.update(id, request);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!schoolRepository.existsById(id)) {
            throw new ResourceNotFoundException("School not found with id: " + id);
        }

        long activeTeachers = userRepository.countBySchoolIdAndActiveTrue(id);

        if (activeTeachers > 0) {
            throw new RuntimeException("Cannot delete a school that has " + activeTeachers + " active teachers.");
        }
        super.delete(id);
    }

    @Transactional(readOnly = true)
    public List<SchoolResponse> findByCounty(String county) {
        return schoolRepository.findByCountyOrderByName(county.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SchoolResponse> searchByName(String name) {
        // CORECȚIE: Trim pe parametru pentru a evita comportamente ciudate cauzate de spații libere accidentale
        return schoolRepository.findByNameContainingIgnoreCase(name.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void incrementTeacherCount(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));
        school.setTeacherCount(school.getTeacherCount() + 1);
        schoolRepository.save(school);
    }

    @Transactional
    public void decrementTeacherCount(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));
        school.setTeacherCount(Math.max(0, school.getTeacherCount() - 1));
        schoolRepository.save(school);
    }
}