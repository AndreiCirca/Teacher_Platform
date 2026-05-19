package com.example.TeacherPlatform.service.functinalitiestests;

import com.example.TeacherPlatform.dataTransferObject.CourseCategoryResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.CourseCategory;
import com.example.TeacherPlatform.repository.CourseCategoryRepository;
import com.example.TeacherPlatform.service.CourseCategoryService;
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
@DisplayName("CourseCategoryService - Functionality Tests")
class CourseCategoryServiceTest {

    @Mock private CourseCategoryRepository courseCategoryRepository;

    @InjectMocks
    private CourseCategoryService courseCategoryService;

    private CourseCategory activeCategory;
    private CourseCategory inactiveCategory;

    @BeforeEach
    void setUp() {
        activeCategory = buildCategory(1L, "Robotică", "#E74C3C", true);
        inactiveCategory = buildCategory(2L, "Arte", "#3498DB", false);
    }

    // -------------------------------------------------------------------------
    // findAllActive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAllActive - returneaza doar categoriile active")
    void findAllActive_returnsOnlyActiveCategories() {
        when(courseCategoryRepository.findByActiveTrue()).thenReturn(List.of(activeCategory));

        List<CourseCategoryResponse> result = courseCategoryService.findAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Robotică");
        assertThat(result.get(0).getActive()).isTrue();
        verify(courseCategoryRepository).findByActiveTrue();
    }

    @Test
    @DisplayName("findAllActive - returneaza lista goala daca nu exista categorii active")
    void findAllActive_returnsEmptyList_whenNoActiveCategories() {
        when(courseCategoryRepository.findByActiveTrue()).thenReturn(List.of());

        List<CourseCategoryResponse> result = courseCategoryService.findAllActive();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllActive - nu include categoriile inactive")
    void findAllActive_doesNotIncludeInactiveCategories() {
        when(courseCategoryRepository.findByActiveTrue()).thenReturn(List.of(activeCategory));

        List<CourseCategoryResponse> result = courseCategoryService.findAllActive();

        assertThat(result).extracting(CourseCategoryResponse::getName)
                .doesNotContain("Arte");
    }

    @Test
    @DisplayName("findAllActive - mapeaza corect campurile response")
    void findAllActive_mapsFieldsCorrectly() {
        when(courseCategoryRepository.findByActiveTrue()).thenReturn(List.of(activeCategory));

        List<CourseCategoryResponse> result = courseCategoryService.findAllActive();

        CourseCategoryResponse resp = result.get(0);
        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getName()).isEqualTo("Robotică");
        assertThat(resp.getColor()).isEqualTo("#E74C3C");
        assertThat(resp.getActive()).isTrue();
    }

    // -------------------------------------------------------------------------
    // toggleActive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toggleActive - dezactiveaza o categorie activa")
    void toggleActive_deactivatesActiveCategory() {
        when(courseCategoryRepository.findById(1L)).thenReturn(Optional.of(activeCategory));
        when(courseCategoryRepository.save(any(CourseCategory.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseCategoryResponse result = courseCategoryService.toggleActive(1L);

        assertThat(result.getActive()).isFalse();
        verify(courseCategoryRepository).save(activeCategory);
    }

    @Test
    @DisplayName("toggleActive - activeaza o categorie inactiva")
    void toggleActive_activatesInactiveCategory() {
        when(courseCategoryRepository.findById(2L)).thenReturn(Optional.of(inactiveCategory));
        when(courseCategoryRepository.save(any(CourseCategory.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseCategoryResponse result = courseCategoryService.toggleActive(2L);

        assertThat(result.getActive()).isTrue();
        verify(courseCategoryRepository).save(inactiveCategory);
    }

    @Test
    @DisplayName("toggleActive - arunca exceptie daca categoria nu exista")
    void toggleActive_throwsException_whenCategoryNotFound() {
        when(courseCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseCategoryService.toggleActive(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(courseCategoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("toggleActive - apeleaza save o singura data")
    void toggleActive_callsSaveOnce() {
        when(courseCategoryRepository.findById(1L)).thenReturn(Optional.of(activeCategory));
        when(courseCategoryRepository.save(any(CourseCategory.class))).thenAnswer(inv -> inv.getArgument(0));

        courseCategoryService.toggleActive(1L);

        verify(courseCategoryRepository, times(1)).save(any(CourseCategory.class));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CourseCategory buildCategory(Long id, String name, String color, boolean active) {
        CourseCategory cat = new CourseCategory();
        cat.setId(id); cat.setName(name); cat.setColor(color); cat.setActive(active);
        cat.setCreatedAt(LocalDateTime.now()); cat.setUpdatedAt(LocalDateTime.now());
        return cat;
    }
}