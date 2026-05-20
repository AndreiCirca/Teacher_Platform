package com.example.TeacherPlatform.dataTransferObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
}