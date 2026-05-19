package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.*;
import com.example.TeacherPlatform.model.enums.CertificateStatus;
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
public class CertificateServiceCrudTest {

    @Autowired private CertificateService certificateService;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private CourseService courseService;
    @Autowired private UserService userService;
    @Autowired private CourseCategoryService categoryService;

    private Long enrollmentId;

    @BeforeEach
    void setUp() {
        CourseCategoryRequest cat = new CourseCategoryRequest();
        cat.setName("Muzică"); cat.setColor("#555");
        Long catId = categoryService.create(cat).getId();

        UserRequest tr = new UserRequest();
        tr.setFirstName("M"); tr.setLastName("M"); tr.setEmail("muz@test.ro");
        tr.setPassword("1"); tr.setRole(UserRole.FORMATOR);
        Long tId = userService.create(tr).getId();

        UserRequest pr = new UserRequest();
        pr.setFirstName("Laura"); pr.setLastName("Dinu"); pr.setEmail("laura@edu.ro");
        pr.setPassword("2"); pr.setRole(UserRole.PROFESOR);
        Long profId = userService.create(pr).getId();

        CourseRequest cr = new CourseRequest();
        cr.setTitle("Teoria Muzicii"); cr.setCategoryId(catId); cr.setTrainerId(tId);
        cr.setStartDate(LocalDate.now()); cr.setEndDate(LocalDate.now().plusDays(2));
        cr.setCreditHours(8); cr.setMaxParticipants(10);
        Long cId = courseService.create(cr).getId();

        EnrollmentRequest en = new EnrollmentRequest();
        en.setCourseId(cId); en.setTeacherId(profId);
        this.enrollmentId = enrollmentService.create(en).getId();
    }

    @Test
    void testCreateCertificate() {
        CertificateRequest req = new CertificateRequest();
        req.setEnrollmentId(enrollmentId);
        req.setCertificateCode("FORM-2026-99999");
        req.setIssuedDate(LocalDate.now());
        req.setStatus(CertificateStatus.ACTIVE);
        req.setCertificateUrl("https://storage.supabase.com/laura_cert.pdf");

        CertificateResponse res = certificateService.create(req);
        assertNotNull(res.getId());
        assertEquals("FORM-2026-99999", res.getCertificateCode());
        assertEquals("Laura Dinu", res.getTeacherFullName());
        assertEquals("Teoria Muzicii", res.getCourseTitle());
    }
}