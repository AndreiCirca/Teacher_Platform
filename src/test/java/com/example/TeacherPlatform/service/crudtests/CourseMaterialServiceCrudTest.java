package com.example.TeacherPlatform.service.crudtests;

import com.example.TeacherPlatform.dataTransferObject.CourseMaterialRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseMaterialResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.*;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseMaterialService - Business Logic & Access Tests")
class CourseMaterialServiceCrudTest {

    @Mock private CourseMaterialRepository courseMaterialRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks
    private CourseMaterialService courseMaterialService;

    private Course course;
    private User teacher;
    private CourseMaterial material;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("profesor@edu.ro");

        course = new Course();
        course.setId(1L);
        course.setTitle("Clean Code");

        enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setCourse(course);
        enrollment.setTeacher(teacher);

        material = new CourseMaterial();
        material.setId(1L);
        material.setCourse(course);
        material.setFileName("curs1.pdf");
        material.setFileType("application/pdf");
        material.setFileSize(2048L);
        material.setFileUrl("http://storage.com/curs1.pdf");
        material.setDownloadCount(5);
    }

    @Test
    @DisplayName("Create - Formatorul adaugă un material cu succes")
    void create_addsMaterialSuccessfully() {
        CourseMaterialRequest req = new CourseMaterialRequest();
        req.setCourseId(1L);
        req.setFileName("curs1.pdf");
        req.setFileType("application/pdf");
        req.setFileSize(2048L);
        req.setFileUrl("http://storage.com/curs1.pdf");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseMaterialRepository.save(any(CourseMaterial.class))).thenAnswer(i -> {
            CourseMaterial m = i.getArgument(0);
            m.setId(10L);
            return m;
        });

        CourseMaterialResponse response = courseMaterialService.create(req);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getFileName()).isEqualTo("curs1.pdf");
        verify(courseMaterialRepository).save(any(CourseMaterial.class));
    }

    @Test
    @DisplayName("findByCourseId - PROFESOR cu acces primește materialele")
    void findByCourseId_profesorWithAccess_returnsMaterials() {
        mockAuthAs("profesor@edu.ro", "PROFESOR");
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.hasConfirmedOrCompletedEnrollment(1L, "profesor@edu.ro")).thenReturn(true);
        when(courseMaterialRepository.findCourseMaterialsOrdered(1L)).thenReturn(List.of(material));

        List<CourseMaterialResponse> result = courseMaterialService.findByCourseId(1L, authentication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("curs1.pdf");
    }

    @Test
    @DisplayName("findByCourseId - PROFESOR fără acces primește eroare (Acces interzis)")
    void findByCourseId_profesorWithoutAccess_throwsException() {
        mockAuthAs("profesor@edu.ro", "PROFESOR");
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.hasConfirmedOrCompletedEnrollment(1L, "profesor@edu.ro")).thenReturn(false);

        assertThatThrownBy(() -> courseMaterialService.findByCourseId(1L, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Acces interzis");
    }

    @Test
    @DisplayName("incrementDownloadCount - Mărește contorul la descărcare")
    void incrementDownloadCount_increasesCounter() {
        mockAuthAs("profesor@edu.ro", "PROFESOR");
        when(courseMaterialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(enrollmentRepository.hasConfirmedOrCompletedEnrollment(1L, "profesor@edu.ro")).thenReturn(true);
        when(courseMaterialRepository.save(any(CourseMaterial.class))).thenAnswer(i -> i.getArgument(0));

        CourseMaterialResponse result = courseMaterialService.incrementDownloadCount(1L, authentication);

        assertThat(result.getDownloadCount()).isEqualTo(6); // A crescut de la 5 la 6
        verify(courseMaterialRepository).save(material);
    }

    @Test
    @DisplayName("findMyGroupedMaterials - Grupează corect materialele pe numele cursului")
    void findMyGroupedMaterials_groupsCorrectly() {
        when(authentication.getName()).thenReturn("profesor@edu.ro");
        when(userRepository.findByEmail("profesor@edu.ro")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findConfirmedEnrollmentsByTeacher(1L)).thenReturn(List.of(enrollment));
        when(courseMaterialRepository.findCourseMaterialsOrdered(1L)).thenReturn(List.of(material));

        Map<String, List<CourseMaterialResponse>> result = courseMaterialService.findMyGroupedMaterials(authentication);

        assertThat(result).containsKey("Clean Code");
        assertThat(result.get("Clean Code")).hasSize(1);
        assertThat(result.get("Clean Code").get(0).getFileName()).isEqualTo("curs1.pdf");
    }

    private void mockAuthAs(String email, String role) {
        when(authentication.getName()).thenReturn(email);
        Collection<GrantedAuthority> authorities = List.of(() -> role);
        doReturn(authorities).when(authentication).getAuthorities();
    }
}