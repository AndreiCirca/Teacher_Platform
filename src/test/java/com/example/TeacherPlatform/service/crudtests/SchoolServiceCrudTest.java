package com.example.TeacherPlatform.service.crudtests;

import com.example.TeacherPlatform.dataTransferObject.SchoolRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SchoolService - CRUD Tests")
class SchoolServiceCrudTest {

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SchoolService schoolService;

    private School school;
    private SchoolRequest schoolRequest;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        school.setName("Colegiul Național");
        school.setCounty("Cluj");
        school.setAddress("Str. Clinicilor 12");
        school.setTaxId("RO12345");
        school.setCreatedAt(LocalDateTime.now());
        school.setUpdatedAt(LocalDateTime.now());

        schoolRequest = new SchoolRequest();
        schoolRequest.setName("Colegiul Național");
        schoolRequest.setCounty("Cluj");
        schoolRequest.setAddress("Str. Clinicilor 12"); // Aici era problema (lipsea adresa)
        schoolRequest.setTaxId("RO12345");
    }

    // --- CREATE ---
    @Test
    @DisplayName("Create - Creează cu succes o școală nouă")
    void create_createsSchoolSuccessfully() {
        when(schoolRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(schoolRepository.findByTaxId(anyString())).thenReturn(Optional.empty());
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> {
            School s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        SchoolResponse response = schoolService.create(schoolRequest);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Colegiul Național");
        verify(schoolRepository).save(any(School.class));
    }

    @Test
    @DisplayName("Create - Aruncă excepție dacă numele există deja")
    void create_throwsException_whenNameExists() {
        when(schoolRepository.findByName("Colegiul Național")).thenReturn(Optional.of(school));

        assertThatThrownBy(() -> schoolService.create(schoolRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("name already exists");
    }

    // --- READ ---
    @Test
    @DisplayName("FindAll - Returnează lista tuturor școlilor")
    void findAll_returnsAllSchools() {
        when(schoolRepository.findAll()).thenReturn(List.of(school));

        List<SchoolResponse> result = schoolService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Colegiul Național");
    }

    @Test
    @DisplayName("FindById - Returnează școala corectă")
    void findById_returnsSchool() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

        SchoolResponse response = schoolService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Colegiul Național");
    }

    @Test
    @DisplayName("FindById - Aruncă ResourceNotFoundException dacă nu există")
    void findById_throwsException_whenNotFound() {
        when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schoolService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- UPDATE ---
    @Test
    @DisplayName("Update - Actualizează datele cu succes")
    void update_updatesSuccessfully() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(schoolRepository.findByName("Colegiul Național")).thenReturn(Optional.of(school));
        when(schoolRepository.findByTaxId("RO12345")).thenReturn(Optional.of(school));
        when(schoolRepository.save(any(School.class))).thenReturn(school);

        SchoolResponse response = schoolService.update(1L, schoolRequest);

        assertThat(response.getName()).isEqualTo("Colegiul Național");
        verify(schoolRepository).save(school);
    }

    @Test
    @DisplayName("Update - Aruncă excepție dacă CUI-ul este luat de altă școală")
    void update_throwsException_whenTaxIdBelongsToOther() {
        School otherSchool = new School();
        otherSchool.setId(2L);

        when(schoolRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(schoolRepository.findByTaxId("RO12345")).thenReturn(Optional.of(otherSchool));

        assertThatThrownBy(() -> schoolService.update(1L, schoolRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tax ID is already used");
    }

    // --- DELETE ---
    @Test
    @DisplayName("Delete - Șterge școala dacă nu are profesori activi")
    void delete_deletesSuccessfully_whenNoActiveTeachers() {
        when(schoolRepository.existsById(1L)).thenReturn(true);
        when(userRepository.countBySchoolIdAndActiveTrue(1L)).thenReturn(0L);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

        schoolService.delete(1L);

        verify(schoolRepository).delete(school);
    }

    @Test
    @DisplayName("Delete - Aruncă excepție dacă are profesori activi")
    void delete_throwsException_whenHasActiveTeachers() {
        when(schoolRepository.existsById(1L)).thenReturn(true);
        when(userRepository.countBySchoolIdAndActiveTrue(1L)).thenReturn(5L);

        assertThatThrownBy(() -> schoolService.delete(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot delete a school that has");

        verify(schoolRepository, never()).delete(any());
    }
}