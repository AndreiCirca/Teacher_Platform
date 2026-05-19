package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.*;
import com.example.TeacherPlatform.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CourseMaterialServiceCrudTest {

    @Autowired private CourseMaterialService materialService;
    @Autowired private CourseService courseService;
    @Autowired private UserService userService;
    @Autowired private CourseCategoryService categoryService;

    private Long courseId;

    @BeforeEach
    void setUp() {
        CourseCategoryRequest cat = new CourseCategoryRequest();
        cat.setName("Geografie"); cat.setColor("#444");
        Long catId = categoryService.create(cat).getId();

        UserRequest tr = new UserRequest();
        tr.setFirstName("G"); tr.setLastName("G"); tr.setEmail("geo@test.ro");
        tr.setPassword("1"); tr.setRole(UserRole.FORMATOR);
        Long tId = userService.create(tr).getId();

        CourseRequest cr = new CourseRequest();
        cr.setTitle("Geografia Europei"); cr.setCategoryId(catId); cr.setTrainerId(tId);
        cr.setStartDate(LocalDate.now()); cr.setEndDate(LocalDate.now().plusDays(2));
        cr.setCreditHours(4); cr.setMaxParticipants(5);
        this.courseId = courseService.create(cr).getId();
    }

    @Test
    void testCreateMaterial() {
        CourseMaterialRequest req = new CourseMaterialRequest();
        req.setCourseId(courseId);
        req.setFileName("Harta_Munti.pdf");
        req.setFileType("application/pdf");
        req.setFileSize(2048576L);
        req.setFileUrl("https://storage.supabase.com/harta.pdf");
        req.setDescription("Harta fizică a Europei");

        CourseMaterialResponse res = materialService.create(req);
        assertNotNull(res.getId());
        assertEquals("Harta_Munti.pdf", res.getFileName());
        assertEquals("Geografia Europei", res.getCourseTitle());
    }
}