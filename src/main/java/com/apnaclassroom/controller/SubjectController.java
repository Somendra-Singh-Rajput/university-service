package com.apnaclassroom.controller;

import com.apnaclassroom.model.management.Subject;
import com.apnaclassroom.model.user.RegisterResponse;
import com.apnaclassroom.model.user.User;
import com.apnaclassroom.service.SubjectService;
import com.mysql.cj.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/subject")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class SubjectController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectController.class);

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse addSubject(@RequestBody Subject subject,
                                        @AuthenticationPrincipal User user) {
        RegisterResponse response = null;
        LOG.info("Inside adding subject data method");
        if(Objects.nonNull(subject) && !StringUtils.isNullOrEmpty(user.getUsername())){
            response = subjectService.addSubjectData(subject, user.getUsername());
        }
        return response;
    }

    @GetMapping(path = "/get/{subjId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Subject getSubjectById(@PathVariable String subjId){
        Subject subject = null;
        LOG.info("Inside fetching subject data method for subjId: {}", subjId);

        if(!StringUtils.isNullOrEmpty(subjId)){
            subject =  subjectService.getSubjectDataById(subjId);
        }
        return subject;
    }

    @GetMapping(path = "/getAllSubjects/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Subject> getAllSubjects(){
        LOG.info("Inside fetching all subject data method");
        return subjectService.getAllSubjectsData();
    }

    @DeleteMapping(value = "/delete/{subjId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse deleteSubjectById(@PathVariable String subjId){
        RegisterResponse response = null;
        LOG.info("Inside deleting subject data method for subjId: {}", subjId);
        if(!StringUtils.isNullOrEmpty(subjId)){
            response = subjectService.deleteSubjectDataById(subjId);
        }
        return response;
    }

    @PutMapping(value = "/update/{subjId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse updateSubjectById(@RequestBody Subject subject, @PathVariable String subjId,
                                               @AuthenticationPrincipal User user){
        RegisterResponse response = null;
        LOG.info("Inside updating subject data method for subjId: {}", subjId);
        if(!StringUtils.isNullOrEmpty(subjId) && Objects.nonNull(subject)
                && !StringUtils.isNullOrEmpty(user.getUsername())){
            response = subjectService.updateSubjectDataById(subject, subjId, user.getUsername());
        }
        return response;
    }
}
