package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.*;
import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import com.example.TeacherPlatform.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
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

    @BeforeEach
    void setUp() {
        // Setup date de bază (fără a mai fi nevoie de Authentication aici)
        CourseCategoryRequest cat = new CourseCategoryRequest();
        cat.setName("Biologie"); cat.setColor("#000000");
        Long catId = categoryService.create(cat).getId();

        UserRequest trainer = new UserRequest();
        trainer.setFirstName("Formator"); trainer.setLastName("Test"); trainer.setEmail("t@test.ro");
        trainer.setPassword("1"); trainer.setRole(UserRole.FORMATOR);
        Long tId = userService.create(trainer).getId();

        CourseRequest cr = new CourseRequest();
        cr.setTitle("Botanică"); cr.setCategoryId(catId); cr.setTrainerId(tId);
        cr.setStartDate(LocalDate.now()); cr.setEndDate(LocalDate.now().plusDays(5));
        cr.setCreditHours(10); cr.setMaxParticipants(10);
        cr.setIsOnline(true);
        cr.setMeetingLink("https://link.ro");

        // Cursul creat ca Admin pentru a evita erori de permisiune la setup
        this.courseId = courseService.create(cr).getId();
    }

    @Test
    @WithMockUser(username = "prof@test.ro", authorities = {"PROFESOR"})
    void testCreateEnrollment() {
        // Creăm userul necesar testului
        UserRequest prof = new UserRequest();
        prof.setFirstName("Prof"); prof.setLastName("Test"); prof.setEmail("prof@test.ro");
        prof.setPassword("1"); prof.setRole(UserRole.PROFESOR);
        userService.create(prof);

        EnrollmentRequest req = new EnrollmentRequest();
        req.setCourseId(courseId);

        // Testăm cu contextul de securitate simulat prin @WithMockUser
        EnrollmentResponse res = enrollmentService.createEnrollment(req, null); // null pt că Authentication e injectat de SecurityContextHolder

        assertNotNull(res.getId());
        assertEquals(EnrollmentStatus.PENDING, res.getStatus());
    }
}