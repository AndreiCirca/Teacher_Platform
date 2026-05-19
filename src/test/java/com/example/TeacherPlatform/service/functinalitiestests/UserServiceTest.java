package com.example.TeacherPlatform.service.functinalitiestests;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Functionality Tests (non-CRUD)")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private School schoolCns;
    private School schoolUbb;
    private User profesorFilip;
    private User formatorAlin;

    @BeforeEach
    void setUp() {
        schoolCns = buildSchool(1L, "Colegiul Național Silvania", "Sălaj", 3);
        schoolUbb = buildSchool(2L, "Facultatea de Matematică și Informatică", "Cluj", 10);

        profesorFilip = buildUser(10L, "Filip", "Mureșan", "filip@edu.ro", UserRole.PROFESOR, schoolCns, true);
        formatorAlin  = buildUser(20L, "Alin",  "Pop",     "alin@tech.ro", UserRole.FORMATOR, schoolUbb, true);
    }

    // -------------------------------------------------------------------------
    // getMyProfile
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getMyProfile - returns profile for authenticated user")
    void getMyProfile_returnsProfileForCurrentUser() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(profesorFilip));

        UserResponse result = userService.getMyProfile(authentication);

        assertThat(result.getEmail()).isEqualTo("filip@edu.ro");
        assertThat(result.getFirstName()).isEqualTo("Filip");
        assertThat(result.getRole()).isEqualTo(UserRole.PROFESOR);
    }

    @Test
    @DisplayName("getMyProfile - throws ResourceNotFoundException when authenticated user email not in DB")
    void getMyProfile_throwsException_whenUserNotFound() {
        when(authentication.getName()).thenReturn("ghost@edu.ro");
        when(userRepository.findByEmail("ghost@edu.ro")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyProfile(authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost@edu.ro");
    }

    // -------------------------------------------------------------------------
    // updateMyProfile
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateMyProfile - updates allowed profile fields for authenticated user")
    void updateMyProfile_updatesFieldsSuccessfully() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(profesorFilip));
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(schoolCns));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserRequest request = buildUserRequest("Filip", "Mureșan Nou", "filip@edu.ro", UserRole.PROFESOR, 1L, true, true, "0700000000", null, "pass");

        UserResponse result = userService.updateMyProfile(request, authentication);

        assertThat(result.getLastName()).isEqualTo("Mureșan Nou");
        assertThat(result.getPhoneNumber()).isEqualTo("0700000000");
    }

    @Test
    @DisplayName("updateMyProfile - throws exception when email is already used by another user")
    void updateMyProfile_throwsException_whenEmailTakenByOther() {
        User otherUser = buildUser(99L, "Alt", "User", "filip@edu.ro", UserRole.PROFESOR, schoolCns, true);

        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(profesorFilip));
        // second call simulates the email-uniqueness check returning a DIFFERENT user
        when(userRepository.findByEmail("filip@edu.ro"))
                .thenReturn(Optional.of(profesorFilip))   // first call: resolve authenticated user
                .thenReturn(Optional.of(otherUser));       // second call: uniqueness check => conflict

        UserRequest request = buildUserRequest("Filip", "Mureșan", "filip@edu.ro", UserRole.PROFESOR, 1L, true, true, null, null, "pass");

        // Only triggers if the ID differs; here otherUser.id=99 ≠ profesorFilip.id=10
        assertThatThrownBy(() -> userService.updateMyProfile(request, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("updateMyProfile - changes school when new schoolId is provided")
    void updateMyProfile_changesSchool_whenNewSchoolIdProvided() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(profesorFilip));
        when(schoolRepository.findById(2L)).thenReturn(Optional.of(schoolUbb));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        UserRequest request = buildUserRequest("Filip", "Mureșan", "filip@edu.ro", UserRole.PROFESOR, 2L, true, true, null, null, "pass");

        UserResponse result = userService.updateMyProfile(request, authentication);

        assertThat(result.getSchoolId()).isEqualTo(2L);
    }

    // -------------------------------------------------------------------------
    // changeMyPassword
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("changeMyPassword - changes password when old password is correct")
    void changeMyPassword_changesPasswordSuccessfully() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(profesorFilip));
        when(passwordEncoder.matches("oldPass", profesorFilip.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> userService.changeMyPassword("oldPass", "newPass", authentication))
                .doesNotThrowAnyException();

        assertThat(profesorFilip.getPassword()).isEqualTo("encodedNewPass");
        verify(userRepository).save(profesorFilip);
    }

    @Test
    @DisplayName("changeMyPassword - throws exception when old password is incorrect")
    void changeMyPassword_throwsException_whenOldPasswordWrong() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(profesorFilip));
        when(passwordEncoder.matches("wrongPass", profesorFilip.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.changeMyPassword("wrongPass", "newPass", authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("incorectă");

        verify(userRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // updateMyAvatar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateMyAvatar - updates avatarUrl for authenticated user")
    void updateMyAvatar_updatesAvatarUrl() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(profesorFilip));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.updateMyAvatar("https://cdn.example.com/avatar.png", authentication);

        assertThat(result.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar.png");
        verify(userRepository).save(profesorFilip);
    }

    // -------------------------------------------------------------------------
    // toggleActiveStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toggleActiveStatus - deactivates an active user and decrements school teacher count for PROFESOR")
    void toggleActiveStatus_deactivatesProfesor_andDecrementsSchoolCount() {
        profesorFilip.setActive(true);
        schoolCns.setTeacherCount(3);

        when(userRepository.findById(10L)).thenReturn(Optional.of(profesorFilip));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.toggleActiveStatus(10L);

        assertThat(result.getActive()).isFalse();
        assertThat(schoolCns.getTeacherCount()).isEqualTo(2);
        verify(schoolRepository).save(schoolCns);
    }

    @Test
    @DisplayName("toggleActiveStatus - activates an inactive user and increments school teacher count for PROFESOR")
    void toggleActiveStatus_activatesProfesor_andIncrementsSchoolCount() {
        profesorFilip.setActive(false);
        schoolCns.setTeacherCount(2);

        when(userRepository.findById(10L)).thenReturn(Optional.of(profesorFilip));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.toggleActiveStatus(10L);

        assertThat(result.getActive()).isTrue();
        assertThat(schoolCns.getTeacherCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("toggleActiveStatus - toggles FORMATOR without changing any school counter")
    void toggleActiveStatus_togglesFormator_noSchoolCounterChange() {
        formatorAlin.setActive(true);

        when(userRepository.findById(20L)).thenReturn(Optional.of(formatorAlin));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.toggleActiveStatus(20L);

        verify(schoolRepository, never()).save(any());
    }

    @Test
    @DisplayName("toggleActiveStatus - throws ResourceNotFoundException when user not found")
    void toggleActiveStatus_throwsException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.toggleActiveStatus(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("găsit");
    }

    @Test
    @DisplayName("toggleActiveStatus - school teacherCount does not go below 0 when deactivating")
    void toggleActiveStatus_doesNotDecrementBelowZero() {
        profesorFilip.setActive(true);
        schoolCns.setTeacherCount(0);

        when(userRepository.findById(10L)).thenReturn(Optional.of(profesorFilip));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.toggleActiveStatus(10L);

        assertThat(schoolCns.getTeacherCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // findUnverifiedUsers
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findUnverifiedUsers - returns users with emailVerified=false, ordered by createdAt")
    void findUnverifiedUsers_returnsUnverifiedUsers() {
        User unverified = buildUser(30L, "Marc", "Stan", "marc@edu.ro", UserRole.PROFESOR, schoolUbb, false);
        unverified.setEmailVerified(false);

        when(userRepository.findUnverifiedUsers()).thenReturn(List.of(unverified));

        List<UserResponse> result = userService.findUnverifiedUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("marc@edu.ro");
        verify(userRepository).findUnverifiedUsers();
    }

    @Test
    @DisplayName("findUnverifiedUsers - returns empty list when all users are verified")
    void findUnverifiedUsers_returnsEmpty_whenAllVerified() {
        when(userRepository.findUnverifiedUsers()).thenReturn(List.of());

        List<UserResponse> result = userService.findUnverifiedUsers();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getUserStats
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getUserStats - returns correct stat map with all keys")
    void getUserStats_returnsCorrectStats() {
        User inactiveUser = buildUser(31L, "Victor", "Dan", "victor@edu.ro", UserRole.PROFESOR, schoolCns, false);

        when(userRepository.count()).thenReturn(3L);
        when(userRepository.countByRole(UserRole.PROFESOR)).thenReturn(2L);
        when(userRepository.countByRole(UserRole.FORMATOR)).thenReturn(1L);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(0L);
        when(userRepository.findAll()).thenReturn(List.of(profesorFilip, formatorAlin, inactiveUser));

        Map<String, Long> stats = userService.getUserStats();

        assertThat(stats).containsKeys("total", "profesori", "formatori", "admini", "activi", "inactivi");
        assertThat(stats.get("total")).isEqualTo(3L);
        assertThat(stats.get("profesori")).isEqualTo(2L);
        assertThat(stats.get("formatori")).isEqualTo(1L);
        assertThat(stats.get("admini")).isEqualTo(0L);
        assertThat(stats.get("activi")).isEqualTo(2L);   // profesorFilip + formatorAlin
        assertThat(stats.get("inactivi")).isEqualTo(1L); // inactiveUser
    }

    @Test
    @DisplayName("getUserStats - returns all zeros on empty database")
    void getUserStats_returnsZeros_whenNoUsers() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByRole(UserRole.PROFESOR)).thenReturn(0L);
        when(userRepository.countByRole(UserRole.FORMATOR)).thenReturn(0L);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(0L);
        when(userRepository.findAll()).thenReturn(List.of());

        Map<String, Long> stats = userService.getUserStats();

        assertThat(stats.get("total")).isEqualTo(0L);
        assertThat(stats.get("activi")).isEqualTo(0L);
        assertThat(stats.get("inactivi")).isEqualTo(0L);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private School buildSchool(Long id, String name, String county, int teacherCount) {
        School school = new School();
        school.setId(id);
        school.setName(name);
        school.setCounty(county);
        school.setTeacherCount(teacherCount);
        school.setCreatedAt(LocalDateTime.now());
        school.setUpdatedAt(LocalDateTime.now());
        return school;
    }

    private User buildUser(Long id, String firstName, String lastName, String email,
                           UserRole role, School school, boolean active) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword("encoded_pass");
        user.setRole(role);
        user.setSchool(school);
        user.setActive(active);
        user.setEmailVerified(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private UserRequest buildUserRequest(String firstName, String lastName, String email,
                                         UserRole role, Long schoolId, Boolean active,
                                         Boolean emailVerified, String phone, String avatar, String password) {
        UserRequest req = new UserRequest();
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setEmail(email);
        req.setRole(role);
        req.setSchoolId(schoolId);
        req.setActive(active);
        req.setEmailVerified(emailVerified);
        req.setPhoneNumber(phone);
        req.setAvatarUrl(avatar);
        req.setPassword(password);
        return req;
    }
}
