package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.UserRequest;
import com.example.TeacherPlatform.dataTransferObject.UserResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.School;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.UserRole;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.SchoolRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService extends GenericService<User, UserRequest, UserResponse> {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected BaseRepository<User> getRepository() {
        return userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getMyProfile(Authentication authentication) {
        return toResponse(getUserByEmail(authentication.getName()));
    }

    @Transactional
    public UserResponse updateMyProfile(UserRequest request, Authentication authentication) {
        User user = getUserByEmail(authentication.getName());

        String emailClean = request.getEmail().trim().toLowerCase();
        userRepository.findByEmail(emailClean).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new RuntimeException("Acest email este deja folosit de alt utilizator.");
            }
        });

        School oldSchool = user.getSchool();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getSchoolId() != null) {
            School newSchool = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Școala nu a fost găsită"));
            user.setSchool(newSchool);
        }

        User updatedUser = userRepository.save(user);
        handleSchoolCounters(oldSchool, user.getRole(), updatedUser);
        return toResponse(updatedUser);
    }

    @Transactional
    public void changeMyPassword(String oldPassword, String newPassword, Authentication authentication) {
        User user = getUserByEmail(authentication.getName());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Parola curentă este incorectă.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateMyAvatar(String avatarUrl, Authentication authentication) {
        User user = getUserByEmail(authentication.getName());
        user.setAvatarUrl(avatarUrl);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse toggleActiveStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit"));

        user.setActive(!user.getActive());

        if (user.getRole() == UserRole.PROFESOR && user.getSchool() != null) {
            School school = user.getSchool();
            if (user.getActive()) {
                school.setTeacherCount(school.getTeacherCount() + 1);
            } else {
                school.setTeacherCount(Math.max(0, school.getTeacherCount() - 1));
            }
            schoolRepository.save(school);
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findUnverifiedUsers() {
        return userRepository.findUnverifiedUsers().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUserStats() {
        long total = userRepository.count();
        long profesori = userRepository.countByRole(UserRole.PROFESOR);
        long formatori = userRepository.countByRole(UserRole.FORMATOR);
        long admini = userRepository.countByRole(UserRole.ADMIN);
        long activi = userRepository.findAll().stream().filter(User::getActive).count();

        return Map.of(
                "total", total,
                "profesori", profesori,
                "formatori", formatori,
                "admini", admini,
                "activi", activi,
                "inactivi", total - activi
        );
    }

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        String emailClean = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(emailClean).isPresent()) {
            throw new RuntimeException("An account with this email address already exists.");
        }
        User user = toEntity(request);
        User savedUser = userRepository.save(user);
        if (savedUser.getRole() == UserRole.PROFESOR && savedUser.getSchool() != null) {
            School school = savedUser.getSchool();
            school.setTeacherCount(school.getTeacherCount() + 1);
            schoolRepository.save(school);
        }
        return toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String emailClean = request.getEmail().trim().toLowerCase();
        userRepository.findByEmail(emailClean).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("This email address is already in use by another user.");
            }
        });

        School oldSchool = user.getSchool();
        UserRole oldRole = user.getRole();
        updateEntity(user, request);
        User updatedUser = userRepository.save(user);
        handleSchoolCounters(oldSchool, oldRole, updatedUser);
        return toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if (user.getRole() == UserRole.PROFESOR && user.getSchool() != null) {
            School school = user.getSchool();
            school.setTeacherCount(Math.max(0, school.getTeacherCount() - 1));
            schoolRepository.save(school);
        }
        super.delete(id);
    }

    @Override
    protected User toEntity(UserRequest request) {
        User user = new User();
        mapFields(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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
            entity.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    }

    private void mapFields(User entity, UserRequest request) {
        entity.setFirstName(request.getFirstName().trim());
        entity.setLastName(request.getLastName().trim());
        entity.setEmail(request.getEmail().trim().toLowerCase());
        entity.setRole(request.getRole());
        entity.setActive(request.getActive() != null ? request.getActive() : true);
        entity.setEmailVerified(request.getEmailVerified() != null ? request.getEmailVerified() : false);
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

    private void handleSchoolCounters(School oldSchool, UserRole oldRole, User updatedUser) {
        School newSchool = updatedUser.getSchool();
        UserRole newRole = updatedUser.getRole();

        if (oldRole == UserRole.PROFESOR && oldSchool != null) {
            if (newRole != UserRole.PROFESOR || newSchool == null || !oldSchool.getId().equals(newSchool.getId())) {
                oldSchool.setTeacherCount(Math.max(0, oldSchool.getTeacherCount() - 1));
                schoolRepository.save(oldSchool);
            }
        }
        if (newRole == UserRole.PROFESOR && newSchool != null) {
            if (oldRole != UserRole.PROFESOR || oldSchool == null || !oldSchool.getId().equals(newSchool.getId())) {
                newSchool.setTeacherCount(newSchool.getTeacherCount() + 1);
                schoolRepository.save(newSchool);
            }
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}