package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.SchoolRequest;
import com.example.TeacherPlatform.dataTransferObject.SchoolResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.repository.SchoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SchoolServiceCrudTest {

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private SchoolRepository schoolRepository;

    private SchoolRequest testSchoolRequest;

    @BeforeEach
    void setUp() {
        testSchoolRequest = new SchoolRequest();
        testSchoolRequest.setName("Liceul Teoretic de Test");
        testSchoolRequest.setCounty("Cluj");
        testSchoolRequest.setTaxId("RO99988877");
        testSchoolRequest.setAddress("Strada Testului nr. 1");
        testSchoolRequest.setDirectorEmail("director.test@scoala.ro");
        testSchoolRequest.setTeacherCount(20);
    }

    @Test
    void testCreateSchool_ShouldSaveAndReturnResponse() {
        SchoolResponse response = schoolService.create(testSchoolRequest);

        assertNotNull(response.getId());
        assertEquals("Liceul Teoretic de Test", response.getName());
        assertEquals("Cluj", response.getCounty());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void testFindSchoolById_ShouldReturnCorrectSchool() {
        SchoolResponse savedSchool = schoolService.create(testSchoolRequest);

        SchoolResponse foundSchool = schoolService.findById(savedSchool.getId());

        assertNotNull(foundSchool);
        assertEquals(savedSchool.getId(), foundSchool.getId());
        assertEquals("Liceul Teoretic de Test", foundSchool.getName());
    }

    @Test
    void testFindSchoolById_WhenIdDoesNotExist_ShouldThrowException() {
        Long invalidId = 999999L;

        assertThrows(ResourceNotFoundException.class, () -> {
            schoolService.findById(invalidId);
        });
    }

    @Test
    void testUpdateSchool_ShouldModifyFieldsCorrectly() {
        SchoolResponse savedSchool = schoolService.create(testSchoolRequest);

        SchoolRequest updateRequest = new SchoolRequest();
        updateRequest.setName("Nume Școală Actualizat");
        updateRequest.setCounty("Sălaj");
        updateRequest.setTaxId(savedSchool.getTaxId());
        updateRequest.setTeacherCount(35);

        SchoolResponse updatedResponse = schoolService.update(savedSchool.getId(), updateRequest);

        assertEquals(savedSchool.getId(), updatedResponse.getId());
        assertEquals("Nume Școală Actualizat", updatedResponse.getName());
        assertEquals("Sălaj", updatedResponse.getCounty());
        assertEquals(35, updatedResponse.getTeacherCount());
    }

    @Test
    void testDeleteSchool_ShouldRemoveFromDatabase() {
        SchoolResponse savedSchool = schoolService.create(testSchoolRequest);
        Long schoolId = savedSchool.getId();

        schoolService.delete(schoolId);

        assertThrows(ResourceNotFoundException.class, () -> {
            schoolService.findById(schoolId);
        });
    }

    @Test
    void testFindAllSchools_ShouldReturnList() {
        int initialSize = schoolService.findAll().size();

        schoolService.create(testSchoolRequest);

        SchoolRequest secondSchool = new SchoolRequest();
        secondSchool.setName("A doua școală de test");
        secondSchool.setCounty("Bihor");
        secondSchool.setTaxId("RO55544433");
        schoolService.create(secondSchool);

        List<SchoolResponse> allSchools = schoolService.findAll();


        assertEquals(initialSize + 2, allSchools.size(), "Lista ar trebui să crească cu exact 2 școli");
    }
}