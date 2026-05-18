package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.UserRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    List<User> findBySchoolId(Long schoolId);
    
    List<User> findByRoleAndActive(UserRole role, Boolean active);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = ?1")
    Long countByRole(UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false ORDER BY u.createdAt ASC")
    List<User> findUnverifiedUsers();
    
    @Query("SELECT u FROM User u WHERE u.active = true AND u.role = ?1 ORDER BY u.lastName ASC, u.firstName ASC")
    List<User> findActiveUsersByRole(UserRole role);
}



