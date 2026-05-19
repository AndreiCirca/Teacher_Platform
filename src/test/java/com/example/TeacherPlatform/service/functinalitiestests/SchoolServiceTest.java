package com.example.TeacherPlatform.service.functinalitiestests;

import com.example.TeacherPlatform.dataTransferObject.SchoolResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.School;
import com.example.TeacherPlatform.repository.SchoolRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.SchoolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SchoolService - Functionality Tests (non-CRUD)")
class SchoolServiceTest {

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SchoolService schoolService;

    private School schoolCluj;
    private School schoolSalaj;

    @BeforeEach
    void setUp() {
        schoolCluj = buildSchool(1L, "Colegiul Național Emil Racoviță", "Cluj", "RO111111", "Str. Avram Iancu, Cluj-Napoca", 30);
        schoolSalaj = buildSchool(2L, "Colegiul Național Silvania", "Sălaj", "RO222222", "Str. Unirii, Zalău", 45);
    }

    // -------------------------------------------------------------------------
    // findByCounty
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByCounty - returns schools from given county, sorted by name")
    void findByCounty_returnsSchoolsForCounty() {
        when(schoolRepository.findByCountyOrderByName("Cluj"))
                .thenReturn(List.of(schoolCluj));

        List<SchoolResponse> result = schoolService.findByCounty("Cluj");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Colegiul Național Emil Racoviță");
        assertThat(result.get(0).getCounty()).isEqualTo("Cluj");

        verify(schoolRepository).findByCountyOrderByName("Cluj");
    }

    @Test
    @DisplayName("findByCounty - trims whitespace from county parameter")
    void findByCounty_trimsWhitespace() {
        when(schoolRepository.findByCountyOrderByName("Cluj"))
                .thenReturn(List.of(schoolCluj));

        List<SchoolResponse> result = schoolService.findByCounty("  Cluj  ");

        assertThat(result).hasSize(1);
        verify(schoolRepository).findByCountyOrderByName("Cluj");
    }

    @Test
    @DisplayName("findByCounty - returns empty list when no schools in county")
    void findByCounty_returnsEmptyList_whenNoSchoolsInCounty() {
        when(schoolRepository.findByCountyOrderByName("Timiș"))
                .thenReturn(List.of());

        List<SchoolResponse> result = schoolService.findByCounty("Timiș");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByCounty - returns multiple schools from same county")
    void findByCounty_returnsMultipleSchools() {
        School anotherCluj = buildSchool(3L, "Liceul Teoretic Gheorghe Lazăr", "Cluj", "RO333333", "Str. Dorobanților, Cluj-Napoca", 20);

        when(schoolRepository.findByCountyOrderByName("Cluj"))
                .thenReturn(List.of(schoolCluj, anotherCluj));

        List<SchoolResponse> result = schoolService.findByCounty("Cluj");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SchoolResponse::getName)
                .containsExactly("Colegiul Național Emil Racoviță", "Liceul Teoretic Gheorghe Lazăr");
    }

    // -------------------------------------------------------------------------
    // searchByName
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("searchByName - returns schools matching partial name, case-insensitive")
    void searchByName_returnsMatchingSchools() {
        when(schoolRepository.findByNameContainingIgnoreCase("colegiu"))
                .thenReturn(List.of(schoolCluj, schoolSalaj));

        List<SchoolResponse> result = schoolService.searchByName("colegiu");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SchoolResponse::getName)
                .containsExactlyInAnyOrder(
                        "Colegiul Național Emil Racoviță",
                        "Colegiul Național Silvania"
                );
    }

    @Test
    @DisplayName("searchByName - trims whitespace from name parameter")
    void searchByName_trimsWhitespace() {
        when(schoolRepository.findByNameContainingIgnoreCase("silvania"))
                .thenReturn(List.of(schoolSalaj));

        List<SchoolResponse> result = schoolService.searchByName("  silvania  ");

        assertThat(result).hasSize(1);
        verify(schoolRepository).findByNameContainingIgnoreCase("silvania");
    }

    @Test
    @DisplayName("searchByName - returns empty list when no school matches")
    void searchByName_returnsEmptyList_whenNoMatch() {
        when(schoolRepository.findByNameContainingIgnoreCase("inexistent"))
                .thenReturn(List.of());

        List<SchoolResponse> result = schoolService.searchByName("inexistent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("searchByName - maps all response fields correctly")
    void searchByName_mapsResponseFieldsCorrectly() {
        when(schoolRepository.findByNameContainingIgnoreCase("silvania"))
                .thenReturn(List.of(schoolSalaj));

        List<SchoolResponse> result = schoolService.searchByName("silvania");

        SchoolResponse response = result.get(0);
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Colegiul Național Silvania");
        assertThat(response.getCounty()).isEqualTo("Sălaj");
        assertThat(response.getTaxId()).isEqualTo("RO222222");
        assertThat(response.getAddress()).isEqualTo("Str. Unirii, Zalău");
        assertThat(response.getTeacherCount()).isEqualTo(45);
    }

    // -------------------------------------------------------------------------
    // incrementTeacherCount
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("incrementTeacherCount - increments teacherCount by 1")
    void incrementTeacherCount_incrementsCount() {
        schoolCluj.setTeacherCount(30);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(schoolCluj));
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        schoolService.incrementTeacherCount(1L);

        assertThat(schoolCluj.getTeacherCount()).isEqualTo(31);
        verify(schoolRepository).save(schoolCluj);
    }

    @Test
    @DisplayName("incrementTeacherCount - works when count starts at 0")
    void incrementTeacherCount_worksFromZero() {
        schoolCluj.setTeacherCount(0);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(schoolCluj));
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        schoolService.incrementTeacherCount(1L);

        assertThat(schoolCluj.getTeacherCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("incrementTeacherCount - throws ResourceNotFoundException when school not found")
    void incrementTeacherCount_throwsException_whenSchoolNotFound() {
        when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schoolService.incrementTeacherCount(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(schoolRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // decrementTeacherCount
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("decrementTeacherCount - decrements teacherCount by 1")
    void decrementTeacherCount_decrementsCount() {
        schoolCluj.setTeacherCount(30);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(schoolCluj));
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        schoolService.decrementTeacherCount(1L);

        assertThat(schoolCluj.getTeacherCount()).isEqualTo(29);
        verify(schoolRepository).save(schoolCluj);
    }

    @Test
    @DisplayName("decrementTeacherCount - does not go below 0 (clamps to 0)")
    void decrementTeacherCount_doesNotGoBelowZero() {
        schoolCluj.setTeacherCount(0);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(schoolCluj));
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        schoolService.decrementTeacherCount(1L);

        assertThat(schoolCluj.getTeacherCount()).isEqualTo(0);
        verify(schoolRepository).save(schoolCluj);
    }

    @Test
    @DisplayName("decrementTeacherCount - throws ResourceNotFoundException when school not found")
    void decrementTeacherCount_throwsException_whenSchoolNotFound() {
        when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schoolService.decrementTeacherCount(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(schoolRepository, never()).save(any());
    }

    @Test
    @DisplayName("decrementTeacherCount - clamps correctly from count of 1 to 0")
    void decrementTeacherCount_fromOneToZero() {
        schoolCluj.setTeacherCount(1);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(schoolCluj));
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        schoolService.decrementTeacherCount(1L);

        assertThat(schoolCluj.getTeacherCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private School buildSchool(Long id, String name, String county, String taxId, String address, int teacherCount) {
        School school = new School();
        school.setId(id);
        school.setName(name);
        school.setCounty(county);
        school.setTaxId(taxId);
        school.setAddress(address);
        school.setTeacherCount(teacherCount);
        // BaseEntity fields
        school.setCreatedAt(LocalDateTime.now());
        school.setUpdatedAt(LocalDateTime.now());
        return school;
    }
}