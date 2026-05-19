package com.example.TeacherPlatform.service.functinalitiestests;

import com.example.TeacherPlatform.dataTransferObject.CourseMaterialResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.*;
import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import com.example.TeacherPlatform.model.enums.UserRole;
import com.example.TeacherPlatform.repository.*;
import com.example.TeacherPlatform.service.CourseMaterialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseMaterialService - Functionality Tests")
class CourseMaterialServiceTest {

    @Mock private CourseMaterialRepository courseMaterialRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks
    private CourseMaterialService courseMaterialService;

    private Course course;
    private User teacher;
    private User trainer;
    private Enrollment enrollment;
    private CourseMaterial material;

    @BeforeEach
    void setUp() {
        trainer = buildUser(2L, "Ana", "Ionescu", "ana@tech.ro", UserRole.FORMATOR);
        teacher = buildUser(1L, "Ion", "Pop", "ion@edu.ro", UserRole.PROFESOR);

        course = buildCourse(1L, "Clean Code", trainer);

        enrollment = buildEnrollment(1L, course, teacher, EnrollmentStatus.CONFIRMED);

        material = buildMaterial(1L, course, "slides.pdf", "pdf", 1024L, 5);
    }

    // -------------------------------------------------------------------------
    // findByCourseId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByCourseId - FORMATOR poate vedea materialele fara verificare inscriere")
    void findByCourseId_formatorCanSeeMaterials() {
        mockAuthAs("ana@tech.ro", UserRole.FORMATOR);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(courseMaterialRepository.findCourseMaterialsOrdered(1L)).thenReturn(List.of(material));

        List<CourseMaterialResponse> result = courseMaterialService.findByCourseId(1L, authentication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("slides.pdf");
    }

    @Test
    @DisplayName("findByCourseId - PROFESOR cu inscriere confirmata poate vedea materialele")
    void findByCourseId_profesorWithConfirmedEnrollment_canSeeMaterials() {
        mockAuthAs("ion@edu.ro", UserRole.PROFESOR);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.hasConfirmedOrCompletedEnrollment(1L, "ion@edu.ro")).thenReturn(true);
        when(courseMaterialRepository.findCourseMaterialsOrdered(1L)).thenReturn(List.of(material));

        List<CourseMaterialResponse> result = courseMaterialService.findByCourseId(1L, authentication);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByCourseId - PROFESOR fara inscriere confirmata primeste exceptie")
    void findByCourseId_profesorWithoutEnrollment_throwsException() {
        mockAuthAs("ion@edu.ro", UserRole.PROFESOR);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.hasConfirmedOrCompletedEnrollment(1L, "ion@edu.ro")).thenReturn(false);

        assertThatThrownBy(() -> courseMaterialService.findByCourseId(1L, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Acces interzis");
    }

    @Test
    @DisplayName("findByCourseId - arunca exceptie daca cursul nu exista")
    void findByCourseId_throwsException_whenCourseNotFound() {
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> courseMaterialService.findByCourseId(99L, authentication))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findByCourseId - mapeaza corect campurile response")
    void findByCourseId_mapsFieldsCorrectly() {
        mockAuthAs("ana@tech.ro", UserRole.FORMATOR);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(courseMaterialRepository.findCourseMaterialsOrdered(1L)).thenReturn(List.of(material));

        List<CourseMaterialResponse> result = courseMaterialService.findByCourseId(1L, authentication);

        CourseMaterialResponse resp = result.get(0);
        assertThat(resp.getCourseId()).isEqualTo(1L);
        assertThat(resp.getCourseTitle()).isEqualTo("Clean Code");
        assertThat(resp.getFileName()).isEqualTo("slides.pdf");
        assertThat(resp.getFileType()).isEqualTo("pdf");
        assertThat(resp.getFileSize()).isEqualTo(1024L);
        assertThat(resp.getDownloadCount()).isEqualTo(5);
    }

    // -------------------------------------------------------------------------
    // incrementDownloadCount
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("incrementDownloadCount - FORMATOR incrementeaza download count")
    void incrementDownloadCount_formatorCanIncrement() {
        mockAuthAs("ana@tech.ro", UserRole.FORMATOR);
        when(courseMaterialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(courseMaterialRepository.save(any(CourseMaterial.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseMaterialResponse result = courseMaterialService.incrementDownloadCount(1L, authentication);

        assertThat(result.getDownloadCount()).isEqualTo(6);
        verify(courseMaterialRepository).save(material);
    }

    @Test
    @DisplayName("incrementDownloadCount - PROFESOR cu acces incrementeaza download count")
    void incrementDownloadCount_profesorWithAccess_canIncrement() {
        mockAuthAs("ion@edu.ro", UserRole.PROFESOR);
        when(courseMaterialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(enrollmentRepository.hasConfirmedOrCompletedEnrollment(1L, "ion@edu.ro")).thenReturn(true);
        when(courseMaterialRepository.save(any(CourseMaterial.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseMaterialResponse result = courseMaterialService.incrementDownloadCount(1L, authentication);

        assertThat(result.getDownloadCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("incrementDownloadCount - PROFESOR fara acces primeste exceptie")
    void incrementDownloadCount_profesorWithoutAccess_throwsException() {
        mockAuthAs("ion@edu.ro", UserRole.PROFESOR);
        when(courseMaterialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(enrollmentRepository.hasConfirmedOrCompletedEnrollment(1L, "ion@edu.ro")).thenReturn(false);

        assertThatThrownBy(() -> courseMaterialService.incrementDownloadCount(1L, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Acces interzis");
    }

    @Test
    @DisplayName("incrementDownloadCount - arunca exceptie daca materialul nu exista")
    void incrementDownloadCount_throwsException_whenMaterialNotFound() {
        when(courseMaterialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseMaterialService.incrementDownloadCount(99L, authentication))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // findMyGroupedMaterials
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findMyGroupedMaterials - returneaza materialele grupate pe cursuri")
    void findMyGroupedMaterials_returnsMaterialsGroupedByCourse() {
        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findConfirmedEnrollmentsByTeacher(1L)).thenReturn(List.of(enrollment));
        when(courseMaterialRepository.findCourseMaterialsOrdered(1L)).thenReturn(List.of(material));

        Map<String, List<CourseMaterialResponse>> result = courseMaterialService.findMyGroupedMaterials(authentication);

        assertThat(result).containsKey("Clean Code");
        assertThat(result.get("Clean Code")).hasSize(1);
        assertThat(result.get("Clean Code").get(0).getFileName()).isEqualTo("slides.pdf");
    }

    @Test
    @DisplayName("findMyGroupedMaterials - nu include cursuri fara materiale")
    void findMyGroupedMaterials_excludesCourseWithoutMaterials() {
        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findConfirmedEnrollmentsByTeacher(1L)).thenReturn(List.of(enrollment));
        when(courseMaterialRepository.findCourseMaterialsOrdered(1L)).thenReturn(List.of());

        Map<String, List<CourseMaterialResponse>> result = courseMaterialService.findMyGroupedMaterials(authentication);

        assertThat(result).doesNotContainKey("Clean Code");
    }

    @Test
    @DisplayName("findMyGroupedMaterials - returneaza map gol daca nu are inscrieri")
    void findMyGroupedMaterials_returnsEmptyMap_whenNoEnrollments() {
        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findConfirmedEnrollmentsByTeacher(1L)).thenReturn(List.of());

        Map<String, List<CourseMaterialResponse>> result = courseMaterialService.findMyGroupedMaterials(authentication);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findMyGroupedMaterials - arunca exceptie daca userul nu exista")
    void findMyGroupedMaterials_throwsException_whenUserNotFound() {
        when(authentication.getName()).thenReturn("unknown@edu.ro");
        when(userRepository.findByEmail("unknown@edu.ro")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseMaterialService.findMyGroupedMaterials(authentication))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void mockAuthAs(String email, UserRole role) {
        when(authentication.getName()).thenReturn(email);
        Collection<GrantedAuthority> authorities = List.of(() -> role.name());
        doReturn(authorities).when(authentication).getAuthorities();
    }

    private User buildUser(Long id, String firstName, String lastName, String email, UserRole role) {
        User u = new User();
        u.setId(id); u.setFirstName(firstName); u.setLastName(lastName);
        u.setEmail(email); u.setRole(role); u.setActive(true);
        return u;
    }

    private Course buildCourse(Long id, String title, User trainer) {
        Course c = new Course();
        c.setId(id); c.setTitle(title); c.setTrainer(trainer);
        c.setMaxParticipants(20); c.setCurrentEnrolled(0); c.setSessionCount(0);
        return c;
    }

    private Enrollment buildEnrollment(Long id, Course course, User teacher, EnrollmentStatus status) {
        Enrollment e = new Enrollment();
        e.setId(id); e.setCourse(course); e.setTeacher(teacher);
        e.setStatus(status); e.setCertificateGenerated(false);
        return e;
    }

    private CourseMaterial buildMaterial(Long id, Course course, String fileName, String fileType, Long fileSize, int downloadCount) {
        CourseMaterial m = new CourseMaterial();
        m.setId(id); m.setCourse(course); m.setFileName(fileName);
        m.setFileType(fileType); m.setFileSize(fileSize);
        m.setFileUrl("http://storage/" + fileName);
        m.setDownloadCount(downloadCount);
        m.setCreatedAt(LocalDateTime.now()); m.setUpdatedAt(LocalDateTime.now());
        return m;
    }
}