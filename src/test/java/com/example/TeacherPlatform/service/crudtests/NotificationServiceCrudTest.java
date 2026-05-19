//package com.example.TeacherPlatform.service;
//
//import com.example.TeacherPlatform.dataTransferObject.NotificationRequest;
//import com.example.TeacherPlatform.dataTransferObject.NotificationResponse;
//import com.example.TeacherPlatform.dataTransferObject.UserRequest;
//import com.example.TeacherPlatform.model.enums.NotificationType;
//import com.example.TeacherPlatform.model.enums.UserRole;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class NotificationServiceCrudTest {
//
//    @Autowired private NotificationService notificationService;
//    @Autowired private UserService userService;
//
//    private Long userId;
//
//    @BeforeEach
//    void setUp() {
//        UserRequest ur = new UserRequest();
//        ur.setFirstName("Marc");
//        ur.setLastName("Vlad");
//        // Am schimbat email-ul pentru a fi unic în contextul testului
//        ur.setEmail("marc_test_notification@edu.ro");
//        ur.setPassword("pass");
//        ur.setRole(UserRole.PROFESOR);
//        this.userId = userService.create(ur).getId();
//    }
//
//    @Test
//    void testCreateNotification() {
//        NotificationRequest req = new NotificationRequest();
//        req.setUserId(userId);
//        req.setTitle("Material nou adăugat");
//        req.setMessage("Formatorul a încărcat fișierul de laborator.");
//        req.setType(NotificationType.INFO);
//        req.setActionUrl("/materials");
//
//        NotificationResponse res = notificationService.create(req);
//
//        assertNotNull(res.getId());
//        assertEquals("Material nou adăugat", res.getTitle());
//        assertFalse(res.getRead());
//        // Verificăm folosind noul email setat mai sus
//        assertEquals("marc_test_notification@edu.ro", res.getUserEmail());
//    }
//}