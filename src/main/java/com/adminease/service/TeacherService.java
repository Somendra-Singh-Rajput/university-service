package com.adminease.service;

import com.adminease.dao.TeacherDao;
import com.adminease.model.Address;
import com.adminease.model.teacher.Teacher;
import com.adminease.model.user.RegisterResponse;
import com.adminease.util.MailContentBuilder;
import com.mysql.cj.util.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.adminease.model.CommonConstants.*;

@Service
public class TeacherService {

    private static final Logger LOG = LoggerFactory.getLogger(TeacherService.class);

    @Value("${send.mail}")
    private String sendEmailFlag;

    private final MailContentBuilder mailContentBuilder;

    private final TeacherDao teacherDao; //Constructor injection of loose coupling

    private final EmailService emailService;

    public TeacherService(MailContentBuilder mailContentBuilder, final TeacherDao teacherDao, EmailService emailService){
        this.mailContentBuilder = mailContentBuilder;
        this.teacherDao =teacherDao;
        this.emailService = emailService;
    }

    public RegisterResponse saveTeacherData(Teacher teacher, String createBy) {
        RegisterResponse registerResponse = null;
        LOG.info("Saving teacher data");
        try{
            Map<String,String> teacherInfoMap = teacherDao.saveTeacherData(teacher, createBy);
            if (!CollectionUtils.isEmpty(teacherInfoMap)) {
                //Here we are sending new registered teacher's teacherId & Password to his/her provided emailId
                Map<String, String> mailContentMap = mailContentBuilder.generateTeacherWelcomeEmail(teacherInfoMap);
                if("Y".equalsIgnoreCase(sendEmailFlag)){
                    registerResponse = sendGeneratedCredentials(teacherInfoMap, mailContentMap);
                } else {
                    registerResponse = RegisterResponse.builder().status(HttpStatus.OK.name())
                            .id(STUDENTID+": " + teacherInfoMap.get(STUDENT_ID)).message("Teacher registration successfully completed!, " +
                                    "A mail has not been sent, Mail functionality is disabled.").build();
                }
            }
        } catch (Exception ex){
            LOG.error("Unable to save teacher data into DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
            registerResponse = RegisterResponse.builder().status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id("TeacherId not generated").message("Registration failed!").build();
        }
        return registerResponse;
    }

    private RegisterResponse sendGeneratedCredentials(Map<String, String> teacherInfoMap, Map<String, String> mailContentMap) {
        RegisterResponse registerResponse;
        String toEmail = mailContentMap.get(TO_EMAIL);
        boolean emailStatus =
                emailService.sendEmail(toEmail, mailContentMap.get(SUBJECT), mailContentMap.get(BODY));
        if (emailStatus) {
            LOG.info("Email has sent successfully to the provided emailId: {}", toEmail);
        } else{
            LOG.info("Email has not sent to the provided emailId: {}", toEmail);
        }
        registerResponse = RegisterResponse.builder().status(HttpStatus.OK.name())
                .id(TEACHERID+": "+ teacherInfoMap.get(TEACHER_ID)).message("Teacher registration successfully completed!, " +
                        "A mail has been sent to your registered emailId with all the info.").build();
        return registerResponse;
    }

    public Teacher getTeacherDataById(String teacherId){
        Teacher teacher = new Teacher();
        try{
            teacher = teacherDao.getTeacherDataById(teacherId);
            LOG.info("Teacher data fetched successfully for teacherId: {}", teacherId);
        } catch (Exception ex){
            LOG.error("Unable to fetch teacher data for teacherId: {}", teacherId);
        }
        return teacher;
    }

    public List<Teacher> getAllTeachersData() {
        List<Teacher> teacherList = new ArrayList<>();
        LOG.info("Fetching all teachers data");
        try{
            teacherList = teacherDao.getAllTeachersData();
            if(CollectionUtils.isEmpty(teacherList)){
                LOG.info("There is no data retrieved from DB, It might be because there is no data in DB");
            }
        } catch (Exception ex){
            LOG.error("Unable to fetch teachers data");
        }
        return teacherList;
    }

    public RegisterResponse deleteTeacherDataById(String teacherId, String deleteBy) {
        RegisterResponse registerResponse = null;
        LOG.info("Deleting teacher data for teacherId: {}", teacherId);
        try{
            int deleteStatus = teacherDao.deleteTeacherDataById(teacherId, deleteBy);
            if(deleteStatus > 0) {
                LOG.info("Teacher data deleted successfully from DB for teacherId: {}", teacherId);
                registerResponse = RegisterResponse.builder()
                        .status(HttpStatus.OK.name())
                        .id(TEACHERID+": "+teacherId)
                        .message("Teacher data deleted successfully from DB").build();
            }
        } catch (Exception ex){
            LOG.error("Unable to delete teacher data from DB for teacherId: {}, Exception: {}", teacherId, ExceptionUtils.getStackTrace(ex));
            registerResponse = RegisterResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id(TEACHERID+": "+teacherId)
                    .message("Unable to delete teacher data from DB").build();
        }
        return registerResponse;
    }

    public Teacher updateTeacherDataById(Teacher teacher, String teacherId, String updateBy) {
        Teacher updatedTeacher = new Teacher();
        LOG.info("Checking teacher is registered or not to update the data");
        Teacher registeredTeacher = teacherDao.getTeacherDataById(teacherId);
        if(Objects.nonNull(registeredTeacher) && !StringUtils.isNullOrEmpty(registeredTeacher.getTeacherId())){
            LOG.info("Found {} as registered teacher in DB", teacher.getFirstName());
            try{
                updatedTeacher = teacherDao.updateTeacherDataById(updateOldTeacherDataWithNew(registeredTeacher, teacher, updateBy), teacherId);
                if(Objects.nonNull(updatedTeacher)){
                    LOG.info("Teacher data has been updated successfully for teacherId: {}", teacherId);
                }
            } catch (Exception ex){
                LOG.error("Unable to update teacher data for teacherId: {}, Exception: {}", teacherId,ExceptionUtils.getStackTrace(ex));
            }
        } else{
            LOG.info("{} is not a registered teacher in DB, hence details can't be updated, Teacher needs to be registered first!", teacherId);
        }
        return updatedTeacher;
    }

    public Teacher updateOldTeacherDataWithNew(Teacher oldData, Teacher newData, String updateBy){
        if(Objects.nonNull(oldData) && Objects.nonNull(newData)){
            oldData.setFirstName(!StringUtils.isNullOrEmpty(newData.getFirstName()) ? newData.getFirstName() : oldData.getFirstName());
            oldData.setLastName(!StringUtils.isNullOrEmpty(newData.getLastName()) ? newData.getLastName() : oldData.getLastName());
            oldData.setPhone(!StringUtils.isNullOrEmpty(newData.getPhone()) ? newData.getPhone() : oldData.getPhone());
            oldData.setDob(!StringUtils.isNullOrEmpty(newData.getDob()) ? newData.getDob() : oldData.getDob());
            oldData.setGender(!StringUtils.isNullOrEmpty(newData.getGender()) ? newData.getGender() : oldData.getGender());
            oldData.setFatherName(!StringUtils.isNullOrEmpty(newData.getFatherName()) ? newData.getFatherName() : oldData.getFatherName());
            oldData.setMotherName(!StringUtils.isNullOrEmpty(newData.getMotherName()) ? newData.getMotherName() : oldData.getMotherName());

            updateOldAddressWithNew(oldData, newData);

            oldData.setDepartmentId(!StringUtils.isNullOrEmpty(newData.getDepartmentId()) ? newData.getDepartmentId() : oldData.getDepartmentId());
            oldData.setProfilePhoto(!StringUtils.isNullOrEmpty(newData.getProfilePhoto()) ? newData.getProfilePhoto() : oldData.getProfilePhoto());
            oldData.setExpertise(!StringUtils.isNullOrEmpty(newData.getExpertise()) ? newData.getExpertise() : oldData.getExpertise());
            oldData.setCreateTs(!StringUtils.isNullOrEmpty(newData.getCreateTs()) ? newData.getCreateTs() : oldData.getCreateTs());
            oldData.setUpdateTs(!StringUtils.isNullOrEmpty(newData.getUpdateTs()) ? newData.getUpdateTs() : oldData.getUpdateTs());
            oldData.setCreateBy(!StringUtils.isNullOrEmpty(newData.getCreateBy()) ? newData.getCreateBy() : oldData.getCreateBy());
            oldData.setUpdateBy(updateBy);
        }
        return oldData;
    }

    private static void updateOldAddressWithNew(Teacher oldData, Teacher newData) {
        Address oldAddress = oldData.getAddress();
        Address newAddress = newData.getAddress();
        oldAddress.setStreet(!StringUtils.isNullOrEmpty(newAddress.getStreet()) ? newAddress.getStreet() : oldAddress.getStreet());
        oldAddress.setCity(!StringUtils.isNullOrEmpty(newAddress.getCity()) ? newAddress.getCity() : oldAddress.getCity());
        oldAddress.setState(!StringUtils.isNullOrEmpty(newAddress.getState()) ? newAddress.getState() : oldAddress.getState());
        oldAddress.setCountry(!StringUtils.isNullOrEmpty(newAddress.getCountry()) ? newAddress.getCountry() : oldAddress.getCountry());
        oldData.setAddress(Objects.nonNull(newData.getAddress()) ? newData.getAddress() : oldData.getAddress());
    }
}
