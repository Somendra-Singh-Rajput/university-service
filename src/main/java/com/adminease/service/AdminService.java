package com.adminease.service;

import com.adminease.dao.AdminDao;
import com.adminease.model.Address;
import com.adminease.model.Admin;
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
public class AdminService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminService.class);

    @Value("${send.mail}")
    private String sendEmailFlag;

    private final MailContentBuilder mailContentBuilder;

    private final EmailService emailService;

    private final AdminDao adminDao;

    public AdminService(MailContentBuilder mailContentBuilder, EmailService emailService, AdminDao adminDao) {
        this.mailContentBuilder = mailContentBuilder;
        this.emailService = emailService;
        this.adminDao = adminDao;
    }

    public RegisterResponse saveAdminData(Admin admin, String createBy) {
        RegisterResponse registerResponse = null;
        LOG.info("Saving admin data");
        try{
            Map<String,String> adminInfoMap = adminDao.saveAdminData(admin, createBy);
            if (!CollectionUtils.isEmpty(adminInfoMap)) {
                //Here we are sending new registered admin adminId & Password to his/her provided emailId
                Map<String, String> mailContentMap = mailContentBuilder.generateAdminWelcomeEmail(adminInfoMap);
                if("Y".equalsIgnoreCase(sendEmailFlag)){
                    registerResponse = sendGeneratedCredentials(adminInfoMap, mailContentMap);
                } else {
                    registerResponse = RegisterResponse.builder().status(HttpStatus.OK.name())
                            .id(ADMINID+": " + adminInfoMap.get(ADMIN_ID)).message("Admin registration successfully completed!, " +
                                    "A mail has not been sent, Mail functionality is disabled.").build();
                }
            }
        } catch (Exception ex){
            LOG.error("Unable to save admin data into DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
            registerResponse = RegisterResponse.builder().status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id("AdminId not generated").message("Registration failed!").build();
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
                .id(ADMINID+": "+ teacherInfoMap.get(ADMIN_ID)).message("Admin registration successfully completed!, " +
                        "A mail has been sent to your registered emailId with all the info.").build();
        return registerResponse;
    }

    public Admin getAdminDataById(String adminId){
        Admin admin = new Admin();
        try{
            admin = adminDao.getAdminDataById(adminId);
            LOG.info("Admin data fetched successfully for adminId: {}", adminId);
        } catch (Exception ex){
            LOG.error("Unable to fetch admin data for adminId: {}", adminId);
        }
        return admin;
    }

    public List<Admin> getAllAdminsData() {
        List<Admin> adminList = new ArrayList<>();
        LOG.info("Fetching all admins data");
        try{
            adminList = adminDao.getAllAdminsData();
            if(CollectionUtils.isEmpty(adminList)){
                LOG.info("There is no data retrieved from DB, It might be because there is no data in DB");
            }
        } catch (Exception ex){
            LOG.error("Unable to fetch admins data");
        }
        return adminList;
    }

    public RegisterResponse deleteAdminDataById(String adminId, String deleteBy) {
        RegisterResponse registerResponse = null;
        LOG.info("Deleting admin data for adminId: {}", adminId);
        try{
            int deleteStatus = adminDao.deleteAdminDataById(adminId, deleteBy);
            if(deleteStatus > 0) {
                LOG.info("Admin data deleted successfully from DB for adminId: {}", adminId);
                registerResponse = RegisterResponse.builder()
                        .status(HttpStatus.OK.name())
                        .id(ADMINID+": "+adminId)
                        .message("Admin data deleted successfully from DB").build();
            }
        } catch (Exception ex){
            LOG.error("Unable to delete admin data from DB for adminId: {}, Exception: {}", adminId, ExceptionUtils.getStackTrace(ex));
            registerResponse = RegisterResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id(ADMINID+": "+adminId)
                    .message("Unable to delete admin data from DB").build();
        }
        return registerResponse;
    }

    public Admin updateAdminDataById(Admin admin, String adminId, String updateBy) {
        Admin updatedAdmin = new Admin();
        LOG.info("Checking admin is registered or not to update the data");
        Admin registeredAdmin = adminDao.getAdminDataById(adminId);
        if(Objects.nonNull(registeredAdmin) && !StringUtils.isNullOrEmpty(registeredAdmin.getAdminId())){
            LOG.info("Found {} as registered admin in DB", admin.getFirstName());
            try{
                updatedAdmin = adminDao.updateAdminDataById(updateOldAdminDataWithNew(registeredAdmin, admin, updateBy), adminId);
                if(Objects.nonNull(updatedAdmin)){
                    LOG.info("Admin data has been updated successfully for adminId: {}", adminId);
                }
            } catch (Exception ex){
                LOG.error("Unable to update admin data for adminId: {}, Exception: {}", adminId,ExceptionUtils.getStackTrace(ex));
            }
        } else{
            LOG.info("{} is not a registered admin in DB, hence details can't be updated, Admin needs to be registered first!", adminId);
        }
        return updatedAdmin;
    }

    public Admin updateOldAdminDataWithNew(Admin oldData, Admin newData, String updateBy){
        if(Objects.nonNull(oldData) && Objects.nonNull(newData)){
            oldData.setFirstName(!StringUtils.isNullOrEmpty(newData.getFirstName()) ? newData.getFirstName() : oldData.getFirstName());
            oldData.setLastName(!StringUtils.isNullOrEmpty(newData.getLastName()) ? newData.getLastName() : oldData.getLastName());
            oldData.setPhone(!StringUtils.isNullOrEmpty(newData.getPhone()) ? newData.getPhone() : oldData.getPhone());
            oldData.setDob(!StringUtils.isNullOrEmpty(newData.getDob()) ? newData.getDob() : oldData.getDob());
            oldData.setGender(!StringUtils.isNullOrEmpty(newData.getGender()) ? newData.getGender() : oldData.getGender());
            oldData.setFatherName(!StringUtils.isNullOrEmpty(newData.getFatherName()) ? newData.getFatherName() : oldData.getFatherName());
            oldData.setMotherName(!StringUtils.isNullOrEmpty(newData.getMotherName()) ? newData.getMotherName() : oldData.getMotherName());

            updateOldAddressWithNew(oldData, newData);

            oldData.setProfilePhoto(!StringUtils.isNullOrEmpty(newData.getProfilePhoto()) ? newData.getProfilePhoto() : oldData.getProfilePhoto());
            oldData.setDoj(!StringUtils.isNullOrEmpty(newData.getDoj()) ? newData.getDoj() : oldData.getDoj());
            oldData.setDol(!StringUtils.isNullOrEmpty(newData.getDol()) ? newData.getDol() : oldData.getDol());
            oldData.setRole(!StringUtils.isNullOrEmpty(String.valueOf(newData.getRole())) ? newData.getRole() : oldData.getRole());
            oldData.setEnabled(newData.isEnabled());
            oldData.setCreateTs(!StringUtils.isNullOrEmpty(newData.getCreateTs()) ? newData.getCreateTs() : oldData.getCreateTs());
            oldData.setUpdateTs(!StringUtils.isNullOrEmpty(newData.getUpdateTs()) ? newData.getUpdateTs() : oldData.getUpdateTs());
            oldData.setCreateBy(!StringUtils.isNullOrEmpty(newData.getCreateBy()) ? newData.getCreateBy() : oldData.getCreateBy());
            oldData.setUpdateBy(updateBy);
            oldData.setPassword(!StringUtils.isNullOrEmpty(newData.getPassword()) ? newData.getPassword() : oldData.getPassword());
            oldData.setOldPassword(!StringUtils.isNullOrEmpty(newData.getOldPassword()) ? newData.getOldPassword() : oldData.getOldPassword());
            oldData.setPosition(!StringUtils.isNullOrEmpty(newData.getPosition()) ? newData.getPosition() : oldData.getPosition());
            oldData.setOfficeLocation(!StringUtils.isNullOrEmpty(newData.getOfficeLocation()) ? newData.getOfficeLocation() : oldData.getOfficeLocation());
            oldData.setOfficialEmail(!StringUtils.isNullOrEmpty(newData.getOfficialEmail()) ? newData.getOfficialEmail() : oldData.getOfficialEmail());
        }
        return oldData;
    }

    private static void updateOldAddressWithNew(Admin oldData, Admin newData) {
        Address oldAddress = oldData.getAddress();
        Address newAddress = newData.getAddress();
        oldAddress.setStreet(!StringUtils.isNullOrEmpty(newAddress.getStreet()) ? newAddress.getStreet() : oldAddress.getStreet());
        oldAddress.setCity(!StringUtils.isNullOrEmpty(newAddress.getCity()) ? newAddress.getCity() : oldAddress.getCity());
        oldAddress.setState(!StringUtils.isNullOrEmpty(newAddress.getState()) ? newAddress.getState() : oldAddress.getState());
        oldAddress.setCountry(!StringUtils.isNullOrEmpty(newAddress.getCountry()) ? newAddress.getCountry() : oldAddress.getCountry());
        oldData.setAddress(Objects.nonNull(newData.getAddress()) ? newData.getAddress() : oldData.getAddress());
    }
}
