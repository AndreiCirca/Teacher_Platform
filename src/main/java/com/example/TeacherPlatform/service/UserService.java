package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.UserRequest;
import com.example.TeacherPlatform.dataTransferObject.UserResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.School;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.SchoolRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService extends GenericService<User, UserRequest, UserResponse> {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    @Override
    protected BaseRepository<User> getRepository() {
        return userRepository;
    }

    @Override
    protected User toEntity(UserRequest request) {
        User user = new User();
        mapFields(user, request);
        user.setPassword(request.getPassword());
        return user;
    }

    @Override
    protected UserResponse toResponse(User entity) {
        UserResponse response = new UserResponse();
        response.setId(entity.getId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setFullName(entity.getFullName());
        response.setEmail(entity.getEmail());
        response.setRole(entity.getRole());

        if (entity.getSchool() != null) {
            response.setSchoolId(entity.getSchool().getId());
            response.setSchoolName(entity.getSchool().getName());
        }

        response.setActive(entity.getActive());
        response.setEmailVerified(entity.getEmailVerified());
        response.setPhoneNumber(entity.getPhoneNumber());
        response.setAvatarUrl(entity.getAvatarUrl());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(User entity, UserRequest request) {
        mapFields(entity, request);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entity.setPassword(request.getPassword());
        }
    }

    private void mapFields(User entity, UserRequest request) {
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setEmail(request.getEmail());
        entity.setRole(request.getRole());
        entity.setActive(request.getActive());
        entity.setEmailVerified(request.getEmailVerified());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setAvatarUrl(request.getAvatarUrl());

        if (request.getSchoolId() != null) {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + request.getSchoolId()));
            entity.setSchool(school);
        } else {
            entity.setSchool(null);
        }
    }
}