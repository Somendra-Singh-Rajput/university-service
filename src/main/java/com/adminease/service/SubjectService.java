package com.adminease.service;

import com.adminease.dao.SubjectDao;
import com.adminease.model.management.Subject;
import com.adminease.model.user.RegisterResponse;
import com.adminease.util.RandomIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.adminease.model.CommonConstants.SUBJECT;
import static com.adminease.model.CommonConstants.SUBJECTID;

@Service
public class SubjectService {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectService.class);

    private final SubjectDao subjectDao;

    public SubjectService(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    public RegisterResponse addSubjectData(Subject subject, String createBy) {
        RegisterResponse response;
        Subject exitingSubData = subjectDao.checkRegisteredSubject(subject.getSubjName());

        if(StringUtils.isEmpty(exitingSubData.getSubjId()) && StringUtils.isEmpty(exitingSubData.getSubjName())){
            subject.setSubjId(RandomIdGenerator.generateId(SUBJECT));
            subject.setCreateTs(String.valueOf(LocalDateTime.now()));
            int insertStatus = subjectDao.saveSubjectData(subject, createBy);
            if(insertStatus > 0){
                response = RegisterResponse.builder()
                        .status(HttpStatus.OK.name())
                        .id(SUBJECTID+": "+subject.getSubjId())
                        .message("Subject data saved successfully!").build();
            } else {
                response = RegisterResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                        .id(SUBJECTID+": "+subject.getSubjId())
                        .message("Failed to save subject data!").build();
            }
        } else{
            response = RegisterResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id(SUBJECTID+": "+subject.getSubjId())
                    .message("With this subjId data already exits in DB.").build();
        }
        return response;
    }

    public Subject getSubjectDataById(String subjId) {
        Subject subject = subjectDao.getSubjectDataById(subjId);
        if(Objects.nonNull(subject)){
            return subject;
        } else{
            LOG.info("There is no subject data found for subjId: {}", subjId);
            return null;
        }
    }

    public List<Subject> getAllSubjectsData() {
        List<Subject> subjectList = subjectDao.getAllSubjectsData();

        if(!CollectionUtils.isEmpty(subjectList)){
            return subjectList;
        } else {
            LOG.info("There is no subject data found in DB");
            return Collections.emptyList();
        }
    }

    public RegisterResponse deleteSubjectDataById(String subjId) {
        int deleteStatus = subjectDao.deleteSubjectDataById(subjId);
        if(deleteStatus > 0){
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(SUBJECTID+": " + subjId)
                    .message("Subject data deleted successfully!").build();
        } else {
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(SUBJECTID+": " + subjId)
                    .message("Failed to delete subject data!").build();
        }
    }

    public RegisterResponse updateSubjectDataById(Subject subject, String subjId, String updateBy) {
        int updateStatus = subjectDao.updateSubjectDataById(subject, subjId, updateBy);

        if(updateStatus > 0){
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(SUBJECTID+": " + subjId)
                    .message("Subject data updated successfully!").build();
        } else {
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(SUBJECTID+": " + subjId)
                    .message("Failed to update subject data!").build();
        }
    }
}
