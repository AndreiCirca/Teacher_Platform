package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.SchoolRequest;
import com.example.TeacherPlatform.dataTransferObject.SchoolResponse;
import com.example.TeacherPlatform.dataTransferObject.UserRequest;
import com.example.TeacherPlatform.dataTransferObject.UserResponse;
import com.example.TeacherPlatform.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceCrudTest {

    @Autowired private UserService userService;
    @Autowired private SchoolService schoolService;

    private Long schoolId;

    @BeforeEach
    void setUp() {
        SchoolRequest sr = new SchoolRequest();
        sr.setName("Școala Generală de Test nr. 4");
        sr.setCounty("Satu Mare");
        sr.setTaxId("RO667788");
        SchoolResponse schoolRes = schoolService.create(sr);
        this.schoolId = schoolRes.getId();
    }

    @Test
    void testCreateAndFindUser() {
        UserRequest req = new UserRequest();
        req.setFirstName("Andreea");
        req.setLastName("Banu");
        req.setEmail("andreea.banu@edu.ro");
        req.setPassword("PlaintextPassword123");
        req.setRole(UserRole.PROFESOR);
        req.setSchoolId(schoolId);
        req.setPhoneNumber("0722111222");

        UserResponse res = userService.create(req);
        assertNotNull(res.getId());
        assertEquals("Andreea Banu", res.getFullName());
        assertEquals("Școala Generală de Test nr. 4", res.getSchoolName());

        UserResponse found = userService.findById(res.getId());
        assertEquals("andreea.banu@edu.ro", found.getEmail());
    }
}