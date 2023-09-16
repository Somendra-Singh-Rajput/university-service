package com.apnaclassroom.service;

import com.apnaclassroom.dao.DepartmentDao;
import com.apnaclassroom.model.management.Department;
import com.apnaclassroom.model.user.RegisterResponse;
import com.apnaclassroom.util.RandomIdGenerator;
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

import static com.apnaclassroom.model.CommonConstants.DEPARTMENT;
import static com.apnaclassroom.model.CommonConstants.DEPARTMENTID;

@Service
public class DepartmentService {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentDao departmentDao; //Constructor injection of loose coupling

    public DepartmentService(final DepartmentDao departmentDao){
        this.departmentDao=departmentDao;
    }

    public RegisterResponse addDepartmentData(Department department, String createBy) {
        RegisterResponse response;
        Department exitingDeptData = departmentDao.checkRegisteredDept(department.getDeptName());

        if(StringUtils.isEmpty(exitingDeptData.getDeptId()) && StringUtils.isEmpty(exitingDeptData.getDeptName())){
            department.setDeptId(RandomIdGenerator.generateId(DEPARTMENT));
            department.setCreateTs(String.valueOf(LocalDateTime.now()));
            int insertStatus = departmentDao.saveDepartmentData(department, createBy);
            if(insertStatus > 0){
                response = RegisterResponse.builder()
                        .status(HttpStatus.OK.name())
                        .id(DEPARTMENTID+": "+department.getDeptId())
                        .message("Department data saved successfully!").build();
            } else {
                response = RegisterResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                        .id(DEPARTMENTID+": "+department.getDeptId())
                        .message("Failed to save department data!").build();
            }
        } else{
            response = RegisterResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id(DEPARTMENTID+": "+department.getDeptId())
                    .message("With this departmentId data already exits in DB.").build();
        }
        return response;
    }

    public Department getDepartmentDataById(String deptId) {
        Department department = departmentDao.getDepartmentDataById(deptId);
        if(Objects.nonNull(department)){
            return department;
        } else{
            LOG.info("There is no department data found for deptId: {}", deptId);
            return null;
        }
    }

    public List<Department> getAllDepartments() {
        List<Department> departmentList = departmentDao.getAllDepartmentsData();

        if(!CollectionUtils.isEmpty(departmentList)){
            return departmentList;
        } else {
            LOG.info("There is no department data found in DB");
            return Collections.emptyList();
        }
    }

    public RegisterResponse deleteDepartmentDataById(String deptId) {
        int deleteStatus = departmentDao.deleteDepartmentDataById(deptId);
        if(deleteStatus > 0){
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(DEPARTMENTID+": " + deptId)
                    .message("Department data deleted successfully!").build();
        } else {
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(DEPARTMENTID+": " + deptId)
                    .message("Failed to delete department data!").build();
        }
    }

    public RegisterResponse updateDepartmentDataById(Department department, String deptId, String updateBy) {
            int updateStatus = departmentDao.updateDepartmentById(department, deptId, updateBy);

            if(updateStatus > 0){
                return RegisterResponse.builder()
                        .status(HttpStatus.OK.name())
                        .id(DEPARTMENTID+": " + deptId)
                        .message("Department data updated successfully!").build();
            } else {
                return RegisterResponse.builder()
                        .status(HttpStatus.OK.name())
                        .id(DEPARTMENTID+": " + deptId)
                        .message("Failed to update department data!").build();
            }
        }
}
