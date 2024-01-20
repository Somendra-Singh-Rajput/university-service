package com.adminease.controller;

import com.adminease.model.Admin;
import com.adminease.model.user.RegisterResponse;
import com.adminease.model.user.User;
import com.adminease.service.AdminService;
import com.mysql.cj.util.StringUtils;
import io.swagger.v3.oas.annotations.Hidden;
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
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('admin:create')")
    @Hidden
    public RegisterResponse saveAdmin(@RequestBody Admin admin,
                                            @AuthenticationPrincipal User user) {
        RegisterResponse registerResponse = null;
        LOG.info("Inside saving admin data method");
        if(Objects.nonNull(admin) && !StringUtils.isNullOrEmpty(user.getUsername())){
            registerResponse = adminService.saveAdminData(admin, user.getUsername());
        }
        return registerResponse;
    }

    @GetMapping(path = "/get/{adminId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('admin:read')")
    public Admin getAdminDataById(@PathVariable String adminId){
        Admin admin = null;
        LOG.info("Inside fetching admin data method for adminId: {}", adminId);

        if(!StringUtils.isNullOrEmpty(adminId)){
            admin =  adminService.getAdminDataById(adminId);
        }
        return admin;
    }

    @GetMapping(path = "/getAllAdminsData", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('admin:read')")
    public List<Admin> getAllAdminsData(){
        LOG.info("Inside fetching all admins data method");
        return adminService.getAllAdminsData();
    }

    @DeleteMapping(value = "/delete/{adminId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('admin:delete')")
    @Hidden
    public RegisterResponse deleteAdminDataById(@PathVariable String adminId,
                                                  @AuthenticationPrincipal User user){
        RegisterResponse registerResponse = null;
        LOG.info("Inside deleting admin data method for adminId: {}", adminId);
        if(!StringUtils.isNullOrEmpty(adminId) && !StringUtils.isNullOrEmpty(user.getUsername())){
            registerResponse = adminService.deleteAdminDataById(adminId, user.getUsername());
        }
        return registerResponse;
    }

    @PutMapping(value = "/update/{adminId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('admin:update')")
    @Hidden
    public Admin updateTeacherDataById(@RequestBody Admin admin, @PathVariable String adminId,
                                         @AuthenticationPrincipal User user) {
        Admin updatedAdmin = new Admin();
        LOG.info("Inside update admin data method");
        if(Objects.nonNull(admin) && !StringUtils.isNullOrEmpty(adminId)
                && !StringUtils.isNullOrEmpty(user.getUsername())){
            updatedAdmin = adminService.updateAdminDataById(admin, adminId, user.getUsername());
        } else{
            LOG.info("Admin data or adminId is either empty or null");
        }
        return updatedAdmin;
    }
}
