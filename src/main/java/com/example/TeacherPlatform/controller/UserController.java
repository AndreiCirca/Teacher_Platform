package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.UserRequest;
import com.example.TeacherPlatform.dataTransferObject.UserResponse;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.service.UserService;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends GenericController<User, UserRequest, UserResponse> {

    private final UserService userService;

    @Override
    protected GenericService<User, UserRequest, UserResponse> getService() {
        return userService;
    }
}