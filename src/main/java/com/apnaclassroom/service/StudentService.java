package com.apnaclassroom.service;

import com.apnaclassroom.dao.StudentDao;
import com.apnaclassroom.model.Address;
import com.apnaclassroom.model.FeeReceipt;
import com.apnaclassroom.model.student.Student;
import com.apnaclassroom.model.user.RegisterResponse;
import com.apnaclassroom.util.FileUtilityService;
import com.apnaclassroom.util.MailContentBuilder;
import com.mysql.cj.util.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.apnaclassroom.model.CommonConstants.*;

@Service
public class StudentService {

    private static final Logger LOG = LoggerFactory.getLogger(StudentService.class);

    @Value("${send.mail}")
    private String sendEmailFlag;

    private final MailContentBuilder mailContentBuilder;

    private final StudentDao studentDao; //Constructor injection of loose coupling

    private final EmailService emailService;

    private final FileUtilityService fileUtilityService;

    public StudentService(MailContentBuilder mailContentBuilder, final StudentDao studentDao, EmailService emailService, FileUtilityService fileUtilityService){
        this.mailContentBuilder = mailContentBuilder;
        this.studentDao=studentDao;
        this.emailService = emailService;
        this.fileUtilityService = fileUtilityService;
    }

    public RegisterResponse saveStudentData(Student student, String createBy) {
        RegisterResponse registerResponse = null;
        LOG.info("Saving student data");
        try{
            Map<String,String> studentInfoMap = studentDao.saveStudentData(student, createBy);
            if (!CollectionUtils.isEmpty(studentInfoMap)) {
                //Here we are sending new registered student's StudentId & Password to his/her provided emailId
                Map<String, String> mailContentMap = mailContentBuilder.generateUserWelcomeEmail(studentInfoMap);
                if("Y".equalsIgnoreCase(sendEmailFlag)){
                    registerResponse = sendGeneratedCredentials(studentInfoMap, mailContentMap);
                } else {
                    registerResponse = RegisterResponse.builder().status(HttpStatus.OK.name())
                            .id(STUDENTID+": " + studentInfoMap.get(STUDENT_ID)).message("Student registration successfully completed!, " +
                                    "A mail has not been sent, Mail functionality is disabled.").build();
                }
            }
        } catch (Exception ex){
            LOG.error("Unable to save student data into DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
            registerResponse = RegisterResponse.builder().status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id("StudentId not generated").message("Registration failed!").build();
        }
        return registerResponse;
    }

    private RegisterResponse sendGeneratedCredentials(Map<String, String> studentInfoMap, Map<String, String> mailContentMap) {
        RegisterResponse registerResponse;
        String toEmail = mailContentMap.get(TO_EMAIL);
        boolean emailStatus =
                emailService.sendEmail(toEmail, mailContentMap.get(SUBJECT), mailContentMap.get(BODY));
        if (emailStatus){
            LOG.info("Email has sent successfully to the provided emailId: {}", toEmail);
        } else {
            LOG.info("Email has not sent to the provided emailId: {}", toEmail);
        }
        registerResponse = RegisterResponse.builder().status(HttpStatus.OK.name())
                .id(STUDENTID+": "+ studentInfoMap.get(STUDENT_ID)).message("Student registration successfully completed!, " +
                        "A mail has been sent to your registered emailId with all the info.").build();
        return registerResponse;
    }

    public Student getStudentDataById(String studentId){
        Student student = new Student();
        try{
            student = studentDao.getStudentDataById(studentId);
            LOG.info("Student data fetched successfully for studentId: {}", student);
        } catch (Exception ex){
            LOG.error("Unable to fetch student data for studentId: {}", student);
        }

        return student;
    }

    public List<Student> getAllStudentsData() {
        List<Student> studentList = new ArrayList<>();
        LOG.info("Fetching all students data");
        try{
            studentList = studentDao.getAllStudentsData();
            if(CollectionUtils.isEmpty(studentList)){
                LOG.info("There is no data retrieved from DB, It might be because there is no data in DB");
            }
        } catch (Exception ex){
            LOG.error("Unable to fetch students data");
        }
        return studentList;
    }

    public RegisterResponse deleteStudentDataById(String studentId, String deleteBy) {
        RegisterResponse registerResponse = null;
        LOG.info("Deleting student data for studentId: {}", studentId);
        try{
            int deleteStatus = studentDao.deleteStudentDataById(studentId, deleteBy);
            if(deleteStatus > 0) {
                LOG.info("Student data deleted successfully from DB for studentId: {}", studentId);
                registerResponse = RegisterResponse.builder()
                        .status(HttpStatus.OK.name())
                        .id("StudentId: "+studentId)
                        .message("Student data deleted successfully from DB").build();
            }
        } catch (Exception ex){
            LOG.error("Unable to delete student data from DB for studentId: {}, Exception: {}", studentId, ExceptionUtils.getStackTrace(ex));
            registerResponse = RegisterResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id("StudentId: "+studentId)
                    .message("Unable to delete student data from DB").build();
        }
        return registerResponse;
    }

    public Student updateStudentDataById(Student student, String studentId, String updateBy) {
        Student updatedStudent = new Student();
        LOG.info("Checking student is registered or not to update the data");
        Student registeredStudent = studentDao.getStudentDataById(studentId);
        if(Objects.nonNull(registeredStudent) && !StringUtils.isNullOrEmpty(registeredStudent.getStudentId())){
            LOG.info("Found {} as registered student in DB", student.getFirstName());
            try{
                updatedStudent = studentDao.updateStudentDataById(updateOldStudentDataWithNew(registeredStudent, student, updateBy), studentId);
                if(Objects.nonNull(updatedStudent)){
                    LOG.info("Student data has been updated successfully for studentId: {}", studentId);
                }
            } catch (Exception ex){
                LOG.error("Unable to update student data for studentId: {}, Exception: {}", studentId,ExceptionUtils.getStackTrace(ex));
            }
        } else{
            LOG.info("{} is not a registered student in DB, hence details can't be updated, Student needs to be registered first!", studentId);
        }

        return updatedStudent;
    }

    public Student updateOldStudentDataWithNew(Student oldData, Student newData, String updateBy){
        if(Objects.nonNull(oldData) && Objects.nonNull(newData)){
            oldData.setFirstName(!StringUtils.isNullOrEmpty(newData.getFirstName()) ? newData.getFirstName() : oldData.getFirstName());
            oldData.setLastName(!StringUtils.isNullOrEmpty(newData.getLastName()) ? newData.getLastName() : oldData.getLastName());
            oldData.setPhone(!StringUtils.isNullOrEmpty(newData.getPhone()) ? newData.getPhone() : oldData.getPhone());
            oldData.setDob(!StringUtils.isNullOrEmpty(newData.getDob()) ? newData.getDob() : oldData.getDob());
            oldData.setGender(!StringUtils.isNullOrEmpty(newData.getGender()) ? newData.getGender() : oldData.getGender());
            oldData.setFatherName(!StringUtils.isNullOrEmpty(newData.getFatherName()) ? newData.getFatherName() : oldData.getFatherName());
            oldData.setMotherName(!StringUtils.isNullOrEmpty(newData.getMotherName()) ? newData.getMotherName() : oldData.getMotherName());

            updateOldAddressWithNew(oldData, newData);

            oldData.setCourseId(!StringUtils.isNullOrEmpty(newData.getCourseId()) ? newData.getCourseId() : oldData.getCourseId());
            oldData.setDepartmentId(!StringUtils.isNullOrEmpty(newData.getDepartmentId()) ? newData.getDepartmentId() : oldData.getDepartmentId());
            oldData.setProfilePhoto(!StringUtils.isNullOrEmpty(newData.getProfilePhoto()) ? newData.getProfilePhoto() : oldData.getProfilePhoto());
            oldData.setCreateTs(!StringUtils.isNullOrEmpty(newData.getCreateTs()) ? newData.getCreateTs() : oldData.getCreateTs());
            oldData.setUpdateTs(!StringUtils.isNullOrEmpty(newData.getUpdateTs()) ? newData.getUpdateTs() : oldData.getUpdateTs());
            oldData.setCreateBy(!StringUtils.isNullOrEmpty(newData.getCreateBy()) ? newData.getCreateBy() : oldData.getCreateBy());
            oldData.setUpdateBy(updateBy);
        }
        return oldData;
    }

    private static void updateOldAddressWithNew(Student oldData, Student newData) {
        Address oldAddress = oldData.getAddress();
        Address newAddress = newData.getAddress();
        oldAddress.setStreet(!StringUtils.isNullOrEmpty(newAddress.getStreet()) ? newAddress.getStreet() : oldAddress.getStreet());
        oldAddress.setCity(!StringUtils.isNullOrEmpty(newAddress.getCity()) ? newAddress.getCity() : oldAddress.getCity());
        oldAddress.setState(!StringUtils.isNullOrEmpty(newAddress.getState()) ? newAddress.getState() : oldAddress.getState());
        oldAddress.setCountry(!StringUtils.isNullOrEmpty(newAddress.getCountry()) ? newAddress.getCountry() : oldAddress.getCountry());
        oldData.setAddress(Objects.nonNull(newData.getAddress()) ? newData.getAddress() : oldData.getAddress());
    }

    public void exportStudentsData() throws IOException {
        fileUtilityService.generateStudentDataExcel(getAllStudentsData());
    }

    public void generatePaymentReceipt(FeeReceipt feeReceipt, String generateBy) throws IOException {
        fileUtilityService.generatePaymentReceipt(feeReceipt, generateBy);
    }
}
