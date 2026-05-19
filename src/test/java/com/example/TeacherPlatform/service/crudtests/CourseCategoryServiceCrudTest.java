package com.example.TeacherPlatform.service.crudtests;

import com.example.TeacherPlatform.dataTransferObject.CourseCategoryRequest;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseCategoryService - CRUD Tests")
class CourseCategoryServiceCrudTest {

    @Mock
    private CourseCategoryRepository categoryRepository;

    @InjectMocks
    private CourseCategoryService categoryService;

    private CourseCategory category;
    private CourseCategoryRequest request;

    @BeforeEach
    void setUp() {
        category = new CourseCategory();
        category.setId(1L);
        category.setName("Matematică");
        category.setColor("#FF0000");
        category.setActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        request = new CourseCategoryRequest();
        request.setName("Matematică");
        request.setColor("#FF0000");
        request.setActive(true);
    }

    @Test
    @DisplayName("Create - Adaugă o categorie nouă cu succes")
    void create_createsCategorySuccessfully() {
        when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CourseCategory.class))).thenAnswer(i -> {
            CourseCategory c = i.getArgument(0);
            c.setId(10L);
            return c;
        });

        CourseCategoryResponse response = categoryService.create(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Matematică");
        verify(categoryRepository).save(any());
    }

    @Test
    @DisplayName("Create - Aruncă excepție dacă numele există deja")
    void create_throwsException_whenNameExists() {
        when(categoryRepository.findByNameIgnoreCase("Matematică")).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Delete - Șterge categoria dacă nu are cursuri")
    void delete_deletesSuccessfully_whenNoCourses() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.hasCourses(1L)).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("Delete - Aruncă excepție dacă are cursuri asociate")
    void delete_throwsException_whenHasCourses() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.hasCourses(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot delete a category that has courses");

        verify(categoryRepository, never()).delete(any());
    }
}