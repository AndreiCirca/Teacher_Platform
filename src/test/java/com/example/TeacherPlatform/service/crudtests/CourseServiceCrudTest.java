//package com.example.TeacherPlatform.service.crudtests;
//
//import com.example.TeacherPlatform.dataTransferObject.*;
//import com.example.TeacherPlatform.model.enums.UserRole;
//import com.example.TeacherPlatform.service.CourseService;
//import com.example.TeacherPlatform.service.UserService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.test.context.support.WithMockUser;
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
//    @Autowired private CourseService courseService;
//    @Autowired private UserService userService;
//
//    @Test
//    @WithMockUser(username = "formator@test.ro", authorities = {"FORMATOR"})
//    void testProposeCourse() {
//        // 1. Setup trainer
//        UserRequest trainerReq = new UserRequest();
//        trainerReq.setFirstName("F"); trainerReq.setLastName("T"); trainerReq.setEmail("formator@test.ro");
//        trainerReq.setPassword("1"); trainerReq.setRole(UserRole.FORMATOR);
//        userService.create(trainerReq);
//
//        // 2. Request curs
//        CourseRequest cr = new CourseRequest();
//        cr.setTitle("Curs Nou"); cr.setCategoryId(1L); cr.setTrainerId(1L); // ID-ul depinde de baza de date
//        cr.setStartDate(LocalDate.now()); cr.setEndDate(LocalDate.now().plusDays(2));
//        cr.setIsOnline(true); cr.setMeetingLink("https://link.ro");
//
//        // 3. Testam metoda propunere curs (care foloseste Authentication din context)
//        var res = courseService.proposeCourse(cr, null);
//
//        assertNotNull(res.getId());
//        assertEquals("Curs Nou", res.getTitle());
//    }
//}