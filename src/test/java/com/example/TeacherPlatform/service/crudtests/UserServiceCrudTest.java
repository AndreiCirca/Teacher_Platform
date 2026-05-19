package com.example.TeacherPlatform.service.crudtests;

import com.example.TeacherPlatform.dataTransferObject.UserRequest;
import com.example.TeacherPlatform.dataTransferObject.UserResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.School;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.UserRole;
import com.example.TeacherPlatform.repository.SchoolRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - CRUD Tests")
class UserServiceCrudTest {

    @Mock private UserRepository userRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private School school;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        school.setName("Liceul Teoretic");
        school.setTeacherCount(10);

        user = new User();
        user.setId(1L);
        user.setFirstName("Ion");
        user.setLastName("Pop");
        user.setEmail("ion@edu.ro");
        user.setRole(UserRole.PROFESOR);
        user.setSchool(school);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRequest = new UserRequest();
        userRequest.setFirstName("Ion");
        userRequest.setLastName("Pop");
        userRequest.setEmail("ion@edu.ro");
        userRequest.setPassword("parola123");
        userRequest.setRole(UserRole.PROFESOR);
        userRequest.setSchoolId(1L);
    }

    // --- CREATE ---
    @Test
    @DisplayName("Create - Creeaza user, hash-uieste parola si incrementeaza counter-ul scolii (pentru Profesor)")
    void create_createsUserAndIncrementsSchoolCount() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(passwordEncoder.encode("parola123")).thenReturn("encoded_pass");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserResponse response = userService.create(userRequest);

        assertThat(response.getId()).isEqualTo(10L);
        verify(passwordEncoder).encode("parola123");
        verify(schoolRepository).save(school); // verificam ca scoala a fost salvata (pentru counter)
        assertThat(school.getTeacherCount()).isEqualTo(11); // 10 initial + 1
    }

    @Test
    @DisplayName("Create - Arunca exceptie daca emailul e deja luat")
    void create_throwsException_whenEmailExists() {
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.create(userRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    // --- UPDATE ---
    @Test
    @DisplayName("Update - Actualizeaza userul cu succes")
    void update_updatesSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(user)); // acelasi user
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(passwordEncoder.encode("parola123")).thenReturn("encoded_new");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.update(1L, userRequest);

        assertThat(response.getFirstName()).isEqualTo("Ion");
        verify(userRepository).save(user);
    }

    // --- DELETE ---
    @Test
    @DisplayName("Delete - Sterge userul si decrementeaza counter-ul scolii")
    void delete_deletesSuccessfully_andDecrementsSchoolCount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(schoolRepository).save(school);
        assertThat(school.getTeacherCount()).isEqualTo(9); // 10 initial - 1
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Delete - Nu decrementeaza sub zero")
    void delete_clampsCounterToZero() {
        school.setTeacherCount(0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        assertThat(school.getTeacherCount()).isEqualTo(0);
    }
}