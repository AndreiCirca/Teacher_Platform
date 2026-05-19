package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.School;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolRepository extends BaseRepository<School> {
    
    Optional<School> findByName(String name);

    Optional<School> findByTaxId(String taxId);
    
    List<School> findByCounty(String county);

    List<School> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT s FROM School s ORDER BY s.teacherCount DESC")
    List<School> findByMostTeachers();
    
    @Query("SELECT s FROM School s WHERE s.county = ?1 ORDER BY s.name ASC")
    List<School> findByCountyOrderByName(String county);
}



