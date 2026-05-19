package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.*;
import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
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
public class EnrollmentServiceCrudTest {

    @Autowired private EnrollmentService enrollmentService;
    @Autowired private CourseService courseService;
    @Autowired private UserService userService;
    @Autowired private CourseCategoryService categoryService;

    private Long courseId;
    private Long teacherId;

    @BeforeEach
    void setUp() {
        CourseCategoryRequest cat = new CourseCategoryRequest();
        cat.setName("Biologie"); cat.setColor("#222");
        Long catId = categoryService.create(cat).getId();

        UserRequest trainer = new UserRequest();
        trainer.setFirstName("T1"); trainer.setLastName("L1"); trainer.setEmail("t1@test.ro");
        trainer.setPassword("1"); trainer.setRole(UserRole.FORMATOR);
        Long tId = userService.create(trainer).getId();

        UserRequest teacher = new UserRequest();
        teacher.setFirstName("George"); teacher.setLastName("Vancu"); teacher.setEmail("george@edu.ro");
        teacher.setPassword("2"); teacher.setRole(UserRole.PROFESOR);
        this.teacherId = userService.create(teacher).getId();

        CourseRequest cr = new CourseRequest();
        cr.setTitle("Botanică"); cr.setCategoryId(catId); cr.setTrainerId(tId);
        cr.setStartDate(LocalDate.now()); cr.setEndDate(LocalDate.now().plusDays(1));
        cr.setCreditHours(5); cr.setMaxParticipants(5);
        this.courseId = courseService.create(cr).getId();
    }

    @Test
    void testCreateEnrollment() {
        EnrollmentRequest req = new EnrollmentRequest();
        req.setCourseId(courseId);
        req.setTeacherId(teacherId);
        req.setStatus(EnrollmentStatus.PENDING);

        EnrollmentResponse res = enrollmentService.create(req);
        assertNotNull(res.getId());
        assertEquals("George Vancu", res.getTeacherFullName());
        assertEquals(EnrollmentStatus.PENDING, res.getStatus());
    }
}