package com.adminease.service;

import com.adminease.dao.ManagerDao;
import com.adminease.model.Address;
import com.adminease.model.management.Manager;
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
public class ManagerService {

    private static final Logger LOG = LoggerFactory.getLogger(ManagerService.class);

    @Value("${send.mail}")
    private String sendEmailFlag;

    private final MailContentBuilder mailContentBuilder;

    private final EmailService emailService;

    private final ManagerDao managerDao;

    public ManagerService(MailContentBuilder mailContentBuilder, EmailService emailService, ManagerDao managerDao) {
        this.mailContentBuilder = mailContentBuilder;
        this.emailService = emailService;
        this.managerDao = managerDao;
    }

    public RegisterResponse saveManagerData(Manager manager, String createBy) {
        RegisterResponse registerResponse = null;
        LOG.info("Saving manager data");
        try{
            Map<String,String> managerInfoMap = managerDao.saveManagerData(manager, createBy);
            if (!CollectionUtils.isEmpty(managerInfoMap)) {
                //Here we are sending new registered manager managerId & Password to his/her provided emailId
                Map<String, String> mailContentMap = mailContentBuilder.generateManagerWelcomeEmail(managerInfoMap);
                if("Y".equalsIgnoreCase(sendEmailFlag)){
                    registerResponse = sendGeneratedCredentials(managerInfoMap, mailContentMap);
                } else {
                    registerResponse = RegisterResponse.builder().status(HttpStatus.OK.name())
                            .id(MANAGERID+": " + managerInfoMap.get(ADMIN_ID)).message("Manager registration successfully completed!, " +
                                    "A mail has not been sent, Mail functionality is disabled.").build();
                }
            }
        } catch (Exception ex){
            LOG.error("Unable to save manager data into DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
            registerResponse = RegisterResponse.builder().status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id("ManagerId not generated").message("Registration failed!").build();
        }
        return registerResponse;
    }

    private RegisterResponse sendGeneratedCredentials(Map<String, String> managerInfoMap, Map<String, String> mailContentMap) {
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
                .id(MANAGERID+": "+ managerInfoMap.get(ADMIN_ID)).message("Manager registration successfully completed!, " +
                        "A mail has been sent to your registered emailId with all the info.").build();
        return registerResponse;
    }

    public Manager getManagerDataById(String managerId){
        Manager manager = new Manager();
        try{
            manager = managerDao.getManagerDataById(managerId);
            LOG.info("Manager data fetched successfully for managerId: {}", managerId);
        } catch (Exception ex){
            LOG.error("Unable to fetch manager data for managerId: {}", managerId);
        }
        return manager;
    }

    public List<Manager> getAllManagersData() {
        List<Manager> managerList = new ArrayList<>();
        LOG.info("Fetching all managers data");
        try{
            managerList = managerDao.getAllManagersData();
            if(CollectionUtils.isEmpty(managerList)){
                LOG.info("There is no data retrieved from DB, It might be because there is no data in DB");
            }
        } catch (Exception ex){
            LOG.error("Unable to fetch managers data");
        }
        return managerList;
    }

    public RegisterResponse deleteManagerDataById(String managerId, String deleteBy) {
        RegisterResponse registerResponse = null;
        LOG.info("Deleting admin data for managerId: {}", managerId);
        try{
            int deleteStatus = managerDao.deleteManagerDataById(managerId, deleteBy);
            if(deleteStatus > 0) {
                LOG.info("Manager data deleted successfully from DB for managerId: {}", managerId);
                registerResponse = RegisterResponse.builder()
                        .status(HttpStatus.OK.name())
                        .id(MANAGERID+": "+managerId)
                        .message("Manager data deleted successfully from DB").build();
            }
        } catch (Exception ex){
            LOG.error("Unable to delete manager data from DB for managerId: {}, Exception: {}", managerId, ExceptionUtils.getStackTrace(ex));
            registerResponse = RegisterResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id(MANAGERID+": "+managerId)
                    .message("Unable to delete manager data from DB").build();
        }
        return registerResponse;
    }

    public Manager updateManagerDataById(Manager manager, String managerId, String updateBy) {
        Manager updatedManager = new Manager();
        LOG.info("Checking manager is registered or not to update the data");
        Manager registeredManager = managerDao.getManagerDataById(managerId);
        if(Objects.nonNull(registeredManager) && !StringUtils.isNullOrEmpty(registeredManager.getManagerId())){
            LOG.info("Found {} as registered manager in DB", manager.getFirstName());
            try{
                updatedManager = managerDao.updateManagerDataById(updateOldManagerDataWithNew(registeredManager, manager, updateBy), managerId);
                if(Objects.nonNull(updatedManager)){
                    LOG.info("Manager data has been updated successfully for managerId: {}", managerId);
                }
            } catch (Exception ex){
                LOG.error("Unable to update manager data for managerId: {}, Exception: {}", managerId,ExceptionUtils.getStackTrace(ex));
            }
        } else{
            LOG.info("{} is not a registered manager in DB, hence details can't be updated, Manager needs to be registered first!", managerId);
        }
        return updatedManager;
    }

    public Manager updateOldManagerDataWithNew(Manager oldData, Manager newData, String updateBy){
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
            oldData.setReportingTo(!StringUtils.isNullOrEmpty(newData.getReportingTo()) ? newData.getReportingTo() : oldData.getReportingTo());
            oldData.setOfficeLocation(!StringUtils.isNullOrEmpty(newData.getOfficeLocation()) ? newData.getOfficeLocation() : oldData.getOfficeLocation());
            oldData.setOfficialEmail(!StringUtils.isNullOrEmpty(newData.getOfficialEmail()) ? newData.getOfficialEmail() : oldData.getOfficialEmail());
        }
        return oldData;
    }

    private static void updateOldAddressWithNew(Manager oldData, Manager newData) {
        Address oldAddress = oldData.getAddress();
        Address newAddress = newData.getAddress();
        oldAddress.setStreet(!StringUtils.isNullOrEmpty(newAddress.getStreet()) ? newAddress.getStreet() : oldAddress.getStreet());
        oldAddress.setCity(!StringUtils.isNullOrEmpty(newAddress.getCity()) ? newAddress.getCity() : oldAddress.getCity());
        oldAddress.setState(!StringUtils.isNullOrEmpty(newAddress.getState()) ? newAddress.getState() : oldAddress.getState());
        oldAddress.setCountry(!StringUtils.isNullOrEmpty(newAddress.getCountry()) ? newAddress.getCountry() : oldAddress.getCountry());
        oldData.setAddress(Objects.nonNull(newData.getAddress()) ? newData.getAddress() : oldData.getAddress());
    }
}
