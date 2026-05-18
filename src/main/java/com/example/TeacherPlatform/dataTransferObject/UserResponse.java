package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.UserRole;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private UserRole role;
    private Long schoolId;
    private String schoolName;
    private Boolean active;
    private Boolean emailVerified;
    private String phoneNumber;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}