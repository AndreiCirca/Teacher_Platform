package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.AttendanceRequest;
import com.example.TeacherPlatform.dataTransferObject.AttendanceResponse;
import com.example.TeacherPlatform.model.Attendance;
import com.example.TeacherPlatform.service.AttendanceService;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController extends GenericController<Attendance, AttendanceRequest, AttendanceResponse> {

    private final AttendanceService attendanceService;

    @Override
    protected GenericService<Attendance, AttendanceRequest, AttendanceResponse> getService() {
        return attendanceService;
    }
}