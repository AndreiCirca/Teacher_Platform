package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.SchoolRequest;
import com.example.TeacherPlatform.dataTransferObject.SchoolResponse;
import com.example.TeacherPlatform.model.School;
import com.example.TeacherPlatform.service.SchoolService;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController extends GenericController<School, SchoolRequest, SchoolResponse> {

    private final SchoolService schoolService;

    @Override
    protected GenericService<School, SchoolRequest, SchoolResponse> getService() {
        return schoolService;
    }
}