package com.apnaclassroom.controller;

import com.apnaclassroom.model.management.Semester;
import com.apnaclassroom.model.user.RegisterResponse;
import com.apnaclassroom.model.user.User;
import com.apnaclassroom.service.SemesterService;
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
@RequestMapping("/api/v1/semester")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@SecurityRequirement(name = "bearerAuth")
public class SemesterController {

    private static final Logger LOG = LoggerFactory.getLogger(SemesterController.class);

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse addSemester(@RequestBody Semester semester,
                                      @AuthenticationPrincipal User user) {
        RegisterResponse response = null;
        LOG.info("Inside adding semester data method");
        if(Objects.nonNull(semester) && !StringUtils.isNullOrEmpty(user.getUsername())){
            response = semesterService.addSemesterData(semester, user.getUsername());
        }
        return response;
    }

    @GetMapping(path = "/get/{semId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Semester getSemesterById(@PathVariable String semId){
        Semester semester = null;
        LOG.info("Inside fetching semester data method for semId: {}", semId);

        if(!StringUtils.isNullOrEmpty(semId)){
            semester =  semesterService.getSemesterDataById(semId);
        }
        return semester;
    }

    @GetMapping(path = "/getAllSemesters/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Semester> getAllSemesters(){
        LOG.info("Inside fetching all semester data method");
        return semesterService.getAllSemestersData();
    }

    @DeleteMapping(value = "/delete/{semId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse deleteSemesterById(@PathVariable String semId){
        RegisterResponse response = null;
        LOG.info("Inside deleting semester data method for semId: {}", semId);
        if(!StringUtils.isNullOrEmpty(semId)){
            response = semesterService.deleteSemesterDataById(semId);
        }
        return response;
    }

    @PutMapping(value = "/update/{semId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse updateSemesterById(@RequestBody Semester semester, @PathVariable String semId,
                                             @AuthenticationPrincipal User user){
        RegisterResponse response = null;
        LOG.info("Inside updating semester data method for deptId: {}", semId);
        if(!StringUtils.isNullOrEmpty(semId) && Objects.nonNull(semester)
                && !StringUtils.isNullOrEmpty(user.getUsername())){
            response = semesterService.updateSemesterDataById(semester, semId, user.getUsername());
        }
        return response;
    }
}
