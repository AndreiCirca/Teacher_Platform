//package com.example.TeacherPlatform.service;
//
//import com.example.TeacherPlatform.dataTransferObject.CourseCategoryRequest;
//import com.example.TeacherPlatform.dataTransferObject.CourseCategoryResponse;
//import com.example.TeacherPlatform.dataTransferObject.CourseRequest;
//import com.example.TeacherPlatform.dataTransferObject.CourseResponse;
//import com.example.TeacherPlatform.dataTransferObject.UserRequest;
//import com.example.TeacherPlatform.dataTransferObject.UserResponse;
//import com.example.TeacherPlatform.model.enums.CourseStatus;
//import com.example.TeacherPlatform.model.enums.UserRole;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class CourseServiceCrudTest {
//
//    @Autowired
//    private CourseService courseService;
//    @Autowired
//    private CourseCategoryService categoryService;
//    @Autowired
//    private UserService userService;
//
//    private Long categoryId;
//    private Long trainerId;
//
//    @BeforeEach
//    void setUp() {
//        CourseCategoryRequest catReq = new CourseCategoryRequest();
//        catReq.setName("Istorie");
//        catReq.setColor("#A1B2C3");
//        CourseCategoryResponse catRes = categoryService.create(catReq);
//        this.categoryId = catRes.getId();
//
//        UserRequest userReq = new UserRequest();
//        userReq.setFirstName("Dan");
//        userReq.setLastName("Nistor");
//        userReq.setEmail("dan.nistor@formaprof.ro");
//        userReq.setPassword("Plaintext123!");
//        userReq.setRole(UserRole.FORMATOR);
//        UserResponse userRes = userService.create(userReq);
//        this.trainerId = userRes.getId();
//    }
//
//    @Test
//    void testCreateAndFindCourse() {
//        CourseRequest req = new CourseRequest();
//        req.setTitle("Istoria Modernă a României");
//        req.setDescription("Curs axat pe secolul XX");
//        req.setCategoryId(categoryId);
//        req.setTrainerId(trainerId);
//        req.setStartDate(LocalDate.now().plusDays(1));
//        req.setEndDate(LocalDate.now().plusDays(5));
//        req.setCreditHours(15);
//        req.setMaxParticipants(20);
//
//        CourseResponse res = courseService.create(req);
//        assertNotNull(res.getId());
//        assertEquals("Istoria Modernă a României", res.getTitle());
//        assertEquals("Dan Nistor", res.getTrainerFullName());
//
//        CourseResponse found = courseService.findById(res.getId());
//        assertEquals(CourseStatus.DRAFT, found.getStatus());
//    }
//}