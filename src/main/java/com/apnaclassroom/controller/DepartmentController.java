package com.apnaclassroom.controller;

import com.apnaclassroom.model.management.Department;
import com.apnaclassroom.model.user.RegisterResponse;
import com.apnaclassroom.model.user.User;
import com.apnaclassroom.service.DepartmentService;
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
@RequestMapping("/api/v1/department")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class DepartmentController {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentService departmentService;

    public DepartmentController(final DepartmentService departmentService){
        this.departmentService = departmentService;
    }

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse addDepartment(@RequestBody Department department,
                                          @AuthenticationPrincipal User user) {
        RegisterResponse response = null;
        LOG.info("Inside adding department data method");
        if(Objects.nonNull(department) && !StringUtils.isNullOrEmpty(user.getUsername())){
            response = departmentService.addDepartmentData(department, user.getUsername());
        }
        return response;
    }

    @GetMapping(path = "/get/{deptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Department getDepartmentById(@PathVariable String deptId){
        Department department = null;
        LOG.info("Inside fetching department data method for deptId: {}", deptId);

        if(!StringUtils.isNullOrEmpty(deptId)){
            department =  departmentService.getDepartmentDataById(deptId);
        }
        return department;
    }

    @GetMapping(path = "/getAllDepartments/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Department> getAllDepartments(){
        LOG.info("Inside fetching all departments data method");
        return departmentService.getAllDepartments();
    }

    @DeleteMapping(value = "/delete/{deptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse deleteDepartmentById(@PathVariable String deptId){
        RegisterResponse response = null;
        LOG.info("Inside deleting department data method for deptId: {}", deptId);
        if(!StringUtils.isNullOrEmpty(deptId)){
            response = departmentService.deleteDepartmentDataById(deptId);
        }
        return response;
    }

    @PutMapping(value = "/update/{deptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse updateDepartmentById(@RequestBody Department department, @PathVariable String deptId,
                                                 @AuthenticationPrincipal User user){
        RegisterResponse response = null;
        LOG.info("Inside updating department data method for deptId: {}", deptId);
        if(!StringUtils.isNullOrEmpty(deptId) && Objects.nonNull(department)
                && !StringUtils.isNullOrEmpty(user.getUsername())){
            response = departmentService.updateDepartmentDataById(department, deptId, user.getUsername());
        }
        return response;
    }
}
