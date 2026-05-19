package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseCategoryRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseCategoryResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.repository.CourseCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CourseCategoryServiceCrudTest {

    @Autowired
    private CourseCategoryService categoryService;

    @Autowired
    private CourseCategoryRepository categoryRepository;

    @Test
    void testCreateAndFindCategory() {
        CourseCategoryRequest req = new CourseCategoryRequest();
        req.setName("Arte vizuale");
        req.setDescription("Cursuri de design și istoria artei");
        req.setColor("#FF5733");
        req.setActive(true);

        CourseCategoryResponse res = categoryService.create(req);
        assertNotNull(res.getId());
        assertEquals("Arte vizuale", res.getName());

        CourseCategoryResponse found = categoryService.findById(res.getId());
        assertEquals("#FF5733", found.getColor());
    }

    @Test
    void testUpdateCategory() {
        CourseCategoryRequest req = new CourseCategoryRequest();
        req.setName("Sport electronic");
        req.setColor("#000000");
        CourseCategoryResponse saved = categoryService.create(req);

        CourseCategoryRequest updateReq = new CourseCategoryRequest();
        updateReq.setName("Educație Fizică");
        updateReq.setColor("#BLUE01");

        CourseCategoryResponse updated = categoryService.update(saved.getId(), updateReq);
        assertEquals("Educație Fizică", updated.getName());
    }

    @Test
    void testDeleteCategory() {
        CourseCategoryRequest req = new CourseCategoryRequest();
        req.setName("De Șters");
        req.setColor("#FFF");
        CourseCategoryResponse saved = categoryService.create(req);

        categoryService.delete(saved.getId());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(saved.getId()));
    }
}