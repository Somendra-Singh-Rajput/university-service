package com.adminease.service;

import com.adminease.dao.SemesterDao;
import com.adminease.model.management.Semester;
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

import static com.adminease.model.CommonConstants.SEMESTER;
import static com.adminease.model.CommonConstants.SEMESTERID;

@Service
public class SemesterService {

    private static final Logger LOG = LoggerFactory.getLogger(SemesterService.class);

    private final SemesterDao semesterDao;

    public SemesterService(SemesterDao semesterDao) {
        this.semesterDao = semesterDao;
    }

    public RegisterResponse addSemesterData(Semester semester, String createBy) {
        RegisterResponse response;
        Semester exitingSemData = semesterDao.checkRegisteredSem(semester.getSemName());

        if(StringUtils.isEmpty(exitingSemData.getSemId()) && StringUtils.isEmpty(exitingSemData.getSemName())){
            semester.setSemId(RandomIdGenerator.generateId(SEMESTER));
            semester.setCreateTs(String.valueOf(LocalDateTime.now()));
            int insertStatus = semesterDao.saveSemesterData(semester, createBy);
            if(insertStatus > 0){
                response = RegisterResponse.builder()
                        .status(HttpStatus.OK.name())
                        .id(SEMESTERID+": "+semester.getSemId())
                        .message("Semester data saved successfully!").build();
            } else {
                response = RegisterResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                        .id(SEMESTERID+": "+semester.getSemId())
                        .message("Failed to save semester data!").build();
            }
        } else{
            response = RegisterResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id(SEMESTERID+": "+semester.getSemId())
                    .message("With this semId data already exits in DB.").build();
        }
        return response;
    }

    public Semester getSemesterDataById(String semId) {
        Semester semester = semesterDao.getSemesterDataById(semId);
        if(Objects.nonNull(semester)){
            return semester;
        } else{
            LOG.info("There is no semester data found for semId: {}", semId);
            return null;
        }
    }

    public List<Semester> getAllSemestersData() {
        List<Semester> semesterList = semesterDao.getAllSemestersData();

        if(!CollectionUtils.isEmpty(semesterList)){
            return semesterList;
        } else {
            LOG.info("There is no semester data found in DB");
            return Collections.emptyList();
        }
    }

    public RegisterResponse deleteSemesterDataById(String semId) {
        int deleteStatus = semesterDao.deleteSemesterDataById(semId);
        if(deleteStatus > 0){
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(SEMESTERID+": " + semId)
                    .message("Semester data deleted successfully!").build();
        } else {
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(SEMESTERID+": " + semId)
                    .message("Failed to delete semester data!").build();
        }
    }

    public RegisterResponse updateSemesterDataById(Semester semester, String semId, String updateBy) {
        int updateStatus = semesterDao.updateSemesterDataById(semester, semId, updateBy);

        if(updateStatus > 0){
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(SEMESTERID+": " + semId)
                    .message("Semester data updated successfully!").build();
        } else {
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(SEMESTERID+": " + semId)
                    .message("Failed to update semester data!").build();
        }
    }
}
