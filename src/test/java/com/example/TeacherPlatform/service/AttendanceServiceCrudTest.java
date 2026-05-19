package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.*;
import com.example.TeacherPlatform.model.enums.AttendanceStatus;
import com.example.TeacherPlatform.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AttendanceServiceCrudTest {

    @Autowired private AttendanceService attendanceService;
    @Autowired private CourseSessionService sessionService;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private CourseService courseService;
    @Autowired private UserService userService;
    @Autowired private CourseCategoryService categoryService;

    private Long sessionId;
    private Long enrollmentId;

    @BeforeEach
    void setUp() {
        CourseCategoryRequest cat = new CourseCategoryRequest();
        cat.setName("Chimie"); cat.setColor("#333");
        Long catId = categoryService.create(cat).getId();

        UserRequest tr = new UserRequest();
        tr.setFirstName("F"); tr.setLastName("L"); tr.setEmail("form@test.ro");
        tr.setPassword("1"); tr.setRole(UserRole.FORMATOR);
        Long tId = userService.create(tr).getId();

        UserRequest pr = new UserRequest();
        pr.setFirstName("Alin"); pr.setLastName("Blaj"); pr.setEmail("alin@edu.ro");
        pr.setPassword("2"); pr.setRole(UserRole.PROFESOR);
        Long profId = userService.create(pr).getId();

        CourseRequest cr = new CourseRequest();
        cr.setTitle("Chimie Organică"); cr.setCategoryId(catId); cr.setTrainerId(tId);
        cr.setStartDate(LocalDate.now()); cr.setEndDate(LocalDate.now().plusDays(2));
        cr.setCreditHours(6); cr.setMaxParticipants(6);
        Long cId = courseService.create(cr).getId();

        CourseSessionRequest ss = new CourseSessionRequest();
        ss.setCourseId(cId); ss.setTopic("Alcane");
        ss.setStartTime(LocalDateTime.now()); ss.setEndTime(LocalDateTime.now().plusHours(1));
        ss.setSessionNumber(1);
        this.sessionId = sessionService.create(ss).getId();

        EnrollmentRequest en = new EnrollmentRequest();
        en.setCourseId(cId); en.setTeacherId(profId);
        this.enrollmentId = enrollmentService.create(en).getId();
    }

    @Test
    void testCreateAttendance() {
        AttendanceRequest req = new AttendanceRequest();
        req.setSessionId(sessionId);
        req.setEnrollmentId(enrollmentId);
        req.setStatus(AttendanceStatus.PRESENT);

        AttendanceResponse res = attendanceService.create(req);
        assertNotNull(res.getId());
        assertEquals(AttendanceStatus.PRESENT, res.getStatus());
        assertEquals("Alin Blaj", res.getTeacherFullName());
    }
}