//package com.example.TeacherPlatform.service;
//
//import com.example.TeacherPlatform.dataTransferObject.*;
//import com.example.TeacherPlatform.model.enums.UserRole;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class CourseSessionServiceCrudTest {
//
//    @Autowired private CourseSessionService sessionService;
//    @Autowired private CourseService courseService;
//    @Autowired private CourseCategoryService categoryService;
//    @Autowired private UserService userService;
//
//    private Long courseId;
//
//    @BeforeEach
//    void setUp() {
//        CourseCategoryRequest cat = new CourseCategoryRequest();
//        cat.setName("Fizică"); cat.setColor("#111");
//        Long catId = categoryService.create(cat).getId();
//
//        UserRequest trainer = new UserRequest();
//        trainer.setFirstName("Ion"); trainer.setLastName("Popescu");
//        trainer.setEmail("ion@fizica.ro"); trainer.setPassword("123"); trainer.setRole(UserRole.FORMATOR);
//        Long tId = userService.create(trainer).getId();
//
//        CourseRequest course = new CourseRequest();
//        course.setTitle("Fizică Cuantică"); course.setCategoryId(catId); course.setTrainerId(tId);
//        course.setStartDate(LocalDate.now()); course.setEndDate(LocalDate.now().plusDays(2));
//        course.setCreditHours(10); course.setMaxParticipants(10);
//        this.courseId = courseService.create(course).getId();
//    }
//
//    @Test
//    void testCreateSession() {
//        CourseSessionRequest req = new CourseSessionRequest();
//        req.setCourseId(courseId);
//        req.setTopic("Mecanica Undelor");
//        req.setStartTime(LocalDateTime.now().plusDays(1));
//        req.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
//        req.setSessionNumber(1);
//
//        CourseSessionResponse res = sessionService.create(req);
//        assertNotNull(res.getId());
//        assertEquals("Mecanica Undelor", res.getTopic());
//        assertEquals("Fizică Cuantică", res.getCourseTitle());
//    }
//}