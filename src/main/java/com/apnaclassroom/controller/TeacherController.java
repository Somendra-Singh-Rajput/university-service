package com.apnaclassroom.controller;

import com.apnaclassroom.model.teacher.Teacher;
import com.apnaclassroom.model.user.RegisterResponse;
import com.apnaclassroom.model.user.User;
import com.apnaclassroom.service.TeacherService;
import com.mysql.cj.util.StringUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/teacher")
@SecurityRequirement(name = "bearerAuth")
public class TeacherController {

    private static final Logger LOG = LoggerFactory.getLogger(TeacherController.class);

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public RegisterResponse saveTeacherData(@RequestBody Teacher teacher,
                                            @AuthenticationPrincipal User user) {
        RegisterResponse registerResponse = null;
        LOG.info("Inside saving teacher data method");
        if(Objects.nonNull(teacher) && !StringUtils.isNullOrEmpty(user.getUsername())){
            registerResponse = teacherService.saveTeacherData(teacher, user.getUsername());
        }
        return registerResponse;
    }

    @GetMapping(path = "/get/{teacherId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')")
    public Teacher getTeacherDataById(@PathVariable String teacherId){
        Teacher teacher = null;
        LOG.info("Inside fetching teacher data method for teacherId: {}", teacherId);

        if(!StringUtils.isNullOrEmpty(teacherId)){
            teacher =  teacherService.getTeacherDataById(teacherId);
        }
        return teacher;
    }

    @GetMapping(path = "/getAllTeachersData", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<Teacher> getAllTeachersData(){
        LOG.info("Inside fetching all teachers data method");
        return teacherService.getAllTeachersData();
    }

    @DeleteMapping(value = "/delete/{teacherId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public RegisterResponse deleteTeacherDataById(@PathVariable String teacherId,
                                                  @AuthenticationPrincipal User user){
        RegisterResponse registerResponse = null;
        LOG.info("Inside deleting teacher data method for teacherId: {}", teacherId);
        if(!StringUtils.isNullOrEmpty(teacherId) && !StringUtils.isNullOrEmpty(user.getUsername())){
            registerResponse = teacherService.deleteTeacherDataById(teacherId, user.getUsername());
        }
        return registerResponse;
    }

    @PutMapping(value = "/update/{teacherId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')")
    public Teacher updateTeacherDataById(@RequestBody Teacher teacher, @PathVariable String teacherId,
                                         @AuthenticationPrincipal User user) {
        Teacher updatedTeacher = new Teacher();
        LOG.info("Inside update teacher data method");
        if(Objects.nonNull(teacher) && !StringUtils.isNullOrEmpty(teacherId)
                && !StringUtils.isNullOrEmpty(user.getUsername())){
            updatedTeacher = teacherService.updateTeacherDataById(teacher, teacherId, user.getUsername());
        } else{
            LOG.info("Teacher data or teacherId is either empty or null");
        }
        return updatedTeacher;
    }
}
