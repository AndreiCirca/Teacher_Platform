package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CourseSessionRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseSessionResponse;
import com.example.TeacherPlatform.model.CourseSession;
import com.example.TeacherPlatform.service.CourseSessionService;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/course-sessions")
@RequiredArgsConstructor
public class CourseSessionController extends GenericController<CourseSession, CourseSessionRequest, CourseSessionResponse> {

    private final CourseSessionService courseSessionService;

    @Override
    protected GenericService<CourseSession, CourseSessionRequest, CourseSessionResponse> getService() {
        return courseSessionService;
    }
}