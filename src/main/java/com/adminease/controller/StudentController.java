package com.adminease.controller;

import com.adminease.model.FeeReceipt;
import com.adminease.model.student.Student;
import com.adminease.model.user.RegisterResponse;
import com.adminease.model.user.User;
import com.adminease.service.StudentService;
import com.mysql.cj.util.StringUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/student")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private static final Logger LOG = LoggerFactory.getLogger(StudentController.class);

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public RegisterResponse saveStudentData(@RequestBody Student student,
                                            @AuthenticationPrincipal User user) {
        RegisterResponse registerResponse = null;
        LOG.info("Inside saving student data method");
        if(Objects.nonNull(student) && !StringUtils.isNullOrEmpty(user.getUsername())){
            registerResponse = studentService.saveStudentData(student, user.getUsername());
        }
        return registerResponse;
    }

    @GetMapping(path = "/get/{studentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public Student getStudentDataById(@PathVariable String studentId){
        Student student = null;
        LOG.info("Inside fetching student data method for studentId: {}", studentId);

        if(!StringUtils.isNullOrEmpty(studentId)){
             student =  studentService.getStudentDataById(studentId);
        }
        return student;
    }

    @GetMapping(path = "/getAllStudentsData", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<Student> getAllStudentsData(){
        LOG.info("Inside fetching all students data method");
        return studentService.getAllStudentsData();
    }

    @DeleteMapping(value = "/delete/{studentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public RegisterResponse deleteStudentDataById(@PathVariable String studentId,
                                                  @AuthenticationPrincipal User user){
        RegisterResponse registerResponse = null;
        LOG.info("Inside deleting student data method for studentId: {}", studentId);
        if(!StringUtils.isNullOrEmpty(studentId) && !StringUtils.isNullOrEmpty(user.getUsername())){
            registerResponse = studentService.deleteStudentDataById(studentId, user.getUsername());
        }
        return registerResponse;
    }

    @PutMapping(value = "/update/{studentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public Student updateStudentDataById(@RequestBody Student student, @PathVariable String studentId,
                                         @AuthenticationPrincipal User user) {
        Student updatedStudent = new Student();
        LOG.info("Inside update student data method");
        if(Objects.nonNull(student) && !StringUtils.isNullOrEmpty(studentId)
                && !StringUtils.isNullOrEmpty(user.getUsername())){
            updatedStudent = studentService.updateStudentDataById(student, studentId, user.getUsername());
        } else{
            LOG.info("Student data or StudentId is either empty or null");
        }
        return updatedStudent;
    }

    @GetMapping(path = "/exportStudents", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void exportStudentsData() throws IOException {
        LOG.info("Inside exporting student data in excel method");
        studentService.exportStudentsData();
    }

    @PostMapping(value = "/generateReceipt", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void generatePaymentReceipt(@RequestBody FeeReceipt feeReceipt,
                                            @AuthenticationPrincipal User user) throws IOException {
        LOG.info("Inside generate payment receipt method");
        if(Objects.nonNull(feeReceipt) && !StringUtils.isNullOrEmpty(user.getUsername())){
             studentService.generatePaymentReceipt(feeReceipt, user.getUsername());
        }
    }
}
